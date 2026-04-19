/*
 * Copyright 2024 The LibreMobileOS Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "CameraProviderExtension.h"

#include <algorithm>
#include <fstream>
#include <optional>

// #define LOG_NDEBUG 0
#include <log/log.h>

namespace {

#define TORCH_DEFAULT_STRENGTH_LEVEL 200 // matches kernel driver
// NOTE: Limit to 254 as LED_FULL (255) is reserved for camera flash and fires
// twice (LED_HALF (127) then LED_FULL); flash will turn off automatically on
// timeout (configured in DTS) after the second fire.
#define TORCH_DEFAULT_MAX_STRENGTH_LEVEL 254
#define TORCH_TRIGGER_SETTING "flashlight-trigger" // matches DTS in kernel

#define TORCH_FLASHLIGHT_PATH(file) "/sys/class/leds/flashlight/" file
#define TORCH_BRIGHTNESS_PATH TORCH_FLASHLIGHT_PATH("brightness")
#define TORCH_MAX_BRIGHTNESS_PATH TORCH_FLASHLIGHT_PATH("max_brightness")
#define TORCH_TRIGGER_SETTING_PATH TORCH_FLASHLIGHT_PATH("trigger")

const uint8_t torchStepsToStrengthMap[] = {
    16, 29, 42, 54, 67, 80, 92, 105, 118, 131, 143, 156, 169, 181, 194,
    TORCH_DEFAULT_STRENGTH_LEVEL, 215, 228, 241,
    TORCH_DEFAULT_MAX_STRENGTH_LEVEL
};
#define TORCH_MAX_STRENGTH_STEPS std::size(torchStepsToStrengthMap)

/**
 * Write value to path and close file.
 */
template <typename T>
std::ofstream::iostate writeValue(const std::string& path, const T& value) {
    std::ofstream file(path);
    file << value;
    return file.rdstate();
}

/**
 * Read value from the path and close file.
 */
template <typename T>
T readValue(const std::string& path, const T& def) {
    std::ifstream file(path);
    T result;
    file >> result;
    return file.fail() ? def : result;
}

/**
 * Search for value from the path using custom predicate and close file.
 */
template <typename T, typename Pred>
std::optional<T> searchValue(const std::string& path, Pred pred) {
    std::ifstream file(path);
    T result;
    while (file >> result) {
        if (pred(result)) {
            return result;
        }
    }
    return std::nullopt;
}

bool isTorchTriggeringAllowed() {
    auto node = TORCH_TRIGGER_SETTING_PATH;
    // Search for "[word]" in file formatted as "opt1 [selected-opt2] opt3 ..."
    return searchValue<std::string>(node, [](const auto& value) {
        if (value.front() == '[' && value.back() == ']') {
            const std::string_view opt_bracketless(value.data() + 1,
                    value.size() - 2);
            return opt_bracketless == TORCH_TRIGGER_SETTING;
        }
        return false;
    }).has_value();
}

int32_t getTorchMaxStrengthLevel() {
    auto node = TORCH_MAX_BRIGHTNESS_PATH;
    return std::min(readValue(node, 0), TORCH_DEFAULT_MAX_STRENGTH_LEVEL);
}

int32_t getTorchStepSize() {
    const auto max = getTorchMaxStrengthLevel();
    return max / TORCH_MAX_STRENGTH_STEPS;
}

inline int32_t convertStrengthLevelToSteps(int32_t torchStrength) {
    // NOTE: round up to match the UI slider interpolation
    return std::round(torchStrength / getTorchStepSize());
}

} // namespace

bool supportsTorchStrengthControlExt() {
    // Always supported, so that this extension's APIs are called, otherwise
    // cameraserver will propagate the call to camera HAL, which doesn't
    // implement the torch control, so SystemUI will crash..
    return true;
}

int32_t getTorchDefaultStrengthLevelExt() {
    const int32_t max = getTorchMaxStrengthLevel();
    const auto torchStrength = std::min(TORCH_DEFAULT_STRENGTH_LEVEL, max);
    return convertStrengthLevelToSteps(torchStrength);
}

int32_t getTorchMaxStrengthLevelExt() {
    return TORCH_MAX_STRENGTH_STEPS;
}

int32_t getTorchStrengthLevelExt() {
    auto node = TORCH_BRIGHTNESS_PATH;
    const auto torchStrength = readValue(node, 0);
    return convertStrengthLevelToSteps(torchStrength);
}

void setTorchStrengthLevelExt(int32_t steps, bool enabled) {
    if (!isTorchTriggeringAllowed()) {
        ALOGW("%s: triggering torch has been disabled in driver.",
                __FUNCTION__);
        return;
    }

    // NOTE: do not set strength when cameraserver is turning off torch, as
    // this will be done via camera HAL's CameraProviderManager::setTorchMode
    // API, otherwise writing zero (0) to sysfs node will force the driver to
    // switch the trigger mode to "none", making camera service being unable to
    // toggle flashlight anymore. According to API specs, enabled=false param
    // is only for cleanups and restoring purposes anyways..
    if (enabled) {
        auto node = TORCH_BRIGHTNESS_PATH;
        const auto torchStrength = torchStepsToStrengthMap[steps - 1];
        ALOGV("%s: steps=%d, enabled=%d, torchStrength=%d.", __FUNCTION__,
                steps, enabled, torchStrength);
        const auto state = writeValue(node, std::to_string(torchStrength));
        if (state != std::ios_base::goodbit) {
            ALOGE("%s: I/O Error: %d.", __FUNCTION__, state);
        }
    }
}

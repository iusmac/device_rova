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

namespace {

#define TORCH_DEFAULT_STRENGTH_LEVEL 200 // matches kernel driver

#define TORCH_FLASHLIGHT_PATH(file) "/sys/class/leds/flashlight/" file
#define TORCH_BRIGHTNESS_PATH TORCH_FLASHLIGHT_PATH("brightness")
#define TORCH_MAX_BRIGHTNESS_PATH TORCH_FLASHLIGHT_PATH("max_brightness")

/**
 * Write value to path and close file.
 */
template <typename T>
void writeValue(const std::string& path, const T& value) {
    std::ofstream file(path);
    file << value;
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

} // namespace

bool supportsTorchStrengthControlExt() {
    // Always supported, so that this extension's APIs are called, otherwise
    // cameraserver will propagate the call to camera HAL, which doesn't
    // implement the torch control, so SystemUI will crash..
    return true;
}

int32_t getTorchDefaultStrengthLevelExt() {
    const int32_t max = getTorchMaxStrengthLevelExt();
    return std::min(TORCH_DEFAULT_STRENGTH_LEVEL, max);
}

int32_t getTorchMaxStrengthLevelExt() {
    auto node = TORCH_MAX_BRIGHTNESS_PATH;
    return readValue(node, 0);
}

int32_t getTorchStrengthLevelExt() {
    auto node = TORCH_BRIGHTNESS_PATH;
    return readValue(node, 0);
}

void setTorchStrengthLevelExt(int32_t torchStrength, bool enabled) {
    auto node = TORCH_BRIGHTNESS_PATH;
    writeValue(node, torchStrength);
}

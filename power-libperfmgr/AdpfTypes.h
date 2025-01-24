/*
 * Copyright 2023 The Android Open Source Project
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

#pragma once

#include <aidl/android/hardware/power/Boost.h>
#include <aidl/android/hardware/power/ChannelConfig.h>
#include <aidl/android/hardware/power/ChannelMessage.h>
#include <aidl/android/hardware/power/IPower.h>
#include <aidl/android/hardware/power/IPowerHintSession.h>
#include <aidl/android/hardware/power/Mode.h>
#include <aidl/android/hardware/power/SessionConfig.h>
#include <aidl/android/hardware/power/SessionTag.h>
#include <aidl/android/hardware/power/WorkDuration.h>
#include <android-base/thread_annotations.h>
#include <fmq/AidlMessageQueue.h>
#include <fmq/EventFlag.h>

#include <cstdint>

namespace aidl::google::hardware::power::impl::pixel {

using namespace android::hardware::power;

using ::android::AidlMessageQueue;
using ::android::hardware::EventFlag;
using android::hardware::common::fmq::MQDescriptor;
using android::hardware::common::fmq::SynchronizedReadWrite;

using ChannelQueueDesc = MQDescriptor<ChannelMessage, SynchronizedReadWrite>;
using ChannelQueue = AidlMessageQueue<ChannelMessage, SynchronizedReadWrite>;
using FlagQueueDesc = MQDescriptor<int8_t, SynchronizedReadWrite>;
using FlagQueue = AidlMessageQueue<int8_t, SynchronizedReadWrite>;

enum class AdpfErrorCode : int32_t { ERR_OK = 0, ERR_BAD_STATE = -1, ERR_BAD_ARG = -2 };

enum class SessionJankyLevel : int32_t {
    /**
     * Small number of jank frames in the monitoring window.
     * No extra heuristic boost will be applied.
     */
    LIGHT = 0,
    /**
     * Moderate number of jank frames in the monitoring window.
     * Heuristic boost applied.
     */
    MODERATE,
    /**
     * Significant number of jank frames in the monitoring window.
     * Heuristic boost applied.
     */
    SEVERE,
};

enum class AdpfVoteType : int32_t {
    CPU_VOTE_DEFAULT = 0,
    CPU_LOAD_UP,
    CPU_LOAD_RESET,
    CPU_LOAD_RESUME,
    VOTE_POWER_EFFICIENCY,
    GPU_LOAD_UP,
    GPU_LOAD_DOWN,
    GPU_LOAD_RESET,
    GPU_CAPACITY,
    VOTE_TYPE_SIZE
};

constexpr const char *AdpfVoteTypeToStr(AdpfVoteType voteType) {
    switch (voteType) {
        case AdpfVoteType::CPU_VOTE_DEFAULT:
            return "CPU_VOTE_DEFAULT";
        case AdpfVoteType::CPU_LOAD_UP:
            return "CPU_LOAD_UP";
        case AdpfVoteType::CPU_LOAD_RESET:
            return "CPU_LOAD_RESET";
        case AdpfVoteType::CPU_LOAD_RESUME:
            return "CPU_LOAD_RESUME";
        case AdpfVoteType::VOTE_POWER_EFFICIENCY:
            return "VOTE_POWER_EFFICIENCY";
        case AdpfVoteType::GPU_LOAD_UP:
            return "GPU_LOAD_UP";
        case AdpfVoteType::GPU_LOAD_DOWN:
            return "GPU_LOAD_DOWN";
        case AdpfVoteType::GPU_LOAD_RESET:
            return "GPU_LOAD_RESET";
        case AdpfVoteType::GPU_CAPACITY:
            return "GPU_CAPACITY";
        default:
            return "INVALID_VOTE";
    }
}

class Immobile {
  public:
    Immobile() {}
    Immobile(const Immobile &) = delete;
    Immobile(Immobile &&) = delete;
    Immobile &operator=(const Immobile &) = delete;
    Immobile &operator=(Immobile &) = delete;
};

constexpr int kUclampMin{0};
constexpr int kUclampMax{1024};

}  // namespace aidl::google::hardware::power::impl::pixel

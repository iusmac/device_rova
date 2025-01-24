/*
 * Copyright 2024 The Android Open Source Project
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

#define LOG_TAG "powerhal-libperfmgr"

#include "SessionRecords.h"

#include <android-base/logging.h>

namespace aidl {
namespace google {
namespace hardware {
namespace power {
namespace impl {
namespace pixel {

SessionRecords::SessionRecords(const int32_t maxNumOfRecords, const double jankCheckTimeFactor)
    : kMaxNumOfRecords(maxNumOfRecords), kJankCheckTimeFactor(jankCheckTimeFactor) {
    mRecords.resize(maxNumOfRecords);
}

void SessionRecords::addReportedDurations(const std::vector<WorkDuration> &actualDurationsNs,
                                          int64_t targetDurationNs) {
    for (auto &duration : actualDurationsNs) {
        int32_t totalDurationUs = duration.durationNanos / 1000;

        if (mNumOfFrames >= kMaxNumOfRecords) {
            // Remove the oldest record when the number of records is greater
            // than allowed.
            int32_t indexOfRecordToRemove = (mLatestRecordIndex + 1) % kMaxNumOfRecords;
            mSumOfDurationsUs -= mRecords[indexOfRecordToRemove].totalDurationUs;
            if (mRecords[indexOfRecordToRemove].isMissedCycle) {
                mNumOfMissedCycles--;
                if (mNumOfMissedCycles < 0) {
                    LOG(ERROR) << "Invalid number of missed cycles: " << mNumOfMissedCycles;
                }
            }
            mNumOfFrames--;

            // If the record to be removed is the max duration, pop it out of the
            // descending dequeue of record indexes.
            if (mRecordsIndQueue.front() == indexOfRecordToRemove) {
                mRecordsIndQueue.pop_front();
            }
        }

        mLatestRecordIndex = (mLatestRecordIndex + 1) % kMaxNumOfRecords;

        // Track start delay
        auto startTimeNs = duration.timeStampNanos - duration.durationNanos;
        int32_t startIntervalUs = 0;
        if (mNumOfFrames > 0) {
            startIntervalUs = (startTimeNs - mLastStartTimeNs) / 1000;
        }
        mLastStartTimeNs = startTimeNs;

        bool cycleMissed = totalDurationUs > (targetDurationNs / 1000) * kJankCheckTimeFactor;
        mRecords[mLatestRecordIndex] = CycleRecord{startIntervalUs, totalDurationUs, cycleMissed};
        mNumOfFrames++;
        if (cycleMissed) {
            mNumOfMissedCycles++;
        }

        // Pop out the indexes that their related values are not greater than the
        // latest one.
        while (!mRecordsIndQueue.empty() &&
               (mRecords[mRecordsIndQueue.back()].totalDurationUs <= totalDurationUs)) {
            mRecordsIndQueue.pop_back();
        }
        mRecordsIndQueue.push_back(mLatestRecordIndex);

        mSumOfDurationsUs += totalDurationUs;
        mAvgDurationUs = mSumOfDurationsUs / mNumOfFrames;
    }
}

std::optional<int32_t> SessionRecords::getMaxDuration() {
    if (mRecordsIndQueue.empty()) {
        return std::nullopt;
    }
    return mRecords[mRecordsIndQueue.front()].totalDurationUs;
}

std::optional<int32_t> SessionRecords::getAvgDuration() {
    if (mNumOfFrames <= 0) {
        return std::nullopt;
    }
    return mAvgDurationUs;
}

int32_t SessionRecords::getNumOfRecords() {
    return mNumOfFrames;
}

int32_t SessionRecords::getNumOfMissedCycles() {
    return mNumOfMissedCycles;
}

bool SessionRecords::isLowFrameRate(int32_t fpsLowRateThreshold) {
    // Check the last three records. If all of their start delays are larger
    // than the cycle duration threshold, return "true".
    auto cycleDurationThresholdUs = 1000000.0 / fpsLowRateThreshold;
    if (mNumOfFrames >= 3) {  // Todo: make this number as a tunable config
        int32_t ind1 = mLatestRecordIndex;
        int32_t ind2 = ind1 == 0 ? (kMaxNumOfRecords - 1) : (ind1 - 1);
        int32_t ind3 = ind2 == 0 ? (kMaxNumOfRecords - 1) : (ind2 - 1);
        return (mRecords[ind1].startIntervalUs >= cycleDurationThresholdUs) &&
               (mRecords[ind2].startIntervalUs >= cycleDurationThresholdUs) &&
               (mRecords[ind3].startIntervalUs >= cycleDurationThresholdUs);
    }

    return false;
}

}  // namespace pixel
}  // namespace impl
}  // namespace power
}  // namespace hardware
}  // namespace google
}  // namespace aidl

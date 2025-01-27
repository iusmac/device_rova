#
# Copyright (C) 2021 The LineageOS Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

ifneq (,$(findstring hardware/google/interfaces, $(PRODUCT_SOONG_NAMESPACES)))
ifneq (,$(findstring hardware/google/pixel, $(PRODUCT_SOONG_NAMESPACES)))

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_RELATIVE_PATH := hw

LOCAL_SHARED_LIBRARIES := \
    libbase \
    libbinder_ndk \
    libcutils \
    libdl \
    liblog \
    libperfmgr \
    libprocessgroup \
    libutils \
    pixel-power-ext-V1-ndk \
    android.hardware.common.fmq-V1-ndk \
    libfmq

# Keep these libraries in sync with the android.hardware.power-ndk_shared
# module located in hardware/interfaces/power/aidl/Android.bp
LOCAL_SHARED_LIBRARIES += \
    android.hardware.power-V5-ndk

LOCAL_STATIC_LIBRARIES := \
    libgmock \
    libgtest

LOCAL_SRC_FILES := \
    BackgroundWorker.cpp \
    GpuCalculationHelpers.cpp \
    GpuCapacityNode.cpp \
    service.cpp \
    InteractionHandler.cpp \
    Power.cpp \
    PowerExt.cpp \
    PowerHintSession.cpp \
    PowerSessionManager.cpp \
    UClampVoter.cpp \
    SessionRecords.cpp \
    SessionTaskMap.cpp \
    SessionValueEntry.cpp

LOCAL_CFLAGS := -std=gnu++20 -Wthread-safety -Wno-unused-parameter -Wno-unused-variable

ifneq ($(TARGET_POWERHAL_MODE_EXT),)
    LOCAL_CFLAGS += -DMODE_EXT
    LOCAL_SRC_FILES += ../../../../$(TARGET_POWERHAL_MODE_EXT)
endif

ifneq ($(TARGET_TAP_TO_WAKE_NODE),)
    LOCAL_CFLAGS += -DTAP_TO_WAKE_NODE=\"$(TARGET_TAP_TO_WAKE_NODE)\"
endif

LOCAL_MODULE := android.hardware.power-service.xiaomi_rova-libperfmgr
LOCAL_INIT_RC := android.hardware.power-service.xiaomi_rova-libperfmgr.rc
LOCAL_MODULE_TAGS := optional
LOCAL_VENDOR_MODULE := true
LOCAL_VINTF_FRAGMENTS := android.hardware.power-service.xiaomi_rova.xml

include $(BUILD_EXECUTABLE)

endif
endif

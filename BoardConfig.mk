#
# Copyright (C) 2021 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from common rova-common
include device/xiaomi/rova-common/BoardConfigCommon.mk

DEVICE_PATH := device/xiaomi/rova

# Asserts
TARGET_OTA_ASSERT_DEVICE := rolex,riva,rova

# Init
TARGET_INIT_VENDOR_LIB := //$(DEVICE_PATH):libinit_rova
TARGET_RECOVERY_DEVICE_MODULES := libinit_rova

# Kernel
TARGET_KERNEL_CONFIG := mi8937_defconfig

# Security patch level
VENDOR_SECURITY_PATCH := 2018-07-01

# SELinux
BOARD_VENDOR_SEPOLICY_DIRS += $(DEVICE_PATH)/sepolicy/vendor

# Inherit from the proprietary version
include vendor/xiaomi/rova/BoardConfigVendor.mk

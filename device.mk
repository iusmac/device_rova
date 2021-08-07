#
# Copyright (C) 2021 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from rova-common
$(call inherit-product, device/xiaomi/rova-common/rova.mk)
$(call inherit-product, frameworks/native/build/phone-xhdpi-2048-dalvik-heap.mk)

# Rootdir
PRODUCT_PACKAGES += \
    init.xiaomi.device.rc \
    init.xiaomi.device.sh

# Soong namespaces
PRODUCT_SOONG_NAMESPACES += \
    $(LOCAL_PATH)

# Inherit from vendor blobs
$(call inherit-product, vendor/xiaomi/rova/rova-vendor.mk)

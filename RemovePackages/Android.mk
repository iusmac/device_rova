LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := RemovePackages
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional 
LOCAL_OVERRIDES_PACKAGES := AmbientSensePrebuilt DevicePolicyPrebuilt Drive Maps MyVerizonServices PixelWallpapers2020 SafetyHubPrebuilt ScribePrebuilt Showcase SoundAmplifierPrebuilt YouTube YouTubeMusicPrebuilt WallpapersBReel2020 obdm_stub Calendar GoogleTTS LocationHistoryPrebuilt MarkupGoogle Velvet WellbeingPrebuilt FM2 PrebuiltGmail talkback Videos
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_SRC_FILES := /dev/null
include $(BUILD_PREBUILT)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := Remove_Packages
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_OVERRIDES_PACKAGES := AmbientSensePrebuilt AppDirectedSMSService SCONE
LOCAL_OVERRIDES_PACKAGES += Camera2 CarrierSetup ConnMO ConnMetrics DCMO
LOCAL_OVERRIDES_PACKAGES += DMService DevicePolicyPrebuilt
LOCAL_OVERRIDES_PACKAGES += DiagnosticsToolPrebuilt HelpRtcPrebuilt
LOCAL_OVERRIDES_PACKAGES += MyVerizonServices NgaResources
LOCAL_OVERRIDES_PACKAGES += OBDM_Permissions OemDmTrigger
LOCAL_OVERRIDES_PACKAGES += PixelLiveWallpaperPrebuilt
LOCAL_OVERRIDES_PACKAGES += PixelWallpapers2021 PrebuiltGmail Maps
LOCAL_OVERRIDES_PACKAGES += RecorderPrebuilt SafetyHubPrebuilt arcore
LOCAL_OVERRIDES_PACKAGES += ScribePrebuilt Showcase GoogleTTS   
LOCAL_OVERRIDES_PACKAGES += SoundAmplifierPrebuilt PixelBuds
LOCAL_OVERRIDES_PACKAGES += SprintDM SprintHM Tycho USCCDM VZWAPNLib GoogleFeedback
LOCAL_OVERRIDES_PACKAGES += VzwOmaTrigger obdm_stub
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_SRC_FILES := /dev/null
include $(BUILD_PREBUILT)

ifeq ($(call my-dir),$(call project-path-for,qcom-audio))

MY_LOCAL_PATH := $(call my-dir)

ifeq ($(AUDIO_FEATURE_ENABLED_STT_SUPPORT),true)
include $(MY_LOCAL_PATH)/stt_meta/Android.mk
endif

endif

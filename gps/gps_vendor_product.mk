PRODUCT_PACKAGES += gps.conf
PRODUCT_PACKAGES += flp.conf
PRODUCT_PACKAGES += gnss_antenna_info.conf
PRODUCT_PACKAGES += gnss@2.0-base.policy
PRODUCT_PACKAGES += gnss@2.0-xtra-daemon.policy
PRODUCT_PACKAGES += gnss@2.0-xtwifi-client.policy
PRODUCT_PACKAGES += gnss@2.0-xtwifi-inet-agent.policy
PRODUCT_PACKAGES += libloc_pla_headers
PRODUCT_PACKAGES += liblocation_api_headers
PRODUCT_PACKAGES += libgps.utils_headers
PRODUCT_PACKAGES += liblocation_api
PRODUCT_PACKAGES += libgps.utils
PRODUCT_PACKAGES += libbatching
PRODUCT_PACKAGES += libgeofencing
PRODUCT_PACKAGES += libloc_core
PRODUCT_PACKAGES += libgnss

ifeq ($(strip $(TARGET_BOARD_AUTO)),true)
PRODUCT_PACKAGES += libgnssauto_power
endif #TARGET_BOARD_AUTO

PRODUCT_PACKAGES += android.hardware.gnss@2.1-impl-qti
PRODUCT_PACKAGES += android.hardware.gnss@2.1-service-qti

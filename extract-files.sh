#!/bin/bash
#
# Copyright (C) 2016 The CyanogenMod Project
# Copyright (C) 2017-2020 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

set -e

export DEVICE=rova
export VENDOR=xiaomi

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

ANDROID_ROOT="${MY_DIR}/../../.."

HELPER="${ANDROID_ROOT}/tools/extract-utils/extract_utils.sh"
if [ ! -f "${HELPER}" ]; then
    echo "Unable to find helper script at ${HELPER}"
    exit 1
fi
source "${HELPER}"

# Default to sanitizing the vendor folder before extraction
CLEAN_VENDOR=true

ONLY_COMMON=
ONLY_TARGET=
KANG=
SECTION=

SETUP_MAKEFILES_ARGS=

while [ "${#}" -gt 0 ]; do
    case "${1}" in
        --only-common )
                ONLY_COMMON=true
                SETUP_MAKEFILES_ARGS+=" ${1}"
                ;;
        --only-target )
                ONLY_TARGET=true
                SETUP_MAKEFILES_ARGS+=" ${1}"
                ;;
        -n | --no-cleanup )
                CLEAN_VENDOR=false
                ;;
        -k | --kang )
                KANG="--kang"
                ;;
        -s | --section )
                SECTION="${2}"; shift
                CLEAN_VENDOR=false
                ;;
        * )
                SRC="${1}"
                ;;
    esac
    shift
done

if [ -z "${SRC}" ]; then
    SRC="adb"
fi

function blob_fixup() {
    case "${1}" in
        product/etc/permissions/vendor.qti.hardware.data.connection-V1.0-java.xml \
        | product/etc/permissions/vendor.qti.hardware.data.connection-V1.1-java.xml)
            sed -i 's|version="2.0"|version="1.0"|g' "${2}"
            ;;
        system_ext/lib64/lib-imscamera.so)
            for LIBSHIM_IMSVIDEOCODEC in $(grep -L "libshim_imscamera.so" "${2}"); do
                "${PATCHELF}" --add-needed "libshim_imscamera.so" "${2}"
            done
            ;;
        vendor/lib64/libQmiservices.so | vendor/lib64/libril-qc-hal-qmi.so )
            sed -i 's|libqmiservices.so|libQmiservices.so|g' "${2}"
            ;;
        vendor/lib/libmmsw_platform.so|vendor/lib/libmmsw_detail_enhancement.so)
            "${PATCHELF}" --remove-needed "libbinder.so" "${2}"
            sed -i 's|libgui.so|libwui.so|g' "${2}"
            ;;
        vendor/lib/libmmcamera2_sensor_modules.so)
            sed -i 's|/system/etc/camera/|/vendor/etc/camera/|g' "${2}"
            sed -i 's|data/misc/camera|data/vendor/qcam|g' "${2}"
            ;;
        vendor/lib/libmmcamera_tintless_bg_pca_algo.so \
        |vendor/lib/libmmcamera_pdafcamif.so \
        |vendor/lib/libmmcamera2_dcrf.so \
        |vendor/lib/libmmcamera_imglib.so \
        |vendor/lib/libmmcamera_dbg.so \
        |vendor/lib/libmmcamera2_stats_algorithm.so \
        |vendor/lib/libmmcamera2_mct.so \
        |vendor/lib/libmmcamera_tuning.so \
        |vendor/lib/libmmcamera_tintless_algo.so \
        |vendor/lib/libmmcamera2_iface_modules.so \
        |vendor/lib/libmmcamera2_q3a_core.so \
        |vendor/lib/libmmcamera2_pproc_modules.so \
        |vendor/lib/libmmcamera2_imglib_modules.so \
        |vendor/lib/libmmcamera2_cpp_module.so \
        |vendor/lib/libmmcamera_pdaf.so \
        |vendor/bin/mm-qcamera-daemon)
            sed -i 's|data/misc/camera|data/vendor/qcam|g' "${2}"
            ;;
        vendor/lib/libmmcamera2_stats_modules.so)
            sed -i 's|data/misc/camera|data/vendor/qcam|g' "${2}"
            sed -i 's|libgui.so|libwui.so|g' "${2}"
            "${PATCHELF}" --replace-needed "libandroid.so" "libshims_android.so" "${2}"
            ;;
        vendor/lib/libmmcamera_ppeiscore.so)
            sed -i 's|libgui.so|libwui.so|g' "${2}"
            if ! "${PATCHELF}" --print-needed "${2}" | grep "libshims_ui.so" >/dev/null; then
                "${PATCHELF}" --add-needed "libshims_ui.so" "${2}"
            fi
            ;;
        vendor/lib/libmpbase.so)
            "${PATCHELF}" --replace-needed "libandroid.so" "libshims_android.so" "${2}"
            ;;
    esac
}

if [ -z "${ONLY_TARGET}" ]; then
    # Initialize the helper
    setup_vendor "${DEVICE}" "${VENDOR}" "${ANDROID_ROOT}" true "${CLEAN_VENDOR}"

    extract "${MY_DIR}/proprietary-files.txt" "${SRC}" "${KANG}" --section "${SECTION}"
fi

"${MY_DIR}/setup-makefiles.sh"

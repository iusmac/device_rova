/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 *//**
 * @file ICwbControlConst.aidl
 * @brief Defines the different types of CWB flags to control CWB output and its process.
 *
 * This interface defines different CWB process controlling flag constants, which all can be used
 * together by using bitwise OR (|) operator, but enums with prefix OUTPUT, PRIORITY and DOWNSCALE
 * cannot be OR'd with itself.
 * e.g. With prefix OUTPUT, PRIORITY and DOWNSCALE flag can't repeat with OR operator.
 */
///////////////////////////////////////////////////////////////////////////////
// THIS FILE IS IMMUTABLE. DO NOT EDIT IN ANY CASE.                          //
///////////////////////////////////////////////////////////////////////////////

// This file is a snapshot of an AIDL file. Do not edit it manually. There are
// two cases:
// 1). this is a frozen version file - do not edit this in any case.
// 2). this is a 'current' file. If you make a backwards compatible change to
//     the interface (from the latest frozen version), the build system will
//     prompt you to update this file with `m <name>-update-api`.
//
// You must not make a backward incompatible change to any AIDL file built
// with the aidl_interface module type with versions property set. The module
// type is used to build AIDL files in a way that they can be used across
// independently updatable components of the system. If a device is shipped
// with such a backward incompatible change, it has a high risk of breaking
// later when a module using the interface is updated, e.g., Mainline modules.

package vendor.qti.hardware.display.config;
@VintfStability
interface ICwbControlConst {
  const int OUTPUT_AS_LM_DUMP = 0;
  const int OUTPUT_AS_DSPP_DUMP = 1;
  const int OUTPUT_AS_DEMURA_DUMP = 2;
  const int PU_AS_CWB_ROI = (1 << 4) /* 16 */;
  const int FORCED_REFRESH_ON_REQUEST = (1 << 5) /* 32 */;
  const int PRIORITY_SCALE = (1 << 8) /* 256 */;
  const int PRIORITY_AS_HIGH = (2 << 8) /* 512 */;
  const int PRIORITY_AS_MEDIUM = (8 << 8) /* 2048 */;
  const int PRIORITY_AS_LOW = (15 << 8) /* 3840 */;
  const int DOWNSCALE_CONFIG_BY_PERCENT = (2 << 12) /* 8192 */;
  const int DOWNSCALE_CONFIG_BY_DIVISOR = (3 << 12) /* 12288 */;
  const int HCENTER_ALIGN_DOWNSCALE_IMAGE = (1 << 14) /* 16384 */;
  const int VCENTER_ALIGN_DOWNSCALE_IMAGE = (1 << 15) /* 32768 */;
  const int WIDTH_DOWNSCALER = (1 << 16) /* 65536 */;
  const int HEIGHT_DOWNSCALER = (1 << 24) /* 16777216 */;
}

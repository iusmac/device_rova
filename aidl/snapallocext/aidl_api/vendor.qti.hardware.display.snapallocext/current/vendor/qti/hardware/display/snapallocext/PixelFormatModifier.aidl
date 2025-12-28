/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 *//**
 * @file PixelFormatModifier.aidl
 * @brief Enum for pixel format modifiers.
 *
 * This enum defines various modifiers that can be applied to pixel formats,
 * indicating specific memory layouts, compression schemes, or hardware-specific
 * optimizations for buffer allocation.
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

package vendor.qti.hardware.display.snapallocext;
@Backing(type="int") @VintfStability
enum PixelFormatModifier {
  PIXEL_FORMAT_MODIFIER_NONE = 0,
  PIXEL_FORMAT_MODIFIER_VENUS = 1,
  PIXEL_FORMAT_MODIFIER_ADRENO = 2,
  PIXEL_FORMAT_MODIFIER_FLEX = 3,
  PIXEL_FORMAT_MODIFIER_LINEAR_FLEX = 4,
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX = 5,
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX_2_BATCH = 6,
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX_4_BATCH = 7,
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX_8_BATCH = 8,
  PIXEL_FORMAT_MODIFIER_TILED = 9,
  PIXEL_FORMAT_MODIFIER_ENCODEABLE = 10,
  PIXEL_FORMAT_MODIFIER_HEIF = 11,
  PIXEL_FORMAT_MODIFIER_4R = 12,
  PIXEL_FORMAT_MODIFIER_EXPLICIT_UBWC = 13,
  PIXEL_FORMAT_MODIFIER_MIPMAP = 14,
  PIXEL_FORMAT_MODIFIER_UBWC_MIPMAP = 15,
  PIXEL_FORMAT_MODIFIER_1K_ALIGNED = 16,
}

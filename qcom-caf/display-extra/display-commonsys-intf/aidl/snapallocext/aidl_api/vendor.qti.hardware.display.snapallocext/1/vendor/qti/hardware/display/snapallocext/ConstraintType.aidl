/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 *//**
 * @file ConstraintType.aidl
 * @brief Enum for the ConstraintType.
 *
 * This enum defines different categories or types of allocation constraints.
 * These types help in applying specific alignment rules based on the intended
 * usage of a buffer (e.g., CPU, display, video).
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
enum ConstraintType {
  DEFAULT_ALIGNMENT = 1,
  CPU_ALIGNMENT = 2,
  DISPLAY_ALIGNEMNT = 3,
  VIDEO_ALIGNMENT = 4,
  GRAPHIC_ALIGNMENT = 5,
  CAMERA_ALIGNMENT = 6,
  UBWC_ALIGNMENT = 7,
}

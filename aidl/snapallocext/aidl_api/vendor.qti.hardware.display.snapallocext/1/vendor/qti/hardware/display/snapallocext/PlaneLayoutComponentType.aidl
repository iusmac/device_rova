/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 *//**
 * @file PlaneLayoutComponentType.aidl
 * @brief Enum defining the types of components within a plane's layout.
 *
 * This enum identifies the semantic meaning of data components (e.g., color channels,
 * luma, chroma, alpha) within a buffer plane.
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
@Backing(type="long") @VintfStability
enum PlaneLayoutComponentType {
  PLANE_LAYOUT_COMPONENT_TYPE_Y = (1 << 0) /* 1 */,
  PLANE_LAYOUT_COMPONENT_TYPE_CB = (1 << 1) /* 2 */,
  PLANE_LAYOUT_COMPONENT_TYPE_CR = (1 << 2) /* 4 */,
  PLANE_LAYOUT_COMPONENT_TYPE_R = (1 << 10) /* 1024 */,
  PLANE_LAYOUT_COMPONENT_TYPE_G = (1 << 11) /* 2048 */,
  PLANE_LAYOUT_COMPONENT_TYPE_B = (1 << 12) /* 4096 */,
  PLANE_LAYOUT_COMPONENT_TYPE_RAW = (1 << 20) /* 1048576 */,
  PLANE_LAYOUT_COMPONENT_TYPE_BLOB = (1 << 29) /* 536870912 */,
  PLANE_LAYOUT_COMPONENT_TYPE_A = (1 << 30) /* 1073741824 */,
  PLANE_LAYOUT_COMPONENT_TYPE_META = (1 << 31) /* -2147483648 */,
}

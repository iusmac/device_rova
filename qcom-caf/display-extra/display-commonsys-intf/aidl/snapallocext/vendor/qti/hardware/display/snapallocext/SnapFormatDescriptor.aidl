/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file SnapFormatDescriptor.aidl
 * @brief Parcelable representing a complete description of a graphics format.
 *
 * This parcelable combines pixel format, modifier, and a boolean value.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.PixelFormat;
import vendor.qti.hardware.display.snapallocext.PixelFormatModifier;

@VintfStability
parcelable SnapFormatDescriptor {
  /**
   * @brief The base pixel format (e.g., RGBA_8888, YCbCr_420_SP).
   */
  PixelFormat format;
  /**
   * @brief An optional modifier that provides details about the format's properties,
   *        such as tiling, compression, or hardware-specific optimizations.
   */
  PixelFormatModifier modifier;
}
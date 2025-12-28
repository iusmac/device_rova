/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PixelFormatData.aidl
 * @brief Parcelable associating a pixel format with its detailed format data.
 *
 * This parcelable maps a pixel format identifier to its structure and properties.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.PixelFormat;
import vendor.qti.hardware.display.snapallocext.FormatData;

@VintfStability
parcelable PixelFormatData{
  /**
   * @brief The specific pixel format identifier (e.g., RGBA_8888, YCRCB_420_SP).
   */
  PixelFormat pixelFormat;
  /**
   * @brief Detailed data describing the layout and properties of the specified pixel format.
   *        This includes information like bits per pixel and plane-specific layouts.
   */
  FormatData formatData;
}
/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file FormatData.aidl
 * @brief Parcelable representing detailed format information for a buffer.
 *
 * This parcelable details a buffer's format, including bits per pixel and plane layouts.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.PlaneLayoutData;

@VintfStability
parcelable FormatData {
  /**
   * @brief The total number of bits used per pixel for this format.
   */
  int bits_per_pixel;
  /**
   * @brief A list of data structures describing the layout for each plane within the buffer.
   */
  List<PlaneLayoutData> planes;
}
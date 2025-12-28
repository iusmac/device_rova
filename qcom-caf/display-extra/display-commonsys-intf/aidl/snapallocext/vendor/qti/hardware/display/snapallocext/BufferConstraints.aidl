/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file BufferConstraints.aidl
 * @brief Parcelable for defining buffer allocation constraints.
 *
 * This parcelable defines buffer allocation constraints like modifier,
 * plane settings, and size alignment.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.PlaneConstraints;

@VintfStability
parcelable BufferConstraints {
  /**
   * @brief Specifies the buffer modifier.
   *        A modifier indicates special properties or tiling formats for the buffer.
   *        A value of 0 typically means no specific modifier is required.
   */
  long modifier;
  /**
   * @brief A list of constraints for each plane within the buffer.
   */
  List<PlaneConstraints> planes;
  /**
   * @brief The alignment requirement in bytes for the overall buffer size.
   */
  int size_align_bytes;
}
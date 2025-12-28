/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PlaneConstraints.aidl
 * @brief Parcelable for defining allocation constraints for a single plane within a buffer.
 *
 * This parcelable specifies plane constraints like component info, alignment, dimensions.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.AlignmentType;
import vendor.qti.hardware.display.snapallocext.PlaneLayoutComponentType;
import vendor.qti.hardware.display.snapallocext.Stride;
import vendor.qti.hardware.display.snapallocext.Scanline;

@VintfStability
parcelable PlaneConstraints {
  /**
   * @brief An array of component sizes or identifiers within this plane.
   */
  PlaneLayoutComponentType[] components;
  /**
   * @brief The type of alignment to be applied to this plane.
   *        This determines the specific alignment rules (e.g., for output, general alignment).
   */
  AlignmentType alignment_type;
  /**
   * @brief The required alignment in bytes for the size of this plane.
   *        The allocated size for this plane must be a multiple of this value.
   */
  long size_align;
  /**
   * @brief The width of the block used for alignment or tiling within this plane.
   */
  int block_width;
  /**
   * @brief The height of the block used for alignment or tiling within this plane.
   */
  int block_height;
  /**
   * @brief Specifies the stride constraints for this plane (bytes per row).
   */
  Stride stride;
  /**
   * @brief Specifies the scanline constraints for this plane (number of rows).
   */
  Scanline scanline;
}
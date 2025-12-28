/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file AlignmentType.aidl
 * @brief Enum for the Alignment type.
 *
 * This enum defines different types of alignment that can be applied to graphical elements.
 */

package vendor.qti.hardware.display.snapallocext;

@Backing(type="int") @VintfStability
enum AlignmentType {
  /**
   * @brief Represents an uninitialized or default alignment state.
   */
  ALIGN_UNINITIALIZED = 0,
  /**
   * @brief Indicates that the output of an operation should be aligned.
   */
  ALIGNED_OUTPUT = 1,
  /**
   * @brief Represents a general alignment specification or requirement.
   */
  ALIGNMENT = 2,
}
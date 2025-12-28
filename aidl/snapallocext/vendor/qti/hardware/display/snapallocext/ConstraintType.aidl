/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file ConstraintType.aidl
 * @brief Enum for the ConstraintType.
 *
 * This enum defines different categories or types of allocation constraints.
 * These types help in applying specific alignment rules based on the intended
 * usage of a buffer (e.g., CPU, display, video).
 */

package vendor.qti.hardware.display.snapallocext;

@Backing(type="int") @VintfStability
enum ConstraintType{
  /**
   * @brief Represents the default alignment constraints.
   */
  DEFAULT_ALIGNMENT = 1,
  /**
   * @brief Specifies alignment constraints optimized for CPU access.
   */
  CPU_ALIGNMENT =  2,
  /**
   * @brief Specifies alignment constraints suitable for display output.
   */
  DISPLAY_ALIGNEMNT = 3,
  /**
   * @brief Specifies alignment constraints for video processing or playback.
   */
  VIDEO_ALIGNMENT = 4,
  /**
   * @brief Specifies alignment constraints for general graphics operations.
   */
  GRAPHIC_ALIGNMENT = 5,
  /**
   * @brief Specifies alignment constraints relevant for camera sensor data or processing.
   */
  CAMERA_ALIGNMENT = 6,
  /**
   * @brief Specifies alignment constraints for UBWC optimized buffers.
   */
  UBWC_ALIGNMENT = 7,
}
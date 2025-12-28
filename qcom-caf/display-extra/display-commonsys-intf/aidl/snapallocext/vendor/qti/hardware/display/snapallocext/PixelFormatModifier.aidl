/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PixelFormatModifier.aidl
 * @brief Enum for pixel format modifiers.
 *
 * This enum defines various modifiers that can be applied to pixel formats,
 * indicating specific memory layouts, compression schemes, or hardware-specific
 * optimizations for buffer allocation.
 */

package vendor.qti.hardware.display.snapallocext;

@Backing(type="int") @VintfStability
enum PixelFormatModifier {
  /**
   * @brief No specific modifier is applied; represents a standard, unoptimized layout.
   */
  PIXEL_FORMAT_MODIFIER_NONE = 0,
  /**
   * @brief Modifier for Venus-optimized pixel formats.
   */
  PIXEL_FORMAT_MODIFIER_VENUS = 1,
  /**
   * @brief Modifier for Adreno-optimized pixel formats.
   */
  PIXEL_FORMAT_MODIFIER_ADRENO = 2,
  /**
   * @brief General flexible modifier for pixel formats.
   */
  PIXEL_FORMAT_MODIFIER_FLEX = 3,
  /**
   * @brief Modifier for linear flexible pixel formats.
   */
  PIXEL_FORMAT_MODIFIER_LINEAR_FLEX = 4,
  /**
   * @brief Modifier for Universal Bandwidth Compression (UBWC) flexible pixel formats.
   */
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX = 5,
  /**
   * @brief UBWC flexible modifier for 2-batch processing.
   */
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX_2_BATCH = 6,
  /**
   * @brief UBWC flexible modifier for 4-batch processing.
   */
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX_4_BATCH = 7,
  /**
   * @brief UBWC flexible modifier for 8-batch processing.
   */
  PIXEL_FORMAT_MODIFIER_UBWC_FLEX_8_BATCH = 8,
  /**
   * @brief Modifier for tiled memory layout.
   */
  PIXEL_FORMAT_MODIFIER_TILED = 9,
  /**
   * @brief Modifier indicating the format is encodeable.
   */
  PIXEL_FORMAT_MODIFIER_ENCODEABLE = 10,
  /**
   * @brief Modifier for High Efficiency Image File (HEIF) formats.
   */
  PIXEL_FORMAT_MODIFIER_HEIF = 11,
  /**
   * @brief Modifier for 4R-optimized formats.
   */
  PIXEL_FORMAT_MODIFIER_4R = 12,
  /**
   * @brief Explicit UBWC modifier.
   */
  PIXEL_FORMAT_MODIFIER_EXPLICIT_UBWC = 13,
  /**
   * @brief Modifier for mipmap support.
   */
  PIXEL_FORMAT_MODIFIER_MIPMAP = 14,
  /**
   * @brief UBWC modifier with mipmap support.
   */
  PIXEL_FORMAT_MODIFIER_UBWC_MIPMAP = 15,
  /**
   * @brief Modifier indicating 1KB alignment requirement.
   */
  PIXEL_FORMAT_MODIFIER_1K_ALIGNED = 16,
}
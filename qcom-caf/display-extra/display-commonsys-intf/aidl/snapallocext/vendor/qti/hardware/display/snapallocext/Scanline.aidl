/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file Scanline.aidl
 * @brief Union representing scanline information for a buffer plane.
 *
 * This union holds explicit scanline value or scanline alignment for a plane.
 */

package vendor.qti.hardware.display.snapallocext;

@VintfStability
union Scanline {
  /**
   * @brief The explicit value for the number of scanlines in a plane.
   */
  long scanline_value;
  /**
   * @brief The alignment requirement in bytes for the scanline of a plane.
   */
  long scanline_align;
}
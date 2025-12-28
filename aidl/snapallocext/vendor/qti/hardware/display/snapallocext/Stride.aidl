/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file Stride.aidl
 * @brief Union representing stride information for a buffer plane.
 *
 * This union holds an explicit horizontal stride or a stride alignment requirement.
 */

package vendor.qti.hardware.display.snapallocext;

@VintfStability
union Stride {
  /**
   * @brief The explicit value for the horizontal stride of a plane.
   */
  long horizontal_stride;
  /**
   * @brief The alignment requirement in bytes for the horizontal stride of a plane.
   */
  long horizontal_stride_align;
}
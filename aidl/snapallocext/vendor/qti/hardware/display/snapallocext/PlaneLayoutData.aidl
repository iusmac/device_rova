/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PlaneLayoutData.aidl
 * @brief Parcelable describing the layout details of a single plane within a buffer.
 *
 * This parcelable defines component organization, sampling, and subsampling factors.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.PlaneLayoutComponent;

@VintfStability
parcelable PlaneLayoutData {
  /**
   * @brief The increment in bits between samples within this plane.
   */
  int sample_increment_bits;
  /**
   * @brief The horizontal subsampling factor for this plane relative to the primary plane.
   */
  long horizontal_subsampling;
  /**
   * @brief The vertical subsampling factor for this plane relative to the primary plane.
   */
  long vertical_subsampling;
  /**
   * @brief A list of individual components that constitute this plane's data.
   */
  List<PlaneLayoutComponent> components;
}
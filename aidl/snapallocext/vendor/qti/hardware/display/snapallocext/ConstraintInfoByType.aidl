/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file ConstraintInfoByType.aidl
 * @brief Parcelable representing a collection of constraints categorized by type.
 *
 * This parcelable groups constraint configurations by type for structured retrieval.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.ConstraintInfo;
import vendor.qti.hardware.display.snapallocext.ConstraintType;

@VintfStability
parcelable ConstraintInfoByType {
  /**
   * @brief Specifies the type or category of constraints contained within this set.
   */
  ConstraintType type;
  /**
   * @brief A list of individual constraint information entries.
   *        Each entry associates a format descriptor with its specific buffer constraints.
   */
  List<ConstraintInfo> constraintInfo;
}
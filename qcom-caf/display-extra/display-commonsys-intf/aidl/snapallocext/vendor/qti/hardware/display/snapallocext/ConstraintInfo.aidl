/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file ConstraintInfo.aidl
 * @brief Parcelable representing a set of constraints associated with a specific format.
 *
 * This parcelable combines a format descriptor with its buffer allocation constraints.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.BufferConstraints;
import vendor.qti.hardware.display.snapallocext.SnapFormatDescriptor;

@VintfStability
parcelable ConstraintInfo{
  /**
   * @brief Describes the format for which these constraints apply.
   *        This includes details like pixel format, width, and height.
   */
  SnapFormatDescriptor format;
  /**
   * @brief Specifies the buffer allocation constraints for the given format.
   *        This includes modifier, plane-specific constraints, and size alignment.
   */
  BufferConstraints constraints;
}
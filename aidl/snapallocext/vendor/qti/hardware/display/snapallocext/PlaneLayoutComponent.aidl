/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PlaneLayoutComponent.aidl
 * @brief Parcelable representing a single component within a plane's layout.
 *
 * This parcelable describes a data component's position, size, and type in a buffer plane.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.PlaneLayoutComponentType;

@VintfStability
parcelable PlaneLayoutComponent {
  /**
   * @brief The bit offset of this component.
   */
  int offset_in_bits;
  /**
   * @brief The size of this component in bits.
   */
  int size_in_bits;
  /**
   * @brief The type or semantic meaning of this component.
   */
  PlaneLayoutComponentType type;
}
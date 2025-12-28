/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file ISnapAllocExt.aidl
 * @brief Interface for Snap Allocator Extensions.
 *
 * This interface provides methods for retrieving allocation constraints and format information.
 */

package vendor.qti.hardware.display.snapallocext;

import vendor.qti.hardware.display.snapallocext.ConstraintInfoByType;
import vendor.qti.hardware.display.snapallocext.PixelFormatData;
@VintfStability
interface ISnapAllocExt {
  /**
   * @brief Retrieves a list of constraint information grouped by type.
   *
   * This method returns a collection of allocation constraints, categorized
   * by their intended use (e.g., CPU, display, video).
   * @return A list of ConstraintInfoByType objects, each containing a type
   *         and associated buffer constraints.
   */
  List<ConstraintInfoByType> getConstraintInfo();
  /**
   * @brief Retrieves a list of detailed pixel format data.
   *
   * This method returns a collection of pixel format descriptions,
   * associating each pixel format with its structural properties like
   * bits per pixel and plane layouts.
   * @return A list of PixelFormatData objects, each describing a pixel format
   *         and its corresponding layout details.
   */
  List<PixelFormatData> getFormatInfo();
}
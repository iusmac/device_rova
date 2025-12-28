/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PlaneLayoutComponentType.aidl
 * @brief Enum defining the types of components within a plane's layout.
 *
 * This enum identifies the semantic meaning of data components (e.g., color channels,
 * luma, chroma, alpha) within a buffer plane.
 */

package vendor.qti.hardware.display.snapallocext;

@Backing(type="long") @VintfStability
enum PlaneLayoutComponentType {
  /**
   * @brief Luma component (Y).
   */
  PLANE_LAYOUT_COMPONENT_TYPE_Y = 1 << 0,
  /**
   * @brief Chroma blue component (Cb/U).
   */
  PLANE_LAYOUT_COMPONENT_TYPE_CB = 1 << 1,
  /**
   * @brief Chroma red component (Cr/V).
   */
  PLANE_LAYOUT_COMPONENT_TYPE_CR = 1 << 2,
  /**
   * @brief Red color component.
   */
  PLANE_LAYOUT_COMPONENT_TYPE_R = 1 << 10,
  /**
   * @brief Green color component.
   */
  PLANE_LAYOUT_COMPONENT_TYPE_G = 1 << 11,
  /**
   * @brief Blue color component.
   */
  PLANE_LAYOUT_COMPONENT_TYPE_B = 1 << 12,
  /**
   * @brief Raw data component.
   */
  PLANE_LAYOUT_COMPONENT_TYPE_RAW = 1 << 20,
  /**
   * @brief Blob data component (unstructured data).
   */
  PLANE_LAYOUT_COMPONENT_TYPE_BLOB = 1 << 29,
  /**
   * @brief Alpha component.
   */
  PLANE_LAYOUT_COMPONENT_TYPE_A = 1 << 30,
  /**
   * @brief Meta data component.
   */
  PLANE_LAYOUT_COMPONENT_TYPE_META = 1 << 31,
}
/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file PixelFormat.aidl
 * @brief Enum defining various pixel formats supported for graphic buffers.
 *
 * This enum lists different pixel organizations and color spaces, including
 * RGB, YUV, raw, and compressed formats, used for image and video data.
 */

package vendor.qti.hardware.display.snapallocext;

@Backing(type="int") @VintfStability
enum PixelFormat {
  /**
   * @brief Represents an unspecified or default pixel format.
   */
  QTI_PIXEL_FORMAT_UNSPECIFIED = 0,
  /**
   * RGBA_8888 format:
   * Contains 1 plane in the following order -
   * (A) RGBA plane
   *
   * <-------- RGB_Stride -------->
   * <------- Width ------->
   * R G B A R G B A R G B A . . . .  ^           ^
   * R G B A R G B A R G B A . . . .  |           |
   * R G B A R G B A R G B A . . . .  Height      |
   * R G B A R G B A R G B A . . . .  |       RGB_Scanlines
   * R G B A R G B A R G B A . . . .  |           |
   * R G B A R G B A R G B A . . . .  |           |
   * R G B A R G B A R G B A . . . .  |           |
   * R G B A R G B A R G B A . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   */
  QTI_RGBA_8888 = 0x1,
  /**
   * RGBX_8888 format:
   * Contains 1 plane in the following order -
   * (A) RGBX plane, where X is an unused component
   *
   * <-------- RGB_Stride -------->
   * <------- Width ------->
   * R G B X R G B X R G B X . . . .  ^           ^
   * R G B X R G B X R G B X . . . .  |           |
   * R G B X R G B X R G B X . . . .  Height      |
   * R G B X R G B X R G B X . . . .  |       RGB_Scanlines
   * R G B X R G B X R G B X . . . .  |           |
   * R G B X R G B X R G B X . . . .  |           |
   * R G B X R G B X R G B X . . . .  |           |
   * R G B X R G B X R G B X . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   */
  QTI_RGBX_8888 = 0x2,
  /**
   * RGB_888 format:
   * Contains 1 plane in the following order -
   * (A) RGB plane
   *
   * <-------- RGB_Stride -------->
   * <------- Width ------->
   * R G B R G B R G B R G B . . . .  ^           ^
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  Height      |
   * R G B R G B R G B R G B . . . .  |       RGB_Scanlines
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   */
  QTI_RGB_888 = 0x3,
  /**
   * RGB_565 format:
   * Contains 1 plane in the following order -
   * (A) RGB plane
   *
   * <-------- RGB_Stride -------->
   * <------- Width ------->
   * R G B R G B R G B R G B . . . .  ^           ^
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  Height      |
   * R G B R G B R G B R G B . . . .  |       RGB_Scanlines
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  |           |
   * R G B R G B R G B R G B . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   */
  QTI_RGB_565 = 0x4,
  /**
   * BGRA_8888 format:
   * Contains 1 plane in the following order -
   * (A) BGRA plane
   *
   * <-------- RGB_Stride -------->
   * <------- Width ------->
   * B G R A B G R A B G R A . . . .  ^           ^
   * B G R A B G R A B G R A . . . .  |           |
   * B G R A B G R A B G R A . . . .  Height      |
   * B G R A B G R A B G R A . . . .  |       RGB_Scanlines
   * B G R A B G R A B G R A . . . .  |           |
   * B G R A B G R A B G R A . . . .  |           |
   * B G R A B G R A B G R A . . . .  |           |
   * B G R A B G R A B G R A . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   */
  QTI_BGRA_8888 = 0x5,
  /**
   * @brief YCbCr 4:2:2 interleaved format (NV16).
   */
  QTI_YCBCR_422_SP = 0x10, // NV16
  /**
   * YCrCb_420_SP format:
   * YUV 4:2:0 image with a plane of 8 bit Y samples followed
   * by an interleaved V/U plane containing 8 bit 2x2 subsampled
   * colour difference samples.
   *
   * <-------- Y/UV_Stride -------->
   * <------- Width ------->
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  ^           ^
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  Height      |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |          Y_Scanlines
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   * V U V U V U V U V U V U . . . .  ^
   * V U V U V U V U V U V U . . . .  |
   * V U V U V U V U V U V U . . . .  |
   * V U V U V U V U V U V U . . . .  UV_Scanlines
   * . . . . . . . . . . . . . . . .  |
   * . . . . . . . . . . . . . . . .  V
   * . . . . . . . . . . . . . . . .  --> Padding & Buffer size alignment
   *
   */
  QTI_YCrCb_420_SP = 0x11, // NV21
  QTI_YCBCR_422_I = 0x14, // YUY2
  QTI_RGBA_FP16 = 0x16,
  QTI_RAW16 = 0x20,
  QTI_BLOB = 0x21,
  QTI_IMPLEMENTATION_DEFINED = 0x22,
  QTI_YCBCR_420_888 = 0x23,
  QTI_RAW_OPAQUE = 0x24,
  QTI_RAW10 = 0x25,
  QTI_RAW12 = 0x26,
  QTI_RAW14 = 0x144,
  QTI_RGBA_1010102 = 0x2B,
  QTI_Y8 = 0x20203859,
  QTI_Y16 = 0x20363159,
  QTI_YV12 = 0x32315659, // YCrCb 4:2:0 Planar
  QTI_DEPTH_16 = 0x30,
  QTI_DEPTH_24 = 0x31,
  QTI_DEPTH_24_STENCIL_8 = 0x32,
  QTI_DEPTH_32F = 0x33,
  QTI_DEPTH_32F_STENCIL_8 = 0x34,
  QTI_STENCIL_8 = 0x35,
  /**
   * YCBCR_P010 format:
   * YUV 4:2:0 image with a plane of 10 bit Y samples followed
   * by an interleaved U/V plane containing 10 bit 2x2 subsampled
   * colour difference samples.
   *
   * <-------- Y/UV_Stride -------->
   * <------- Width ------->
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  ^           ^
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  Height      |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |          Y_Scanlines
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   * U V U V U V U V U V U V . . . .  ^
   * U V U V U V U V U V U V . . . .  |
   * U V U V U V U V U V U V . . . .  |
   * U V U V U V U V U V U V . . . .  UV_Scanlines
   * . . . . . . . . . . . . . . . .  |
   * . . . . . . . . . . . . . . . .  V
   * . . . . . . . . . . . . . . . .  --> Buffer size alignment
   *
   */
  QTI_YCBCR_P010 = 0x36,
  QTI_HSV_888 = 0x37,
  /* R8 format:
   * Contains 1 plane in the following order -
   * (A) R plane
   *
   * <-------- RGB_Stride -------->
   * <------- Width ------->
   * R R R R R R R R R R R R . . . .  ^           ^
   * R R R R R R R R R R R R . . . .  |           |
   * R R R R R R R R R R R R . . . .  Height      |
   * R R R R R R R R R R R R . . . .  |       RGB_Scanlines
   * R R R R R R R R R R R R . . . .  |           |
   * R R R R R R R R R R R R . . . .  |           |
   * R R R R R R R R R R R R . . . .  |           |
   * R R R R R R R R R R R R . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   *
   */
  QTI_R_8 = 0x38,
  QTI_RGBA_5551 = 0x6,
  QTI_RGBA_4444 = 0x7,
  /**
   * YCbCr_420_SP format:
   * YUV 4:2:0 image with a plane of 8 bit Y samples followed
   * by an interleaved U/V plane containing 8 bit 2x2 subsampled
   * colour difference samples.
   *
   * <-------- Y/UV_Stride -------->
   * <------- Width ------->
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  ^           ^
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  Height      |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |          Y_Scanlines
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  |           |
   * Y Y Y Y Y Y Y Y Y Y Y Y . . . .  V           |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              |
   * . . . . . . . . . . . . . . . .              V
   * U V U V U V U V U V U V . . . .  ^
   * U V U V U V U V U V U V . . . .  |
   * U V U V U V U V U V U V . . . .  |
   * U V U V U V U V U V U V . . . .  UV_Scanlines
   * . . . . . . . . . . . . . . . .  |
   * . . . . . . . . . . . . . . . .  V
   * . . . . . . . . . . . . . . . .  --> Padding & Buffer size alignment
   *
   */
  QTI_YCbCr_420_SP = 0x109,
  QTI_YCrCb_422_SP = 0x10B,
  QTI_RG_88 = 0x10E,
  QTI_YCbCr_444_SP = 0x10F,
  QTI_YCrCb_444_SP = 0x110,
  QTI_YCrCb_422_I = 0x111,
  QTI_BGRX_8888 = 0x112,
  QTI_NV21_ZSL = 0x113,
  QTI_YCrCb_420_SP_VENUS = 0x114,
  QTI_BGR_565 = 0x115,
  QTI_RAW8 = 0x123,
  // 10 bit
  QTI_ARGB_2101010 = 0x117,
  QTI_RGBX_1010102 = 0x118,
  QTI_XRGB_2101010 = 0x119,
  QTI_BGRA_1010102 = 0x11A,
  QTI_ABGR_2101010 = 0x11B,
  QTI_BGRX_1010102 = 0x11C,
  QTI_XBGR_2101010 = 0x11D,
  QTI_TP10 = 0x7FA30C09,
  QTI_YCBCR_P210 = 0x3c,
  QTI_CbYCrY_422_I = 0x120,
  QTI_BGR_888 = 0x121,
  // Camera utils format
  QTI_MULTIPLANAR_FLEX = 0x127,
  // Khronos ASTC formats
  QTI_COMPRESSED_RGBA_ASTC_4x4_KHR = 0x93B0,
  QTI_COMPRESSED_RGBA_ASTC_5x4_KHR = 0x93B1,
  QTI_COMPRESSED_RGBA_ASTC_5x5_KHR = 0x93B2,
  QTI_COMPRESSED_RGBA_ASTC_6x5_KHR = 0x93B3,
  QTI_COMPRESSED_RGBA_ASTC_6x6_KHR = 0x93B4,
  QTI_COMPRESSED_RGBA_ASTC_8x5_KHR = 0x93B5,
  QTI_COMPRESSED_RGBA_ASTC_8x6_KHR = 0x93B6,
  QTI_COMPRESSED_RGBA_ASTC_8x8_KHR = 0x93B7,
  QTI_COMPRESSED_RGBA_ASTC_10x5_KHR = 0x93B8,
  QTI_COMPRESSED_RGBA_ASTC_10x6_KHR = 0x93B9,
  QTI_COMPRESSED_RGBA_ASTC_10x8_KHR = 0x93BA,
  QTI_COMPRESSED_RGBA_ASTC_10x10_KHR = 0x93BB,
  QTI_COMPRESSED_RGBA_ASTC_12x10_KHR = 0x93BC,
  QTI_COMPRESSED_RGBA_ASTC_12x12_KHR = 0x93BD,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = 0x93D0,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = 0x93D1,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = 0x93D2,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = 0x93D3,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = 0x93D4,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = 0x93D5,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = 0x93D6,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = 0x93D7,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = 0x93D8,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = 0x93D9,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = 0x93DA,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = 0x93DB,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = 0x93DC,
  QTI_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = 0x93DD,
  /* Legacy formats/currently unsupported
   * R_16_UINT
   * RG_1616_UINT
   * RGBA_10101010
   * RGB888_UBWC_FSC
   * RGB101010_UBWC_FSC
   * YCbCr_422_I_10BIT
   * YCbCr_422_I_10BIT_COMPRESSED
   * YCbCr_420_SP_4R_UBWC
   **/
  /* Explicit UBWC formats are removed - set UBWC flag with corresponding linear
   * format YCbCr_420_SP_VENUS_UBWC YCbCr_420_P010_UBWC YCbCr_420_TP10_UBWC
   **/
  /* To be deprecated when ExtenableTypes are plumbed */
  QTI_NV12_ENCODEABLE = 0x102,
  QTI_NV21_ENCODEABLE = 0x7FA30C00,
  QTI_YCbCr_420_SP_VENUS = 0x7FA30C04,
  QTI_YCbCr_420_SP_TILED = 0x7FA30C03,
  QTI_YCrCb_420_SP_ADRENO = 0x7FA30C01,
  QTI_YCbCr_420_P010_VENUS = 0x7FA30C0A,
  QTI_NV12_HEIF = 0x116,
  QTI_NV12_LINEAR_FLEX = 0x125,
  QTI_NV12_UBWC_FLEX = 0x126,
  QTI_NV12_UBWC_FLEX_2_BATCH = 0x128,
  QTI_NV12_UBWC_FLEX_4_BATCH = 0x129,
  QTI_NV12_UBWC_FLEX_8_BATCH = 0x130,
  /* Camera MipMap Formats */
  QTI_NV12_UBWC_MIPMAP = 0x223,
  QTI_NV12_MIPMAP = 0x224,
  QTI_TP10_UBWC_MIPMAP = 0x225,
  QTI_P010_MIPMAP = 0x226,
  QTI_YCBCR_P010_HEIF = 0x151, // YCBCR_P010_512
  QTI_YCBCR_P010_1024 = 0x152,
  QTI_NV12_1024 = 0x153,
}
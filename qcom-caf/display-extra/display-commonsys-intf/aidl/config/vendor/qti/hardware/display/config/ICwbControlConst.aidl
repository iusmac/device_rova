/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

/**
 * @file ICwbControlConst.aidl
 * @brief Defines the different types of CWB flags to control CWB output and its process.
 *
 * This interface defines different CWB process controlling flag constants, which all can be used
 * together by using bitwise OR (|) operator, but enums with prefix OUTPUT, PRIORITY and DOWNSCALE
 * cannot be OR'd with itself.
 * e.g. With prefix OUTPUT, PRIORITY and DOWNSCALE flag can't repeat with OR operator.
 */
package vendor.qti.hardware.display.config;

@VintfStability
/**
 * CWB control parameter/flag is a 32 bit integer carrying various configuration to control
 * processing of CWB output and handling of CWB request. Some constants are enumerated for
 * helping in configuration using bitwise OR(|) operation. Following detail explains all
 * bit level, its group level information for CWB control configuration.
 *                             View of 32 bit integer CWB control flag
 *                             =======================================
 * 31     24     16      15      14          12       8      6       5          4      2        0
 * +------+------+-------+-------+-----------+--------+------+-------+----------+------+--------+
 * |H_DNSC|W_DNSC|VCENTER|HCENTER|DNSC_CONFIG|PRIORITY|UNUSED|REFRESH|PUasCwbROI|UNUSED|TAPPOINT|
 * +------+------+-------+-------+-----------+--------+------+-------+----------+------+--------+
 * |8 bits|8 bits| 1 bit | 1 bit | 2 bits    | 4 bits |2 bits| 1 bit |  1 bit   |2 bits| 2 bits |
 * +------+------+-------+-------+-----------+--------+------+-------+----------+------+--------+
 * Where,
 * TAPPOINT     =>  CWB Output tap-points with possible one of the values:
 *                  1. 0 => OUTPUT_AS_LM_DUMP     => Layer Mixer output Tap point.
 *                  2. 1 => OUTPUT_AS_DSPP_DUMP   => DSPP post processed output Tap point.
 *                  3. 2 => OUTPUT_AS_DEMURA_DUMP => Demura final output Tap point.
 * PUasCwbROI   => PU_AS_CWB_ROI => It's a boolean flag to enable and disable capturing of updated
 *                                  region only while partial update. Possible values:
 *                                  1. 0 => Disabled PU as CWB ROI.
 *                                  2. 1 => Enabled PU as CWB ROI.
 * REFRESH      => FORCED_REFRESH_ON_REQUEST => It's a boolean flag to enable and disable
 *                                              triggering forced refresh for current request.
 *                                              Possible values:
 *                                              1. 0 => Disabled forced refresh.
 *                                              2. 1 => Enabled forced refresh.
 * PRIORITY     => It's a client configurable priority among multiple external client requests on
 *                 same display, which decides the order of processing external cwb requests.
 *                 It supports, priority from 1 to 15, but where 1 is top priority and 15 is lowest
 *                 priority. But top priority 1 is only for internal use, so external client can
 *                 only out of 2 to 15. Priority configuration for integer flag can be calculated
 *                 with the help of following enumerations:
 *                 1. PRIORITY_SCALE => It helps to figure out any priority configuration value to
 *                                      to fit in CWB control integer flag/parameter.
 *                 2. PRIORITY_AS_HIGH => High priority configurable to CWB control flag.
 *                 3. PRIORITY_AS_MEDIUM => Medium priority configurable to CWB control flag.
 *                 4. PRIORITY_AS_LOW => Low priority configurable to CWB control flag.
 *                 Now, to calculate any priority to update in CWB control integer flag by using
 *                 just bitwise operator OR(|). Following is the way using above enumerations.
 *                 i.   To set Nth priority, where 2 < N < 15: control_flag |=  N * PRIORITY_SCALE;
 *                 ii.  To set high priority: control_flag |=  PRIORITY_AS_HIGH;
 *                 iii. To set few(N) steps lower proirity than High relative to PRIORITY_AS_HIGH:
 *                      control_flag |=  PRIORITY_AS_HIGH + N * PRIORITY_SCALE;
 *                 iv.  To set Medium priority: control_flag |=  PRIORITY_AS_MEDIUM;
 *                 v.   To set few(N) steps low priority relative to PRIORITY_AS_MEDIUM:
 *                      control_flag |=  PRIORITY_AS_MEDIUM + N * PRIORITY_SCALE;
 *                 vi.  As lowest priority is default priority, so, to keep lowest priority, no
 *                      need to set any priority configuration to control flag. As priority-0 is
 *                      treated as lowest priority equivalent to priority-15. Alternatively, it can
 *                      be set as: control_flag |=  PRIORITY_AS_LOW;
 *                 vii. To set few(N) steps high priority than Low relative to PRIORITY_AS_LOW:
 *                      control_flag |=  PRIORITY_AS_LOW - N * PRIORITY_SCALE;
 * DNSC_CONFIG  => It's a CWB downscale configuration mode, which decides that how the 8 bit values
 *                 H_DNSC(HEIGHT_DOWNSCALER) and W_DNSC(WIDTH_DOWNSCALER) can be interpreted for
 *                 downscale output, like either these can be downscale divisor for width and
 *                 height, or these can be treated as downscaled percentage width and height.
 *                 Following modes are supported, but few is exposed for external use and few are
 *                 just for internal testing and use:
 *                 1. 0 => Default Mode => In this mode, it prefers downscale configuration from
 *                                         downscale input rectangle. If this mode is needed,
 *                                         then no need to set any mode as it is default mode.
 *                 2. 1 => Rational downscale factor => This mode is neither exposed for external
 *                                                      use and nor enumrated, as it is just for
 *                                                      internal testing and use.
 *                 3. 2 => DOWNSCALE_CONFIG_BY_PERCENT => In this mode, 8 bit values H_DNSC and
 *                                                        W_DNSC shall be treated as downscaled
 *                                                        percentage width and height respectively.
 *                 3. 3 => DOWNSCALE_CONFIG_BY_DIVISOR => In this mode, 8 bit values H_DNSC and
 *                                                        W_DNSC shall be treated as downscale
 *                                                        divisors for percentage width and height
 *                                                        respectively.
 * HCENTER      => HCENTER_ALIGN_DOWNSCALE_IMAGE => It's client configurable horizontal offsetting
 *                                                  way for downscaled output. It is just a boolean
 *                                                  value to enable or disable the horizonal center
 *                                                  alignment of output image with in the buffer.
 *                                                  Possible values:
 *                                                  1. 0 => Disabled horizonal center alignment.
 *                                                  2. 1 => Enabled horizonal center alignment.
 * VCENTER      => VCENTER_ALIGN_DOWNSCALE_IMAGE => It's client configurable vertical offsetting
 *                                                  way for downscaled output. It is just a boolean
 *                                                  value to enable or disable the vertical center
 *                                                  alignment of output image with in the buffer.
 *                                                  Possible values:
 *                                                  1. 0 => Disabled vertical center alignment.
 *                                                  2. 1 => Enabled vertical center alignment.
 * W_DNSC       => WIDTH_DOWNSCALER => It's a 8 bit value of CWB downscale factor, which decides
 *                                     downscaled output width. How this value can be used that
 *                                     shall be decided on the basis of of downscale configuration
 *                                     mode as defined by DNSC_CONFIG, which is already explained
 *                                     above.
 * H_DNSC       => HEIGHT_DOWNSCALER => It's a 8 bit value of CWB downscale factor, which decides
 *                                      downscaled output height. How this value can be used that
 *                                      shall be decided on the basis of of downscale configuration
 *                                      mode as defined by DNSC_CONFIG, which is already explained
 *                                      above.
 *
 * e.g. Multiple control flags can be configurated as follows:
 * control_flag = OUTPUT_AS_DSPP_DUMP | FORCED_REFRESH_ON_REQUEST; // Refresh with DSPP tap point
 * control_flag |= PRIORITY_AS_HIGH + 4 * PRIORITY_SCALE; // High(2)+4 = 6 priority setting
 * control_flag |= DOWNSCALE_CONFIG_BY_DIVISOR; // Divide width and height with downscale factors.
 * control_flag |= (2*WIDTH_DOWNSCALER) | (3*HEIGHT_DOWNSCALER); // Output = (width/2, height/3).
 * control_flag |= HCENTER_ALIGN_DOWNSCALE_IMAGE |  VCENTER_ALIGN_DOWNSCALE_IMAGE; // Centered.
 * final destination recatangle with respect to buffer(buf_width X buf_height) =
 * ((buf_width-width/2)/2, (buf_height-height/3)/2, buf_width/2 + width/2, buf_height/2 + height/6)
 * where, width X height is the display resolution.
 */
/**
 * @interface ICwbControlConst
 */
interface ICwbControlConst {

    /**
     * Following three constants with prefix OUTPUT are CWB Tap Point values. Out of these, only
     * one can Tap point contant can be used at a time for one CWB request.
     */
    /**
     * @brief Layer Mixer/blended output
     */
    const int OUTPUT_AS_LM_DUMP = 0;  //!< Tap Point 2bit LSB value of CWB control flag
    /**
     * @brief Destination output with correction to layer mixer output as per panel resolution
     */
    const int OUTPUT_AS_DSPP_DUMP = 1;  //!< Tap Point 2bit LSB value of CWB control flag
    /**
     * @brief Final destination output with more corrections for pixels and destination masks.
     */
    const int OUTPUT_AS_DEMURA_DUMP = 2;  //!< Tap Point 2bit LSB value of CWB control flag
    /**
     * @brief Capture just updated/modified region on screen, if output ROI is not given.
     */
    const int PU_AS_CWB_ROI = (1 << 4);  //!< Set 4th bit of CWB control flag as true
    /**
     * @brief Trigger forced refresh on CWB request
     */
    const int FORCED_REFRESH_ON_REQUEST = (1 << 5);  //!< Set 5th bit of CWB control flag as true
    /**
     * Following four constants with prefix PRIORITY are CWB external request priorities of four
     * bit values from (1 to 15) located from 8th bit position onwards in Cwb Control flag.
     * Out of these, only one priority can be used in any CWB request.
     * Here, only 3 common priority level constants are defined with margin as High(2), Medium(8)
     * and Low(15), but more priorities can be used with using PRIORITY_SCALE within priority margin
     * interval with respect to defined priority constants (2, 8 and 15) as per following comment.
     * So, we can consider two group of priorities one from 2 to 8 and another from 8 to15.
     * To set priority level N, where 2 < N < 8 : cflag = PRIORITY_AS_HIGH + N*PRIORITY_SCALE
     * Or where 8 < N < 15 : cflag = PRIORITY_AS_MEDIUM + N*PRIORITY_SCALE.
     */
    /**
     * @brief This is the interval between consecutive priority levels, which helps to reduce the
     * priority level by addition this flag to base priority like to reduce 3 level with respect to
     * medium priority, need to use PRIORITY_AS_MEDIUM + 3*PRIORITY_SCALE.
     * To increase the priority level subtract PRIORITY_SCALE with multiple of levels from base.
     * e.g. cflag = PRIORITY_AS_LOW - 5*PRIORITY_SCALE; // Increased priority
     */
    const int PRIORITY_SCALE = (1 << 8);  //!< Define 4bit priority scale after 8th bit onwards
    /**
     * @brief External request priority order control as top priority, but internal feature request
     * still be at higher priority than external request. This order is applicable between multiple
     * external clients
     */
    const int PRIORITY_AS_HIGH = (2 << 8);  //!< Value 2 as priority high at 8th bit onwards
    /**
     * @brief External request priority order control as medium priority.
     */
    const int PRIORITY_AS_MEDIUM = (8 << 8);  //!< Value 8 as priority medium at 8th bit onwards
    /**
     * @brief External request priority order control as low priority.
     */
    const int PRIORITY_AS_LOW = (15 << 8);  //!< Value 15 as priority low at 8th bit onwards
    /**
     * @brief This is downscale configuration mode to control downscaling by percentage config.
     */
    const int DOWNSCALE_CONFIG_BY_PERCENT = (2 << 12);  //!< Value 2 as config at 12th bit onwards
    /**
     * @brief This is downscale configuration mode to control downscaling by a divisor.
     */
    const int DOWNSCALE_CONFIG_BY_DIVISOR = (3 << 12);  //!< Value 3 as config at 12th bit onwards
    /**
     * @brief To align horizontally centered output downscaled image to buffer.
     */
    const int HCENTER_ALIGN_DOWNSCALE_IMAGE = (1 << 14);  //!< Set 14th bit of control flag as true
    /**
     * @brief To align vertically centered output downscaled image to buffer.
     */
    const int VCENTER_ALIGN_DOWNSCALE_IMAGE = (1 << 15);  //!< Set 15th bit of control flag as true
    /**
     * @brief This is the downscale configuration coefficient for width, which need to be multiply
     * with downscale divisor or percentage for width as per downscale config.
     * e.g. if width need to be downscale as width/2. So, divisor is 2, flag value would be :
     * cflag = DOWNSCALE_CONFIG_BY_DIVISOR | (2 * WIDTH_DOWNSCALER);
     * e.g. if width need to be downscale to 30% of width. So, percent is 30, flag value would be:
     * cflag = DOWNSCALE_CONFIG_BY_PERCENT | (30 * WIDTH_DOWNSCALER);
     */
    const int WIDTH_DOWNSCALER = (1 << 16);  //!< Set 8bit value at 16th bit onwards or in 3rd byte
    /**
     * @brief This is the downscale configuration coefficient for height, which need to be multiply
     * with downscale divisor or percentage for height as per downscale config.
     * e.g. if height need to be downscale as height/2. So, divisor is 2, flag value would be :
     * cflag = DOWNSCALE_CONFIG_BY_DIVISOR | (2 * HEIGHT_DOWNSCALER);
     * e.g. if width need to be downscale to 30% of width. So, percent is 30, flag value would be:
     * cflag = DOWNSCALE_CONFIG_BY_PERCENT | (30 * HEIGHT_DOWNSCALER);
     */
    const int HEIGHT_DOWNSCALER = (1 << 24);  //!< Set 8bit value at 24th bit onwards or in 4th byte
}

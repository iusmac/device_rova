/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package vendor.qti.hardware.display.composer3;
import android.hardware.graphics.common.Rect;

@VintfStability
parcelable QtiPrivacyRegion {
    float cornerRadius;
    Rect rect;
}

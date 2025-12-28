/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package vendor.qti.hardware.display.composer3;
import vendor.qti.hardware.display.composer3.QtiLayerType;
import vendor.qti.hardware.display.composer3.QtiLayerFlags;
import vendor.qti.hardware.display.composer3.QtiPrivacyRegion;
import vendor.qti.hardware.display.composer3.QtiCornerRadius;

@VintfStability
parcelable QtiLayerCommand {
    /**
     * The layer which this commands refers to.
     * @see IComposer.createLayer
     */
    long layer;

    /**
    * Type of layer - this is a hint to HWC to handle the layer appropriately.
    */
    QtiLayerType qtiLayerType;

    /**
    * Flags attached to the layer
    */
    QtiLayerFlags qtiLayerFlags;

    /**
     * Privacy regions of the layer
     */
    @nullable QtiPrivacyRegion[] qtiPrivacyRegions;

    /**
     * Corner radius of the layer
     */
    @nullable QtiCornerRadius qtiCornerRadius;
}

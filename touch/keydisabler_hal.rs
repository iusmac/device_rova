/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

use binder::{Interface, Result as BinderResult, StatusCode};
use std::fs;

use vendor_lineage_touch::aidl::vendor::lineage::touch::{
    IKeyDisabler::IKeyDisabler,
};

const CONTROL_PATH: &str = "/proc/sys/dev/xiaomi_msm8937_touchscreen/disable_keys";

pub struct KeyDisablerHal;

impl Interface for KeyDisablerHal {}

impl IKeyDisabler for KeyDisablerHal {
    fn getEnabled(&self) -> BinderResult<bool> {
        fs::read_to_string(CONTROL_PATH)
            .map(|mut s| {
                s.pop();
                s == "1"
            })
            .map_err(|err| {
                log::error!("Failed to read from {}: {}", CONTROL_PATH, err);
                StatusCode::UNKNOWN_ERROR.into()
            })
    }

    fn setEnabled(&self, enabled: bool) -> BinderResult<()> {
        fs::write(CONTROL_PATH, if enabled { "1" } else { "0" })
            .map_err(|err| {
                log::error!("Failed to write to {}: {}", CONTROL_PATH, err);
                StatusCode::UNKNOWN_ERROR.into()
            })
    }
}

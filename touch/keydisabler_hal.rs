/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

use binder::{Interface, Result as BinderResult};
use std::fs;

use vendor_lineage_touch::aidl::vendor::lineage::touch::{
    IKeyDisabler::IKeyDisabler,
};

const CONTROL_PATH: &str = "/proc/sys/dev/xiaomi_msm8937_touchscreen/disable_keys";

pub struct KeyDisablerHal {
    has_key_disabler: bool,
}

impl Default for KeyDisablerHal {
    fn default() -> Self {
        Self {
            has_key_disabler: fs::exists(CONTROL_PATH).unwrap_or(false),
        }
    }
}

impl Interface for KeyDisablerHal {}

impl IKeyDisabler for KeyDisablerHal {
    fn getEnabled(&self) -> BinderResult<bool> {
        if !self.has_key_disabler {
            return Ok(false);
        }

        match fs::read_to_string(CONTROL_PATH) {
            Ok(mut content) => {
                content.pop();
                Ok(content == "1")
            }
            Err(err) => {
                log::error!("Failed to read from {}: {}", CONTROL_PATH, err);
                Ok(false)
            }
        }
    }

    fn setEnabled(&self, enabled : bool) -> BinderResult<()> {
        if !self.has_key_disabler {
            return Ok(());
        }

        match fs::write(CONTROL_PATH, if enabled { b"1" } else { b"0" }) {
            Ok(_) => {
                Ok(())
            }
            Err(err) => {
                log::error!("Failed to write to {}: {}", CONTROL_PATH, err);
                Ok(())
            }
        }
    }
}

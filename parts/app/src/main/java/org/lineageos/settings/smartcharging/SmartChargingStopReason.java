/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.lineageos.settings.smartcharging;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
    SmartChargingStopReason.OVERHEATED,
    SmartChargingStopReason.OVERCHARGED,
    SmartChargingStopReason.NOTIFICATION,
    SmartChargingStopReason.UNKNOWN
})
public @interface SmartChargingStopReason {
    int UNKNOWN = 0;
    int OVERHEATED = 1;
    int OVERCHARGED = 2;
    int NOTIFICATION = 3;
}

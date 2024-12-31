/*
   Copyright (c) 2015, The Linux Foundation. All rights reserved.
   Copyright (C) 2016 The CyanogenMod Project.
   Copyright (C) 2019 The LineageOS Project.
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.
   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
   ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
   OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
   IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <android-base/file.h>
#include <cstdlib>
#include <fstab/fstab.h>
#include <fstream>
#include <string.h>
#include <sys/sysinfo.h>
#include <unistd.h>

#include <android-base/properties.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>
#include <fstream>

#include "vendor_init.h"
#include "property_service.h"

#include <string>
#include <vector>

using android::base::GetProperty;

std::vector<std::string> ro_props_default_source_order = {
    "odm.",
    "product.",
    "system.",
    "system_ext.",
    "vendor.",
    "",
};

void property_override(char const prop[], char const value[], bool add)
{
    auto pi = (prop_info *) __system_property_find(prop);

    if (pi != nullptr) {
        __system_property_update(pi, value, strlen(value));
    } else if (add) {
        __system_property_add(prop, strlen(prop), value, strlen(value));
    }
}

void set_ro_build_prop(const std::string &prop, const std::string &value, bool product) {
    std::string prop_name;

    for (const auto &source : ro_props_default_source_order) {
        if (product)
            prop_name = "ro.product." + source + prop;
        else
            prop_name = "ro." + source + "build." + prop;

        property_override(prop_name.c_str(), value.c_str(), true);
    }
}

typedef struct variant_info {
    std::string brand;
    std::string device;
    std::string marketname;
    std::string model;
    std::string build_description;
    std::string build_fingerprint;
} variant_info_t;

void search_variant(const std::vector<variant_info_t> variants);
void set_variant_props(const variant_info_t variant);

void property_override(char const prop[], char const value[], bool add = true);
void set_dalvik_heap_size();
void set_avoid_gfxaccel_config();
#ifdef FORCE_ADB_ROOT
void force_adb_root();
#endif
void set_ro_build_prop(const std::string &prop, const std::string &value, bool product = false);
void set_bootloader_prop(void);

static const variant_info_t rolex_info = {
    .brand = "Xiaomi",
    .device = "rolex",
    .marketname = "",
    .model = "Redmi 4A",
    .build_description = "rolex-user 7.1.2 N2G47H V10.2.3.0.NCCMIXM release-keys",
    .build_fingerprint = "Xiaomi/rolex/rolex:7.1.2/N2G47H/V10.2.3.0.NCCMIXM:user/release-keys",
};

static const variant_info_t riva_info = {
    .brand = "Xiaomi",
    .device = "riva",
    .marketname = "",
    .model = "Redmi 5A",
    .build_description = "riva-user 7.1.2 N2G47H V10.1.1.0.NCKMIFI release-keys",
    .build_fingerprint = "Xiaomi/riva/riva:7.1.2/N2G47H/V10.1.1.0.NCKMIFI:user/release-keys",
};

static void determine_device()
{
    std::string codename;

    android::base::ReadFileToString("/sys/xiaomi-msm8937-mach/codename", &codename, true);
    if (codename.empty())
        return;
    codename.pop_back();

    if (codename == "rolex") {
        set_variant_props(rolex_info);
        property_override("vendor.usb.product_string", "Xiaomi Redmi 4A");
        property_override("bluetooth.device.default_name", "Xiaomi Redmi 4A");
    } else if (codename == "riva") {
        set_variant_props(riva_info);
        property_override("vendor.usb.product_string", "Xiaomi Redmi 5A");
        property_override("bluetooth.device.default_name", "Xiaomi Redmi 5A");
    }
}

static void enable_gatekeeper_uid_offset() {
    std::string boot_device = *android::fs_mgr::GetBootDevices().begin();
    if (boot_device == "soc/7864900.sdhci") {
        property_override("ro.gsid.image_running", "1");
    }
}

void vendor_load_properties() {
    determine_device();
    enable_gatekeeper_uid_offset();
    set_bootloader_prop();
    set_dalvik_heap_size();
    set_avoid_gfxaccel_config();
#ifdef FORCE_ADB_ROOT
    force_adb_root();
#endif
}

void set_avoid_gfxaccel_config() {
    struct sysinfo sys;
    sysinfo(&sys);

    if (sys.totalram <= 3072ull * 1024 * 1024) {
        // Reduce memory footprint
        property_override("ro.config.avoid_gfx_accel", "true");
    }
}

#ifdef FORCE_ADB_ROOT
void force_adb_root() {
    property_override("ro.secure", "0");
    property_override("ro.adb.secure", "0");
    property_override("ro.debuggable", "1");
    property_override("persist.sys.usb.config", "adb");
}
#endif

void set_dalvik_heap_size()
{
    struct sysinfo sys;
    char const *heapstartsize;
    char const *heapgrowthlimit;
    char const *heapsize;
    char const *heapminfree;
    char const *heapmaxfree;
    char const *heaptargetutilization;

    sysinfo(&sys);

    if (sys.totalram > 5072ull * 1024 * 1024) {
        // from - phone-xhdpi-6144-dalvik-heap.mk
        heapstartsize = "16m";
        heapgrowthlimit = "256m";
        heapsize = "512m";
        heaptargetutilization = "0.5";
        heapminfree = "8m";
        heapmaxfree = "32m";
    } else if (sys.totalram > 3072ull * 1024 * 1024) {
        // from - phone-xhdpi-4096-dalvik-heap.mk
        heapstartsize = "8m";
        heapgrowthlimit = "192m";
        heapsize = "512m";
        heaptargetutilization = "0.6";
        heapminfree = "8m";
        heapmaxfree = "16m";
    } else if (sys.totalram > 1024ull * 1024 * 1024) {
        // from - phone-xhdpi-2048-dalvik-heap.mk
        heapstartsize = "8m";
        heapgrowthlimit = "192m";
        heapsize = "512m";
        heaptargetutilization = "0.75";
        heapminfree = "512k";
        heapmaxfree = "8m";
    } else {
        // from - phone-xhdpi-1024-dalvik-heap.mk
        heapstartsize = "8m";
        heapgrowthlimit = "96m";
        heapsize = "256m";
        heaptargetutilization = "0.75";
        heapminfree = "512k";
        heapmaxfree = "8m";
    }

    property_override("dalvik.vm.heapstartsize", heapstartsize);
    property_override("dalvik.vm.heapgrowthlimit", heapgrowthlimit);
    property_override("dalvik.vm.heapsize", heapsize);
    property_override("dalvik.vm.heaptargetutilization", heaptargetutilization);
    property_override("dalvik.vm.heapminfree", heapminfree);
    property_override("dalvik.vm.heapmaxfree", heapmaxfree);
}

void set_variant_props(const variant_info_t variant) {
    set_ro_build_prop("brand", variant.brand, true);
    set_ro_build_prop("device", variant.device, true);
    set_ro_build_prop("marketname", variant.marketname, true);
    set_ro_build_prop("model", variant.model, true);

    set_ro_build_prop("fingerprint", variant.build_fingerprint);
    property_override("ro.bootimage.build.fingerprint", variant.build_fingerprint.c_str());
    property_override("ro.build.description", variant.build_description.c_str());
}

void set_bootloader_prop(void) {
    std::string file = "/sys/devices/soc0/images";
    std::ifstream fp(file);
    if (!fp) {
        return;
    }

    std::string line;
    std::size_t found;
    while (std::getline(fp, line)) {
        // "  CRM:  00:BOOT.BF.3.3-00214"
        found = line.rfind("BOOT.");
        if (found != line.npos) {
            // "BOOT.BF.3.3-00214"
            property_override("ro.bootloader", line.substr(found).c_str());
            return;
        }
    }
}

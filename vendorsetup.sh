#!/usr/bin/env bash

DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"

# Remove the following intermediate buildinfo.prop file to trigger
# gen_from_buildinfo_sh rule in build/core/sysprop.mk. This will populate
# system/build.prop file with fresh infos when making "dirty" build.
rm -vf $OUT_DIR/target/product/$lunch_device/obj/PACKAGING/system_build_prop_intermediates/buildinfo.prop

# ************************************
# Fix Bluetooth calling on legacy SOCs
# ************************************
# git clone --depth=1 https://android.googlesource.com/platform/packages/modules/Bluetooth.git -b android-13.0.0_r13
patch_name='Do not send enhanced sco setup cmd - r13'
patch_dir=packages/modules/Bluetooth
cur_commit="$(git -C $patch_dir show -s --format=%s)" || exit $?

if [ "$cur_commit" != "$patch_name" ]; then
    echo 'Patching Bluetooth stack in "packages/modules/Bluetooth" ...'

    # Apply and commit patch
    git -C $patch_dir apply --verbose < <(cat <<'EOL'
diff --git a/system/main/shim/controller.cc b/system/main/shim/controller.cc
index ca80b52..9171ada 100644
--- a/system/main/shim/controller.cc
+++ b/system/main/shim/controller.cc
@@ -40,7 +40,6 @@ constexpr uint8_t kPhyLe1M = 0x01;
  * Interesting commands supported by controller
  */
 constexpr int kReadRemoteExtendedFeatures = 0x41c;
-constexpr int kEnhancedSetupSynchronousConnection = 0x428;
 constexpr int kEnhancedAcceptSynchronousConnection = 0x429;
 constexpr int kLeSetPrivacyMode = 0x204e;
 constexpr int kConfigureDataPath = 0x0c83;
@@ -220,9 +219,9 @@ FORWARD_IF_RUST(
 FORWARD_IF_RUST(supports_reading_remote_extended_features,
                 GetController()->IsSupported((bluetooth::hci::OpCode)
                                                  kReadRemoteExtendedFeatures))
-FORWARD_IF_RUST(supports_enhanced_setup_synchronous_connection,
-                GetController()->IsSupported((
-                    bluetooth::hci::OpCode)kEnhancedSetupSynchronousConnection))
+static bool supports_enhanced_setup_synchronous_connection(void) {
+    return false;
+}
 FORWARD_IF_RUST(
     supports_enhanced_accept_synchronous_connection,
     GetController()->IsSupported((bluetooth::hci::OpCode)
EOL
    ) &&
    git -C $patch_dir commit -am "$patch_name" || exit $?
else
    echo 'Skipping patching Bluetooth stack in "packages/modules/Bluetooth". Already applied.'
fi

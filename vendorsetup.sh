#!/usr/bin/env bash

DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"

# Remove the following Soong-generated build.prop intermediate file to trigger
# regeneration rule in build/soong/Android.bp. This will populate
# system/build.prop file with fresh infos when making "dirty" build.
rm -vf out/soong/.intermediates/build/soong/*-build.prop/android_common/build.prop

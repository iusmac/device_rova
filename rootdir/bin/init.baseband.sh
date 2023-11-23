#!/vendor/bin/sh

baseband_str=$(grep -m1 -az 'QC_IMAGE_VERSION_STRING=' /vendor/firmware_mnt/image/modem.b12 | cut -c 25-)

if [ ! -z $baseband_str ]; then
    setprop vendor.gsm.version.baseband $baseband_str
fi

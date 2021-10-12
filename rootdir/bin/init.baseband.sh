#!/vendor/bin/sh

baseband_str=$(strings /vendor/firmware_mnt/image/modem.b12 | grep "QC_IMAGE_VERSION_STRING=" | cut -c 25-)

if [ ! -z $baseband_str ]; then
    setprop gsm.version.baseband $baseband_str
fi

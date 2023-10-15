#!/vendor/bin/sh

baseband_str=$(grep -Eo 'MPSS.*' /sys/devices/soc0/images)

if [ ! -z $baseband_str ]; then
    setprop vendor.gsm.version.baseband $baseband_str
fi

#!/vendor/bin/sh

if [ -e /sys/class/leds/infrared/transmit ]; then
	setprop ro.vendor.xiaomi.device rolex
else
	setprop ro.vendor.xiaomi.device riva
fi

exit 0

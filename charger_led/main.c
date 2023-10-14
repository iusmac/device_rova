// #define DEBUG

#ifndef DEBUG
#include <cutils/klog.h>
#endif
#include <fcntl.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#if defined(DEBUG)
#define UPDATE_INTERVAL_SEC 1
#elif defined(__ANDROID_RECOVERY__)
#define UPDATE_INTERVAL_SEC 5
#else
#define UPDATE_INTERVAL_SEC 60
#endif

#define BAT_CAPACITY_PATH "/sys/class/power_supply/battery/capacity"
#define USB_CURRENT_MAX_PATH "/sys/class/power_supply/usb/current_max"
#define USB_ONLINE_PATH "/sys/class/power_supply/usb/online"

#define LED_PATH(led, file) "/sys/class/leds/" led "/" file

#ifdef DEBUG
#define LOG_TAG "charger_led: "
#define LOG_ERROR(...) fprintf(stderr, LOG_TAG __VA_ARGS__)
#define LOG_INFO(...) fprintf(stdout, LOG_TAG __VA_ARGS__)
#else
#define LOG_TAG "charger_led"
#define LOG_ERROR(x...) KLOG_ERROR(LOG_TAG, x)
#define LOG_INFO(x...) KLOG_INFO(LOG_TAG, x)
#endif

/* Definitions */
enum led_types {
    LED_UNKNOWN = 0,
    WHITE,
    RED,
    GREEN,
    BLUE,
    LED_STANDARD_TYPES_MAX,
    CYAN,    // blue+green
    PINK,    // blue+red
    YELLOW,  // green+red
    LED_ALL_TYPES_MAX
};

enum led_states { LED_STATE_OFF = 0, LED_STATE_BREATH, LED_STATE_BRIGHTNESS };

const char led_blink_paths[LED_STANDARD_TYPES_MAX][64] = {
        [WHITE] = LED_PATH("white", "blink"),
        [RED] = LED_PATH("red", "blink"),
        [GREEN] = LED_PATH("green", "blink"),
        [BLUE] = LED_PATH("blue", "blink"),
};

const char led_breath_paths[LED_STANDARD_TYPES_MAX][64] = {
        [WHITE] = LED_PATH("white", "breath"),
        [RED] = LED_PATH("red", "breath"),
        [GREEN] = LED_PATH("green", "breath"),
        [BLUE] = LED_PATH("blue", "breath"),
};

const char led_brightness_paths[LED_STANDARD_TYPES_MAX][64] = {
        [WHITE] = LED_PATH("white", "brightness"),
        [RED] = LED_PATH("red", "brightness"),
        [GREEN] = LED_PATH("green", "brightness"),
        [BLUE] = LED_PATH("blue", "brightness"),
};

const char led_max_brightness_paths[LED_STANDARD_TYPES_MAX][64] = {
        [WHITE] = LED_PATH("white", "max_brightness"),
        [RED] = LED_PATH("red", "max_brightness"),
        [GREEN] = LED_PATH("green", "max_brightness"),
        [BLUE] = LED_PATH("blue", "max_brightness"),
};

/* Variables */
// Settings
bool use_blink_node_for_breath = false;
float led_brightness_multiplier = 1.0;
// Charge state
int bat_capacity = 0;
int usb_current_max = 0;
#ifndef __ANDROID_RECOVERY__
int usb_online = 0;
#endif
// LED state
struct led {
    enum led_types id;
    enum led_states state;
} active_led = {LED_UNKNOWN, LED_STATE_OFF};

/* Helper functions */
bool file_is_writeable(const char* path) {
    int fd = open(path, O_WRONLY);

    if (fd < 0) {
        return false;
    } else {
        close(fd);
        return true;
    }
}

bool read_fd_to_int(int fd, int* val) {
    char buf[10];

    if (!lseek(fd, 0, SEEK_SET) && read(fd, buf, sizeof(buf))) {
        *val = atoi(buf);
        return true;
    } else {
        return false;
    }
}

bool read_file_to_int(const char* path, int* val) {
    bool succeed = false;
    int fd;

    fd = open(path, O_RDONLY);
    if (fd < 0) {
        LOG_ERROR("Failed to open %s for read\n", path);
        return succeed;
    } else {
        succeed = read_fd_to_int(fd, val);
        if (!succeed) {
            LOG_ERROR("Failed to read %s\n", path);
        }
    }
#ifdef DEBUG
    LOG_INFO("%s: path = %s, val = %d\n", __FUNCTION__, path, *val);
#endif
    close(fd);
    return succeed;
}

bool write_int(const char* path, int val) {
    int fd;

#ifdef DEBUG
    LOG_INFO("%s: path = %s, val = %d\n", __FUNCTION__, path, val);
#endif
    fd = open(path, O_WRONLY);
    if (fd >= 0) {
        char buffer[5];
        int bytes = snprintf(buffer, sizeof(buffer), "%d\n", val);
        ssize_t amt = write(fd, buffer, (size_t)bytes);
        close(fd);
        return amt == -1 ? false : true;
    } else {
        LOG_ERROR("Failed to open %s for write\n", path);
        return false;
    }
}

bool write_led_breath(enum led_types led_id, bool led_enable) {
    if (use_blink_node_for_breath)
        return write_int(led_blink_paths[led_id], led_enable);
    else
        return write_int(led_breath_paths[led_id], led_enable);
}

bool write_led_brightness(enum led_types led_id, int led_brightness) {
    return write_int(led_brightness_paths[led_id], led_brightness);
}

const char* led_id_to_str(enum led_types led_id) {
    switch (led_id) {
        case WHITE:
            return "White";
        case RED:
            return "Red";
        case GREEN:
            return "Green";
        case BLUE:
            return "Blue";
        case CYAN:
            return "Cyan";
        case PINK:
            return "Pink";
        case YELLOW:
            return "Yellow";
        default:
            return "Unknown";
    }
}

const char* led_state_to_str(enum led_states led_state) {
    switch (led_state) {
        case LED_STATE_BREATH:
            return "breath";
        case LED_STATE_BRIGHTNESS:
            return "brightness";
        case LED_STATE_OFF:
            return "off";
        default:
            return "unknown";
    }
}

int calc_brightness(int max_brightness) {
    return max_brightness * led_brightness_multiplier;
}

/* Main functions */

// These functions don't change any variable

void reset_all_leds(void) {
    int i;

    LOG_INFO("Reset all LEDs - Begin\n");
    for (i = 1; i < LED_STANDARD_TYPES_MAX; ++i) {
        if (use_blink_node_for_breath)
            write_int(led_blink_paths[i], 0);
        else
            write_int(led_breath_paths[i], 0);

        write_int(led_brightness_paths[i], 0);
    }
    LOG_INFO("Reset all LEDs - End\n");
}

bool set_led_brightness(enum led_types led_id, bool enable) {
    enum led_types max_brightness_led_id = LED_UNKNOWN;
    int max_brightness_val = 0;
    int new_brightness = 0;

    // Just write 0 for disable
    if (!enable) return write_led_brightness(led_id, 0);

    // Decide where to read max brightness
    if (led_id > LED_UNKNOWN && led_id < LED_STANDARD_TYPES_MAX)
        max_brightness_led_id = led_id;
    else
        max_brightness_led_id = RED;

    // Read max brightness
    if (!read_file_to_int(led_max_brightness_paths[max_brightness_led_id], &max_brightness_val))
        LOG_ERROR("Failed to read max_brightness of %s LED\n",
                  led_id_to_str(max_brightness_led_id));

    // Calculate and set brightness
    new_brightness = calc_brightness(max_brightness_val);
    return write_led_brightness(led_id, new_brightness);
}

bool set_led_state_standard_color(enum led_types led_id, enum led_states led_state) {
    bool succeed = false;
    switch (led_state) {
        case LED_STATE_BREATH:
            succeed = write_led_breath(led_id, true);
            break;
        case LED_STATE_BRIGHTNESS:
            succeed = set_led_brightness(led_id, true);
            break;
        case LED_STATE_OFF:
            succeed = set_led_brightness(led_id, false);
            break;
    }
    return succeed;
}

bool set_led_state(enum led_types led_id, enum led_states led_state) {
    bool succeed = false;
    switch (led_id) {
        // Standard colors
        case WHITE:
        case RED:
        case GREEN:
        case BLUE:
            succeed = set_led_state_standard_color(led_id, led_state);
            break;
        // Mixed colors
        case CYAN:
            succeed = set_led_state_standard_color(BLUE, led_state);
            succeed &= set_led_state_standard_color(GREEN, led_state);
            break;
        case PINK:
            succeed = set_led_state_standard_color(BLUE, led_state);
            succeed &= set_led_state_standard_color(RED, led_state);
            break;
        case YELLOW:
            succeed = set_led_state_standard_color(GREEN, led_state);
            succeed &= set_led_state_standard_color(RED, led_state);
            break;
        default:
            succeed = false;
            break;
    }
    if (!succeed) {
        LOG_ERROR("Failed to set %s LED state to %s\n", led_id_to_str(led_id),
                  led_state_to_str(led_state));
    }
    return succeed;
}

void update_active_led_brightness(void) {
    if (active_led.id == LED_UNKNOWN) return;
    if (active_led.state != LED_STATE_BRIGHTNESS) return;
    set_led_state(active_led.id, LED_STATE_BRIGHTNESS);
}

// These functions should update `struct active_led`

bool stop_active_led(void) {
    bool succeed = false;
    if (active_led.id == LED_UNKNOWN) return false;
    succeed = set_led_state(active_led.id, LED_STATE_OFF);
    if (succeed) active_led.state = LED_STATE_OFF;
    return succeed;
}

bool update_led(enum led_types led_id, enum led_states led_state) {
    bool succeed = false;

    // Exit if LED is not getting changed
    if (active_led.id == led_id && active_led.state == led_state) return true;

    succeed = stop_active_led();
    succeed &= set_led_state(led_id, led_state);

    active_led.id = led_id;
    active_led.state = led_state;

    return succeed;
}

/* Handle value updates */

void handle_battery_capacity_update_rgb(void) {
    if (bat_capacity <= 10)  // 0 ~ 10
        update_led(RED, LED_STATE_BREATH);
    else if (bat_capacity <= 20)  // 11 ~ 20
        update_led(RED, LED_STATE_BRIGHTNESS);
#ifdef __ANDROID_RECOVERY__
    else if (bat_capacity <= 60)  // 21 ~ 60
        update_led(CYAN, LED_STATE_BRIGHTNESS);
    else if (bat_capacity <= 100)  // 61 ~ 100
        update_led(GREEN, LED_STATE_BRIGHTNESS);
#else
    else if (bat_capacity <= 30)  // 21 ~ 30
        update_led(PINK, LED_STATE_BRIGHTNESS);
    else if (bat_capacity <= 50)  // 31 ~ 50
        update_led(YELLOW, LED_STATE_BRIGHTNESS);
    else if (bat_capacity <= 70)  // 51 ~ 70
        update_led(BLUE, LED_STATE_BRIGHTNESS);
    else if (bat_capacity <= 80)  // 71 ~ 80
        update_led(CYAN, LED_STATE_BRIGHTNESS);
    else if (bat_capacity <= 99)  // 81 ~ 99
        update_led(GREEN, LED_STATE_BRIGHTNESS);
    else if (bat_capacity == 100)  // 100
        update_led(GREEN, LED_STATE_BREATH);
#endif
}

void handle_battery_capacity_update_white(void) {
#ifdef __ANDROID_RECOVERY__
    if (bat_capacity <= 20)  // 0 ~ 20
        update_led(active_led.id, LED_STATE_BREATH);
    else if (bat_capacity <= 100)  // 21 ~ 100
        update_led(active_led.id, LED_STATE_BRIGHTNESS);
#else
    if (bat_capacity <= 10)  // 0 ~ 10
        update_led(active_led.id, LED_STATE_BREATH);
    else if (bat_capacity <= 99)  // 11 ~ 99
        update_led(active_led.id, LED_STATE_BRIGHTNESS);
    else if (bat_capacity == 100)  // 100
        update_led(active_led.id, LED_STATE_BREATH);
#endif
}

void (*handle_battery_capacity_update_callback)(void) = NULL;

void handle_usb_current_max_update(void) {
    if (usb_current_max <= 500000)
        led_brightness_multiplier = 0.4;
    else
        led_brightness_multiplier = 0.8;

    update_active_led_brightness();
}

// Returns false if LED is not found or invalid
bool detect_led_type(void) {
    enum led_types check_led_node = LED_UNKNOWN;

    if (file_is_writeable(led_brightness_paths[RED]) &&
        file_is_writeable(led_brightness_paths[GREEN]) &&
        file_is_writeable(led_brightness_paths[BLUE])) {
        // RGB LED
        LOG_INFO("RGB LED is found\n");
        handle_battery_capacity_update_callback = handle_battery_capacity_update_rgb;
        check_led_node = GREEN;
    } else {
        // Either White color only LED, or nothing
        handle_battery_capacity_update_callback = handle_battery_capacity_update_white;
        if (file_is_writeable(led_brightness_paths[WHITE])) {
            LOG_INFO("White LED is found, using white node\n");
            active_led.id = WHITE;
            check_led_node = WHITE;
        } else if (file_is_writeable(led_brightness_paths[RED])) {
            LOG_INFO("White LED is found, using red node\n");
            active_led.id = RED;
            check_led_node = RED;
        } else {
            LOG_ERROR("Could not find any LED\n");
            return false;
        }
    }

    if (file_is_writeable(led_blink_paths[check_led_node])) {
        LOG_INFO("LED Breath is supported, using blink node\n");
        use_blink_node_for_breath = true;
    } else if (file_is_writeable(led_breath_paths[check_led_node])) {
        LOG_INFO("LED Breath is supported, using breath node\n");
        use_blink_node_for_breath = false;
    } else {
        LOG_ERROR("LED Breath is unsupported\n");
        return false;
    }

    return true;
};

int main(void) {
    bool must_execute_handle_battery_capacity_update = true;
    int bat_capacity_new, bat_capacity_fd;
    int usb_current_max_new, usb_current_max_fd;
#ifndef __ANDROID_RECOVERY__
    int usb_online_new, usb_online_fd;
#endif

    // Detect LED type
    if (!detect_led_type()) goto error;

    // Reset all LEDs
    reset_all_leds();

    // Open
    bat_capacity_fd = open(BAT_CAPACITY_PATH, O_RDONLY);
    if (bat_capacity_fd < 0) {
        LOG_ERROR("Failed to open battery capacity\n");
        goto error;
    }
    usb_current_max_fd = open(USB_CURRENT_MAX_PATH, O_RDONLY);
    if (usb_current_max_fd < 0) {
        LOG_ERROR("Failed to open usb current_max\n");
        goto error_undo_bat_capacity_fd;
    }
#ifndef __ANDROID_RECOVERY__
    usb_online_fd = open(USB_ONLINE_PATH, O_RDONLY);
    if (usb_online_fd < 0) {
        LOG_ERROR("Failed to open usb online\n");
        goto error_undo_usb_current_max_fd;
    }
#endif

    // Event loop
    while (true) {
#ifndef __ANDROID_RECOVERY__
        if (read_fd_to_int(usb_online_fd, &usb_online_new)) {
            if (usb_online != usb_online_new) {
                usb_online = usb_online_new;
                if (usb_online) {
#ifdef DEBUG
                    LOG_INFO("USB is online\n");
#endif
                    must_execute_handle_battery_capacity_update = true;
                } else {
#ifdef DEBUG
                    LOG_INFO("USB is offline, Disable LED and wait for the next update\n");
#endif
                    stop_active_led();
                    sleep(UPDATE_INTERVAL_SEC);
                    continue;
                }
            }
        } else {
            LOG_ERROR("Error in reading usb online\n");
            goto error_undo_fd;
        }
#endif

        if (read_fd_to_int(usb_current_max_fd, &usb_current_max_new)) {
            if (usb_current_max != usb_current_max_new) {
                usb_current_max = usb_current_max_new;
#ifdef DEBUG
                LOG_INFO("Handle usb current_max update, usb_current_max = %d\n", usb_current_max);
#endif
                handle_usb_current_max_update();
            }
        } else {
            LOG_ERROR("Error in reading usb current_max\n");
            goto error_undo_fd;
        }

        // Decide what brightness to set before this
        if (read_fd_to_int(bat_capacity_fd, &bat_capacity_new)) {
            if (must_execute_handle_battery_capacity_update || (bat_capacity != bat_capacity_new)) {
                must_execute_handle_battery_capacity_update = false;
                bat_capacity = bat_capacity_new;
#ifdef DEBUG
                LOG_INFO("Handle battery capacity update, bat_capacity = %d\n", bat_capacity);
#endif
                (*handle_battery_capacity_update_callback)();
            }
        } else {
            LOG_ERROR("Error in reading battery capacity\n");
            goto error_undo_fd;
        }

        sleep(UPDATE_INTERVAL_SEC);
    }

    // We should never reach here
#ifndef __ANDROID_RECOVERY__
    close(usb_online_fd);
#endif
    close(usb_current_max_fd);
    close(bat_capacity_fd);
    return 0;

error_undo_fd:
#ifndef __ANDROID_RECOVERY__
    close(usb_online_fd);
error_undo_usb_current_max_fd:
#endif
    close(usb_current_max_fd);
error_undo_bat_capacity_fd:
    close(bat_capacity_fd);
error:
    return 1;
}

#include <fcntl.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

//#define DEBUG

#if defined(DEBUG)
#define UPDATE_INTERVAL_SEC 1
#elif defined(RECOVERY)
#define UPDATE_INTERVAL_SEC 5
#else
#define UPDATE_INTERVAL_SEC 60
#endif

#define BAT_CAPACITY_PATH "/sys/class/power_supply/battery/capacity"
#define USB_CURRENT_MAX_PATH "/sys/class/power_supply/usb/current_max"

#define LED_PATH(led, file)   "/sys/class/leds/" led "/" file

#define LOG_TAG "charger_led: "
#define LOG_ERROR(...)  fprintf(stderr, LOG_TAG __VA_ARGS__)
#define LOG_INFO(...)  fprintf(stdout, LOG_TAG __VA_ARGS__)

// Definitions
enum led_types {
    LED_UNKNOWN = 0,
    WHITE,
    RED,
    GREEN,
    BLUE,
    LED_TYPES_MAX
};

enum led_states {
    LED_STATE_OFF = 0,
    LED_STATE_BLINK,
    LED_STATE_BRIGHTNESS
};

const char led_blink_paths[LED_TYPES_MAX][64] = {
    [WHITE] = LED_PATH("white", "blink"),
    [RED] = LED_PATH("red", "blink"),
    [GREEN] = LED_PATH("green", "blink"),
    [BLUE] = LED_PATH("blue", "blink"),
};

const char led_breath_paths[LED_TYPES_MAX][64] = {
    [WHITE] = LED_PATH("white", "breath"),
    [RED] = LED_PATH("red", "breath"),
    [GREEN] = LED_PATH("green", "breath"),
    [BLUE] = LED_PATH("blue", "breath"),
};

const char led_brightness_paths[LED_TYPES_MAX][64] = {
    [WHITE] = LED_PATH("white", "brightness"),
    [RED] = LED_PATH("red", "brightness"),
    [GREEN] = LED_PATH("green", "brightness"),
    [BLUE] = LED_PATH("blue", "brightness"),
};

const char led_max_brightness_paths[LED_TYPES_MAX][64] = {
    [WHITE] = LED_PATH("white", "max_brightness"),
    [RED] = LED_PATH("red", "max_brightness"),
    [GREEN] = LED_PATH("green", "max_brightness"),
    [BLUE] = LED_PATH("blue", "max_brightness"),
};

// Variables
bool use_blink_node = false;
float led_brightness_multiplier = 1.0;
int bat_capacity = 0;
int usb_current_max = 0;

struct led {
    int id;
    int state;
    int brightness;
    int max_brightness;
} active_led = {LED_UNKNOWN, LED_STATE_OFF, 0, 0};

// Helper functions
bool file_is_writeable(const char *path) {
    int fd = open(path, O_WRONLY);

    if (fd < 0) {
        return false;
    } else {
        close(fd);
        return true;
    }
}

bool read_fd_to_int(int fd, int *val) {
    char buf[10];

    if (!lseek(fd, 0, SEEK_SET) &&
        read(fd, buf, sizeof(buf))) {
        *val = atoi(buf);
        return true;
    } else {
        return false;
    }
}

bool read_file_to_int(const char *path, int *val) {
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

bool write_int(const char *path, int val) {
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

bool write_led_breath(int led_id, bool led_enable) {
    if (use_blink_node)
        return write_int(led_blink_paths[led_id], led_enable);
    else
        return write_int(led_breath_paths[led_id], led_enable);
}

bool write_led_brightness(int led_id, int led_brightness) {
    return write_int(led_brightness_paths[led_id], led_brightness);
}

const char *led_id_to_str(int led_id) {
    switch (led_id) {
        case WHITE:
            return "White";
        case RED:
            return "Red";
        case GREEN:
            return "Green";
        case BLUE:
            return "Blue";
        default:
            return "Unknown";
    }
}

int calc_brightness(int max_brightness) {
    return max_brightness * led_brightness_multiplier;
}

// Main functions
void reset_all_leds(void) {
    int i;

    LOG_INFO("Reset all LEDs - Begin\n");
    for (i=1; i<LED_TYPES_MAX; ++i) {
        if (use_blink_node)
            write_int(led_blink_paths[i], 0);
        else
            write_int(led_breath_paths[i], 0);

        write_int(led_brightness_paths[i], 0);
    }
    LOG_INFO("Reset all LEDs - End\n");
}

bool stop_active_led(void) {
    bool succeed = false;

    switch (active_led.state) {
        case LED_STATE_BLINK:
            succeed = write_led_breath(active_led.id, false);
            break;
        case LED_STATE_BRIGHTNESS:
            active_led.brightness = 0;
            succeed = write_led_brightness(active_led.id, 0);
            break;
        case LED_STATE_OFF:
            succeed = true;
            break;
    }

    if (succeed)
        active_led.state = LED_STATE_OFF;

    return succeed;
}

bool update_led_brightness(int led_id) {
    int new_brightness = calc_brightness(active_led.max_brightness);

    if (active_led.state != LED_STATE_BRIGHTNESS ||
        active_led.brightness == new_brightness)
        return true;

    active_led.brightness = new_brightness;
    return write_led_brightness(led_id, new_brightness);
}

bool update_led(int led_id, int led_state) {
    bool succeed = false;
    int max_brightness_val = 0;

    // Exit if LED is not getting changed
    if (active_led.id == led_id && active_led.state == led_state) {
        return true;
    }

    // Stop currently active LED first
    if (active_led.id != led_id || active_led.state != led_state)
        stop_active_led();

    if (!read_file_to_int(led_max_brightness_paths[led_id], &max_brightness_val))
        LOG_ERROR("Failed to read max_brightness of %s LED\n", led_id_to_str(led_id));

    active_led.id = led_id;
    active_led.state = led_state;
    active_led.brightness = 0;
    active_led.max_brightness = max_brightness_val;

    switch (led_state) {
        case LED_STATE_BLINK:
            succeed = write_led_breath(led_id, true);
            break;
        case LED_STATE_BRIGHTNESS:
            succeed = update_led_brightness(led_id);
            break;
        case LED_STATE_OFF:
            succeed = true;
            break;
    }

    return succeed;
}

void handle_battery_capacity_update_rgb(void) {
    if (bat_capacity <= 10) // 0 ~ 10
        update_led(RED, LED_STATE_BLINK);
    else if (bat_capacity <= 30) // 11 ~ 30
        update_led(RED, LED_STATE_BRIGHTNESS);
    else if (bat_capacity <= 80) // 31 ~ 80
        update_led(BLUE, LED_STATE_BRIGHTNESS);
#ifdef RECOVERY
    else if (bat_capacity <= 100) // 81 ~ 100
        update_led(GREEN, LED_STATE_BRIGHTNESS);
#else
    else if (bat_capacity <= 99) // 81 ~ 99
        update_led(GREEN, LED_STATE_BRIGHTNESS);
    else if (bat_capacity == 100) // 100
        update_led(GREEN, LED_STATE_BLINK);
#endif
}

void handle_battery_capacity_update_white(void) {
#ifdef RECOVERY
    if (bat_capacity <= 30) // 0 ~ 30
        update_led(active_led.id, LED_STATE_BLINK);
    else if (bat_capacity <= 100) // 31 ~ 100
        update_led(active_led.id, LED_STATE_BRIGHTNESS);
#else
    if (bat_capacity <= 10) // 0 ~ 10
        update_led(active_led.id, LED_STATE_BLINK);
    else if (bat_capacity <= 99) // 11 ~ 99
        update_led(active_led.id, LED_STATE_BRIGHTNESS);
    else if (bat_capacity == 100) // 100
        update_led(active_led.id, LED_STATE_BLINK);
#endif
}

void (*handle_battery_capacity_update_callback)(void) = NULL;

void handle_usb_current_max_update(void) {
    if (usb_current_max <= 500000)
        led_brightness_multiplier = 0.4;
    else
        led_brightness_multiplier = 0.8;

    update_led_brightness(active_led.id);
}

int main(void) {
    int bat_capacity_new;
    int bat_capacity_fd;
    int usb_current_max_new;
    int usb_current_max_fd;

    // Determine LED support
    if (!file_is_writeable(led_brightness_paths[BLUE])) {
        LOG_INFO("RGB LED node does not exist, use White LED\n");
        handle_battery_capacity_update_callback = handle_battery_capacity_update_white;
        active_led.id = WHITE;
        if (!file_is_writeable(led_brightness_paths[WHITE])) {
            LOG_INFO("White LED node does not exist, use Red LED node for White LED\n");
            active_led.id = RED;
            if (!file_is_writeable(led_brightness_paths[RED])) {
                LOG_ERROR("Red LED node does not exist, No available LED\n");
                goto error;
            }
        }
    } else {
        if (!file_is_writeable(led_brightness_paths[GREEN]) ||
            !file_is_writeable(led_brightness_paths[RED])) {
            LOG_ERROR("RGB LED is missing some nodes\n");
            goto error;
        }
        LOG_INFO("RGB LED exists, use RGB LED\n");
        handle_battery_capacity_update_callback = handle_battery_capacity_update_rgb;
    }

    // Use blink node?
    if (active_led.id == LED_UNKNOWN) {
        // RGB LED
        if (file_is_writeable(led_blink_paths[RED]))
            use_blink_node = true;
    } else {
        // White LED
        if (file_is_writeable(led_blink_paths[active_led.id]))
            use_blink_node = true;
    }
    if (use_blink_node)
        LOG_INFO("Use blink node for breath\n");

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
        close(bat_capacity_fd);
        goto error;
    }

    // Read
    while (true) {
        if (read_fd_to_int(bat_capacity_fd, &bat_capacity_new)) {
            if (bat_capacity != bat_capacity_new) {
                bat_capacity = bat_capacity_new;
#ifdef DEBUG
                LOG_INFO("Handle battery capacity update, bat_capacity = %d\n", bat_capacity);
#endif
                (*handle_battery_capacity_update_callback)();
            }
        } else {
            LOG_ERROR("Error in reading battery capacity\n");
            goto error_fd;
        }

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
            goto error_fd;
        }

        sleep(UPDATE_INTERVAL_SEC);
    }

    // We should never reach here
    close(bat_capacity_fd);
    close(usb_current_max_fd);
    return 0;

error_fd:
    close(bat_capacity_fd);
    close(usb_current_max_fd);
error:
    return 1;
}

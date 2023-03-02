#define LOG_TAG "SubsystemStateNotifier"
#include <log/log.h>

#include <poll.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <cutils/properties.h>

#define PROP_SUBSYS_STATE "vendor.subsys_state_notifier.%s.state"
#define SUBSYS_STATE_PATH "/sys/class/subsys/subsys_%s/device/subsys%d/state"

int main(int argc, char **argv) {
    char prop_subsys_state_key[64];
    char subsystem[10];
    char subsystem_state[11];
    char subsystem_state_path[64];
    int fd, i;
    struct pollfd pollfds[1];

    // Get subsystem name from the first arg
    if (argc != 2) {
        ALOGE("Wrong arguments\n");
        goto error;
    }
    strncpy(subsystem, argv[1], sizeof(subsystem));
    ALOGI("Subsystem to monitor: %s\n", subsystem);

    // Open fd
    for (i=0; i<=9; ++i) {
        snprintf(subsystem_state_path, sizeof(subsystem_state_path), SUBSYS_STATE_PATH, subsystem, i);
        fd = open(subsystem_state_path, O_RDONLY);
        if (fd >= 0) {
            ALOGI("Opened %s subsystem state path %s\n", subsystem, subsystem_state_path);
            break;
        }
    }
    if (fd < 0) {
        ALOGE("Failed to open %s subsystem state\n", subsystem);
        goto error;
    }

    // Set property key for notifying state
    snprintf(prop_subsys_state_key, sizeof(prop_subsys_state_key), PROP_SUBSYS_STATE, subsystem);

    // Prepare for poll
    pollfds[0].fd = fd;
    pollfds[0].events = POLLERR | POLLPRI;

    // Poll
    while (true) {
        if (lseek(fd, 0, SEEK_SET) || !read(fd, subsystem_state, sizeof(subsystem_state))) {
            ALOGE("Error in reading\n");
            goto error_fd;
        }

        subsystem_state[strlen(subsystem_state)-1] = '\0';
        ALOGI("%s subsystem state: %s\n", subsystem, subsystem_state);
        property_set(prop_subsys_state_key, subsystem_state);

        if (!poll(pollfds, 1 , -1)) {
            ALOGE("Error in polling\n");
            goto error_fd;
        }
    }

    // We should never reach here
    close(fd);
    return 0;

error_fd:
    close(fd);
error:
    return 1;
}

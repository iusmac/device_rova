# Battery-friendly Pocket Mode At Kernel Level

![GitHub Licence](https://img.shields.io/github/license/iusmac/battery-friendly-pocketmode?style=for-the-badge) ![GitHub release (latest SemVer)](https://img.shields.io/github/v/tag/iusmac/battery-friendly-pocketmode?sort=semver&style=for-the-badge&logo=hackthebox&color=blue&logoColor=aliceblue)

Settings › Display › Pocket detection | Demo
:------------------------------------:|:-----:
<img src="https://user-images.githubusercontent.com/28353279/201163777-5a1943c7-078a-4257-b446-3fb4b808738f.png" /> | <a href="https://user-images.githubusercontent.com/28353279/201133473-56015bf9-e13b-4f73-8b3a-2836fdb170da.mp4" target="_blank"><img src="https://user-images.githubusercontent.com/28353279/201163773-dbdf192d-d7df-4250-b687-086f54158298.png" /></a>

## About
This pocket mode implementation is a fork of [Battery-friendly Pocketmode](https://github.com/maytinhdibo/battery-friendly-pocketmode) written by [Trần Mạnh Cường (Cuong Tran)](https://github.com/maytinhdibo). It's still continues the main idea, which is to run the proximity sensor and checks only on the lock screen, so that deep sleep and battery life aren't affected.

**The weaknesses of the previous pocket mode implementation:**
1. Not translated to most spoken languages
2. The user isn't notified on pocket mode being active
3. Potential screen on/off loop and undesired bugs when hardware buttons are set to explicitly wake up the screen
4. No "safe door" to exit pocket mode in case of proximity sensor issues
5. Unnecessary timeouts that add unpredictable behavior and bad UX like:
    > In case the screen automatically turns off after being taken out of the pocket, please wait for a few seconds and everything will work again.
    >
    > \- Trần Mạnh Cường (Cuong Tran)

**So, what's done in this implementation:**
1. The screen isn't turned off when proximity sensor is positive but instead, we tell the kernel to ignore touchscreen, HW buttons, fingerprint, slider etc., as if they are physically absent
2. Available in 36 languages upon release
3. Volume keys force wake screen if volume buttons should not seek media tracks (_LineageOS-based ROMs only_)
4. The user receives a silent, but a high importance notification on pocket mode being active
5. Pocket mode can be temporarily suspended until next screen on/off cycle by pressing and holding the power button for at least 2 seconds (by default).
6. No "sluggish" reaction or unpredictable behavior. High responsiveness and accuracy thanks to the kernel driver
7. New settings layout, with demo and instructions to improve UX
8. Minor bug fixes discovered in previous pocket mode implementation

## Requirements
_(Compile)_ Android 13 (API level 33) code base

_(Launch)_ Android 12+ (API level 31+)

## How to implement
### Android-side
1. Create a directory named `PocketJudge` (or whatever you want to call it) in your DT (device tree)
2. Copy the content of `java/` directory into newly created directory in step n.1
3. Add package to your `device.mk`
    ```Makefile
    PRODUCT_PACKAGES += \
        PocketJudge
    ```
4. Pick SELinux policies from `sepolicy/` directory to your DT `sepolicy/` directory.

_See implementation example into XiaomiParts:_ [here](https://github.com/iusmac/device_rova/commit/de14e4803bb01a0cc4b19462dd1cdcdab20daa89)

### Kernel-side
1. Apply patches for pocket judge and HW buttons (power/GPIO buttons) drivers from `kernel/` directory
2. Add your global enable/disable [IRQ](https://en.wikipedia.org/wiki/Interrupt_request_(PC_architecture)) callbacks for the desired device components you want to ignore (ex. touchscreen) to `pocket_judge_update` function in `drivers/input/pocket-judge.c` file
3. Enable driver in your defconfig:
    ```
    CONFIG_POCKET_JUDGE=y
    ```
<details>
    <summary>See IRQ callback implementation examples</summary>

- **FocalTech** (_FT5346_) & **Goodix** (_GT9xx_v2.8_) touchscreen drivers: [here](https://github.com/iusmac/kernel_rova/commits/k4.9-battery-friendly-pocketmode)
- **Fingerprint Cards** (_fpc1020_) fingerprint driver & **Synaptics** (_S3320_) touchscreen driver: [here](https://github.com/AICP/kernel_oneplus_msm8998/commit/85d67b7c203f1351c42797cd6ca54b08d1cb63b0)
</details>

## ROM adaptations
<details>
    <summary><b>ERROR</b> '<em>Duplicate declaration of type</em>' at token</summary>

Most custom ROMs already have traditional pocket mode implementation and needed sepolicy types/context definitions for pocket bridge sysfs node. In case of "_Duplicate declaration of type_" error during sepolicy compilation, remove duplicates and leave sepolicy rules from `system_app.te` file only.
</details>

## Under the hood: how does pocket bridge works?
In the previous implementation, the service uses proximity sensor to turn off the display once it's covered. In this implementation, the service uses the proximity sensor to comunicate pocket state to the pocket judge kernel driver via sysfs file defined in path: `/sys/kernel/pocket_judge/inpocket`

<details>
    <summary>Communication in details</summary>

- When IN POCKET (sensor covered), we write ` 1 ` to sysfs node to trigger user's callbacks in the pocket judge driver, which should disable [IRQ](https://en.wikipedia.org/wiki/Interrupt_request_(PC_architecture)) interruptions for desired device components (touchscreen, HW buttons, fingerprint, slider etc.).

- When NOT IN POCKET (sensor uncovered), we write ` 0 ` to sysfs node, which does the opposite effect of IN POCKET state.

- When POWER BUTTON IS PRESSED for n seconds (_2s by default_), the pocket judge driver will output ` 2 ` when reading sysfs node and all IRQ interruptions will be enabled immediately; this state is called "_safe door_". In our java app, we run a thread to listen for "_safe door_" state if proximity sensor was positive (covered) at least ones.

   **Note:** to properly enter the "_safe door_" state, the power button must be released, so that the driver can calculate elapsed time between "_press_" and "_release_" events.

- When IN "SAFE DOOR" state, we temporarily suspend proximity sensor reading until next screen on/off cycle and write ` 0 ` to sysfs node to inform the pocket judge driver that we are aware of "safe door" state.
</details>

## Contributing
Contributions are welcome! Fixes, improvements and implementations for different kernel versions, SoCs, touchscreens, fingerprints etc., are highly appreciated. Share your patches via PR!

## Authors
[Trần Mạnh Cường (Cuong Tran)](https://github.com/maytinhdibo)

[iusmac (Max)](https://github.com/iusmac)

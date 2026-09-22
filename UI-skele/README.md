# Bluetooth Controller

A powerful Android app that turns your phone into a robust Bluetooth Human Interface Device (HID). Control any PC, TV, or Bluetooth-enabled device without needing to install any receiver software on the host machine.

## Features
- **Combo HID Descriptor:** Broadcasts as a Mouse, Keyboard, and Multimedia Controller simultaneously.
- **Trackpad Mode:** Smooth mouse movement, two-finger scrolling, and gesture support (Tap for left click, two-finger tap for right click).
- **Virtual Keyboard Integration:** Instantly map Android virtual keyboard presses to physical USB HID keystrokes. Send entire strings or your clipboard directly to your PC.
- **Thinkpad Mode (WIP):** A classic Thinkpad-style layout featuring the iconic TrackPoint for joystick-style mouse control.
- **Direct Connect:** Connect directly to previously paired devices from within the app, bypassing the standard Bluetooth unpair/repair loop.

## How It Works
This app utilizes the Android `BluetoothHidDevice` API (introduced in API 28) to emulate physical USB/Bluetooth peripherals at a hardware level.

## License
This project is licensed under the GNU General Public License v3.0 (GPLv3) - see the [LICENSE](LICENSE) file for details. This ensures the project remains entirely open-source and prevents proprietary monetization.

# Modern Ice

A client-side Forge 1.8.9 mod that keeps ice momentum at block edges and corners, with adjustable slipperiness.

## Installation

Put `ModernIce-1.8.9-forge-*.jar` in your `mods` folder and launch Forge 1.8.9.

## Settings

Run `/ice` to open the settings.

- **Modern Ice:** turn the mod on or off.
- **Custom Slipperiness:** use the slider when on, or each block's own slipperiness when off.

The slider ranges from `0.98` (default) to `1.0`; `0.989` matches blue ice.
You can also type a value beside the slider and press Enter to apply it.
Both options start enabled. Turning the mod off restores vanilla movement.
Settings save when you close the screen.

## Build

With JDK 21:

```sh
./gradlew build
```

Output: `build/libs/`. Requires Java 8.

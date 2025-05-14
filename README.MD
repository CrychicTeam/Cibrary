# Pibrary

<div align="center">
  <img height="200" src="https://docs.mihono.cn/logo.png" width="200"/>
  <h3>A Powerful Utility Library for Minecraft Modding</h3>
</div>

## Overview

Pibrary is a comprehensive utility library for Minecraft Forge mods that provides a wide range of tools and APIs to simplify mod development. To use this library in your project, change the import reference from `pickaid1201` to `pickaid{your_mc_version}` based on your Minecraft version.

## Supported Versions

| Version | Status           |
|---------|------------------|
| 1.19.2  | May implement    |
| 1.20.1  | In development   | 
| 1.21.1  | Coming soon      |

## Features

- **Key Handling System**: Advanced key state machine for managing complex keyboard inputs and key combinations : record normal press, charging press, held pressing and rapid presses.
- **Custom Effects Framework**: API for creating and managing player and entity effects.
- **Global Sound Management**: Centralized sound system for better audio control across your mod
- **Math Utilities**: Helper functions for common mathematical operations in game development

## Installation

Add the following to your `build.gradle`:

```gradle
repositories {
    // Add Pibrary repository
    maven { url = 'https://maven.mihono.cn/pickaid1201/' }
}

dependencies {
    implementation 'com.mihono.pickaid:pibrary:VERSION' // Replace VERSION with the desired version
}
```

## Usage

### KeyConfig

To register a KeyConfig, you can use the following code:

```java
    KEY = KeyRegistry.register(new KeyConfig.Builder(Pibrary.MOD_ID, "testskill", GLFW.GLFW_KEY_G)
                .category("category") // The category of the key
                .enableCharging() // Enable charging
                .maxPower(4.0f) // The max power of the key
                .autoReleaseOnMax(true) // Auto release on max power
                .setChargingTimeTolerance(500) // The charging time tolerance
                .setTimeoutTime(100000) // The timeout time
                .enableRapidClick() // Enable rapid click
                .setMaxRapidClickCount(10) // The max rapid click count
                .showDebugMessage() // Show debug message
//                        .setKeyModifier(KeyModifier.ALT)
                .build()
        );

```

To include a existing keymaaping inside the keyconfig, you can use the following code:

```java
    KEY = KeyRegistry.register(new KeyConfig.Builder("minecraft", "forward", Minecraft.getInstance().options.keyUp)
                .build());
        );
```

As complex state machine, you might shouldn't use all key features of the keyConfig. If some functions work weird, disable normal click expecially when enable rapid click.

When enable rapid click, be aware that the first click of rapid clicks is Normal Pressed type.

### Combo System

Can be used but still in development.

### Custom Effect

Now there's only one handled effect: playerEffect.

For include your own effect in handler, you need to put your player and effect in a map in Pibrary.

```java
    Pibrary.activeEffects.put(player, new PlayerEffect());
```

and remove the effect from the map when the effect is finished.

```java
    Pibrary.activeEffects.remove(player);
```

This part is still weak and need to be improved.

## Credits

Developed by Pickaid Team

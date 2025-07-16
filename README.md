https://www.curseforge.com/minecraft/mc-mods/show-me-your-build
# SHOW ME YOUR BUILD

A mod used for showing your builds to your friends!

### CONFIGURABLE CHANGES:

```
    assets/showmeyourbuild/override.json
```

### ✍️ Format

The `override.json` file supports two top-level sections:

- `attributes`: controls individual attribute display (icon, label, tooltip, visibility)
- `groups`: controls namespace-wide visual theming

#### ✅ Example Format

```json
{
  "attributes": {
    "minecraft:generic.attack_damage": {
      "icon": "minecraft:textures/item/iron_sword.png",
      "label": "Attack Damage",
      "tooltip": "The base damage this player deals.",
      "hidden": false
    }
  },
  "groups": {
    "minecraft": {
      "icon": "minecraft:textures/item/diamond.png",
      "label": "Minecraft",
      "tooltip": "Vanilla Minecraft Attributes",
      "hidden": false
    }
  }
}

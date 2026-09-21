# Villager News Addon Port

A Fabric port (with a scaffolded NeoForge module) of the **Villager News
Add-On** for Minecraft Java Edition 26.1 and up. It brings the original Villager
News characters, models, animations, textures, and contextual dialogue to
Java Edition while retaining normal Minecraft villager gameplay.

This mod does **not** include Element Animation's / Oreville Studios' voice
lines or sound effects. On first launch it asks you to select the Villager
News `.mcaddon` you already own; it extracts just the sound files into a
plain resource pack (`resourcepacks/Villager News Voices`) and never touches
the rest of your add-on. Skipping that step is fine, but villagers won't have
any Villager News dialogue, voices, or sounds until you import it (from the
title screen prompt or later from the handbook's Settings page).

## Features

- Detailed animated Villager News models converted for Entity Model Features
- Biome, profession, and profession-level villager textures
- The Mayor, Testificate Man, Villager Number 5, Villager Number 9, and
  Villager Unreachable as named characters
- Wooly the Sheep and the Villager News wandering trader
- 2,212 voice clips across 523 dialogue groups and 22 short reaction effects,
  including synchronized villager and wandering-trader hurt effects, imported
  from your own copy of the add-on (see above - not bundled with the mod)
- Context-aware dialogue for player actions, nearby mobs, weather, dimensions,
  combat, trading, work, sleep, spawning, growth, and other world events
- Multi-part conversations between nearby villagers
- Facial expressions and gestures synchronized with each voice line
- Server-controlled dialogue selection, sound playback, cooldowns, and
  villager behavior
- Speakers look toward the player, entity, block, or villager they are talking
  about
- Removable villager noses, character cosmetics, cosmetic reactions, and
  missing-nose conversations
- Character trades for the Mayor Hat, Testificate Man Helmet, Moustache, and
  Microphone
- Persistent natural spawning for one of each special character in distant
  villages
- A craftable Villager News Handbook
- Optional Mod Menu configuration screen

## Requirements

- Minecraft Java Edition 26.1 or newer.
- Java 26 or newer
- Fabric Loader 0.19.5 or newer
- Fabric API for Minecraft 26.3
- Entity Model Features 3.3.8 or newer (Fabric only - no NeoForge build exists)
- Entity Texture Features 7.2.4 or newer (Fabric only)
- Entity Sound Features 0.8.2 or newer (Fabric only)
- Your own legally obtained copy of the Villager News `.mcaddon`

EMF, ETF, and ESF are external dependencies. This project does not bundle or
modify them.

Mod Menu is optional. When installed, its Configure button opens the Villager
News settings directly. Without Mod Menu, the same settings remain available
in the Villager News Handbook.

## Installation

1. Install Fabric Loader for Minecraft 26.1 or later on Java 26+.
2. Download Fabric API, EMF, ETF, and ESF for the same Minecraft version.
3. Put the dependency jars and the Villager News Addon Port jar in the
   Minecraft `mods` folder.
4. Start Minecraft with the Fabric profile. On first launch, select your
   Villager News `.mcaddon` when prompted (or skip - see above).

The dialogue controller runs on the server. For multiplayer, install the mod
and its dependencies on both the server and every connecting client, and make
sure each client has imported the add-on's sounds locally.

## Characters

Use a name tag on a villager to select a character model and voice:

| Name tag | Character |
| --- | --- |
| `Mayor`, `Mayor Villager`, or `The Mayor` | Mayor Villager |
| `Testificate Man` | Testificate Man |
| `Villager Number 5` or `Villager #5` | Villager Number 5 |
| `Villager Number 9` or `Villager #9` | Villager Number 9 |
| `Villager Unreachable` or `Can't Catch Me!` | Villager Unreachable |

Name a sheep `Wooly` or `Wooly The Sheep` to use Wooly's model, animations,
and sounds. Ordinary villagers and wandering traders receive their Villager
News appearance and dialogue automatically.

Special characters can also appear naturally as new distant villages are
generated. Each character appears once at a time and becomes eligible to spawn
again after being killed.

## Items

All custom items are available in the **Villager News** creative-mode tab.

Craft the Villager News Handbook from three pieces of paper. It includes the
add-on's overview, special-character and cosmetic guides, settings reference,
social and support pages, and the complete searchable Triggers & Reactions
guide.

Shear an adult villager to remove its nose. Interact with that villager while
holding the nose to return it. The Mayor, Testificate Man, Villager #5, and
Villager #9 sell their matching cosmetics. Cosmetics can be given to ordinary
villagers and removed again with shears.

## Dialogue

Villagers react to what happens around them. They can comment when a player
approaches, stares, changes game mode, wears armor, receives an effect, breaks
or places a block, uses an item, completes a trade, or spawns a villager with a
spawn egg. They also react to their profession, workstation, level, biome,
weather, time of day, nearby entities, damage source, and other villagers.

The server chooses the exact voice variant and broadcasts its matching
animation. Each speaker remains occupied for the real length of the clip,
preventing unrelated lines from overlapping. Conversation partners take turns
and continue looking at each other throughout multi-part exchanges.

## Building from source

On Windows:

```powershell
.\gradlew.bat build
```

On Linux or macOS:

```bash
./gradlew build
```

The distributable jar is written to `build/libs`.

To include the operator-only dialogue test command in a development build, set
`dialogue_test_command=true` in `gradle.properties` before building. Use
`/dialoguetest <1-523>` in game to spawn the matching speaker and subject, play
every variant from that dialogue group, and remove the test actors when each one ends.
Use `/dialoguetest continuous` to run all 523 groups in order. Each group is
announced with its variant number in chat, and the next variant begins one second
after the current voice line finishes.
The setting defaults to `false` for release builds.
Villager News and the original add-on assets were created by **Oreville
Studios Ltd** and **Element Animation**. The converted models, textures,
animations, and audio remain the property of their respective owners. See
[`LICENSE`](LICENSE) for repository licensing details.

# NovaEssentials

A modern essentials plugin for **Paper 1.21+** servers, inspired by **[EssentialsX](https://github.com/EssentialsX/Essentials)**, featuring **50+ commands**, a **Ban-System**, **Chat-Formatting**, **Discord Integration**, **[PAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) & [Vault](https://github.com/milkbowl/Vault) support** and **fully customizable messages** in different languages.

---

## Features

### Commands

| Command | Description | Aliases                                | Permission |
|---|---|----------------------------------------|---|
| `/gamemode <mode> [player]` | Change own or another player's game mode | `/gm`                                  | `novaess.cmd.gm` / `.gm.other` |
| `/msg <player> <message>` | Send a private message | `/tell`, `/whisper`, `/w`              | `novaess.cmd.msg` |
| `/r <message>` | Reply to the last private message | `/reply`                               | `novaess.cmd.msg` |
| `/vanish [player]` | Toggle vanish mode | `/v`                                   | `novaess.cmd.vanish` / `.vanish.other` |
| `/fly [player]` | Toggle flight mode | —                                      | `novaess.cmd.fly` / `.fly.other` |
| `/speed <0-10> [player]` | Set movement or fly speed | —                                      | `novaess.cmd.speed` / `.speed.other` |
| `/enchant <enchantment> [level]` | Enchant item in hand without vanilla restrictions | `/ench`                                | `novaess.cmd.enchant` |
| `/heal [player]` | Restore health to full | —                                      | `novaess.cmd.heal` / `.heal.other` |
| `/feed [player]` | Restore hunger to full | —                                      | `novaess.cmd.feed` / `.feed.other` |
| `/god [player]` | Toggle god mode (invulnerable to damage) | —                                      | `novaess.cmd.god` / `.god.other` |
| `/invsee <player>` | View (and optionally modify) another player's inventory | —                                      | `novaess.cmd.invsee` / `.invsee.modify` |
| `/ec [player]` | Open an ender chest | `/enderchest`                          | `novaess.cmd.ec` / `.ec.other` / `.ec.other.modify` |
| `/workbench [player]` | Open a virtual crafting table | `/craft`, `/wb`                        | `novaess.cmd.workbench` / `.workbench.other` |
| `/anvil [player]` | Open a virtual anvil | `/anv`                                 | `novaess.cmd.anvil` / `.anvil.other` |
| `/sudo <player> <message\|/cmd>` | Execute a command or message as another player | —                                      | `novaess.cmd.sudo` |
| `/tpa <player>` | Send a teleport request to a player | —                                      | `novaess.cmd.tpa` |
| `/tpaccept` | Accept a pending teleport request | —                                      | `novaess.cmd.tpaccept` |
| `/tpdeny` | Deny a pending teleport request | —                                      | `novaess.cmd.tpdeny` |
| `/tpahere <player>` | Request a player to teleport to you | `/tphere`                              | `novaess.cmd.tpahere` |
| `/tpall` | Teleport all players to your location | —                                      | `novaess.cmd.tpall` |
| `/back` | Teleport to your last death location | —                                      | `novaess.cmd.back` |
| `/block <player>` | Block / unblock a player from messaging and TPA | —                                      | `novaess.cmd.block` |
| `/home [name\|list]` | Teleport to a home, or open the home GUI / list | —                                      | `novaess.cmd.home` |
| `/sethome [name]` | Set a home at your current location | —                                      | `novaess.cmd.sethome` |
| `/delhome <name>` | Delete a home | —                                      | `novaess.cmd.delhome` |
| `/spawn` | Teleport to the server spawn | —                                      | `novaess.cmd.spawn` |
| `/setspawn` | Set the server spawn location | —                                      | `novaess.cmd.setspawn` |
| `/warp <name\|list>` | Teleport to a warp, or open the warp GUI / list | —                                      | `novaess.cmd.warp` |
| `/setwarp <name>` | Create a warp at your location | —                                      | `novaess.cmd.setwarp` |
| `/delwarp <name>` | Delete a warp | —                                      | `novaess.cmd.delwarp` |
| `/time <day\|night\|noon\|midnight\|value>` | Set the world time | `/day`, `/night`, `/noon`, `/midnight` | `novaess.cmd.time` |
| `/kit [name]` | Receive a kit, or open the kit GUI / list | —                                      | `novaess.cmd.kit` |
| `/broadcast <message>` | Broadcast a bold formatted message to all players with prominent dividers | `/bc, /announce`                       | `novaess.cmd.broadcast` |
| `/chat <pause\|unpause\|clear> [duration]` | Pause, unpause, or clear the chat | —                                      | `novaess.cmd.chat` |
| `/settings` | Open a personal settings GUI to configure preferences | —                                      | `novaess.cmd.settings` |
| `/mute <player> [duration] [reason]` | Mute a player (notifies staff and console) | —                                      | `novaess.cmd.mute` |
| `/unmute <player>` | Unmute a player (notifies staff and console) | —                                      | `novaess.cmd.unmute` |
| `/kick <player> [reason]` | Kick a player (notifies staff and console) | —                                      | `novaess.cmd.kick` |
| `/ban <player> <duration\|perm> [reason]` | Ban a player with duration and reason | —                                      | `novaess.cmd.ban` |
| `/unban <player>` | Unban a player | —                                      | `novaess.cmd.unban` |
| `/afk [message]` | Toggle AFK mode with an optional away message | —                                      | `novaess.cmd.afk` |
| `/rename <name>` | Rename the held item (MiniMessage + `&` codes) | —                                      | `novaess.cmd.rename` |
| `/lore <add\|remove\|clear\|insert\|set> [args]` | Edit the lore of the held item | —                                      | `novaess.cmd.lore` |
| `/effect <player> <effect\|clear> [duration] [amplifier]` | Apply a potion effect to a player | —                                      | `novaess.cmd.effect` |
| `/freeze <player>` | Freeze a player in place | —                                      | `novaess.cmd.freeze` |
| `/unfreeze <player>` | Unfreeze a player | —                                      | `novaess.cmd.unfreeze` |
| `/tiny <player> [size\|reset]` | Shrink a player (default 0.5×) | —                                      | `novaess.cmd.tiny` |
| `/giant <player> [size\|reset]` | Enlarge a player (default 2×) | —                                      | `novaess.cmd.giant` |
| `/captcha [player]` | Issue a captcha challenge to a player | —                                      | `novaess.cmd.captcha` / `.captcha.other` |
| `/recaptcha` | Get a new captcha code without resetting failed attempts | —                                      | `novaess.cmd.recaptcha` |
| `/repair [all\|player] [player]` | Repair the held item (or all inventory items) to full durability | `/fix`                                 | `novaess.cmd.repair` / `.repair.other` |

---

### Chat

- **Custom formatter** — fully configurable MiniMessage format with `<player>`, `<display_name>`, `<world>`, `<message>`. PlaceholderAPI tags supported in the format string.
- **Color codes** — players with `novaess.chat.color` can use `&` legacy color codes; players with `novaess.chat.minimessage` can use full MiniMessage tags.
- **[item] placeholder** — typing `[item]` in chat replaces it with a hoverable display of the item in hand.
- **SmallCaps** — converts all outgoing text (chat and command messages like `/msg`) to unicode small capitals. Can be restricted to a permission node.
- **Chat filter** — blocks and replaces configured bad words; optionally notifies online staff silently.
- **Chat pause** — staff can freeze chat for all non-exempt players for a set duration.
- **Mute system** — temporary or permanent mutes persisted in the configured database; respects offline players.

---

### Teleportation

- **TPA / TPAHere** — request-based teleportation with configurable timeout, accept/deny, and block support.
- **TPA Auto-Accept** — players can enable TPAUTO in `/settings` to automatically accept incoming TPA requests (but not TPA-Here).
- **Teleport delay** — configurable warm-up delay before the teleport fires; cancelled if the player moves (optional).
- **Actionbar countdown** — a live countdown is shown in the player's action bar while the teleport delay ticks down.
- **Teleport cooldown** — configurable cooldown between teleports; players are notified how long to wait if they try too soon.
- **Death back** — `/back` returns you to your last death location.

---

### Homes & Warps

- Supports multiple named homes per player with per-permission home limits (e.g. `novaess.homes.5` → 5 homes, `novaess.homes.unlimited` → unlimited).
- Warps are server-wide and managed by staff.
- All data persisted in the configured database (SQLite, MySQL).
- Optional **GUI** for homes (`gui.homes.enabled`) and warps (`gui.warps.enabled`) — paginated clickable chest inventories opened via `/home list` and `/warp list`.

---

### Kits

- Define any number of kits in `config.yml` with custom items, display names, enchantments, lore, and cooldowns.
- Cooldowns are persisted in the configured database; supports one-time-only kits (`cooldown: -1`).
- Per-kit permission nodes.
- Optional **price** per kit — charges via Vault economy when `economy.enabled: true`.
- Optional **GUI** (`gui.kits.enabled: true`) — opens a paginated chest inventory instead of a text list when running `/kit` with no arguments.

---

### Messaging

- `/msg` + `/r` with message spy for staff (`novaess.cmd.msg.spy`).
- Block system prevents messages and TPA requests from blocked players.
- Players can disable private messages entirely via `/settings → Private Messages`.

---

### Join, Leave & Death Messages

- Fully customizable **join** and **leave** messages displayed to all players when someone connects or disconnects.
- **Death messages** — a configurable list of random death messages displayed when a player dies. Multiple entries are defined in `messages/<lang>.yml`; one is chosen at random per death.
- All messages support MiniMessage formatting.

---

### Settings GUI

`/settings` opens a personal 9-slot chest GUI where each player can toggle:

| Setting | Description |
|---|---|
| **TPA Requests** | Allow or block incoming TPA requests |
| **TPA-Here Requests** | Allow or block incoming TPA-Here requests |
| **TPA Auto-Accept** | Automatically accept all TPA requests (not TPA-Here) |
| **Language** | Preferred locale; overrides the client-detected language |
| **GUI Sounds** | Enable or disable sounds in chest GUI menus |
| **Hide Player Chat** | Hide other players' chat messages (does not affect `/msg`) |
| **Private Messages** | Allow or block incoming private messages |
| **Payments** | Allow or block incoming `/pay` payments |
| **Balance Visibility** | Allow or block others from checking your balance with `/balance` |

All setting names and descriptions are fully customizable in the `settings:` section of any message file.

---

### GUI Sounds

When a player has **GUI Sounds** enabled (default on), a configurable sound plays on every click inside any chest GUI (kits, warps, homes, settings). Sounds are configured per action in `sounds.yml`:

```yaml
gui-click:
  sound: "BLOCK_NOTE_BLOCK_PLING"
  volume: 1.0
  pitch: 1.2
```

---

### Freeze & Captcha

- **`/freeze` / `/unfreeze`** — prevents frozen players from moving, interacting, opening inventories, attacking, or running any commands. Freeze state is **persisted in the database** so players remain frozen across server restarts and reconnects.
- **`/captcha [player]`** — issues a map-based captcha challenge. The player is frozen until they type the correct code in chat. Features:
  - Each letter is rendered at 3× scale, in a unique vibrant color, and slightly rotated (±13°).
  - Three background design presets (blue / red / green) chosen per code.
  - Diagonal crossing lines and wavy sinusoidal overlay lines make the code harder to OCR.
  - Three wrong attempts trigger a permanent ban; a full summary (actual code + all failed inputs) is sent to staff and console.
  - **Disconnecting** while a captcha is active results in an immediate permanent ban.
- **`/recaptcha`** — frozen players can request a new code if they cannot read the current one, without losing their remaining attempt count.

---

### Permissions

Every command has a dedicated permission node under `novaess.cmd.*`. A full permission tree is declared in `plugin.yml`:

| Permission | Description | Default |
|---|---|---|
| `novaess.*` | All permissions | op |
| `novaess.cmd.*` | All command permissions | op |
| `novaess.chat.*` | All chat permissions | op |
| `novaess.cmd.gm` / `.gm.other` | Change own / other's gamemode | op |
| `novaess.cmd.msg` / `.msg.spy` | Send private messages / spy | true / op |
| `novaess.cmd.vanish` / `.vanish.other` | Toggle own / other's vanish | op |
| `novaess.vanish.see` | See vanished players | op |
| `novaess.cmd.fly` / `.fly.other` | Toggle own / other's flight | op |
| `novaess.cmd.speed` / `.speed.other` | Set own / other's speed | op |
| `novaess.cmd.enchant` | Enchant items without restrictions | op |
| `novaess.cmd.heal` / `.heal.other` | Heal self / others | op |
| `novaess.cmd.feed` / `.feed.other` | Feed self / others | op |
| `novaess.cmd.god` / `.god.other` | Toggle own / other's god mode | op |
| `novaess.cmd.invsee` / `.invsee.modify` | View / modify inventories | op |
| `novaess.cmd.ec` / `.ec.other` / `.ec.other.modify` | Ender chest (self / other / modify) | true / op |
| `novaess.cmd.workbench` / `.workbench.other` | Virtual crafting table | true / op |
| `novaess.cmd.anvil` / `.anvil.other` | Virtual anvil | true / op |
| `novaess.cmd.sudo` | Execute commands as other players | op |
| `novaess.cmd.tpa` / `.tpaccept` / `.tpdeny` | TPA system | true |
| `novaess.cmd.tpahere` / `.tpall` / `.back` | Teleport commands | true / op |
| `novaess.cmd.block` | Block / unblock players | true |
| `novaess.cmd.home` / `.sethome` / `.delhome` | Home management | true |
| `novaess.cmd.spawn` / `.setspawn` | Spawn commands | true / op |
| `novaess.cmd.warp` / `.setwarp` / `.delwarp` | Warp management | true / op |
| `novaess.cmd.time` | Set world time | op |
| `novaess.cmd.kit` | Use kits | true |
| `novaess.cmd.broadcast` / `.broadcast.minimessage` | Broadcast / with MiniMessage | op |
| `novaess.cmd.chat` | Pause / unpause chat | op |
| `novaess.cmd.mute` / `.unmute` | Mute management | op |
| `novaess.cmd.kick` / `.ban` / `.unban` | Moderation | op |
| `novaess.cmd.afk` | Toggle AFK | true |
| `novaess.cmd.rename` / `.lore` | Item editing | op |
| `novaess.cmd.effect` | Apply potion effects | op |
| `novaess.cmd.freeze` / `.unfreeze` | Freeze players | op |
| `novaess.cmd.tiny` / `.giant` | Resize players | op |
| `novaess.cmd.captcha` / `.captcha.other` | Issue captcha (self / others) | op |
| `novaess.cmd.recaptcha` | Request a new captcha code | true |
| `novaess.cmd.repair` / `.repair.other` | Repair items (self / others) | op |
| `novaess.cmd.settings` | Open personal settings GUI | true |
| `novaess.chat.color` / `.minimessage` | Color codes / MiniMessage in chat | op |
| `novaess.chat.bypass-filter` / `.bypass-pause` / `.bypass-mute` | Bypass chat restrictions | op |
| `novaess.homes.2` / `.5` / `.10` / `.unlimited` | Home limit tiers | false / op |

Permission nodes can be **renamed** in `config.yml` under the `permissions:` section:

```yaml
permissions:
  cmd.gm: "mycoolplugin.gamemode"
  cmd.fly: "mycoolplugin.fly"
```

---

### Configuration

Everything is configurable in `config.yml`:

- Enable/disable individual commands (`commands.disabled`)
- Rename any permission node (`permissions.*`)
- Chat formatter format string, item-placeholder format, SmallCaps toggle, filter word list
- TPA timeout, teleport delay, cancel-on-move
- Home limits per permission node, default home name
- Spawn on join / spawn on death
- AFK cancel-on-move
- Economy integration toggle (`economy.enabled`)
- GUI toggle for kits, warps, and homes (`gui.kits.enabled`, `gui.warps.enabled`, `gui.homes.enabled`)
- Full kit definitions (items, enchantments, cooldowns, price, permissions)
- **Database backend** (`database.type: sqlite | mysql | redis`) and per-backend connection settings

---

### Messages & Localization

All messages live in `plugins/NovaEssentials/messages/<lang>.yml` and are written in **MiniMessage** format. The plugin ships `en.yml` as the default.

**Automatic language selection** — the plugin reads each player's Minecraft client language (`player.locale()`) and picks the matching YAML file automatically (e.g. a German client gets `de.yml` or `de_de.yml`). If no matching file exists, it falls back to the configured default.

SmallCaps is applied to every outgoing component (including `/msg` and all command feedback) when enabled.

---

### Discord Webhooks

Optional integration that posts embed messages to a Discord channel via a webhook URL.

Configure in `plugins/NovaEssentials/webhooks.yml`:

```yaml
enabled: true
webhook-url: "https://discord.com/api/webhooks/..."
events:
  ban: true
  unban: true
  kick: true
  captcha-created: true
  captcha-failed: true
```

Events that trigger a webhook embed:

| Event | Color | Fields |
|---|---|---|
| Player banned | Red | Player, Reason, Duration, Banned by |
| Player unbanned | Green | Player, Unbanned by |
| Player kicked | Orange | Player, Reason, Kicked by |
| Captcha issued | Blue | Player, Issued by |
| Captcha failed (ban) | Dark red | Player, Actual code, All failed attempts |
| Captcha dodged (disconnect ban) | Dark red | Player, Actual code, Reason |

---

### PlaceholderAPI

Soft dependency. When PlaceholderAPI is installed, the following placeholders are available:

| Placeholder | Description |
|---|---|
| `%novaess_is_vanished%` | `true` / `false` — requesting player is vanished |
| `%novaess_is_god%` | `true` / `false` — requesting player has god mode |
| `%novaess_is_flying%` | `true` / `false` — requesting player has plugin fly |
| `%novaess_is_muted%` | `true` / `false` — requesting player is muted |
| `%novaess_home_count%` | Number of homes the requesting player has |
| `%novaess_home_limit%` | Max homes allowed (∞ if unlimited) |
| `%novaess_visible_players%` | Count of online players visible to the requesting player |
| `%novaess_is_afk_<player>%` | `true` / `false` — named player is AFK |
| `%novaess_afk_message_<player>%` | AFK message of the named player (empty if none) |
| `%novaess_blocked_players_<player>%` | Number of players the named player has blocked |
| `%novaess_tpa_requests_<player>%` | `true` / `false` — named player has a pending incoming TPA request |
| `%novaess_mute_status_<player>%` | `muted` / `unmuted` — mute status of named player |
| `%novaess_god_mode_<player>%` | `true` / `false` — god mode status of named player |
| `%novaess_vanish_status_<player>%` | `true` / `false` — vanish status of named player |

---

### Database

Three storage backends are supported, selectable via `database.type` in `config.yml`:

| Backend | Default | Notes |
|---|---|---|
| `sqlite` | Yes | Zero-config; stores data in `plugins/NovaEssentials/db/*.db` |
| `mysql` | No | Requires a MySQL 8+ server; all tables created automatically |
| `redis` | No | Requires a Redis server; all data stored as Redis keys |

> **Please note:** Redis and MySQL aren't tested! Use at your own risk and make backups before switching!

If MySQL or Redis fails to connect on startup the plugin falls back to SQLite and logs a warning.

#### SQLite files (when `type: sqlite`)

| File | Contents |
|---|---|
| `homes.db` | Player homes |
| `warps.db` | Server warps |
| `blocks.db` | Blocked-player pairs |
| `mutes.db` | Mutes (temporary and permanent, with expiry timestamps) |
| `kits.db` | Kit cooldowns |
| `spawn.db` | Spawn location |
| `frozen.db` | Frozen players (persists across restarts) |
| `settings.db` | Per-player settings (TPA toggles, language preference, sounds, etc.) |

Data is loaded into memory on player join and flushed on quit for performance.

#### MySQL (when `type: mysql`)

```yaml
database:
  type: mysql
  mysql:
    host: localhost
    port: 3306
    database: novaessentials
    username: root
    password: ""
    pool-size: 10
```

#### Redis (when `type: redis`)

All data is stored directly in Redis using structured keys under the `novaess:` prefix (e.g. `novaess:homes:<uuid>`, `novaess:warps`, `novaess:frozen`). Player data maps and cooldowns are stored as JSON strings; block lists and the frozen set use native Redis Sets.

```yaml
database:
  type: redis
  redis:
    host: localhost
    port: 6379
    password: ""
    database: 0
```

---

### Player Utilities

- **`/rename`** — renames the held item; supports full MiniMessage and `&` legacy color codes.
- **`/lore`** — add, remove, clear, insert, or set individual lore lines with MiniMessage support.
- **`/effect`** — apply any potion effect by name with custom duration and amplifier; `/effect <player> clear` removes all effects.
- **`/repair [all]`** — restores the held item (or every damageable item in the inventory with `all`) to full durability. Also works on other players with `novaess.cmd.repair.other`.
- **`/tiny` / `/giant`** — resize players using the `generic.scale` attribute (range 0.0625–16); `reset` restores normal size.

---

## Dependencies

| Dependency | Type | Purpose |
|---|---|---|
| Paper 1.21+ | Required | Server API |
| PlaceholderAPI | Optional (soft) | PAPI placeholder support |
| Vault | Optional (soft) | Economy integration for kit prices |
| MySQL 8+ | Optional | Required only when `database.type: mysql` |
| Redis | Optional | Required only when `database.type: redis` |

---

## Installation

1. Drop `NovaEssentials-<version>.jar` into your `plugins/` folder.
2. Restart the server — `plugins/NovaEssentials/config.yml`, `messages/en.yml`, and `webhooks.yml` are generated automatically.
3. Edit `config.yml` to configure commands, chat, homes, kits, TP cooldowns, and more.
4. *(Optional)* Switch the database backend — set `database.type` to `mysql` or `redis` and fill in the connection details under `database.mysql` or `database.redis`.
6. Edit `sounds.yml` to customize the sounds that play in GUI menus.
7. To enable Discord webhooks, edit `webhooks.yml` and set `enabled: true` with your webhook URL.
8. To add another language, create `plugins/NovaEssentials/messages/<lang>.yml` (e.g. `de.yml`) using `en.yml` as a template.

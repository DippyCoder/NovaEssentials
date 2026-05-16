# NovaEssentials

A modern essentials plugin for **Paper 1.21+** servers, inspired by **[EssentialsX](https://github.com/EssentialsX/Essentials)**, featuring **60+ commands**, a **Ban-System**, **Chat-Formatting**, **Discord Integration**, **[PAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) & [Vault](https://github.com/milkbowl/Vault) support** and **fully customizable messages** in different languages.

---

## Features

### Commands

| Command | Description | Aliases | Permission |
|---|---|---|---|
| `/gamemode <mode> [player]` | Change own or another player's game mode | `/gm` | `novaess.cmd.gm` / `.gm.other` |
| `/msg <player> <message>` | Send a private message | `/tell`, `/whisper`, `/w` | `novaess.cmd.msg` |
| `/r <message>` | Reply to the last private message | `/reply` | `novaess.cmd.msg` |
| `/vanish [player]` | Toggle vanish mode | `/v` | `novaess.cmd.vanish` / `.vanish.other` |
| `/fly [player]` | Toggle flight mode | — | `novaess.cmd.fly` / `.fly.other` |
| `/speed <0-10> [player]` | Set movement or fly speed | — | `novaess.cmd.speed` / `.speed.other` |
| `/enchant <enchantment> [level]` | Enchant item in hand without vanilla restrictions | `/ench` | `novaess.cmd.enchant` |
| `/heal [player]` | Restore health to full | — | `novaess.cmd.heal` / `.heal.other` |
| `/feed [player]` | Restore hunger to full | — | `novaess.cmd.feed` / `.feed.other` |
| `/god [player]` | Toggle god mode (invulnerable to damage) | — | `novaess.cmd.god` / `.god.other` |
| `/invsee <player>` | View (and optionally modify) another player's inventory | — | `novaess.cmd.invsee` / `.invsee.modify` |
| `/ec [player]` | Open an ender chest | `/enderchest` | `novaess.cmd.ec` / `.ec.other` / `.ec.other.modify` |
| `/workbench [player]` | Open a virtual crafting table | `/craft`, `/wb` | `novaess.cmd.workbench` / `.workbench.other` |
| `/anvil [player]` | Open a virtual anvil | `/anv` | `novaess.cmd.anvil` / `.anvil.other` |
| `/sudo <player> <message\|/cmd>` | Execute a command or message as another player | — | `novaess.cmd.sudo` |
| `/tpa <player>` | Send a teleport request to a player | — | `novaess.cmd.tpa` |
| `/tpaccept` | Accept a pending teleport request | — | `novaess.cmd.tpaccept` |
| `/tpdeny` | Deny a pending teleport request | — | `novaess.cmd.tpdeny` |
| `/tpahere <player>` | Request a player to teleport to you | `/tphere` | `novaess.cmd.tpahere` |
| `/tpall` | Teleport all players to your location | — | `novaess.cmd.tpall` |
| `/back` | Teleport to your last death location | — | `novaess.cmd.back` |
| `/block <player>` | Block / unblock a player from messaging and TPA | — | `novaess.cmd.block` |
| `/home [name\|list]` | Teleport to a home, or open the home GUI / list | — | `novaess.cmd.home` |
| `/sethome [name]` | Set a home at your current location | — | `novaess.cmd.sethome` |
| `/delhome <name>` | Delete a home | — | `novaess.cmd.delhome` |
| `/spawn` | Teleport to the server spawn | — | `novaess.cmd.spawn` |
| `/setspawn` | Set the server spawn location | — | `novaess.cmd.setspawn` |
| `/warp <name\|list>` | Teleport to a warp, or open the warp GUI / list | — | `novaess.cmd.warp` |
| `/setwarp <name>` | Create a warp at your location | — | `novaess.cmd.setwarp` |
| `/delwarp <name>` | Delete a warp | — | `novaess.cmd.delwarp` |
| `/time <day\|night\|noon\|midnight\|value>` | Set the world time | `/day`, `/night`, `/noon`, `/midnight` | `novaess.cmd.time` |
| `/kit [name]` | Receive a kit, or open the kit GUI / list | — | `novaess.cmd.kit` |
| `/broadcast <message>` | Broadcast a formatted message to all players | `/bc` | `novaess.cmd.broadcast` |
| `/chat <pause\|unpause> [duration]` | Pause or unpause the chat | — | `novaess.cmd.chat` |
| `/mute <player> [duration] [reason]` | Mute a player (notifies staff and console) | — | `novaess.cmd.mute` |
| `/unmute <player>` | Unmute a player (notifies staff and console) | — | `novaess.cmd.unmute` |
| `/kick <player> [reason]` | Kick a player (notifies staff and console) | — | `novaess.cmd.kick` |
| `/ban <player> <duration\|perm> [reason]` | Ban a player with duration and reason | — | `novaess.cmd.ban` |
| `/unban <player>` | Unban a player | — | `novaess.cmd.unban` |
| `/ipban <player\|ip> <duration\|perm> [reason]` | IP ban a player or address | — | `novaess.cmd.ipban` |
| `/afk [message]` | Toggle AFK mode with an optional away message | — | `novaess.cmd.afk` |
| `/rename <name>` | Rename the held item (MiniMessage + `&` codes) | — | `novaess.cmd.rename` |
| `/lore <add\|remove\|clear\|insert\|set> [args]` | Edit the lore of the held item | — | `novaess.cmd.lore` |
| `/effect <player> <effect\|clear> [duration] [amplifier]` | Apply a potion effect to a player | — | `novaess.cmd.effect` |
| `/freeze <player>` | Freeze a player in place | — | `novaess.cmd.freeze` |
| `/unfreeze <player>` | Unfreeze a player | — | `novaess.cmd.unfreeze` |
| `/tiny <player> [size\|reset]` | Shrink a player (default 0.5×) | — | `novaess.cmd.tiny` |
| `/giant <player> [size\|reset]` | Enlarge a player (default 2×) | — | `novaess.cmd.giant` |
| `/captcha [player]` | Issue a captcha challenge to a player | — | `novaess.cmd.captcha` / `.captcha.other` |
| `/recaptcha` | Get a new captcha code without resetting failed attempts | — | `novaess.cmd.recaptcha` |
| `/repair [all\|player] [player]` | Repair the held item (or all inventory items) to full durability | `/fix` | `novaess.cmd.repair` / `.repair.other` |
| `/summon <entity> [x y z] [amount]` | Summon entities at your position or a specific location | — | `novaess.cmd.summon` |
| `/strike [player]` | Strike a player with lightning | — | `novaess.cmd.strike` |
| `/burn [player] [duration]` | Set a player on fire with a fire trail | — | `novaess.cmd.burn` |
| `/nuke` | Spawn a large amount of TNT in the sky | — | `novaess.cmd.nuke` |
| `/fnuke` | Like `/nuke` but TNT does not destroy blocks | — | `novaess.cmd.fnuke` |
| `/find <player>` | Show a player's current world and coordinates | — | `novaess.cmd.find` |
| `/givehead [player]` | Give yourself a player skull | — | `novaess.cmd.givehead` |
| `/book` | Convert a signed book to a writable book | — | `novaess.cmd.book` |
| `/playtime` | View your total playtime on the server | — | `novaess.cmd.playtime` |
| `/sun` | Set the weather to clear | `/sunny` | `novaess.cmd.weather` |
| `/rain` | Set the weather to rain | `/rainy` | `novaess.cmd.weather` |
| `/thunder` | Set the weather to thunderstorm | — | `novaess.cmd.weather` |
| `/novaess [version\|reload\|update\|check-for-updates\|help] [page]` | Plugin management — version info, config reload, update check, help | `/nova` | `novaess.cmd.novaess` |

---

### Chat

- **Custom formatter** — fully configurable MiniMessage format with `<player>`, `<display_name>`, `<world>`, `<message>`. PlaceholderAPI tags supported in the format string.
- **Color codes** — players with `novaess.chat.color` can use `&` legacy color codes; players with `novaess.chat.minimessage` can use full MiniMessage tags.
- **[item] placeholder** — typing `[item]` in chat replaces it with a hoverable display of the item in hand.
- **SmallCaps** — converts all outgoing text (chat and command messages like `/msg`) to unicode small capitals. Can be restricted to a permission node.
- **Chat filter** — blocks and replaces configured bad words; optionally notifies online staff silently.
- **Chat pause** — staff can freeze chat for all non-exempt players for a set duration.
- **Mute system** — temporary or permanent mutes stored in SQLite; respects offline players.

---

### Teleportation

- **TPA / TPAHere** — request-based teleportation with configurable timeout, accept/deny, and block support.
- **Teleport delay** — configurable warm-up delay before the teleport fires; cancelled if the player moves (optional).
- **Death back** — `/back` returns you to your last death location.

---

### Homes & Warps

- Supports multiple named homes per player with per-permission home limits (e.g. `novaess.homes.5` → 5 homes, `novaess.homes.unlimited` → unlimited).
- Warps are server-wide and managed by staff.
- All data persisted in SQLite.
- Optional **GUI** for homes (`gui.homes.enabled`) and warps (`gui.warps.enabled`) — paginated clickable chest inventories opened via `/home list` and `/warp list`.

---

### Kits

- Define any number of kits in `config.yml` with custom items, display names, enchantments, lore, and cooldowns.
- Cooldowns are persisted in SQLite; supports one-time-only kits (`cooldown: -1`).
- Per-kit permission nodes.
- Optional **price** per kit — charges via Vault economy when `economy.enabled: true`.
- Optional **GUI** (`gui.kits.enabled: true`) — opens a paginated chest inventory instead of a text list when running `/kit` with no arguments.

---

### Messaging

- `/msg` + `/r` with message spy for staff (`novaess.cmd.msg.spy`).
- Block system prevents messages and TPA requests from blocked players.

---

### Freeze & Captcha

- **`/freeze` / `/unfreeze`** — prevents frozen players from moving, interacting, opening inventories, attacking, or running any commands. Freeze state is **persisted in SQLite** so players remain frozen across server restarts and reconnects.
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
| `novaess.cmd.ipban` | IP ban players or addresses | op |
| `novaess.cmd.afk` | Toggle AFK | true |
| `novaess.cmd.rename` / `.lore` | Item editing | op |
| `novaess.cmd.effect` | Apply potion effects | op |
| `novaess.cmd.freeze` / `.unfreeze` | Freeze players | op |
| `novaess.cmd.tiny` / `.giant` | Resize players | op |
| `novaess.cmd.captcha` / `.captcha.other` | Issue captcha (self / others) | op |
| `novaess.cmd.recaptcha` | Request a new captcha code | true |
| `novaess.cmd.repair` / `.repair.other` | Repair items (self / others) | op |
| `novaess.cmd.summon` | Summon entities | op |
| `novaess.cmd.strike` | Strike players with lightning | op |
| `novaess.cmd.burn` | Set players on fire | op |
| `novaess.cmd.nuke` / `.fnuke` | TNT nuke / block-safe nuke | op |
| `novaess.cmd.find` | Find a player's location | op |
| `novaess.cmd.givehead` | Give player skulls | op |
| `novaess.cmd.book` | Convert signed books to writable books | op |
| `novaess.cmd.playtime` | View playtime | true |
| `novaess.cmd.weather` | Change weather (sun / rain / thunder) | op |
| `novaess.cmd.novaess` | Plugin management command | op |
| `novaess.chat.color` / `.minimessage` | Color codes / MiniMessage in chat | op |
| `novaess.chat.bypass-filter` / `.bypass-pause` / `.bypass-mute` | Bypass chat restrictions | op |
| `novaess.homes.2` / `.5` / `.10` / `.unlimited` | Home limit tiers | false / op |
| `novaess.update-notify` | Receive an in-game notification on join when an update is available | op |

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

---

### Messages & Localization

All messages live in `plugins/NovaEssentials/messages/<lang>.yml` and are written in **MiniMessage** format. The plugin ships the following language files out of the box:

| Code | Language |
|---|---|
| `en.yml` | English (default) |
| `de.yml` | German |
| `fr.yml` | French |
| `es.yml` | Spanish |
| `it.yml` | Italian |
| `nl.yml` | Dutch |
| `pl.yml` | Polish |
| `cs.yml` | Czech |
| `ru.yml` | Russian |
| `tr.yml` | Turkish |
| `pt_br.yml` | Portuguese (Brazil) |

**Automatic language selection** — the plugin reads each player's Minecraft client language (`player.locale()`) and picks the matching YAML file automatically (e.g. a German client gets `de.yml`). If no matching file exists, it falls back to the configured default.

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

SQLite is used for persistent storage. Each data type lives in its own file under `plugins/NovaEssentials/db/`:

| File | Contents |
|---|---|
| `homes.db` | Player homes |
| `warps.db` | Server warps |
| `blocks.db` | Blocked-player pairs |
| `mutes.db` | Mutes (temporary and permanent, with expiry timestamps) |
| `kits.db` | Kit cooldowns |
| `spawn.db` | Spawn location |
| `frozen.db` | Frozen players (persists across restarts) |
| `playtime.db` | Player playtime (milliseconds) |

Data is loaded into memory on player join and flushed on quit for performance.

---

### Player Utilities

- **`/rename`** — renames the held item; supports full MiniMessage and `&` legacy color codes.
- **`/lore`** — add, remove, clear, insert, or set individual lore lines with MiniMessage support.
- **`/effect`** — apply any potion effect by name with custom duration and amplifier; `/effect <player> clear` removes all effects.
- **`/repair [all]`** — restores the held item (or every damageable item in the inventory with `all`) to full durability. Also works on other players with `novaess.cmd.repair.other`.
- **`/tiny` / `/giant`** — resize players using the `generic.scale` attribute (range 0.0625–16); `reset` restores normal size.
- **`/book`** — converts a signed book back to a writable book so it can be edited again.
- **`/givehead [player]`** — gives the sender a skull with the skin of the specified player (or themselves).
- **`/playtime`** — displays the total time the player has spent on the server, tracked in SQLite.

---

## Dependencies

| Dependency | Type | Purpose |
|---|---|---|
| Paper 1.21+ | Required | Server API |
| PlaceholderAPI | Optional (soft) | PAPI placeholder support |
| Vault | Optional (soft) | Economy integration for kit prices |

---

## Installation

1. Drop `NovaEssentials-<version>.jar` into your `plugins/` folder.
2. Restart the server — `plugins/NovaEssentials/config.yml`, `messages/en.yml`, and `webhooks.yml` are generated automatically.
3. Edit `config.yml` to configure commands, chat, homes, kits, and more.
4. To enable Discord webhooks, edit `webhooks.yml` and set `enabled: true` with your webhook URL.
5. To add another language, create `plugins/NovaEssentials/messages/<lang>.yml` (e.g. `de.yml`) using `en.yml` as a template.

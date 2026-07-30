# HotBaaaar — 超长快捷栏（纯客户端 fork）/ Super long hotbar (client-only fork)

[中文](#中文) · [English](#english)

> 本仓库是 [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar) 的 fork，改造为**纯客户端**并扩展为多版本/多加载器矩阵。
> This repository is a fork of [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar), reworked to be **client-only** and expanded into a multi-version / multi-loader matrix.

---

## 中文

**HotBaaaar** 把快捷栏扩展成最多 4 行（你的整个 36 格背包），是一个 **纯客户端** mod。

用滚轮滚到当前行的边缘时会自动「翻行」——把那一整行通过容器点击换进真实快捷栏（第 0–8 格）。这是原版服务器本来就接受的操作，所以 **服务器无需安装本 mod**，你可以在任意原版 / 无 mod 的联机服上使用。

- 打开**任意 GUI** 时会自动把玩家物品栏**物理复位成正常顺序**；不依赖 E、ESC、按键绑定、界面类或容器类型；
- GUI 之间切换不会重复交换，回到 HUD 后才重新应用之前的活动行，**手持物品保持不变**；
- GUI 打开期间不会在后台响应滚轮翻行。

容器兼容按能力判断：只要当前菜单暴露真实的玩家 `Inventory` 槽位并保留原版 SWAP
语义，原版箱子、右键打开的容器和 mod 自定义背包都无需专门适配。若菜单隐藏玩家主物品栏、
只暴露无法确认的代理槽，或改写 SWAP 语义，客户端不会猜测槽位或发送部分点击，而会保持原映射并逐 tick 重试。

### 支持的版本与加载器

本仓库用**分支矩阵**管理：每个 `mc/<版本>-<加载器>` 分支都是一个独立可构建的工程，`master` 只作为放置 CI / 文档的 hub。

|  | 1.16.5 | 1.18.2 | 1.19.2 | 1.20.1 | 1.21.1 | 1.21.11 | 26.1.2 |
|--|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| **Forge** | ✅ | ✅ | ✅ | ✅ | ✅ | — | — |
| **NeoForge** | — | — | — | ✅ | ✅ | ✅ | ✅ |
| **Fabric** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

> ✅ 已构建；— 本仓库不提供该组合。当前共 16 个可构建目标。26.1.2 Fabric 使用非重映射 Loom、Java 25，且不依赖 Fabric API。

下载见 [Releases](../../releases)；构建说明与如何新增目标见 [BUILDING.md](BUILDING.md)。

### 与原版（fork 前）的区别

原版 [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar) 是 **NeoForge 的两端 mod**：服务端也必须安装，通过网络包把每个玩家的屏幕宽度同步给服务端，从而决定快捷栏格数。

本 fork：
- **纯客户端**——用「翻行」技巧绕过服务端对「手持哪一格」的校验，**服务器不用装**；
- 任意 GUI 打开时安全归位，并从活动菜单动态解析真实玩家槽位；
- 扩展为 **Forge / NeoForge / Fabric × 1.16.5 / 1.18.2 / 1.19.2 / 1.20.1 / 1.21.1 / 1.21.11 / 26.1.2** 的 16 目标矩阵。

正常退出前，暂停界面会触发归位；意外断线后客户端已无法再向服务器发送点击，因此无法保证最后一次归位。

### 致谢

感谢原作者 **USS_Shenzhou** 的创意与原版实现 ❤️。

### 许可

GPLv3，详见 [LICENSE](LICENSE)。

---

## English

**HotBaaaar** extends the hotbar to up to 4 rows (your whole 36-slot inventory). It is a **client-only** mod.

Scrolling past the edge of the current row auto-"flips" rows: that whole row is swapped into the real hotbar (slots 0–8) via container clicks — an operation vanilla servers already accept. So **the server does not need this mod**, and you can use it on any vanilla / unmodded multiplayer server.

- Opening **any GUI** physically **restores the normal player-inventory order**. This does not depend on E, ESC, key bindings, screen classes, or container types;
- Switching between GUIs does not repeat the swap. The saved active row is re-applied only after returning to the HUD, so the **held item is preserved**;
- Background wheel row-flips are disabled while a GUI is open.

Container compatibility is capability-based: vanilla containers, right-click-opened menus, and modded
backpacks work without special cases when the active menu exposes real player `Inventory` slots and
keeps vanilla SWAP semantics. If a menu hides those slots, exposes only unverifiable proxies, or
changes SWAP semantics, the client sends no partial clicks, preserves its mapping, and retries each tick.

### Supported versions & loaders

Managed as a **branch matrix**: every `mc/<version>-<loader>` branch is a standalone buildable project, while `master` is just a hub holding the CI / docs.

|  | 1.16.5 | 1.18.2 | 1.19.2 | 1.20.1 | 1.21.1 | 1.21.11 | 26.1.2 |
|--|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| **Forge** | ✅ | ✅ | ✅ | ✅ | ✅ | — | — |
| **NeoForge** | — | — | — | ✅ | ✅ | ✅ | ✅ |
| **Fabric** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

> ✅ built; — combination not provided by this repository. There are 16 buildable targets. The 26.1.2 Fabric target uses non-remapping Loom and Java 25, with no Fabric API dependency.

Downloads: [Releases](../../releases). Building / adding targets: [BUILDING.md](BUILDING.md).

### Difference from the upstream (pre-fork) original

The original [USS-Shenzhou/HotBaaar](https://github.com/USS-Shenzhou/HotBaaar) is a **both-sides NeoForge mod**: the server must also install it, and it syncs each player's screen width to the server to decide the hotbar size.

This fork:
- is **client-only** — it uses the row-flip trick to work around the server's held-slot validation, so **no server install is needed**;
- safely restores on any GUI and resolves real player slots dynamically from the active menu;
- expands into a 16-target **Forge / NeoForge / Fabric × 1.16.5 / 1.18.2 / 1.19.2 / 1.20.1 / 1.21.1 / 1.21.11 / 26.1.2** matrix.

A normal quit opens a pause screen and restores first. After an abrupt disconnect the client can no
longer send inventory clicks, so a final restore cannot be guaranteed.

### Credits

Thanks to the original author **USS_Shenzhou** for the original idea and implementation ❤️.

### License

GPLv3 — see [LICENSE](LICENSE).

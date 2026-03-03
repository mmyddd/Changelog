# CTNHChangelog

A Minecraft mod that displays changelog information and checks for updates.

## Configuration / 配置说明

### English

The configuration file is located at `config/ctnhchangelog-client.toml`.

| Field | Description |
|-------|-------------|
| `changelogUrl` | URL of the remote JSON changelog file (e.g., `https://example.com/changelog.json`). |
| `enableChangelogTab` | If `true`, displays the changelog tab in the "Create New World" menu. |
| `ModpackVersion` | Current modpack version. Used to compare with the remote version for update checks. |
| `enableVersionCheck` | If `true`, compares `ModpackVersion` with the latest version in the remote file.|
| `buttonLocation` | Button display location. Options: `BOTH` (show on both Title Screen and Select World Screen), `TITLE_SCREEN` (only Title Screen), `SELECT_WORLD` (only Select World Screen). |

### 中文

配置文件位于 `config/ctnhchangelog-client.toml`。

| 字段 | 说明 |
|------|------|
| `changelogUrl` | 远程 JSON 更新日志文件的 URL（例如：`https://example.com/changelog.json`）。 |
| `enableChangelogTab` | 设为 `true` 时，在"创建新的世界"菜单中显示更新日志标签页。 |
| `ModpackVersion` | 当前整合包版本号。用于与远程最新版本对比，检测更新。 |
| `enableVersionCheck` | 设为 `true` 时，会自动对比本地与远程版本。|
| `buttonLocation` | 按钮显示位置。可选值：`BOTH`（标题界面和选择世界界面都显示）、`TITLE_SCREEN`（仅标题界面）、`SELECT_WORLD`（仅选择世界界面）。 |

---

## Changelog JSON Format / 更新日志 JSON 格式

### Field Description

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `footer` | String | No | Gradient text rendered at the bottom of the tab page |
| `tagColors` | Object | No | Custom tag colors, supports `0xAARRGGBB` format (e.g., `0xFFFF5555`) or `#RRGGBB` format |
| `entries` | Array | Yes | Collection of changelog entries |

**Entry Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `version` | String | Yes | Version identifier |
| `date` | String | No | Release date (ISO 8601 format) |
| `title` | String | No | Version title/name |
| `type` | String/Array | No | Update type enumeration, affects left side icon and default tags; Available values: major/minor/patch/hotfix/danger, The type comes with its own color and cannot be modified |
| `tags` | String/Array | No | Custom tags, used in conjunction with tagColors |
| `color` | String | No | Left border color of the entry, supports `0xAARRGGBB` or `#RRGGBB` format |
| `changes` | Array | Yes | List of change details, each item is a single text entry |

### 字段说明

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| `footer` | String | 否 | 标签页底部渲染的渐变色文本 |
| `tagColors` | Object | 否 | 自定义标签颜色，支持 `0xAARRGGBB` 格式（如 `0xFFFF5555`）或 `#RRGGBB` 格式 |
| `entries` | Array | 是 | 更新日志条目集合 |

**Entry 字段：**

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| `version` | String | 是 | 版本标识符 |
| `date` | String | 否 | 发布日期（ISO 8601 格式） |
| `title` | String | 否 | 版本标题/名称 |
| `type` | String/Array | 否 | 更新类型枚举，影响左侧图标及默认标签；可选值：major/minor/patch/hotfix/danger，type自带颜色且无法修改 |
| `tags` | String/Array | 否 | 自定义标签，与 tagColors 配合使用 |
| `color` | String | 否 | 条目左侧边框颜色，支持 `0xAARRGGBB` 或 `#RRGGBB` 格式 |
| `changes` | Array | 是 | 变更明细列表，每项为单条文本 |



Example: [changelog.json](src/main/resources/changelog.json)
A Useful Tool [CTNHChangelog](https://mmyddd.github.io/CTNHChangelog/)


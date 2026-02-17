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
| `type` | String/Array | No | Update type enumeration, affects left side icon and default tags; Available values: major/minor/patch/hotfix/danger,The type comes with its own color and cannot be modified |
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
| `type` | String/Array | 否 | 更新类型枚举，影响左侧图标及默认标签；可选值：major/minor/patch/hotfix/danger，type自带颜色且无法修改|
| `tags` | String/Array | 否 | 自定义标签，与 tagColors 配合使用 |
| `color` | String | 否 | 条目左侧边框颜色，支持 `0xAARRGGBB` 或 `#RRGGBB` 格式 |

| `changes` | Array | 是 | 变更明细列表，每项为单条文本 |


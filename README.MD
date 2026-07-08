# Create: Webdisc

**机械动力：网络唱片** — NeoForge 1.21.1 附属模组，在 Create 中播放网络音频。

| | |
|---|---|
| **Mod ID** | `create_webdisc` |
| **版本** | 2.0.0 |
| **维护者** | [qwer854645](https://github.com/qwer854645) |
| **仓库** | [github.com/qwer854645/create_harmonics_unofficial](https://github.com/qwer854645/create_harmonics_unofficial) |
| **问题反馈** | [Issues](https://github.com/qwer854645/create_webdisc/issues) |

> **这不是官版 [Create: Harmonics](https://github.com/bitmochibit/createharmonics)。**  
> 本项目是独立二次创作：新 Mod ID、新名称、替换美术，与上游作者及品牌无隶属关系。

---

## 版权审计结论

已对仓库进行逐项检查，结论如下：

| 检查项 | 结果 |
|--------|------|
| 上游 ARR 美术（PNG / OGG / BBMODEL） | **未发现** |
| `assets/createharmonics/` 目录 | **不存在** |
| 上游 `docs/` 网站资源 | **不存在** |
| 上游 JAR / class 二进制 | **未提交** |
| 玩法代码（MIT） | 源自 [bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics)，保留 MIT 归属 |
| 本仓库美术 | `assets/create_webdisc/` 为原创或脚本生成，MIT 许可 |

**结论：仓库中不含上游 All Rights Reserved 美术文件。** 可再分发的内容为 MIT 代码与本项目替换资源。完整条款见 [LICENSE.MD](LICENSE.MD)。

---

## 与官版的区别

| | 官版 Create: Harmonics | 本仓库 Create: Webdisc |
|---|---|---|
| Mod ID | `createharmonics` | `create_webdisc` |
| 美术许可 | All Rights Reserved | MIT（替换资源） |
| 代码许可 | MIT | MIT（fork 修改） |
| 黄铜网络唱片机 | 官版有 ARR 美术 | 本 fork 使用原创贴图 + 侧面进度 GUI |
| JAR 可互换？ | — | **否**（方块/物品 ID 不同） |

请勿与官版 JAR 混用；旧存档中的 `createharmonics:*` 方块不会自动迁移。

---

## 功能简介

- 制作 **网络唱片（Webdisc）** 并绑定在线音频 URL
- **安山网络唱片机** — 放置唱片、切换播放模式
- **黄铜网络唱片机** — 在安山版基础上增加侧面进度条 GUI，支持实时时长、精确跳转、从头播放
- **网络唱片压制机（Webdisc Imprinter）** — 用 Create 动力压制唱片
- 音频在客户端从源站流式播放；可与 Create 机关联动，转速影响音高

> 请遵守各音频平台的服务条款。本模组不托管、不代理音频内容。

---

## 依赖

- Minecraft **1.21.1**
- **NeoForge** 21.1+
- [Create](https://modrinth.com/mod/create) 6.0.9+
- [Kotlin for Forge](https://modrinth.com/mod/kotlin-for-forge) 5.10+

---

## 安装

1. 将 `create_webdisc-neoforge-1.21.1-*.jar` 放入 `mods` 文件夹。
2. 首次启动时按提示安装 **ffmpeg** / **yt-dlp**，或手动放到 `audio_providers/`。
3. 若使用 Bilibili 链接，可运行 `tools/repatch-ytdlp-bilibili.ps1` 打补丁。

---

## 从源码构建

**环境：** JDK 21

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
cd create_harmonics_unofficial
.\gradlew.bat :neoforge:build
```

输出：`neoforge/build/libs/create_webdisc-neoforge-1.21.1-2.0.0.jar`

开发客户端：

```powershell
.\gradlew.bat :neoforge:runClient
```

### 生成美术资源（构建前必须执行）

仓库默认**不包含**贴图与音效二进制，需本地生成：

```powershell
py -3 -m pip install pillow
py -3 tools/generate_assets.py
py -3 tools/generate_ponder_nbt.py
py -3 tools/patch_gametest_nbt.py
```

生成目录：`common/src/main/resources/assets/create_webdisc/`

---

## 许可与归属

| 部分 | 许可 | 版权方 |
|------|------|--------|
| 上游玩法代码 | MIT | Leonardo Importuna（[bitmochibit/createharmonics](https://github.com/bitmochibit/createharmonics)） |
| 本 fork 代码修改 | MIT | qwer854645 |
| 本 fork 美术资源 | MIT | qwer854645 |
| 上游原始美术 | All Rights Reserved | **未包含于本仓库** |

MIT 要求在所有副本中保留版权声明；发布 JAR 已打包 `META-INF/LICENSE.MD`。

### 第三方运行时组件

- [Create](https://github.com/Creators-of-Create/Create) — MIT（运行时贴图引用，不随本 JAR 分发）
- [yt-dlp](https://github.com/yt-dlp/yt-dlp) — Unlicense（官方构建可能为 GPLv3+）
- [ffmpeg](https://ffmpeg.org/) — 依构建版本为 LGPL/GPL
- `org.tukaani:xz` — 公共领域（已 shade 进 JAR）

详见 [LICENSE.MD](LICENSE.MD) 与 [NOTICE.MD](NOTICE.MD)。

---

## 致谢

- **上游代码（MIT）：** Leonardo Importuna / mochibit — Create: Harmonics
- **本二次创作：** qwer854645 — Create: Webdisc
- **社区翻译：** de_de、ru_ru、sv_se、pl_pl、ja_jp、it_it 等语言文件部分沿用上游社区贡献

---

## English summary

**Create: Webdisc** is an unofficial derivative of **Create: Harmonics**. Gameplay code is forked under MIT from Leonardo Importuna's upstream project. Upstream All Rights Reserved artwork is **not** present in this repository; replacement assets are MIT-licensed. This is a separate mod (`create_webdisc`) and must not be confused with the official release.

Report bugs for **this fork only** at [github.com/qwer854645/create_webdisc/issues](https://github.com/qwer854645/create_webdisc/issues).

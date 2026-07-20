# Create: Resonance

**机械动力：共鸣** — NeoForge 1.21.1 · Create 6 附属：动力八音盒演奏多乐器 MIDI（独奏 / 指挥合奏），网络唱片流式播放在线音频。

| | |
|---|---|
| **Mod ID** | `create_resonance` |
| **版本** | 1.0.0 |
| **仓库** | [qwer854645/create_resonance](https://github.com/qwer854645/create_resonance) |
| **维护者** | qwer854645 |

> **非官版 Create: Harmonics。**  
> MIT 衍生改造：新 Mod ID、新包名、新功能（八音盒 / 合奏 / MIDI 刻录）。上游 ARR 美术未包含，与上游品牌及作者无隶属关系。

---

## 功能

### MIDI 八音盒（无需网络唱片）

- **MIDI 刻录台**：配置 `.mid` 一次，动能将空白曲谱刻录为乐谱（无需辊压机）
- **动力八音盒**：独奏或声部模式；转速控制演奏速度（约 64 RPM = 1.0×）
- **指挥台**：持谱静默指挥；同频率声部八音盒合奏同步（播放 / 暂停 / 跳转）
- 曲谱可从 GUI 导入，或将 `.mid` / `.midi` 放入 `config/create_resonance/import/`
- **多乐器**：遵循 GM Program Change；可将 `.sf2` 放入 `config/create_resonance/soundfonts/`
- 声部模式：本机只奏选定 MIDI 通道；声部转速决定对指挥的接受距离（最远 128 格）

### 网络唱片（并列）

- **网络唱片** + **动力网络唱片机** + **网络唱片压印台**
- 压印台配置 URL 列表后，用辊压机写入空白唱片
- 客户端流式播放（ffmpeg / yt-dlp，需用户同意安装外部库）
- 转速调音高；红石 / 播放·暂停模式；支持动态结构与机械臂

### 其它

- 工程师护目镜可读各机器状态
- Create 思索（Ponder）教程覆盖全部机器

---

## 依赖

Minecraft 1.21.1 · NeoForge 21.1+ · Create 6.0.10+ · Kotlin for NeoForge

---

## 构建

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat :neoforge:build
```

产物：`neoforge/build/libs/create_resonance-neoforge-1.21.1-1.0.0.jar`

---

## 许可

- **上游玩法代码**：MIT © Leonardo Importuna（[createharmonics](https://github.com/bitmochibit/createharmonics)）
- **本模组修改、新功能与替换美术**：MIT © qwer854645
- 上游 ARR 美术**未**包含在本仓库；运行时引用的 Create 贴图仍遵循 Create 许可
- 详见 [LICENSE.MD](LICENSE.MD)、[NOTICE.MD](NOTICE.MD)

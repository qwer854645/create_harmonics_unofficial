# Create: Resonance

**机械动力：共鸣** — NeoForge 1.21.1 · Create 6 附属模组。

用动力网络演奏 MIDI、播放网络音频。MIT 衍生自 [Create: Harmonics](https://github.com/bitmochibit/createharmonics)，**非官版、无隶属关系**。

| | |
|---|---|
| **Mod ID** | `create_resonance` |
| **版本** | 1.0.0 |
| **仓库** | [qwer854645/create_resonance](https://github.com/qwer854645/create_resonance) |

---

## 功能概览

### MIDI 演奏
- **刻录台**：把 MIDI 写入曲谱
- **八音盒**：动力驱动播放；可独奏，也可作为合奏声部
- **指挥台**：统一带动多台八音盒合奏

### 网络唱片
- **压印台**：给唱片绑定在线音源
- **动力唱片机**：流式播放网络音频（可选安装 ffmpeg / yt-dlp）

### 其它
- 工程师护目镜可读机器状态
- 附带 Create 思索教程

---

## 依赖

Minecraft 1.21.1 · NeoForge · Create 6 · Kotlin for NeoForge

---

## 构建

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat :neoforge:build
```

产物：`neoforge/build/libs/create_resonance-neoforge-1.21.1-1.0.0.jar`

---

## 许可

- 上游代码：MIT © Leonardo Importuna
- 本模组修改与替换美术：MIT © qwer854645
- 上游 ARR 美术未包含

详见 [LICENSE.MD](LICENSE.MD)、[NOTICE.MD](NOTICE.MD)

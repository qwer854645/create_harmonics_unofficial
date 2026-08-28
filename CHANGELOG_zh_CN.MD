# 更新日志 — Create: Resonance

## 1.0.5

### 修复
- **网络唱片压印台配方：** 恢复机械合成配方（5×4），生存可再次合成。

### 界面
- Resonance 主菜单移除 **模组设置** 按钮。
- **标题画面** 不再显示唱片图标入口（游戏内暂停菜单入口保留）。

## 1.0.4

### 修复
- **扳手 / 挖掘回收：** Resonance 机器现在会正常掉落自身。此前缺少方块战利品表，潜行扳手（以及普通破坏）会直接清空方块且不返还物品。
- **八音盒 / 指挥台内容物：** 拆除方块时会掉出频道过滤物品，频道物品不再随拆除丢失。

---

## 相对 1.0.0 的更新汇总

**1.0.0** 之后至 **1.0.5** 的全部变更：

### 放置与朝向
- 共鸣压印机放置时朝向玩家（此前正面是反的）
- 动能八音盒与音乐指挥台保持竖轴，放置时正面朝向玩家（`front` 方块状态）

### 回收
- 潜行扳手与挖掘会返还 Resonance 机器（战利品表）
- 拆除时掉出内部物品与频道过滤物

### 八音盒与合奏
- 点击 Solo/Section 数值框不再打开 GUI（Create 滚轮 / 长按配置恢复正常）
- 合奏不再在 UUID / MIDI 重同步边界情况下一直静音，直到切换 Solo↔Section 才恢复
- 客户端音频玩家 ID 变化时正确释放 SoftSynth；客户端丢失 MIDI 字节而服务端仍有乐谱时可重同步
- 播放中途移除乐谱会硬静音 SoftSynth，避免爆音 / 怪声

### 唱片机（动能网络唱片机）
- 进度 UI 增加播放 / 暂停 / 重开控制
- 控制键左端与时间 / 进度行对齐
- 空手侧面点击不再抢走播放模式数值框
- 默认音量提高
- 无红石时听距使用 `maxSoundRange`；默认由 32 提到 **64** 格

### 音频与客户端
- SoftSynthesizer MIDI 复音提到 **1024**（密集乐谱少丢音）
- 可选：Resonance 播放时静音原版背景音乐（`muteBackgroundMusicWhilePlaying`，默认 **true**）
- 优先使用本地 `audio_providers/venv` 中的 yt-dlp（便于 HMCL / CurseForge 实例）

---

## 1.0.3

### 唱片机
- 提高默认听距：无红石时使用 `maxSoundRange`（默认 **64**；此前固定 16 / 配置默认 32）
- 进度 UI：播放 / 暂停 / 重开与进度、时间输入左对齐

### 八音盒 / 合奏
- 修复合奏静音直到切换 Solo↔Section（孤儿 SoftSynth + 客户端缓存为空时的 MIDI 重同步）
- 关闭 / 移除乐谱时硬静音 SoftSynth，避免音频毛刺

### 唱片机进度 UI
- 增加播放 / 暂停（以及原有的重开）
- 点击播放模式数值框时不打开进度界面
- 提高唱片机输出音量

## 1.0.2

### 修复
- 动能八音盒：点击 Solo/Section 数值框不再打开 GUI，Create 滚轮 / 长按配置恢复正常
- 频率槽点击处理改在 Create ValueSettings 之后，避免抢走模式框点击

## 1.0.1

### 放置与朝向
- **共鸣压印机** 放置时朝向玩家（此前正面反了）
- **动能八音盒** 与 **音乐指挥台** 放置时保持竖轴，正面朝向玩家（`front` 方块状态）

### 音频
- SoftSynthesizer MIDI 复音提到 **1024**（Gervill 默认仅 64），减轻密集乐谱丢音
- 可选：Resonance 播放时静音原版背景音乐（`muteBackgroundMusicWhilePlaying`，默认 **true**）

### 稳定性与工具
- 优先使用本地 `audio_providers/venv` 中的 yt-dlp（便于 HMCL / CurseForge 实例）
- 八音盒频率渲染不再对非八音盒方块查询朝向
- Bilibili yt-dlp 重打补丁脚本更新（`tools/repatch-ytdlp-bilibili.ps1` + 包装器源码）

## 1.0.0

### 重塑品牌
- 新模组身份：**Create: Resonance**（`create_resonance`）
- 包名迁移至 `io.github.qwer854645.createresonance`
- 网络相关命名：共鸣唱片 / 共鸣器 / 共鸣压印机

### 八音盒
- 动能八音盒（无需唱片）— Solo 或 Section 角色
- **音乐指挥台**（独立方块）— 导入乐谱、设置乐队频道、同步 Section 八音盒
- 从 `config/create_resonance/import/` 导入 MIDI
- Solo 播放，转速联动速度
- 合奏：指挥台 + 同频道 Section 八音盒
- SoundFont：`config/create_resonance/soundfonts/`
- 动能八音盒 / 指挥台：可跳转进度；指挥台可静音指挥

### 保留内容
- 在线共鸣唱片、压印机、动能共鸣器、动能网络共鸣器进度 GUI

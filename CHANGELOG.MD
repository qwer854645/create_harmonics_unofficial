# Changelog — Create: Resonance

## 1.0.1

### Placement & facing
- **Resonance Press** now faces the player when placed (front was previously reversed)
- **Kinetic Music Box** and **Music Conductor** keep a vertical shaft and yaw their front toward the player on place (`front` blockstate)

### Audio
- Raise SoftSynthesizer MIDI polyphony to **1024** (default Gervill limit is 64) to reduce dropped notes on dense scores
- Optionally mute vanilla background music while Resonance audio is playing (`muteBackgroundMusicWhilePlaying`, default **true**)

### Stability & tooling
- Prefer a local `audio_providers/venv` yt-dlp when present (helps HMCL/CurseForge instances)
- Music box frequency rendering no longer queries facing on non-box blocks
- Bilibili yt-dlp repatch helper updated (`tools/repatch-ytdlp-bilibili.ps1` + wrapper source)

## 1.0.0

### Rebrand
- New mod identity: **Create: Resonance** (`create_resonance`)
- Package moved to `io.github.qwer854645.createresonance`
- Network gear renamed: Resonance Disc / Resonator / Resonance Press

### Music boxes
- Kinetic Music Box (no disc required) — Solo or Section roles
- **Music Conductor** (dedicated block) — imports the score, sets band channel, syncs Section boxes
- Import MIDI from `config/create_resonance/import/`
- Solo playback with RPM-linked tempo
- Ensemble: Conductor block + Section music boxes on the same channel
- SoundFont support via `config/create_resonance/soundfonts/`
- Kinetic Music Box / Conductor: seek; Conductor can stay silent while leading

### Retained
- Online resonance discs, imprint press, kinetic resonators, seek GUI on kinetic network resonator

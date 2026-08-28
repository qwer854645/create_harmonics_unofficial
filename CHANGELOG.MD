# Changelog — Create: Resonance

## 1.0.5

### Fixes
- **Resonance Press recipe:** Restore the mechanical crafting recipe (5×4 pattern) so the press can be obtained in survival again.

### UI
- Remove the **Mod Settings** button from the Resonance menu screen.
- Remove the record-disc icon entry from the **title screen** (pause-menu library shortcut unchanged).

## 1.0.4

### Fixes
- **Wrench / mining recovery:** Resonance machines now drop themselves again. Missing block loot tables caused sneak-wrench (and normal breaks) to void the block with no item returned.
- **Music Box / Conductor contents:** Frequency filter items are dropped when the block is removed, so channel items are no longer lost on dismantle.

---

## Updates since 1.0.0

Everything shipped after **1.0.0**, through **1.0.5**:

### Placement & facing
- Resonance Press faces the player when placed (front was previously reversed)
- Kinetic Music Box and Music Conductor keep a vertical shaft and yaw their front toward the player (`front` blockstate)

### UI
- Resonance menu: Mod Settings removed; title-screen disc icon removed (pause-menu library shortcut kept)

### Recovery
- Sneak-wrench and mining return Resonance machines (loot tables)
- Contained items and frequency filters drop on remove

### Music box & ensemble
- Solo/Section value-box clicks no longer open the GUI (Create scroll / hold-to-configure works again)
- Ensemble no longer goes silent until a Solo↔Section toggle after UUID / MIDI resync edge cases
- SoftSynth is released correctly when the client audio player id changes; MIDI can resync when the client lost bytes but the server still has a score
- Removing a score mid-playback hard-silences SoftSynth to avoid pops / glitches

### Record player (Kinetic Network Jukebox)
- Seek UI adds play / pause / restart transport controls
- Transport buttons left-align with the time / progress row
- Playback-mode value box is no longer stolen by the seek UI on empty-hand side clicks
- Louder default output
- Hear distance uses `maxSoundRange` when unpowered; default range increased from 32 → **64** blocks

### Audio & client
- SoftSynthesizer MIDI polyphony raised to **1024** (fewer dropped notes on dense scores)
- Optional mute of vanilla background music while Resonance audio plays (`muteBackgroundMusicWhilePlaying`, default **true**)
- Prefer a local `audio_providers/venv` yt-dlp when present (helps HMCL / CurseForge instances)

---

## 1.0.3

### Record player
- Raise default hear distance: unpowered players use `maxSoundRange` (default **64**; previously a fixed 16 / config default 32)
- Seek UI: left-align play / pause / restart with the progress and time fields

### Music box / ensemble
- Fix ensemble silence until Solo↔Section toggle (orphan SoftSynth + MIDI resync when the client cache is empty)
- Hard-silence SoftSynth on close / score removal to avoid audio glitches

### Record player seek UI
- Add play / pause (plus existing restart) transport controls
- Do not open seek when clicking the playback-mode value box
- Raise record player output volume

## 1.0.2

### Fixes
- Kinetic Music Box: clicking the Solo/Section value box no longer opens the GUI, so Create scroll / hold-to-configure works again
- Frequency-slot click handling runs after Create's ValueSettings handler so it cannot steal mode-box clicks

## 1.0.1

### Placement & facing
- **Resonance Press** now faces the player when placed (front was previously reversed)
- **Kinetic Music Box** and **Music Conductor** keep a vertical shaft and yaw their front toward the player on place (`front` blockstate)

### Audio
- Raise SoftSynthesizer MIDI polyphony to **1024** (default Gervill limit is 64) to reduce dropped notes on dense scores
- Optionally mute vanilla background music while Resonance audio is playing (`muteBackgroundMusicWhilePlaying`, default **true**)

### Stability & tooling
- Prefer a local `audio_providers/venv` yt-dlp when present (helps HMCL / CurseForge instances)
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

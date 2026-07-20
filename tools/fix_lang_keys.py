import json
from pathlib import Path

root = Path(r"f:\others\create_harmonics_unofficial\common\src\main\resources\assets\create_resonance\lang")
zh_path = root / "zh_cn.json"
en_path = root / "default" / "en_us.json"

zh = json.loads(zh_path.read_text(encoding="utf-8"))
en = json.loads(en_path.read_text(encoding="utf-8"))

zh_updates = {
    "create_resonance.mod_display_name": "机械动力：共鸣",
    "create_resonance.display_source.loading_title": "加载中...",
    "create_resonance.display_source.read_record_player_state": "网络唱片机状态",
    "create_resonance.display_source.record_player.playing": "播放中",
    "create_resonance.display_source.record_player.paused": "已暂停",
    "create_resonance.display_source.record_player.stopped": "已停止",
    "create_resonance.display_source.audio_name.display": "文本显示模式",
    "create_resonance.display_source.audio_name.full": "完整",
    "create_resonance.display_source.audio_name.scroll": "滚动",
    "create_resonance.display_source.audio_name.wrap": "换行",
    "create_resonance.gui.library_setup.status.ffprobe_missing": "缺少 FFProbe！",
    "block.create_resonance.andesite_music_box": "安山八音盒",
    "block.create_resonance.kinetic_music_box": "动力八音盒",
    "block.create_resonance.music_conductor": "指挥台",
    "item.create_resonance.resonance_disc": "网络唱片",
    "item.create_resonance.andesite_music_box": "安山八音盒",
    "item.create_resonance.kinetic_music_box": "动力八音盒",
    "item.create_resonance.music_conductor": "指挥台",
    "item.create_resonance.andesite_resonator": "安山网络唱片机",
    "item.create_resonance.brass_resonator": "黄铜网络唱片机",
    "item.create_resonance.resonance_press": "网络唱片压印台",
    # outdated ponder line about material effects
    "create_resonance.ponder.andesite_resonator.text_3": "网络唱片绑定在线音源后即可播放",
}

en_updates = {
    "item.create_resonance.resonance_disc": "Resonance Disc",
    "block.create_resonance.andesite_resonator": "Andesite Resonator",
    "block.create_resonance.brass_resonator": "Brass Resonator",
    "block.create_resonance.resonance_press": "Resonance Press",
    "block.create_resonance.andesite_music_box": "Andesite Music Box",
    "block.create_resonance.kinetic_music_box": "Kinetic Music Box",
    "block.create_resonance.music_conductor": "Music Conductor",
    "item.create_resonance.andesite_music_box": "Andesite Music Box",
    "item.create_resonance.kinetic_music_box": "Kinetic Music Box",
    "item.create_resonance.music_conductor": "Music Conductor",
    "item.create_resonance.andesite_resonator": "Andesite Resonator",
    "item.create_resonance.brass_resonator": "Brass Resonator",
    "item.create_resonance.resonance_press": "Resonance Press",
    "create_resonance.gui.resonance_press.title": "Resonance Disc Imprinting",
    "sounds.create_resonance.subtitle.glitter": "Resonance disc sparkles",
}

zh.update(zh_updates)
en.update(en_updates)

zh_path.write_text(json.dumps(zh, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
en_path.write_text(json.dumps(en, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("zh keys:", len(zh), "en keys:", len(en))
print("updated zh:", len(zh_updates), "en:", len(en_updates))

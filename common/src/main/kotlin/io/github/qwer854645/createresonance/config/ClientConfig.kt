package io.github.qwer854645.createresonance.config

import net.createmod.catnip.config.ConfigBase
import net.neoforged.neoforge.common.ModConfigSpec

object ClientConfig : ConfigBase() {
    lateinit var muteBackgroundMusicWhilePlaying: ConfigBool
        private set

    lateinit var minPitch: ConfigFloat
        private set

    lateinit var maxPitch: ConfigFloat
        private set

    lateinit var reverberatorScanRadius: ConfigInt
        private set

    lateinit var ytdlpOverrideArgs: CValue<String, ModConfigSpec.ConfigValue<String>>
        private set

    lateinit var mainMenuLibButtonRow: ConfigInt
        private set

    lateinit var mainMenuLibButtonOffsetX: ConfigInt
        private set

    lateinit var ingameMenuLibButtonRow: ConfigInt
        private set

    lateinit var ingameMenuLibButtonOffsetX: ConfigInt
        private set

    lateinit var renderMainMenuDecorations: ConfigBool
        private set

    lateinit var renderLibrarySetupDecorations: ConfigBool
        private set

    lateinit var neverShowLibraryDisclaimer: ConfigBool
        private set

    lateinit var debugFFmpeg: ConfigBool
        private set

    lateinit var debugAudioPlayer: ConfigBool
        private set

    override fun registerAll(builder: ModConfigSpec.Builder) {
        menuButtonsGroup(builder)
        audioSourceGroup(builder)
        audioEffectsGroup(builder)
        libraryGroup(builder)
        debugGroup(builder)
        super.registerAll(builder)
    }

    private fun menuButtonsGroup(builder: ModConfigSpec.Builder) {
        group(
            1,
            "menu_buttons",
            "Title / pause menu entry for Create: Resonance (FancyMenu can also hide or restyle these widgets)",
        )

        mainMenuLibButtonRow =
            i(
                2,
                0,
                4,
                "mainMenuLibButtonRow",
                "",
                "Title-screen row for the Resonance disc button (1 = top row)",
                "Set to 0 to disable injection (use FancyMenu layouts instead if desired)",
            )

        mainMenuLibButtonOffsetX =
            i(
                -28,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "mainMenuLibButtonOffsetX",
                "",
                "X offset from the anchored title-screen button; negative = left side",
            )

        ingameMenuLibButtonRow =
            i(
                3,
                0,
                5,
                "ingameMenuLibButtonRow",
                "",
                "Pause-menu row for the Resonance disc button",
                "Set to 0 to disable injection",
            )

        ingameMenuLibButtonOffsetX =
            i(
                -28,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "ingameMenuLibButtonOffsetX",
                "",
                "X offset from the anchored pause-menu button; negative = left side",
            )

        renderMainMenuDecorations =
            b(
                true,
                "renderMainMenuDecorations",
                "Draw panorama / logo / rotating discs / title on the Resonance menu screen",
                "Set false for a blank canvas so FancyMenu layouts or Custom GUI overrides fully control visuals",
            )

        renderLibrarySetupDecorations =
            b(
                true,
                "renderLibrarySetupDecorations",
                "Draw custom cards / titles / progress UI on the library setup screen",
                "Set false to keep only the action buttons (FancyMenu can restyle the rest)",
            )
    }

    private fun audioSourceGroup(builder: ModConfigSpec.Builder) {
        group(1, "audio_sources", "Configuration for audio sources and playback")

        muteBackgroundMusicWhilePlaying =
            b(
                true,
                "muteBackgroundMusicWhilePlaying",
                "When Resonance audio is playing (network jukebox or music box MIDI), stop vanilla background music",
                "Does not affect ambient/UI sounds or the in-game Music volume slider permanently",
            )

        minPitch = f(0.5f, 0.1f, 1.0f, "minPitch", "Minimum pitch for audio playback")
        maxPitch = f(2.0f, 1.0f, 4.0f, "maxPitch", "Maximum pitch for audio playback")

        ytdlpOverrideArgs =
            CValue<String, ModConfigSpec.ConfigValue<String>>(
                "ytdlpOverrideArgs",
                { builder ->
                    builder.define("ytdlpOverrideArgs", "")
                },
                "Override arguments for yt-dlp when requesting streaming URLs",
            )
    }

    private fun audioEffectsGroup(builder: ModConfigSpec.Builder) {
        group(1, "audio_effects", "Configuration for audio effects")

        reverberatorScanRadius =
            i(
                5,
                1,
                30,
                "reverberatorScanRadius",
                "Radius within which reverberator blocks are detected to adjust the reverb effect.",
                "Beware: higher values will impact performance.",
            )
    }

    private fun libraryGroup(builder: ModConfigSpec.Builder) {
        group(1, "library", "Configuration for external library management")

        neverShowLibraryDisclaimer =
            b(
                false,
                "neverShowLibraryDisclaimer",
                "Never show library installation disclaimer on startup",
                "When enabled, the prompt will not appear on game startup",
                "You can still open Library Management from the Resonance menu (disc button)",
            )
    }

    private fun debugGroup(builder: ModConfigSpec.Builder) {
        group(1, "debug", "Debug utilities for detecting errors")

        debugFFmpeg =
            b(
                false,
                "debugFFmpeg",
                "Enables extensive logging for debugging ffmpeg lifecycle",
                "This is useful for reporting bugs and discovering why ffmpeg is failing.",
            )

        debugAudioPlayer =
            b(
                false,
                "debugAudioPlayer",
                "Enables extensive logging for debugging the audio player lifecycle",
                "This is useful for reporting bugs and discovering why the audio player is not working as expected.",
            )
    }

    override fun getName(): String = "client"
}

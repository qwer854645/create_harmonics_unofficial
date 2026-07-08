package me.mochibit.createharmonics.content.records

import me.mochibit.createharmonics.audio.bin.FFMPEGProvider
import me.mochibit.createharmonics.audio.bin.YTDLProvider
import me.mochibit.createharmonics.audio.effect.AudioEffect
import me.mochibit.createharmonics.audio.info.AudioInfo
import me.mochibit.createharmonics.audio.player.AudioPlayer
import me.mochibit.createharmonics.audio.player.AudioRequest
import me.mochibit.createharmonics.audio.stream.Ogg2PcmInputStream
import me.mochibit.createharmonics.audio.utils.getStreamDirectly
import me.mochibit.createharmonics.config.ClientConfig
import me.mochibit.createharmonics.foundation.debug
import me.mochibit.createharmonics.foundation.registry.ModDataComponents
import me.mochibit.createharmonics.foundation.warn
import me.mochibit.createharmonics.handler.RecordCraftingHandler
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.JukeboxSong
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level

object RecordUtilities {
    fun getAudioUrl(stack: ItemStack): String? {
        if (stack.item !is EtherealRecordItem) return null

        stack.get(ModDataComponents.RECORD_URL)?.let { return it }

        val legacyValue =
            stack
                .get(DataComponents.CUSTOM_DATA)
                ?.copyTag()
                ?.getString("audio_url")
                ?.takeIf { it.isNotEmpty() } ?: return null
        stack.set(ModDataComponents.RECORD_URL, legacyValue)
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY) { data ->
            data.update { tag -> tag.remove("audio_url") }
        }
        return legacyValue
    }

    fun setAudioUrl(
        stack: ItemStack,
        url: String,
    ) {
        if (stack.item !is EtherealRecordItem) return

        stack.set(ModDataComponents.RECORD_URL, url)
    }

    fun AudioPlayer.playFromRecord(
        etherealRecord: ItemStack,
        initialPos: Double = 0.0,
        level: Level,
    ) {
        if (etherealRecord.item !is EtherealRecordItem) return
        val url = getAudioUrl(etherealRecord) ?: ""

        this.soundEventComposition.removeAll { true }
        this.effectChain.cleanAllExceptScopes(AudioEffect.Scope.MACHINE_CONTROLLED_PITCH)

        if (url.isNotBlank() && FFMPEGProvider.isAvailable() && YTDLProvider.isAvailable()) {
            if (FFMPEGProvider.isProbeAvailable()) {
                this.request(
                    AudioRequest.Url(url),
                )
                this.play(initialPos)
                return
            } else {
                (
                    "FFmpeg is correctly installed but it seems ffprobe is missing! \nIf it was installed by the mod" +
                        " please report this here https://github.com/qwer854645/create_webdisc/issues.\n" +
                        "If it was installed manually, make sure ffprobe executable is in the same folder as ffmpeg"
                ).warn()
            }
        }

        if (ClientConfig.debugAudioPlayer.get()) {
            (
                "Url play request was ignored! Is it intended?\n" +
                    "current url = $url\n" +
                    "ffmpeg available = ${FFMPEGProvider.isAvailable()}\n" +
                    "ytdlp available = ${YTDLProvider.isAvailable()}\n" +
                    "ffprobe available = ${FFMPEGProvider.isProbeAvailable()}\n\n" +
                    "Generally if this is intended, you shouldn't see any AudioPlayer fail notices!"
            ).debug()
        }

        val craftedWithItem = RecordCraftingHandler.getCraftedWithDisc(etherealRecord)
        val song = JukeboxSong.fromStack(level.registryAccess(), craftedWithItem)
        if (!song.isPresent) return

        val songData = song.get().value()
        val soundEvent =
            songData
                .soundEvent
                .value()

        val sampleRate =
            soundEvent.getStreamDirectly(false).get().use { audio ->
                audio.format.sampleRate
            }

        this.request(
            AudioRequest.Stream(
                {
                    Ogg2PcmInputStream(soundEvent.getStreamDirectly(false).get())
                },
                AudioInfo(
                    audioUrl = "stream",
                    durationSeconds = 1000,
                    title = songData.description.getString(128),
                    sampleRate = sampleRate,
                    isLive = false,
                ),
            ),
        )
        this.play(initialPos)
    }
}

fun ItemStack.isEtherealRecord(): Boolean = item is EtherealRecordItem

fun ItemStack.hasAssignedUrl(): Boolean = !RecordUtilities.getAudioUrl(this).isNullOrEmpty()

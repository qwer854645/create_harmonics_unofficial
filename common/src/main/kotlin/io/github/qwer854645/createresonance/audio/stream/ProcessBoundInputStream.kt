package io.github.qwer854645.createresonance.audio.stream

import io.github.qwer854645.createresonance.audio.process.ProcessLifecycleManager
import java.io.FilterInputStream
import java.io.InputStream

class ProcessBoundInputStream(
    val processId: Long,
    stream: InputStream,
) : FilterInputStream(stream) {
    override fun close() {
        try {
            this.`in`.close()
        } finally {
            ProcessLifecycleManager.destroyProcess(processId)
        }
    }
}

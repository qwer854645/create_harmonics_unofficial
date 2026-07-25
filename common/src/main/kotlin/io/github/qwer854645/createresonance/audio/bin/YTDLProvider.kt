package io.github.qwer854645.createresonance.audio.bin

import java.io.File

object YTDLProvider : BinProvider(
    "yt-dlp",
) {
    override fun getExecutableBaseName(): String = when {
        isMac -> providerName + "_macos"
        else -> providerName
    }

    override fun getDownloadUrl(): String =
        when {
            isWindows -> {
                "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
            }

            isMac -> {
                "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos.zip"
            }

            isLinux -> {
                if (isArm) {
                    "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_aarch64"
                } else {
                    "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux"
                }
            }

            else -> {
                throw UnsupportedOperationException("Unsupported OS: ${System.getProperty("os.name")}")
            }
        }

    /**
     * Prefer a local venv (`python -m yt_dlp`) when present so Bilibili extractor patches
     * under `venv/.../yt_dlp/extractor/bilibili.py` actually apply.
     *
     * The tiny `venv/Scripts/yt-dlp.exe` launcher is intentionally avoided: it breaks on
     * game dirs that contain `&` (common HMCL version names).
     */
    fun getLaunchCommand(): List<String>? {
        resolveVenvPython()?.let { python ->
            return listOf(python.absolutePath, "-m", "yt_dlp")
        }
        return getExecutablePath()?.let { listOf(it) }
    }

    override fun isAvailable(): Boolean = getLaunchCommand() != null

    private fun resolveVenvPython(): File? {
        val python =
            File(
                directory,
                if (isWindows) "venv/Scripts/python.exe" else "venv/bin/python",
            )
        if (!python.isFile) return null
        if (findVenvYtDlpPackage() == null) return null

        // Relocated/broken venvs point home= to a missing interpreter and exit 103 with no useful stderr.
        val cfg = File(directory, "venv/pyvenv.cfg")
        if (cfg.isFile) {
            val home =
                cfg
                    .readLines()
                    .firstOrNull { it.trimStart().startsWith("home") }
                    ?.substringAfter('=')
                    ?.trim()
            if (!home.isNullOrBlank() && !File(home).isDirectory) {
                return null
            }
        }

        ensureExecutable(python)
        return python
    }

    private fun findVenvYtDlpPackage(): File? {
        val win = File(directory, "venv/Lib/site-packages/yt_dlp")
        if (win.isDirectory) return win

        val lib = File(directory, "venv/lib")
        if (!lib.isDirectory) return null
        lib.listFiles()?.forEach { pyDir ->
            if (!pyDir.isDirectory || !pyDir.name.startsWith("python")) return@forEach
            val pkg = File(pyDir, "site-packages/yt_dlp")
            if (pkg.isDirectory) return pkg
        }
        return null
    }
}

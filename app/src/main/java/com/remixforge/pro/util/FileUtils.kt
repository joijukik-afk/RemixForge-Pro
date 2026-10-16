package com.remixforge.pro.util

import android.content.Context
import java.io.File

object FileUtils {
    fun remixDir(context: Context): File =
        File(context.filesDir, "remixes").apply { mkdirs() }

    fun exportDir(): File = File(
        android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_MUSIC), "RemixForgePro"
    ).apply { mkdirs() }
}

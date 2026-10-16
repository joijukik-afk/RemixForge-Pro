package com.remixforge.pro.util

import android.content.Context
import java.io.File

object FileUtils {

    fun getRemixDir(context: Context): File {
        val dir = File(context.filesDir, "remixes")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportDir(): File {
        val dir = File(
            android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_MUSIC
            ),
            "RemixForgePro"
        )
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}

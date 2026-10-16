package com.remixforge.pro.data.dsp

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.File
import java.nio.ByteOrder

object AudioDecoder {

    data class Decoded(
        val samples: FloatArray,
        val sampleRate: Int,
        val channels: Int
    )

    /**
     * دیکد از URI (روش قدیمی، ممکنه Permission Denial بده)
     */
    fun decode(context: Context, uri: Uri): Decoded {
        val ex = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r")!!.use {
            ex.setDataSource(it.fileDescriptor)
        }
        return decodeInternal(ex)
    }

    /**
     * دیکد از فایل محلی (روش جدید، بدون مشکل مجوز)
     */
    fun decodeFile(file: File): Decoded {
        val ex = MediaExtractor()
        ex.setDataSource(file.absolutePath)
        return decodeInternal(ex)
    }

    private fun decodeInternal(ex: MediaExtractor): Decoded {
        var ti = -1
        var fmt: MediaFormat? = null
        for (i in 0 until ex.trackCount) {
            val f = ex.getTrackFormat(i)
            val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) { ti = i; fmt = f; break }
        }
        requireNotNull(fmt) { "no audio track found" }
        ex.selectTrack(ti)

        val mime = fmt.getString(MediaFormat.KEY_MIME)!!
        val sr = fmt.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val ch = fmt.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(fmt, null, null, 0)
        codec.start()

        val out = ArrayList<Float>(sr * ch * 30)
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val iIdx = codec.dequeueInputBuffer(10_000)
                if (iIdx >= 0) {
                    val buf = codec.getInputBuffer(iIdx)!!
                    val size = ex.readSampleData(buf, 0)
                    if (size < 0) {
                        codec.queueInputBuffer(iIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(iIdx, 0, size, ex.sampleTime, 0)
                        ex.advance()
                    }
                }
            }
            val oIdx = codec.dequeueOutputBuffer(info, 10_000)
            if (oIdx >= 0) {
                val buf = codec.getOutputBuffer(oIdx)!!
                buf.order(ByteOrder.nativeOrder())
                val shorts = buf.asShortBuffer()
                while (shorts.remaining() > 0) out.add(shorts.get() / 32768f)
                codec.releaseOutputBuffer(oIdx, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
            }
        }
        codec.stop(); codec.release(); ex.release()

        return Decoded(out.toFloatArray(), sr, ch)
    }
}

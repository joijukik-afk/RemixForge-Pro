package com.remixforge.pro.data.dsp

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * دیکودر صوتی — هر فرمتی که Android پشتیبانی کنه
 * خروجی: PCM FloatArray نرمال‌شده
 */
object AudioDecoder {

    data class DecodedAudio(
        val samples: FloatArray,
        val sampleRate: Int,
        val channels: Int,
        val durationMs: Long
    )

    fun decode(context: Context, uri: Uri): DecodedAudio {
        val extractor = MediaExtractor()
        context.contentResolver.openFileDescriptor(uri, "r")!!.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        }

        var trackIndex = -1
        var format: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            val mime = f.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) {
                trackIndex = i
                format = f
                break
            }
        }

        requireNotNull(format) { "ترک صوتی پیدا نشد" }
        extractor.selectTrack(trackIndex)

        val mime = format.getString(MediaFormat.KEY_MIME)!!
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION))
            format.getLong(MediaFormat.KEY_DURATION) else 0L

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val out = ArrayList<Float>(sampleRate * channels * 30)
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val inIdx = codec.dequeueInputBuffer(10_000)
                if (inIdx >= 0) {
                    val buf = codec.getInputBuffer(inIdx)!!
                    val size = extractor.readSampleData(buf, 0)
                    if (size < 0) {
                        codec.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            val outIdx = codec.dequeueOutputBuffer(info, 10_000)
            if (outIdx >= 0) {
                val buf = codec.getOutputBuffer(outIdx)!!
                buf.order(ByteOrder.nativeOrder())

                // بر اساس فرمت نمونه‌برداری
                if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                    val pcmEncoding = format.getInteger(MediaFormat.KEY_PCM_ENCODING)
                    when (pcmEncoding) {
                        android.media.AudioFormat.ENCODING_PCM_FLOAT -> {
                            val floats = buf.asFloatBuffer()
                            while (floats.remaining() > 0) out.add(floats.get())
                        }
                        else -> {
                            val shorts = buf.asShortBuffer()
                            while (shorts.remaining() > 0) out.add(shorts.get() / 32768f)
                        }
                    }
                } else {
                    val shorts = buf.asShortBuffer()
                    while (shorts.remaining() > 0) out.add(shorts.get() / 32768f)
                }

                codec.releaseOutputBuffer(outIdx, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    outputDone = true
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()

        return DecodedAudio(
            samples = out.toFloatArray(),
            sampleRate = sampleRate,
            channels = channels,
            durationMs = durationUs / 1000
        )
    }
}

package com.remixforge.pro.data.dsp

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

object AudioEngine {

    suspend fun processAudio(
        context: Context,
        inputUri: Uri,
        secondUri: Uri?,
        tempo: Float,
        pitch: Float,
        reverb: Float,
        lowpass: Int,
        removeVocals: Boolean,
        onProgress: (Float, String) -> Unit
    ): File = withContext(Dispatchers.Default) {
        onProgress(0.05f, "استخراج فایل...")
        val mainFile = copyUriToCache(context, inputUri)
        
        val audio1 = decodeAudio(mainFile)
        var samples = audio1.samples
        val sr = audio1.sampleRate
        val ch = audio1.channels

        if (secondUri != null) {
            onProgress(0.15f, "میکس آهنگ دوم...")
            val file2 = copyUriToCache(context, secondUri)
            val audio2 = decodeAudio(file2)
            samples = mixTracks(samples, audio2.samples)
        }

        onProgress(0.3f, "حذف وکال...")
        if (removeVocals && ch == 2) {
            samples = removeVocals(samples)
        }

        onProgress(0.5f, "تغییر سرعت (WSOLA)...")
        if (tempo != 1.0f) {
            samples = timeStretchWSOLA(samples, tempo, ch, sr)
        }

        onProgress(0.7f, "اعمال افکت‌ها...")
        samples = applyEffects(samples, ch, sr, reverb, lowpass)

        onProgress(0.85f, "بررسی کیفیت...")
        var max = 0f
        for (s in samples) { if (abs(s) > max) max = abs(s) }
        if (max > 0f) for (i in samples.indices) samples[i] /= max

        onProgress(0.9f, "تبدیل به MP3...")
        val wavFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.wav")
        writeWav(wavFile, samples, sr, ch)

        val mp3File = File(context.cacheDir, "remix_${System.currentTimeMillis()}.mp3")
        val session = FFmpegKit.execute(
            "-y -i ${wavFile.absolutePath} -codec:a libmp3lame -b:a 320k ${mp3File.absolutePath}"
        )
        
        // پاک کردن فایل موقت WAV
        wavFile.delete()

        if (!mp3File.exists() || mp3File.length() == 0L) {
            throw RuntimeException("خطا در تبدیل به MP3")
        }

        onProgress(1.0f, "تمام شد")
        mp3File
    }

    private fun writeWav(file: File, samples: FloatArray, sampleRate: Int, channels: Int) {
        val dataSize = samples.size * 2
        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(36 + dataSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16)
            header.putShort(1)
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(sampleRate * channels * 2)
            header.putShort((channels * 2).toShort())
            header.putShort(16)
            header.put("data".toByteArray())
            header.putInt(dataSize)
            out.write(header.array())

            val pcm = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in samples) {
                pcm.putShort((s.coerceIn(-1f, 1f) * 32767f).toInt().toShort())
            }
            out.write(pcm.array())
        }
    }

    private fun timeStretchWSOLA(input: FloatArray, speed: Float, channels: Int, sr: Int): FloatArray {
        val windowSize = (0.04 * sr).toInt()
        val hopSize = windowSize / 4
        val outLen = (input.size / speed).toInt()
        val output = FloatArray(outLen + windowSize)
        val window = FloatArray(windowSize) { i -> (0.5 * (1.0 - cos(2.0 * PI * i / (windowSize - 1)))).toFloat() }

        var inPos = 0.0
        var outPos = 0

        while (outPos < output.size && inPos < input.size - windowSize) {
            val startIn = inPos.toInt()
            for (i in 0 until windowSize) {
                if (startIn + i < input.size && outPos + i < output.size) {
                    output[outPos + i] += input[startIn + i] * window[i]
                }
            }
            inPos += hopSize * speed
            outPos += hopSize
        }
        return output
    }

    private fun removeVocals(input: FloatArray): FloatArray {
        val out = FloatArray(input.size)
        for (i in 0 until input.size / 2) {
            val l = input[i * 2]
            val r = input[i * 2 + 1]
            val mid = (l - r) * 0.7f
            out[i * 2] = mid
            out[i * 2 + 1] = mid
        }
        return out
    }

    private fun mixTracks(a: FloatArray, b: FloatArray): FloatArray {
        val maxLen = maxOf(a.size, b.size)
        val out = FloatArray(maxLen)
        for (i in 0 until maxLen) {
            val sa = if (i < a.size) a[i] else 0f
            val sb = if (i < b.size) b[i] else 0f
            out[i] = (sa + sb) * 0.5f
        }
        return out
    }

    private fun applyEffects(input: FloatArray, channels: Int, sr: Int, reverb: Float, lowpass: Int): FloatArray {
        var out = input
        if (lowpass < 20000) out = lowPassFilter(out, lowpass, sr)
        if (reverb > 0.01f) out = simpleReverb(out, sr, reverb)
        return out
    }

    private fun lowPassFilter(input: FloatArray, cutoff: Int, sr: Int): FloatArray {
        val alpha = (2 * PI * cutoff / sr).toFloat()
        val out = FloatArray(input.size)
        var prev = 0f
        for (i in input.indices) {
            prev += alpha * (input[i] - prev)
            out[i] = prev
        }
        return out
    }

    private fun simpleReverb(input: FloatArray, sr: Int, wet: Float): FloatArray {
        val delay = (0.05 * sr).toInt()
        val out = FloatArray(input.size)
        for (i in input.indices) {
            val echo = if (i >= delay) input[i - delay] * 0.4f else 0f
            out[i] = input[i] * (1f - wet) + (input[i] + echo) * wet
        }
        return out
    }

    private fun decodeAudio(file: File): DecodedAudio {
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
        var trackIndex = -1; var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                trackIndex = i; format = f; break
            }
        }
        extractor.selectTrack(trackIndex)
        val mime = format!!.getString(MediaFormat.KEY_MIME)!!
        val sr = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val ch = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val out = ArrayList<Float>()
        val info = MediaCodec.BufferInfo()
        var inputDone = false; var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val inIdx = codec.dequeueInputBuffer(10000)
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
            val outIdx = codec.dequeueOutputBuffer(info, 10000)
            if (outIdx >= 0) {
                val buf = codec.getOutputBuffer(outIdx)!!
                buf.order(ByteOrder.nativeOrder())
                val shorts = buf.asShortBuffer()
                while (shorts.remaining() > 0) out.add(shorts.get() / 32768f)
                codec.releaseOutputBuffer(outIdx, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
            }
        }
        codec.stop(); codec.release(); extractor.release()
        return DecodedAudio(out.toFloatArray(), sr, ch)
    }

    private fun copyUriToCache(ctx: Context, uri: Uri): File {
        val f = File(ctx.cacheDir, "input_${System.currentTimeMillis()}.audio")
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            f.outputStream().use { input.copyTo(it) }
        }
        return f
    }

    data class DecodedAudio(val samples: FloatArray, val sampleRate: Int, val channels: Int)
}

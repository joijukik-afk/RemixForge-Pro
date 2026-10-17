package com.remixforge.pro.data.dsp

import com.tianscar.soundtouch.SoundTouch

class SoundTouchManager(sampleRate: Int, channels: Int) {
    private val soundTouch = SoundTouch(0, channels, sampleRate, 2, 1.0f, 0f)

    fun process(input: FloatArray, tempo: Float, pitch: Float): FloatArray {
        soundTouch.setTempo(tempo)
        soundTouch.setPitchSemiTones(pitch)
        
        val inputBytes = floatArrayToByteArray(input)
        soundTouch.putBytes(inputBytes)
        
        val outputBytes = ByteArray(inputBytes.size * 2)
        val bytesReceived = soundTouch.getBytes(outputBytes)
        return byteArrayToFloatArray(outputBytes.copyOf(bytesReceived))
    }

    private fun floatArrayToByteArray(input: FloatArray): ByteArray {
        val buffer = java.nio.ByteBuffer.allocate(input.size * 4).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        input.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    private fun byteArrayToFloatArray(input: ByteArray): FloatArray {
        val buffer = java.nio.ByteBuffer.wrap(input).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        val output = FloatArray(input.size / 4)
        for (i in output.indices) {
            output[i] = buffer.float
        }
        return output
    }
}

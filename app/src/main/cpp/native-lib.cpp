#include <jni.h>
#include <vector>
#include <string>
#include "SoundTouchWrapper.h"
#include "LameWrapper.h"

extern "C" {

// ============= SoundTouch =============
JNIEXPORT jlong JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeCreate(
        JNIEnv*, jobject, jint sampleRate, jint channels) {
    auto* w = new SoundTouchWrapper(sampleRate, channels);
    return reinterpret_cast<jlong>(w);
}

JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeSetTempo(
        JNIEnv*, jobject, jlong handle, jfloat tempo) {
    reinterpret_cast<SoundTouchWrapper*>(handle)->setTempo(tempo);
}

JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeSetPitch(
        JNIEnv*, jobject, jlong handle, jfloat semitones) {
    reinterpret_cast<SoundTouchWrapper*>(handle)->setPitch(semitones);
}

JNIEXPORT jshortArray JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeProcess(
        JNIEnv* env, jobject, jlong handle, jshortArray input) {
    auto* w = reinterpret_cast<SoundTouchWrapper*>(handle);
    jsize len = env->GetArrayLength(input);
    std::vector<int16_t> in(len);
    env->GetShortArrayRegion(input, 0, len, in.data());

    auto out = w->process(in);

    jshortArray result = env->NewShortArray(out.size());
    env->SetShortArrayRegion(result, 0, out.size(), out.data());
    return result;
}

JNIEXPORT jshortArray JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeFlush(
        JNIEnv* env, jobject, jlong handle) {
    auto* w = reinterpret_cast<SoundTouchWrapper*>(handle);
    auto out = w->flush();
    jshortArray result = env->NewShortArray(out.size());
    env->SetShortArrayRegion(result, 0, out.size(), out.data());
    return result;
}

JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeDestroy(
        JNIEnv*, jobject, jlong handle) {
    delete reinterpret_cast<SoundTouchWrapper*>(handle);
}

// ============= LAME =============
JNIEXPORT jlong JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeCreate(
        JNIEnv*, jobject) {
    return reinterpret_cast<jlong>(new LameWrapper());
}

JNIEXPORT jboolean JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeInit(
        JNIEnv*, jobject, jlong handle, jint sampleRate, jint channels, jint bitrate) {
    return reinterpret_cast<LameWrapper*>(handle)->init(sampleRate, channels, bitrate);
}

JNIEXPORT jbyteArray JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeEncode(
        JNIEnv* env, jobject, jlong handle, jshortArray pcm) {
    auto* w = reinterpret_cast<LameWrapper*>(handle);
    jsize len = env->GetArrayLength(pcm);
    std::vector<int16_t> in(len);
    env->GetShortArrayRegion(pcm, 0, len, in.data());

    std::vector<uint8_t> out;
    if (!w->encode(in, out)) return nullptr;

    jbyteArray result = env->NewByteArray(out.size());
    env->SetByteArrayRegion(result, 0, out.size(),
                            reinterpret_cast<const jbyte*>(out.data()));
    return result;
}

JNIEXPORT jbyteArray JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeFinish(
        JNIEnv* env, jobject, jlong handle) {
    auto* w = reinterpret_cast<LameWrapper*>(handle);
    std::vector<uint8_t> out;
    w->finish(out);

    jbyteArray result = env->NewByteArray(out.size());
    env->SetByteArrayRegion(result, 0, out.size(),
                            reinterpret_cast<const jbyte*>(out.data()));
    return result;
}

JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeDestroy(
        JNIEnv*, jobject, jlong handle) {
    delete reinterpret_cast<LameWrapper*>(handle);
}

} // extern "C"

#include <jni.h>
#include <vector>
#include "SoundTouchWrapper.h"
#include "LameWrapper.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeCreate(JNIEnv*, jobject, jint sr, jint ch) {
    return reinterpret_cast<jlong>(new SoundTouchWrapper(sr, ch));
}
JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeSetTempo(JNIEnv*, jobject, jlong h, jfloat t) {
    reinterpret_cast<SoundTouchWrapper*>(h)->setTempo(t);
}
JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeSetPitch(JNIEnv*, jobject, jlong h, jfloat s) {
    reinterpret_cast<SoundTouchWrapper*>(h)->setPitch(s);
}
JNIEXPORT jshortArray JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeProcess(JNIEnv* env, jobject, jlong h, jshortArray in) {
    auto* w = reinterpret_cast<SoundTouchWrapper*>(h);
    jsize len = env->GetArrayLength(in);
    std::vector<int16_t> vin(len);
    env->GetShortArrayRegion(in, 0, len, vin.data());
    auto out = w->process(vin);
    jshortArray r = env->NewShortArray(out.size());
    env->SetShortArrayRegion(r, 0, out.size(), out.data());
    return r;
}
JNIEXPORT jshortArray JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeFlush(JNIEnv* env, jobject, jlong h) {
    auto out = reinterpret_cast<SoundTouchWrapper*>(h)->flush();
    jshortArray r = env->NewShortArray(out.size());
    env->SetShortArrayRegion(r, 0, out.size(), out.data());
    return r;
}
JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_TimeStretchProcessor_nativeDestroy(JNIEnv*, jobject, jlong h) {
    delete reinterpret_cast<SoundTouchWrapper*>(h);
}

JNIEXPORT jlong JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeCreate(JNIEnv*, jobject) {
    return reinterpret_cast<jlong>(new LameWrapper());
}
JNIEXPORT jboolean JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeInit(JNIEnv*, jobject, jlong h, jint sr, jint ch, jint br) {
    return reinterpret_cast<LameWrapper*>(h)->init(sr, ch, br);
}
JNIEXPORT jbyteArray JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeEncode(JNIEnv* env, jobject, jlong h, jshortArray pcm) {
    auto* w = reinterpret_cast<LameWrapper*>(h);
    jsize len = env->GetArrayLength(pcm);
    std::vector<int16_t> in(len);
    env->GetShortArrayRegion(pcm, 0, len, in.data());
    std::vector<uint8_t> out;
    if (!w->encode(in, out)) return nullptr;
    jbyteArray r = env->NewByteArray(out.size());
    env->SetByteArrayRegion(r, 0, out.size(), reinterpret_cast<const jbyte*>(out.data()));
    return r;
}
JNIEXPORT jbyteArray JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeFinish(JNIEnv* env, jobject, jlong h) {
    std::vector<uint8_t> out;
    reinterpret_cast<LameWrapper*>(h)->finish(out);
    jbyteArray r = env->NewByteArray(out.size());
    env->SetByteArrayRegion(r, 0, out.size(), reinterpret_cast<const jbyte*>(out.data()));
    return r;
}
JNIEXPORT void JNICALL
Java_com_remixforge_pro_data_dsp_AudioEncoder_nativeDestroy(JNIEnv*, jobject, jlong h) {
    delete reinterpret_cast<LameWrapper*>(h);
}

}

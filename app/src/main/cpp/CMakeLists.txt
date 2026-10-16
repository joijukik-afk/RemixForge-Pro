cmake_minimum_required(VERSION 3.22.1)
project("remixforge")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

file(GLOB SOUNDTOUCH_SRC ${CMAKE_CURRENT_SOURCE_DIR}/soundtouch/*.cpp)
add_library(soundtouch STATIC ${SOUNDTOUCH_SRC})
target_include_directories(soundtouch PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/soundtouch)

file(GLOB LAME_SRC ${CMAKE_CURRENT_SOURCE_DIR}/lame/*.c)
list(FILTER LAME_SRC EXCLUDE REGEX ".*(mpglib|main|get_audio|lametime|parse|rtp|timestatus|console|brhist|svq3|i386|vector|xmm_quantize_sub).*")
add_library(mp3lame STATIC ${LAME_SRC})
target_include_directories(mp3lame PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/lame)
target_compile_definitions(mp3lame PRIVATE HAVE_CONFIG_H=1)

add_library(remixforge SHARED
    native-lib.cpp
    SoundTouchWrapper.cpp
    LameWrapper.cpp
)

target_link_libraries(remixforge
    soundtouch
    mp3lame
    android
    log
)

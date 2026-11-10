cmake_minimum_required(VERSION 3.22.1)
project("terminal")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Terminal emulator native library
add_library(terminal SHARED
    terminal_jni.cpp
    shell_session.cpp
    command_executor.cpp
    pty_manager.cpp
)

target_link_libraries(terminal
    ${log-lib}
    ${android-lib}
)

# Compiler flags for performance
target_compile_options(terminal PRIVATE
    -O3
    -fexceptions
    -frtti
    -march=armv8-a
    -DANDROID
)
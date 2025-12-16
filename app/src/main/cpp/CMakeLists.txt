cmake_minimum_required(VERSION 3.22.1)
project(termux-native LANGUAGES C CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_library(log-lib log)
find_library(android-lib android)

# Include directories
include_directories(${CMAKE_SOURCE_DIR})

# Source files
set(NATIVE_SOURCES
    termux.c
    termux_jni.cpp
)

# Build shared library
add_library(termux-native SHARED ${NATIVE_SOURCES})

# Link libraries
target_link_libraries(termux-native
    ${log-lib}
    ${android-lib}
)

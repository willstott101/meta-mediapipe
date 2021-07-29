#----------------------------------------------------------------
# Generated CMake target import file for configuration "Release".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "mediapipe" for configuration "Release"
# TODO: Don't hard-code version 0 here.
set_property(TARGET mediapipe APPEND PROPERTY IMPORTED_CONFIGURATIONS RELEASE)
set_target_properties(mediapipe PROPERTIES
  IMPORTED_LOCATION_RELEASE "${_IMPORT_PREFIX}/lib/libmediapipe_gpu.so.%%PV%%"
  IMPORTED_SONAME_RELEASE "libmediapipe_gpu.so.0"
  )

list(APPEND _IMPORT_CHECK_TARGETS mediapipe )
list(APPEND _IMPORT_CHECK_FILES_FOR_mediapipe "${_IMPORT_PREFIX}/lib/libmediapipe_gpu.so.%%PV%%" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
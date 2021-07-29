# - Config file for the mediapipe package
# It defines the following variables
#  MEDIAPIPE_INCLUDE_DIR - include directory
#  MEDIAPIPE_LIBRARIES    - libraries to link against

# Compute paths
get_filename_component(MEDIAPIPE_CMAKE_DIR "${CMAKE_CURRENT_LIST_FILE}" PATH)
set(MEDIAPIPE_INCLUDE_DIR "${MEDIAPIPE_CMAKE_DIR}/../../../include")

# Our library dependencies (contains definitions for IMPORTED targets)
include("${MEDIAPIPE_CMAKE_DIR}/mediapipe-targets.cmake")

# These are IMPORTED targets created by mediapipe-targets.cmake
set(MEDIAPIPE_LIBRARIES "mediapipe")
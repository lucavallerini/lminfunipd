cmake_minimum_required(VERSION 2.8.9)
project (hdr)
link_directories(/usr/local/lib)
add_executable(hdr hdr.cpp)
target_link_libraries(hdr opencv_core opencv_highgui opencv_imgproc opencv_photo opencv_imgcodecs)

# README #

### What is this repository for? ###

Here you can find a C++ script that can create an HDR photo from a sequence of images taken at different exposures times.

All the algorithm are provided by OpenCV. The script can align the photo provided, then it applies an HDR algorithm that produces the HDR radiant map. Subsequently a tone mapping algorithm is applied that results in the related LDR image. Finally, if enabled, the script outputs also the HDR photo with a simple fusion of the sequence of photos.

### How do I get set up? ###

The sequence of images that will be used needs to be all in a folder with a text file called list.txt: on each line we can find the file name of an image and the related exposure time expressed in seconds, separeted by a space. The output images are saved in the same folder.

* Tweak the CMakeLists.txt with your path for the necessary OpenCV library;
* In the source folder run do
        cmake ./
        make
* To start the script:
      ./hdr [path to the photo sequence] [alignment] [hdrAlgorithm] [toneMappingAlgorithm] [mertensAlgorithm]

You can customize the command as follows (all tokens need to be expressed):

* [path to the photo sequence]: the path to the folder where the sequence of images is saved along with the list.txt file, as explained above;
* [alignment]: true to perform alignment of the images (preferable), false otherwise;
* [hdrAlgorithm]: 1 to apply Debeverec, 2 to apply Robertson;
* [toneMappingAlgorithm]: 1 to apply Durand, 2 to apply Drago, 3 to apply Mantiuk, 4 to apply Reinhard;
* [mertensAlgorithm]: true to apply Mertens, false otherwise

The list.txt file can be created programmatically by the Python script in the src folder: just run
			python ./exposureTimes.py [path to the photo sequence]

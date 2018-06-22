#include <opencv2/photo.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <vector>
#include <iostream>
#include <fstream>
#include <sstream>

using namespace cv;
using namespace std;

void loadExposureSequence(String, vector<Mat>&, vector<float>&);
void mergeImages(vector<Mat>&, vector<float>&, bool, int, int, bool,
  Mat&, Mat&, Mat&);

int main(int, char**argv) {
  // Vectors of images and exposure times
  vector<Mat> images;
  vector<float> times;

  // Final images
  Mat hdrImage;
  Mat ldrImage;
  Mat mertensImage;

  // Settings
  bool alignment;
  stringstream(argv[2]) >> boolalpha >> alignment;
  int hdrAlgorithm = strtol(argv[3], NULL, 10);
  int toneMappingAlgorithm = strtol(argv[4], NULL, 10);
  bool mertensAlgorithm;
  stringstream(argv[5]) >> boolalpha >> mertensAlgorithm;

  // Load images and exposure times
  loadExposureSequence(argv[1], images, times);

  // Merge images
  mergeImages(images, times, alignment, hdrAlgorithm, toneMappingAlgorithm,
    mertensAlgorithm, hdrImage, ldrImage, mertensImage);

  // Write images on file system
  String path = argv[1] + string("/");
  imwrite(path + "hdrImage.hdr", hdrImage);
  imwrite(path + "ldrImage.png", ldrImage);
  if (!mertensImage.empty()) {
    imwrite(path + "mertensImage.png", mertensImage);
  }

  return 0;
}

void loadExposureSequence(String path, vector<Mat>& images, vector<float>& times) {
  path = path + string("/");
  ifstream list_file((path + "list.txt").c_str());
  string name;
  float time;
  while(list_file >> name >> time) {
    Mat img = imread(path + name);
    images.push_back(img);
    times.push_back(time);
  }
  list_file.close();
}

void mergeImages(vector<Mat>& images, vector<float>& times, bool alignment,
  int hdrAlgorithm, int toneMappingAlgorithm, bool mertensAlgorithm,
  Mat& hdrImage, Mat& ldrImage, Mat& mertensImage) {
    // Alignment
    if (alignment) {
      Ptr<AlignMTB> align = createAlignMTB(6, 4, true);
      align->process(images, images);
    }

    // Calibration and HDR
    Mat calibrationResponse;
    Ptr<CalibrateCRF> calibrate;
    Ptr<MergeExposures> merge;
    switch (hdrAlgorithm) {
      case 1: {
        calibrate = createCalibrateDebevec();
        merge = createMergeDebevec();
        break;
      }
      case 2: {
        calibrate = createCalibrateRobertson();
        merge = createMergeRobertson();
        break;
      }
    }
    calibrate->process(images, calibrationResponse, times);
    merge->process(images, hdrImage, times, calibrationResponse);

    // Tone mapping
    Ptr<Tonemap> tonemap;
    switch (toneMappingAlgorithm) {
		 case 1: {
			 tonemap = createTonemapDrago(1.2, 1.0, 0.9);
			 break;
		 }
      case 2: {
        tonemap = createTonemapDurand(2.2, 4.0, 2.5, 2.0, 2.0);
        break;
      }
      case 3: {
        tonemap = createTonemapMantiuk(2.2, 0.9, 1.2);
        break;
      }
      case 4: {
        tonemap = createTonemapReinhard(1.0, 2, 0.5, 0.5);
        break;
      }
    }
    tonemap->process(hdrImage, ldrImage);
    ldrImage *= 1 * 255;

    // Mertens
    if (mertensAlgorithm) {
      merge = createMergeMertens();
      merge->process(images, mertensImage, times, calibrationResponse);
      mertensImage *= 255;
    }
}

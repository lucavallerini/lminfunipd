from os import listdir
from os.path import exists, join
from fractions import Fraction
import sys
import exifread
import re

path = sys.argv[1]

if exists(path):
    files = [f for f in listdir(path)]
    text_file = open(join(path, "list.txt"), "w")

    images = []
    for j in files:
        m = re.search("_[0-9]?\.jpg$", j)
        if m is not None:
            images.append(j)

    for i in images:
        image = open(join(path, i), 'rb')
        tags = exifread.process_file(image)
        for tag in tags.keys():
            if tag == 'EXIF ExposureTime':
                exp = Fraction(str(tags[tag]))
                time = float(exp.numerator) / (float(exp.denominator))
                text_file.write("%s %.12f\n" % (i, time))

    text_file.close()

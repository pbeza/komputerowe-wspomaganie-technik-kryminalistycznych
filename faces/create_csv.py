#!/usr/bin/env python

import sys
import os.path
import re
import imghdr

# This is a tiny script to help you creating a CSV file from a face
# database with a similar hierarchie:
#
#  philipp@mango:~/facerec/data/at$ tree
#  .
#  |-- README
#  |-- s1
#  |   |-- 1.pgm
#  |   |-- ...
#  |   |-- 10.pgm
#  |-- s2
#  |   |-- 1.pgm
#  |   |-- ...
#  |   |-- 10.pgm
#  ...
#  |-- s40
#  |   |-- 1.pgm
#  |   |-- ...
#  |   |-- 10.pgm
#

OUTPUT_FILE_PATH = "./faces.csv"
REG = re.compile("^[^0-9]*(\d\d).*$")

def save_images_to_csv(path):
	lines = []
	for dirname, dirnames, filenames in os.walk(BASE_PATH):
		for filename in filenames:
			rel_path = os.path.join(dirname, filename)
			if imghdr.what(rel_path) == None:
				continue
			res = REG.match(rel_path)
			assert res != None
			lines.append("{0}{1}{2}".format(rel_path, SEPARATOR, res.group(1)))
	with open(OUTPUT_FILE_PATH, 'w+') as f:
		lines.sort()
		for l in lines:
			f.write("{0}\n".format(l))

if __name__ == "__main__":
	if len(sys.argv) != 2:
		print "usage: %s <base_path>", __file__
		sys.exit(1)

	BASE_PATH=sys.argv[1]
	SEPARATOR=";"

	save_images_to_csv(BASE_PATH)

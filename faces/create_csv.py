#!/usr/bin/env python

import sys
import os.path
import re
import imghdr

# This is a tiny script to help you creating a CSV file from a face database.

OUTPUT_FILE_PATH = "./faces.csv"
REG = re.compile("^[^0-9]*(\d\d).*$")

def save_images_to_csv(path):
	lines = []
	for dirname, dirnames, filenames in os.walk(path):
		for filename in filenames:
			rel_path = os.path.join(dirname, filename)
			if imghdr.what(rel_path) is not None:
				lines.append(rel_path)
	with open(OUTPUT_FILE_PATH, 'w+') as f:
		lines.sort()
		last_group = 0
		for rel_path in lines:
			res = REG.match(rel_path)
			assert res is not None
			this_group = int(res.group(1))
			if last_group != this_group:
				current_inx_in_group = 0
			current_inx = this_group * 100 + current_inx_in_group
			f.write("{0}{1}{2}\n".format(rel_path, SEPARATOR, current_inx))
			last_group = this_group
			current_inx_in_group += 1

if __name__ == "__main__":
	if len(sys.argv) != 2:
		print "usage: %s <base_path>", __file__
		sys.exit(1)

	BASE_PATH=sys.argv[1]
	SEPARATOR=";"

	save_images_to_csv(BASE_PATH)

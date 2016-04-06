In this directory you should extract all of the faces' images which form
training for our application. Due to licenses and limited space on GitHub, you
should download and extract them manually.

Links with training sets:

1. Yale Facedatabase A:

http://vision.ucsd.edu/datasets/yale_face_dataset_original/yalefaces.zip

extract to 'YaleFacedatabaseA' directory.


2. Yale Facedatabase B (cropped version):

http://vision.ucsd.edu/extyaleb/CroppedYaleBZip/CroppedYale.zip

extract to 'YaleFacedatabaseB' directory.


3. For more images see:

http://docs.opencv.org/2.4/modules/contrib/doc/facerec/facerec_tutorial.html#face-database



After downloading images run Python's script:

	./create_csv.py ./YaleFacedatabaseA

which will traverse given path looking for photos. Result of the traverse is
saved to CSV file 'faces.csv' which is required by main application's algorithm.

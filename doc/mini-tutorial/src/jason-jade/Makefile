#
# by Jomi
#

TARGET = $(shell ls *.tex | cut -f 1 -d ".")

all: 
	latex2html -dir ../../jason-jade -mkdir -split 0 -white -image_type=png -nonavigation -noinfo -math -style ../labs.css $(TARGET)


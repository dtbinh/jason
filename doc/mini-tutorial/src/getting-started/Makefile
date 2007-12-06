#
# by Jomi
#

TARGET = $(shell ls *.tex | cut -f 1 -d ".")

all: 
	latex2html -dir ../../getting-started -mkdir -split 0 -white -image_type=png -nonavigation -noinfo -math -style ../labs.css $(TARGET)
	zip -r ../../getting-started/VacuumCleaning-1.zip VacuumCleaning-1/*.asl VacuumCleaning-1/*.mas2j VacuumCleaning-1/*.java

publish:
	scp ../../getting-started/* jomifred@ssh.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/getting-started

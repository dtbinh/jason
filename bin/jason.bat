@echo off
SET SACI_HOME=..\lib\saci
SET JAVA_HOME=C:\j2sdk1.4.2
SET PATH="%JAVA_HOME%\bin";%PATH%
java -classpath jason.jar;..\lib\log4j.jar;"%SACI_HOME%\bin\saci.jar" jIDE.JasonID "%SACI_HOME%" "%JAVA_HOME%"

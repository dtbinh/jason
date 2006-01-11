@echo off
SET JAVA_HOME=C:\Program Files\jdk1.5
SET PATH="%JAVA_HOME%\bin";%PATH%
java -cp jason.jar;..\lib\saci.jar;..\lib\jedit\jedit.jar org.gjt.sp.jedit.jEdit


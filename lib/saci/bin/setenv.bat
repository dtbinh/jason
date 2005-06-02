rem if your java instalation is not under the program files directory
rem uncomment the two following lines and replace xxxxx
rem by the directory where java is installed
rem for example set JAVA_HOME="c:\jdk1.4"

rem set JAVA_HOME="xxxxxxx"
rem set PATH=%JAVA_HOME%\bin;%PATH%

set CLASSPATH=saci.jar;moise.jar;..\ulib;..\ulib\samples.jar;%CLASSPATH%
set DPOL="-Djava.security.policy"
set VPOL="policy"

set SACIMENU=saci.tools.SaciMenu
set SACIRUNAG=saci.launcher.RunAg
set SACILAUNCHER=saci.launcher.LauncherD
set SACIMONITOR=saci.tools.MonitorApp

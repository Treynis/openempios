@echo off
set JAVA_HOME=d:\tools\jdk1.6.0
set MAVEN_HOME=v:\dev\maven-2.0.9
set MAVEN_OPTS=-Xmx512m -Xms256m
set ANT_HOME=D:\tools\apache-ant-1.7.0
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%ANT_HOME%\bin;%PATH%
set OPENEMPI_HOME=v:\projects\openempi-2.1.3\openempi
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

v:
cd %OPENEMPI_HOME%

echo -----------------------------------
echo 	OpenEMPI 2.0 - Kenai
echo -----------------------------------


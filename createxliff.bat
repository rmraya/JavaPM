@echo off
pushd "%~dp0" 
set JavaPM_HOME=%CD%
popd
%JavaPM_HOME%\bin\java.exe --module-path %JavaPM_HOME%\lib -m javapm/com.maxprograms.javapm.CreateXliff %* 
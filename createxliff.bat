@echo off
pushd "%~dp0" 
bin\java.exe --module-path lib -m javapm/com.maxprograms.javapm.CreateXliff %* 
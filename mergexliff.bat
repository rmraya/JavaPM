@echo off
pushd "%~dp0" 
set OpenXLIFF_HOME=%CD%
popd
%OpenXLIFF_HOME%\bin\java.exe --module-path %OpenXLIFF_HOME%\lib -m javapm/com.maxprograms.javapm.MergeXliff %* 
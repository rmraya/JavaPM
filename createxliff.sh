#!/bin/bash
CURRENT=$PWD
cd `dirname "$0"`
export OpenXLIFF_HOME=$PWD
cd $CURRENT
$OpenXLIFF_HOME/bin/java --module-path $OpenXLIFF_HOME/lib -m javapm/com.maxprograms.javapm.CreateXliff $@

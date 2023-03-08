#!/bin/bash
CURRENT=$PWD
OpenXLIFF_HOME=`dirname "$0"`
cd $OpenXLIFF_HOME
bin/java --module-path lib -m javapm/com.maxprograms.javapm.CreateXliff $@
cd $CURRENT
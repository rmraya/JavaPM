#!/bin/bash
CURRENT=$PWD
cd `dirname "$0"`
OpenXLIFF_HOME=$PWD
bin/java --module-path lib -m javapm/com.maxprograms.javapm.CreateXliff $@
cd $CURRENT
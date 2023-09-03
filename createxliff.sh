#!/bin/bash
CURRENT=$PWD
cd `dirname "$0"`
export JavaPM_HOME=$PWD
cd $CURRENT
$JavaPM_HOME/bin/java --module-path $JavaPM_HOME/lib -m javapm/com.maxprograms.javapm.CreateXliff $@

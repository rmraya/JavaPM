#!/bin/bash

cd "$(dirname "$0")/"

bin/java --module-path lib -m javapm/com.maxprograms.javapm.MergeXliff $@


# JavaPM

<img src="images/Orange_squares.png" alt="javPM icon"/>

## Java Properties Manager

JavaPM is a set of scripts for localizing Java projects using XLIFF as intermediate format.

## Convert .properites to XLIFF

Running `.\createxliff.bat` or `./createxliff.sh` without parameters displays help for XLIFF generation.

```text
Usage:

    createxliff.bat [-help] -src sourceFolder -xliff xliffFile -srcLang sourceLanguage [-tgtLang targetLanguage] [-2.0]

Where:

   -help:      (optional) display this help information and exit
   -src:       source code folder
   -xliff:     XLIFF file to generate
   -srcLang:   source language code
   -tgtLang:   (optional) target language code
   -2.0:       (optional) generate XLIFF 2.0
```

## Import translated XLIFF

Running `.\mergexliff.bat` or `./mergexliff.sh` without parameters displays help for importing translated XLIFF files.

```text
Usage:

    mergexliff.sh [-help] -src sourceFolder -xliff xliffFile

Where:

    -help:      (optional) display this help information and exit
    -src:       source code folder
    -xliff:     XLIFF file to merge    
```

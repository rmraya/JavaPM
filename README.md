# JavaPM

<img src="images/Orange_squares.png" alt="javPM icon"/>

## Java Properties Manager

JavaPM is a set of scripts for localizing Java projects using XLIFF as intermediate format.

## Convert .properties to XLIFF

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

## Build Requirements

- JDK 17 or newer is required for compiling and building. Pre-built binaries already include everything you need to run all options.
- Apache Ant 1.10.12 or newer

## Building

- Checkout this repository.
- Point your JAVA_HOME variable to JDK 17
- Run `ant` to generate a binary distribution in `./dist`

### Steps for building

``` bash
  git clone https://github.com/rmraya/JavaPM.git
  cd JavaPM
  ant
```
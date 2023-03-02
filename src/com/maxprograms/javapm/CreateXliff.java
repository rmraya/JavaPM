/*******************************************************************************
 * Copyright (c) 2023 Maxprograms.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors: Maxprograms - initial API and implementation
 *******************************************************************************/
package com.maxprograms.javapm;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.converters.Constants;
import com.maxprograms.converters.Convert;
import com.maxprograms.converters.EncodingResolver;
import com.maxprograms.converters.FileFormats;
import com.maxprograms.converters.Join;
import com.maxprograms.converters.Utils;
import com.maxprograms.languages.LanguageUtils;

public class CreateXliff {

    private static Logger logger = System.getLogger(CreateXliff.class.getName());
    private static List<String> sourceFiles;

    public static void main(String[] args) {
        String[] arguments = Utils.fixPath(args);
        String srcFolder = "";
        String xliff = "";
        String srcLang = "";
        String tgtLang = "";
        boolean xliff2 = false;

        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i];
            if (arg.equals("-help")) {
                help();
                return;
            }
            if (arg.equals("-src") && (i + 1) < arguments.length) {
                srcFolder = arguments[i + 1];
            }
            if (arg.equals("-srcLang") && (i + 1) < arguments.length) {
                srcLang = arguments[i + 1];
            }
            if (arg.equals("-tgtLang") && (i + 1) < arguments.length) {
                tgtLang = arguments[i + 1];
            }
            if (arg.equals("-xliff") && (i + 1) < arguments.length) {
                xliff = arguments[i + 1];
            }
            if (arg.equals("-2.0")) {
                xliff2 = true;
            }
        }
        if (arguments.length < 6) {
            help();
            return;
        }
        if (srcFolder.isEmpty()) {
            logger.log(Level.ERROR, "Missing '-src' parameter");
            return;
        }
        if (xliff.isEmpty()) {
            logger.log(Level.ERROR, "Missing '-xliff' parameter");
            return;
        }
        if (srcLang.isEmpty()) {
            logger.log(Level.ERROR, "Missing '-srcLang' parameter");
            return;
        }

        try {
            generateXliff(srcFolder, xliff, srcLang, tgtLang, xliff2);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private static void generateXliff(String src, String xliff, String srcLang, String tgtLang, boolean xliff2)
            throws IOException, SAXException, ParserConfigurationException {
        File srcFolder = new File(src);
        if (!srcFolder.exists()) {
            throw new IOException("'src' folder does not exist");
        }
        File catalogFolder = new File("catalog");
        if (!catalogFolder.exists()) {
            throw new IOException("'catalog' folder not found");
        }
        File catalog = new File(catalogFolder, "catalog.xml");
        if (!catalog.exists()) {
            throw new IOException("Catalog file does not exist");
        }
        File srxFolder = new File("srx");
        if (!srxFolder.exists()) {
            throw new IOException("'srx' folder not found");
        }
        File srx = new File(srxFolder, "default.srx");
        if (!srx.exists()) {
            throw new IOException("SRX file does not exist");
        }
        sourceFiles = new ArrayList<>();
        harvestProperties(srcFolder);
        if (sourceFiles.isEmpty()) {
            throw new IOException("There are no '.properties' files to process");
        }
        List<String> xliffs = new ArrayList<>();
        for (int i = 0; i < sourceFiles.size(); i++) {
            String source = sourceFiles.get(i);
            Charset encoding = EncodingResolver.getEncoding(source, FileFormats.JAVA);
            System.out.println(source);
            String skl = source + ".skl";
            String xlf = source + ".xlf";
            Map<String, String> params = new HashMap<>();
            params.put("source", source);
            params.put("xliff", xlf);
            params.put("skeleton", skl);
            params.put("format", FileFormats.JAVA);
            params.put("srcEncoding", encoding.name());
            params.put("catalog", catalog.getAbsolutePath());
            params.put("paragraph", "yes");
            params.put("srxFile", srx.getAbsolutePath());
            params.put("srcLang", srcLang);
            if (!tgtLang.isEmpty()) {
                params.put("tgtLang", tgtLang);
            }
            params.put("embed", "yes");
            params.put("xliff20", xliff2 ? "yes" : "no");
            List<String> result = Convert.run(params);
            if (Constants.ERROR.equals(result.get(0))) {
                throw new IOException(result.get(1));
            }
            xliffs.add(xlf);
        }
        Join.join(xliffs, xliff);
        for (String xlf : xliffs) {
            Files.delete(new File(xlf).toPath());
        }
    }

    private static void harvestProperties(File folder) throws IOException {
        String[] list = folder.list();
        for (int i = 0; i < list.length; i++) {
            String name = list[i];
            File file = new File(folder, name);
            if (file.isDirectory()) {
                harvestProperties(file);
            } else {
                if (name.endsWith(".properties")) {
                    if (name.lastIndexOf('_') != -1) {
                        String langCode = name.substring(name.lastIndexOf('_') + 1, name.lastIndexOf(".properties"));
                        if (LanguageUtils.getLanguage(langCode) == null) {
                            sourceFiles.add(file.getAbsolutePath());
                        }
                    } else {
                        sourceFiles.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static void help() {
        String launcher = File.pathSeparatorChar == '/' ? "createxliff.sh" : "createxliff.bat";
        MessageFormat mf = new MessageFormat(
                "\nUsage:\n\n    {0} [-help] -src sourceFolder -xliff xliffFile -srcLang sourceLanguage [-tgtLang targetLanguage] [-2.0]\n\nWhere:\n\n   -help:      (optional) display this help information and exit\n   -src:       source code folder\n   -xliff:     XLIFF file to generate\n   -srcLang:   source language code\n   -tgtLang:   (optional) target language code\n   -2.0:       (optional) generate XLIFF 2.0");
        System.out.println(mf.format(new String[] { launcher }));
    }
}

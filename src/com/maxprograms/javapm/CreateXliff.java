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
import java.io.FileOutputStream;
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
import com.maxprograms.converters.Utils;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLOutputter;

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
        join(xliffs, src, xliff, srcLang, tgtLang, xliff2);
    }

    private static void join(List<String> xliffs, String src, String xliff, String srcLang, String tgtLang,
            boolean xliff2) throws SAXException, IOException, ParserConfigurationException {
        Document doc = new Document(null, "xliff", null, null);
        Element root = doc.getRootElement();
        if (xliff2) {
            root.setAttribute("version", "2.0");
            root.setAttribute("srcLang", srcLang);
            if (!tgtLang.isEmpty()) {
                root.setAttribute("trgLang", tgtLang);
            }
            root.setAttribute("xmlns", "urn:oasis:names:tc:xliff:document:2.0");
            root.setAttribute("xmlns:mtc", "urn:oasis:names:tc:xliff:matches:2.0");
            root.setAttribute("xmlns:mda", "urn:oasis:names:tc:xliff:metadata:2.0");
        } else {
            root.setAttribute("version", "1.2");
            root.setAttribute("xmlns", "urn:oasis:names:tc:xliff:document:1.2");
            root.setAttribute("xsi:schemaLocation",
                    "urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        }
        SAXBuilder builder = new SAXBuilder();
        for (int i = 0; i < xliffs.size(); i++) {
            Document d = builder.build(xliffs.get(i));
            Element r = d.getRootElement();
            Element file = r.getChild("file");
            String original = file.getAttributeValue("original");
            String relative = Utils.getRelativePath(src, original);
            file.setAttribute("original", relative);
            if (xliff2) {
                file.setAttribute("id", "" + i);
            }
            root.addContent("\n");
            root.addContent(file);
            Files.delete(new File(xliffs.get(i)).toPath());
        }
        XMLOutputter outputter = new XMLOutputter();
        outputter.preserveSpace(true);
        try (FileOutputStream out = new FileOutputStream(xliff)) {
            outputter.output(doc, out);
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
        String launcher = System.getProperty("file.separator").equals("/") ? "createxliff.sh" : "createxliff.bat";
        MessageFormat mf = new MessageFormat(
                "Usage:\n\n    {0} [-help] -src sourceFolder -xliff xliffFile -srcLang sourceLanguage [-tgtLang targetLanguage] [-2.0]\n\nWhere:\n\n   -help:      (optional) display this help information and exit\n   -src:       source code folder\n   -xliff:     XLIFF file to generate\n   -srcLang:   source language code\n   -tgtLang:   (optional) target language code\n   -2.0:       (optional) generate XLIFF 2.0\n\n");
        System.out.println(mf.format(new String[] { launcher }));
    }
}

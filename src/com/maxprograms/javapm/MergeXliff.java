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
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.converters.Constants;
import com.maxprograms.converters.Merge;
import com.maxprograms.converters.Utils;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLOutputter;

public class MergeXliff {

    private static Logger logger = System.getLogger(MergeXliff.class.getName());

    public static void main(String[] args) {
        String[] arguments = Utils.fixPath(args);
        String srcFolder = "";
        String xliff = "";
        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i];
            if (arg.equals("-help")) {
                help();
                return;
            }
            if (arg.equals("-src") && (i + 1) < arguments.length) {
                srcFolder = arguments[i + 1];
            }
            if (arg.equals("-xliff") && (i + 1) < arguments.length) {
                xliff = arguments[i + 1];
            }
        }
        if (arguments.length < 4) {
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
        try {
            mergeXliff(srcFolder, xliff);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private static void mergeXliff(String src, String xliff)
            throws IOException, SAXException, ParserConfigurationException {
        File srcFolder = new File(src);
        if (!srcFolder.exists()) {
            Files.createDirectories(srcFolder.toPath());
        }
        File catalogFolder = new File("catalog");
        if (!catalogFolder.exists()) {
            throw new IOException("'catalog' folder not found");
        }
        File catalog = new File(catalogFolder, "catalog.xml");
        if (!catalog.exists()) {
            throw new IOException("Catalog file does not exist");
        }
        File xliffFile = new File(xliff);
        if (!xliffFile.exists()) {
            throw new IOException("'xliff' file does not exist");
        }
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(xliffFile);
        Element root = doc.getRootElement();
        if (!"xliff".equals(root.getName())) {
            throw new IOException("Selected file is not an XLIFF document");
        }
        String tgtLang = "";
        if (root.getAttributeValue("version").startsWith("2.")) {
            tgtLang = root.getAttributeValue("trgLang");
        } else {
            Element file = root.getChild("file");
            tgtLang = file.getAttributeValue("target-language");
        }
        if (tgtLang.isEmpty()) {
            throw new IOException("Target language not set");
        }
        xliffFile = processFiles(xliffFile, doc, tgtLang);
        List<String> result = Merge.merge(xliffFile.getAbsolutePath(), src, catalog.getAbsolutePath(), true);
        if (Constants.ERROR.equals(result.get(0))) {
            throw new IOException(result.get(1));
        }
    }

    private static File processFiles(File xliffFile, Document doc, String tgtLang) throws IOException {
        Element root = doc.getRootElement();
        List<Element> files = root.getChildren("file");
        for (int i = 0; i < files.size(); i++) {
            Element file = files.get(i);
            String original = file.getAttributeValue("original");
            file.setAttribute("original", updateOriginal(original, tgtLang));
        }
        File temp = File.createTempFile("temp", ".xlf", xliffFile.getParentFile());
        temp.deleteOnExit();
        XMLOutputter outputter = new XMLOutputter();
        outputter.preserveSpace(true);
        try (FileOutputStream out = new FileOutputStream(temp)) {
            outputter.output(doc, out);
        }
        return temp;
    }

    private static String updateOriginal(String original, String tgtLang) throws IOException {
        int index = original.lastIndexOf(".properties");
        if (index == -1) {
            throw new IOException("File is not a Java .properties bundle");
        }
        String name = original.substring(0, index);
        return name + "_" + tgtLang + ".properties";
    }

    private static void help() {
        String launcher = File.separatorChar == '/' ? "mergexliff.sh" : "mergexliff.bat";
        MessageFormat mf = new MessageFormat(
                "Usage:\n\n    {0} [-help] -src sourceFolder -xliff xliffFile\n\nWhere:\n\n    -help:      (optional) display this help information and exit\n    -src:       source code folder\n    -xliff:     XLIFF file to merge\n\n");
        System.out.println(mf.format(new String[] { launcher }));
    }
}

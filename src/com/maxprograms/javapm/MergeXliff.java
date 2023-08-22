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
import com.maxprograms.converters.TmxExporter;
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
        boolean unapproved = false;
        boolean exportTMX = false;
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
            if (arg.equals("-unapproved")) {
                unapproved = true;
            }
            if (arg.equals("-export")) {
                exportTMX = true;
            }
        }
        if (arguments.length < 4) {
            help();
            return;
        }
        if (srcFolder.isEmpty()) {
            logger.log(Level.ERROR, Messages.getString("MergeXliff.0"));
            return;
        }
        File src = new File(srcFolder);
        if (!src.isAbsolute()) {
            srcFolder = src.getAbsoluteFile().getAbsolutePath();
        }
        if (xliff.isEmpty()) {
            logger.log(Level.ERROR, Messages.getString("MergeXliff.1"));
            return;
        }
        File xliffFile = new File(xliff);
        if (!xliffFile.isAbsolute()) {
            xliff = xliffFile.getAbsoluteFile().getAbsolutePath();
        }
        try {
            mergeXliff(srcFolder, xliff, unapproved, exportTMX);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private static void mergeXliff(String src, String xliff, boolean unapproved, boolean exportTMX)
            throws IOException, SAXException, ParserConfigurationException {
        File srcFolder = new File(src);
        if (!srcFolder.exists()) {
            Files.createDirectories(srcFolder.toPath());
        }
        File catalogFolder = new File("catalog");
        if (!catalogFolder.exists()) {
            throw new IOException(Messages.getString("MergeXliff.2"));
        }
        File catalog = new File(catalogFolder, "catalog.xml");
        if (!catalog.exists()) {
            throw new IOException(Messages.getString("MergeXliff.3"));
        }
        File xliffFile = new File(xliff);
        if (!xliffFile.exists()) {
            throw new IOException(Messages.getString("MergeXliff.4"));
        }
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(xliffFile);
        Element root = doc.getRootElement();
        if (!"xliff".equals(root.getName())) {
            throw new IOException(Messages.getString("MergeXliff.5"));
        }
        String tgtLang = "";
        if (root.getAttributeValue("version").startsWith("2.")) {
            tgtLang = root.getAttributeValue("trgLang");
        } else {
            Element file = root.getChild("file");
            tgtLang = file.getAttributeValue("target-language");
        }
        if (tgtLang.isEmpty()) {
            throw new IOException(Messages.getString("MergeXliff.6"));
        }
        xliffFile = processFiles(xliffFile, doc, tgtLang);
        String target = getTarget(src, xliffFile.getAbsolutePath());
        List<String> result = Merge.merge(xliffFile.getAbsolutePath(), target, catalog.getAbsolutePath(), unapproved);
        if (Constants.ERROR.equals(result.get(0))) {
            throw new IOException(result.get(1));
        }
        if (exportTMX) {
            String tmx = "";
            if (xliff.toLowerCase().endsWith(".xlf")) {
                tmx = xliff.substring(0, xliff.lastIndexOf('.')) + ".tmx";
            } else {
                tmx = xliff + ".tmx";
            }
            result = TmxExporter.export(xliffFile.getAbsolutePath(), tmx, catalog.getAbsolutePath());
        }
        if (!Constants.SUCCESS.equals(result.get(0))) {
            MessageFormat mf = new MessageFormat(Messages.getString("MergeXliff.7"));
            logger.log(Level.ERROR, mf.format(new String[] { result.get(1) }));
        }
    }

    private static String getTarget(String src, String xliff)
            throws SAXException, IOException, ParserConfigurationException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(xliff);
        List<Element> files = doc.getRootElement().getChildren("file");
        if (files.size() != 1) {
            return src;
        }
        String original = files.get(0).getAttributeValue("original");
        File target = new File(new File(src), original);
        return target.getAbsolutePath();
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
            throw new IOException(Messages.getString("MergeXliff.8"));
        }
        String name = original.substring(0, index);
        return name + "_" + tgtLang + ".properties";
    }

    private static void help() {
        String launcher = File.separatorChar == '/' ? "mergexliff.sh" : "mergexliff.bat";
        MessageFormat mf = new MessageFormat(Messages.getString("MergeXliff.9"));
        System.out.println(mf.format(new String[] { launcher }));
    }
}

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.converters.Constants;
import com.maxprograms.converters.Convert;
import com.maxprograms.converters.FileFormats;
import com.maxprograms.converters.Utils;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.Indenter;
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
        String encoding = "ISO-8859-1";
        boolean xliff2 = false;
        boolean reuse = false;

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
            if (arg.equals("-enc") && (i + 1) < arguments.length) {
                encoding = arguments[i + 1];
            }
            if (arg.equals("-2.0")) {
                xliff2 = true;
            }
            if (arg.equals("-reuse")) {
                reuse = true;
            }
        }
        if (arguments.length < 6) {
            help();
            return;
        }
        if (srcFolder.isEmpty()) {
            logger.log(Level.ERROR, Messages.getString("CreateXliff.0"));
            return;
        }
        File src = new File(srcFolder);
        if (!src.isAbsolute()) {
            srcFolder = src.getAbsoluteFile().getAbsolutePath();
        }
        if (xliff.isEmpty()) {
            logger.log(Level.ERROR, Messages.getString("CreateXliff.1"));
            return;
        }
        File xliffFile = new File(xliff);
        if (!xliffFile.isAbsolute()) {
            xliff = xliffFile.getAbsoluteFile().getAbsolutePath();
        }
        if (srcLang.isEmpty()) {
            logger.log(Level.ERROR, Messages.getString("CreateXliff.2"));
            return;
        }
        if (reuse && tgtLang.isEmpty()) {
            logger.log(Level.ERROR, Messages.getString("CreateXliff.10"));
            return;
        }
        try {
            generateXliff(srcFolder, xliff, srcLang, tgtLang, encoding, xliff2, reuse);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private static void generateXliff(String src, String xliff, String srcLang, String tgtLang, String encoding,
            boolean xliff2, boolean reuse) throws IOException, SAXException, ParserConfigurationException {
        File srcFolder = new File(src);
        if (!srcFolder.exists()) {
            throw new IOException(Messages.getString("CreateXliff.3"));
        }
        File catalogFolder = new File("catalog");
        if (!catalogFolder.exists()) {
            throw new IOException(Messages.getString("CreateXliff.4"));
        }
        File catalog = new File(catalogFolder, "catalog.xml");
        if (!catalog.exists()) {
            throw new IOException(Messages.getString("CreateXliff.5"));
        }
        File srxFolder = new File("srx");
        if (!srxFolder.exists()) {
            throw new IOException(Messages.getString("CreateXliff.6"));
        }
        File srx = new File(srxFolder, "default.srx");
        if (!srx.exists()) {
            throw new IOException(Messages.getString("CreateXliff.7"));
        }
        sourceFiles = new ArrayList<>();
        harvestProperties(srcFolder);
        if (sourceFiles.isEmpty()) {
            throw new IOException(Messages.getString("CreateXliff.8"));
        }
        List<String> xliffs = new ArrayList<>();
        for (int i = 0; i < sourceFiles.size(); i++) {
            String source = sourceFiles.get(i);
            System.out.println(source);
            String skl = source + ".skl";
            String xlf = source + ".xlf";
            Map<String, String> params = new HashMap<>();
            params.put("source", source);
            params.put("xliff", xlf);
            params.put("skeleton", skl);
            params.put("format", FileFormats.JAVA);
            params.put("srcEncoding", encoding);
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
            if (reuse) {
                String name = source.substring(0, source.lastIndexOf('.'));
                String translations = name + "_" + tgtLang + ".properties";
                if (new File(translations).exists()) {
                    recoverTranslations(translations, xlf, xliff2, encoding);
                }
            }
            xliffs.add(xlf);
        }
        join(xliffs, src, xliff, srcLang, tgtLang, xliff2);
    }

    private static void recoverTranslations(String translations, String xlf, boolean xliff2, String encoding)
            throws IOException, SAXException, ParserConfigurationException {
        Properties props;
        try (FileInputStream is = new FileInputStream(new File(translations))) {
            try (InputStreamReader reader = new InputStreamReader(is, encoding)) {
                props = new Properties();
                props.load(reader);
            }
        }
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(xlf);
        Element root = doc.getRootElement();
        if (xliff2) {
            recoverXliff2(root, props);
        } else {
            recoverXliff1(root, props);
        }
        Indenter.indent(root, 2);
        XMLOutputter outputter = new XMLOutputter();
        outputter.preserveSpace(true);
        try (FileOutputStream out = new FileOutputStream(xlf)) {
            outputter.output(doc, out);
        }
    }

    private static void recoverXliff1(Element root, Properties props) {
        Element file = root.getChild("file");
        Element body = file.getChild("body");
        List<Element> units = body.getChildren("trans-unit");
        for (int i = 0; i < units.size(); i++) {
            Element unit = units.get(i);
            String resname = unit.getAttributeValue("resname");
            String target = props.getProperty(resname);
            if (target != null) {
                String source = unit.getChild("source").getText();
                if (!source.equals(target)) {
                    Element targetElement = new Element("target");
                    targetElement.setText(target);
                    unit.addContent("   ");
                    unit.addContent(targetElement);
                    unit.addContent("\n      ");
                }
            }
        }
    }

    private static void recoverXliff2(Element root, Properties props) {
        Element file = root.getChild("file");
        List<Element> units = file.getChildren("unit");
        for (int i = 0; i < units.size(); i++) {
            Element unit = units.get(i);
            Element metadata = unit.getChild("mda:metadata");
            Element metaGroup = metadata.getChild("mda:metaGroup");
            Element meta = metaGroup.getChild("mda:meta");
            String resname = meta.getText();
            String target = props.getProperty(resname);
            if (target != null) {
                Element segment = unit.getChild("segment");
                String source = segment.getChild("source").getText();
                if (!source.equals(target)) {
                    Element targetElement = new Element("target");
                    targetElement.setAttribute("xml:space", "preserve");
                    targetElement.setText(target);
                    segment.addContent(targetElement);
                }
            }
        }
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

    private static void harvestProperties(File folder) throws IOException, SAXException, ParserConfigurationException {
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
        String launcher = File.separatorChar == '/' ? "createxliff.sh" : "createxliff.bat";
        MessageFormat mf = new MessageFormat(Messages.getString("CreateXliff.9"));
        System.out.println(mf.format(new String[] { launcher }));
    }
}

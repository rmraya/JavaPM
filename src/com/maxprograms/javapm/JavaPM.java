/*******************************************************************************
 * Copyright (c) 2023 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/
package com.maxprograms.javapm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.util.TextUtil;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLOutputter;

public class JavaPM {

	private String projectFile;
	private Document doc;
	private Element root;
	private boolean dirty;
	private Document xdoc;

	public JavaPM() {
		this.projectFile = null;
		doc = new Document(null, "javapm", null, null); //$NON-NLS-1$
		root = doc.getRootElement();
		dirty = false;
	}

	public void openProject(String file) throws SAXException, IOException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		doc = builder.build(file);
		root = doc.getRootElement();
		projectFile = file;
	}

	public void saveProject() throws UnsupportedEncodingException, IOException {
		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);
		FileOutputStream output = new FileOutputStream(projectFile);
		outputter.output(doc, output);
		output.close();
		dirty = false;
	}

	public void addFile(String file) throws IOException {
		Element e = new Element("file"); //$NON-NLS-1$
		e.setAttribute("name", file); //$NON-NLS-1$
		root.addContent("\n"); //$NON-NLS-1$
		root.addContent(e);
		PropertyResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(file));
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement().trim();
			String value = bundle.getString(key);
			Element string = new Element("string"); //$NON-NLS-1$
			string.setAttribute("key", key); //$NON-NLS-1$
			string.setAttribute("xml:space", "preserve"); //$NON-NLS-1$ //$NON-NLS-2$
			string.setText(value);
			e.addContent("\n"); //$NON-NLS-1$
			e.addContent(string);
		}
		dirty = true;
	}

	public String[] getFiles() {
		List<Element> files = root.getChildren("file"); //$NON-NLS-1$
		String[] result = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			result[i] = files.get(i).getAttributeValue("name"); //$NON-NLS-1$
		}
		return result;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void remove(String file) {
		List<Element> files = root.getChildren("file"); //$NON-NLS-1$
		for (int i = 0; i < files.size(); i++) {
			Element e = files.get(i);
			if (file.equals(e.getAttributeValue("name"))) { //$NON-NLS-1$
				root.removeChild(e);
				dirty = true;
			}
		}
	}

	public void addFolder(String folder) throws IOException {
		File f = new File(folder);
		if (f.isDirectory()) {
			String[] files = f.list();
			for (int i = 0; i < files.length; i++) {
				File file = new File(f.getAbsolutePath() + System.getProperty("file.separator") + files[i]); //$NON-NLS-1$
				if (file.isDirectory()) {
					addFolder(file.getAbsolutePath());
				} else {
					String name = file.getName();
					if (name.endsWith(".properties")) { //$NON-NLS-1$
						if (name.lastIndexOf("_") != -1) { //$NON-NLS-1$
							String langCode = name.substring(name.lastIndexOf("_") + 1, //$NON-NLS-1$
									name.lastIndexOf(".properties")); //$NON-NLS-1$
							if (TextUtil.getLanguageName(langCode).equals(langCode)) {
								addFile(file.getAbsolutePath());
							}
						} else {
							addFile(file.getAbsolutePath());
						}
					}
				}
			}
		}
	}

	public void reloadAllStrings() throws IOException {
		String[] files = getFiles();
		root.setContent(new Vector<XMLNode>());
		for (int i = 0; i < files.length; i++) {
			addFile(files[i]);
		}
	}

	public void exportChanged(String output) throws FileNotFoundException, IOException {
		String[] files = getFiles();
		Vector<Hashtable<String, Object>> changes = new Vector<Hashtable<String, Object>>();
		for (int i = 0; i < files.length; i++) {
			Hashtable<String, String> table = buildStringTable(files[i]);
			Hashtable<String, Object> changed = new Hashtable<String, Object>();
			PropertyResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(files[i]));
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement().trim();
				if (!table.containsKey(key)) {
					changed.put(key, bundle.getObject(key));
				} else {
					String value = table.get(key);
					if (!value.equals(bundle.getObject(key))) {
						changed.put(key, bundle.getObject(key));
					}
				}
			}
			changes.add(changed);
			table = null;
		}
		writeChanged(output, files, changes);
	}

	private static void writeChanged(String output, String[] files, Vector<Hashtable<String, Object>> changes)
			throws UnsupportedEncodingException, IOException {
		FileOutputStream out = new FileOutputStream(output);
		for (int i = 0; i < files.length; i++) {
			Hashtable<String, Object> changed = changes.get(i);
			if (changed.size() > 0) {
				writeStr(out, "\n# FILE: " + files[i] + "\n\n", "ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Enumeration<String> keys = changed.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement().trim();
					String value = (String) changed.get(key);
					writeStr(out, files[i].hashCode() + "." + key + "=" + cleanChars(value) + "\n", "ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
			}
		}
		out.close();
	}

	private static void writeStr(FileOutputStream out, String string, String encoding)
			throws UnsupportedEncodingException, IOException {
		out.write(string.getBytes(encoding)); // $NON-NLS-1$
	}

	private Hashtable<String, String> buildStringTable(String name) {
		Hashtable<String, String> table = new Hashtable<String, String>();
		List<Element> files = root.getChildren("file"); //$NON-NLS-1$
		Iterator<Element> i = files.iterator();
		while (i.hasNext()) {
			Element file = i.next();
			if (file.getAttributeValue("name").equals(name)) { //$NON-NLS-1$
				List<Element> strings = file.getChildren();
				Iterator<Element> j = strings.iterator();
				while (j.hasNext()) {
					Element e = j.next();
					table.put(e.getAttributeValue("key"), e.getText()); //$NON-NLS-1$
				}
				break;
			}
		}
		return table;
	}

	public void importTranslations(String file, String lang) throws FileNotFoundException, IOException {
		Hashtable<String, String> hashes = buildHashes();
		PropertyResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(file));
		Enumeration<String> newKeys = bundle.getKeys();
		while (newKeys.hasMoreElements()) {
			String key = newKeys.nextElement().trim();
			String hash = key.substring(0, key.indexOf(".")); //$NON-NLS-1$
			String value = bundle.getString(key);
			updateEntry(hashes.get(hash), lang, key.substring(key.indexOf(".") + 1), value); //$NON-NLS-1$
		}
	}

	private static void updateEntry(String file, String lang, String key, String value) throws IOException {
		File original = new File(file);
		if (!original.exists()) {
			// properties file was removed
			return;
		}
		String fileName = file.substring(0, file.indexOf(".properties")) + "_" + lang.toLowerCase() + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File f = new File(fileName);
		if (!f.exists()) {
			f.createNewFile();
		}
		FileInputStream instream = new FileInputStream(fileName);
		PropertyResourceBundle bundle = new PropertyResourceBundle(instream);
		Iterator<String> keys = getKeys(file).iterator();
		File tmp = File.createTempFile("tmp", ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
		FileOutputStream out = new FileOutputStream(tmp);
		while (keys.hasNext()) {
			String k = keys.next().trim();
			if (k.startsWith("#") || k.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				writeStr(out, k + "\n", "ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			if (k.equals(key)) {
				writeStr(out, k + "=" + cleanChars(value) + "\n", "ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				try {
					writeStr(out, k + "=" + cleanChars(bundle.getString(k)) + "\n", "ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} catch (java.util.MissingResourceException mre) {
					// there is no old value that we can reuse
				}
			}
		}
		out.close();
		instream.close();

		File old = new File(fileName);
		if (old.exists()) {
			try {
				boolean deleted = old.delete();
				if (!deleted) {
					MessageFormat mf = new MessageFormat(Messages.getString("JavaPM.0")); //$NON-NLS-1$
					throw new IOException(mf.format(new Object[] { old.getAbsolutePath() }));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		FileOutputStream to = new FileOutputStream(fileName);
		FileInputStream from = new FileInputStream(tmp);
		byte[] buf = new byte[1024];
		int len;
		while ((len = from.read(buf)) > 0) {
			to.write(buf, 0, len);
		}
		to.close();
		from.close();
		tmp.delete();
	}

	private Hashtable<String, String> buildHashes() {
		Hashtable<String, String> table = new Hashtable<String, String>();
		String[] files = getFiles();
		for (int i = 0; i < files.length; i++) {
			table.put("" + files[i].hashCode(), files[i]); //$NON-NLS-1$
		}
		return table;
	}

	private static String cleanChars(String string) {
		String result = ""; //$NON-NLS-1$
		int size = string.length();
		for (int i = 0; i < size; i++) {
			char c = string.charAt(i);
			switch (c) {
			case '\n':
				result = result + "\\n"; //$NON-NLS-1$
				break;
			case '\r':
				result = result + "\\r"; //$NON-NLS-1$
				break;
			case '\t':
				result = result + "\\t"; //$NON-NLS-1$
				break;
			case '\f':
				result = result + "\\f"; //$NON-NLS-1$
				break;
			case '\\':
				result = result + "\\\\"; //$NON-NLS-1$
				break;
			default:
				if (c >= '\u0020' && c <= '\u00FF') {
					result = result + c;
				} else {
					result = result + toHex(c);
				}
			}
		}
		return pad(result);
	}

	private static String unscape(String string) {
		string = string.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		string = string.replaceAll("\\\\r", "\r"); //$NON-NLS-1$ //$NON-NLS-2$
		string = string.replaceAll("\\\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		string = string.replaceAll("\\\\f", "\f"); //$NON-NLS-1$ //$NON-NLS-2$
		string = string.replaceAll("\\\\ ", " "); //$NON-NLS-1$ //$NON-NLS-2$
		return string;
	}

	private static String cleanTgt(String string) {
		String result = ""; //$NON-NLS-1$
		int size = string.length();
		for (int i = 0; i < size; i++) {
			char c = string.charAt(i);
			switch (c) {
			case '\n':
				result = result + "\\n"; //$NON-NLS-1$
				break;
			case '\r':
				result = result + "\\r"; //$NON-NLS-1$
				break;
			case '\t':
				result = result + "\\t"; //$NON-NLS-1$
				break;
			case '\f':
				result = result + "\\f"; //$NON-NLS-1$
				break;
			case '\\':
				result = result + "\\\\"; //$NON-NLS-1$
				break;
			default:
				result = result + c;
			}
		}
		return pad(result);
	}

	private static String pad(String string) {
		if (string.equals("")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
		if (Character.isWhitespace(string.charAt(0))) {
			string = "\\" + string; //$NON-NLS-1$
		}
		int length = string.length();
		char last = string.charAt(length - 1);
		if (Character.isWhitespace(last)) {
			string = string.substring(0, length - 1) + "\\" + last; //$NON-NLS-1$
		}
		return string;
	}

	private static String toHex(char c) {
		String hex = Integer.toHexString(c);
		while (hex.length() < 4) {
			hex = "0" + hex; //$NON-NLS-1$
		}
		return "\\u" + hex; //$NON-NLS-1$
	}

	private static Vector<String> getKeys(String file) throws IOException {
		Vector<String> result = new Vector<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = ""; //$NON-NLS-1$
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#") || line.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
					result.add(line);
					continue;
				}
				while (line.endsWith("\\")) { //$NON-NLS-1$
					line = line + reader.readLine();
				}
				int index = line.indexOf("="); //$NON-NLS-1$
				try {
					String key = line.substring(0, index).trim();
					result.add(key);
				} catch (StringIndexOutOfBoundsException siobe) {
					// do nothing
				}
			}
		}
		return result;
	}

	public void exportXLIFF(String output, String language) throws UnsupportedEncodingException, IOException {
		FileOutputStream out = new FileOutputStream(output);
		writeStr(out, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		writeStr(out, "<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " + //$NON-NLS-1$
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //$NON-NLS-1$
				"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd\">\n", //$NON-NLS-1$
				"UTF-8"); //$NON-NLS-1$
		String[] files = getFiles();
		for (int i = 0; i < files.length; i++) {
			writeStr(out, "<file datatype=\"javalistresourcebundle\" original=\"" //$NON-NLS-1$
					+ TextUtil.cleanString(files[i]) + "\" source-language=\"en\" target-language=\"" //$NON-NLS-1$
					+ language + "\">\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			writeStr(out, "<header>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			writeStr(out, "<tool tool-id=\"JavaPM\" tool-name=\"Maxprograms JavaPM\"/>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			writeStr(out, "</header>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			writeStr(out, "<body>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			PropertyResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(files[i]));
			String tgtFile = files[i].substring(0, files[i].indexOf(".properties")) + "_" + language.toLowerCase() //$NON-NLS-1$ //$NON-NLS-2$
					+ ".properties"; //$NON-NLS-1$
			File tg = new File(tgtFile);
			if (!tg.exists()) {
				tg.createNewFile();
			}
			PropertyResourceBundle tgtBundle = new PropertyResourceBundle(new FileInputStream(tgtFile));
			Vector<String> keySet = getKeys(files[i]);
			Iterator<String> keys = keySet.iterator();
			while (keys.hasNext()) {
				String key = keys.next().trim();
				try {
					String src = bundle.getString(key);
					writeStr(out, "<trans-unit id=\"" + TextUtil.cleanString(key) + "\" xml:space=\"preserve\">\n", //$NON-NLS-1$ //$NON-NLS-2$
							"UTF-8"); //$NON-NLS-1$
					writeStr(out, "<source xml:lang=\"en\">" + TextUtil.cleanString(cleanChars(src)) + "</source>\n", //$NON-NLS-1$ //$NON-NLS-2$
							"UTF-8"); //$NON-NLS-1$
				} catch (Exception e) {
					continue;
				}
				try {
					String tgt = tgtBundle.getString(key);
					writeStr(out, "<target xml:lang=\"" + language + "\">" + TextUtil.cleanString(cleanTgt(tgt)) //$NON-NLS-1$ //$NON-NLS-2$
							+ "</target>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (Exception e) {
					// do nothing
				}
				writeStr(out, "</trans-unit>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writeStr(out, "</body>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			writeStr(out, "</file>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		writeStr(out, "</xliff>\n", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void importXLIFF(String xliff) throws SAXException, IOException, ParserConfigurationException {
		SAXBuilder builder = new SAXBuilder();
		xdoc = builder.build(xliff);
		Element xroot = xdoc.getRootElement();
		List<Element> files = xroot.getChildren("file"); //$NON-NLS-1$
		Iterator<Element> f = files.iterator();
		while (f.hasNext()) {
			Element file = f.next();
			String sourceFile = file.getAttributeValue("original"); //$NON-NLS-1$
			String lang = file.getAttributeValue("target-language"); //$NON-NLS-1$
			if (checkGroups(file)) {
				removeGroups(file);
			}
			Element body = file.getChild("body"); //$NON-NLS-1$
			List<Element> tus = body.getChildren("trans-unit"); //$NON-NLS-1$
			Iterator<Element> i = tus.iterator();
			while (i.hasNext()) {
				Element tu = i.next();
				if (tu.getAttributeValue("approved").equals("yes")) { //$NON-NLS-1$ //$NON-NLS-2$
					String target = tu.getChild("target").getText(); //$NON-NLS-1$
					String key = tu.getAttributeValue("id"); //$NON-NLS-1$
					updateEntry(sourceFile, lang, key, unscape(target));
				}
			}
		}
		xroot = null;
		xdoc = null;
	}

	private static boolean checkGroups(Element e) {
		if (e.getName().equals("group") && e.getAttributeValue("ts", "").equals("hs-split")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return true;
		}
		List<Element> children = e.getChildren();
		Iterator<Element> i = children.iterator();
		while (i.hasNext()) {
			Element child = i.next();
			if (checkGroups(child)) {
				return true;
			}
		}
		return false;
	}

	private void removeGroups(Element e) {
		List<XMLNode> children = e.getContent();
		for (int i = 0; i < children.size(); i++) {
			XMLNode n = children.get(i);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element child = (Element) n;
				if (child.getName().equals("group") && child.getAttributeValue("ts", "").equals("hs-split")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					child = joinGroup(child);
					Element tu = new Element("trans-unit"); //$NON-NLS-1$
					tu.clone(child);
					children.remove(i);
					children.add(i, tu);
					e.setContent(children);
				} else {
					removeGroups(child);
				}
			}
		}
	}

	private Element joinGroup(Element child) {
		List<Element> pair = child.getChildren();
		Element left = pair.get(0);
		if (left.getName().equals("group")) { //$NON-NLS-1$
			left = joinGroup(left);
		}
		Element right = pair.get(1);
		if (right.getName().equals("group")) { //$NON-NLS-1$
			right = joinGroup(right);
		}
		List<XMLNode> srcContent = right.getChild("source").getContent(); //$NON-NLS-1$
		for (int k = 0; k < srcContent.size(); k++) {
			XMLNode n = srcContent.get(k);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				left.getChild("source").addContent(n); //$NON-NLS-1$
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				left.getChild("source").addContent(n); //$NON-NLS-1$
			}
		}
		List<XMLNode> tgtContent = right.getChild("target").getContent(); //$NON-NLS-1$
		for (int k = 0; k < tgtContent.size(); k++) {
			XMLNode n = tgtContent.get(k);
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				left.getChild("target").addContent(n); //$NON-NLS-1$
			}
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				left.getChild("target").addContent(n); //$NON-NLS-1$
			}
		}
		left.setAttribute("id", child.getAttributeValue("id")); //$NON-NLS-1$ //$NON-NLS-2$
		if (left.getAttributeValue("approved").equalsIgnoreCase("yes") //$NON-NLS-1$ //$NON-NLS-2$
				&& right.getAttributeValue("approved").equalsIgnoreCase("yes")) { //$NON-NLS-1$ //$NON-NLS-2$
			left.setAttribute("approved", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			left.setAttribute("approved", "no"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return left;
	}

	public void setProjectFile(String file) {
		projectFile = file;
	}

}

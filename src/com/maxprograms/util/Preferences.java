package com.maxprograms.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLOutputter;

public class Preferences {

	private Document doc;
	private Element root;
	private String path;

	public Preferences(String file) throws IOException, SAXException, ParserConfigurationException {
		path = ""; //$NON-NLS-1$
		if (System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Windows
			path = System.getenv("AppData") + "\\Maxprograms\\"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (System.getProperty("os.name").startsWith("Mac")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Mac
			path = System.getProperty("user.home") + "/.maxprograms/"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			// Linux
			path = System.getProperty("user.home") + "/.maxprograms/"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		File dir = new File(path);
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new IOException(Messages.getString("Preferences.11")); //$NON-NLS-1$
			}
		}
		path = path + file;
		File out = new File(path);
		if (!out.exists()) {
			Document d = new Document(null,"preferences",null,null); //$NON-NLS-1$
			Element r = d.getRootElement();
			r.addContent("\n"); //$NON-NLS-1$
			FileOutputStream output = new FileOutputStream(path);
			XMLOutputter outputter = new XMLOutputter();
			outputter.preserveSpace(true);
			outputter.output(d, output);
			output.close();
		}
		SAXBuilder builder = new SAXBuilder();
		doc = builder.build(path);
		root = doc.getRootElement();
	}
	
	public void save(String group, String name, String value) throws UnsupportedEncodingException, IOException {
		Element g = root.getChild(group);
		if (g == null) {
			g = new Element(group);
			g.addContent("\n"); //$NON-NLS-1$
			root.addContent("  "); //$NON-NLS-1$
			root.addContent(g);
			root.addContent("\n"); //$NON-NLS-1$
		}
		Element n = g.getChild(name);
		if (n == null) {
			n = new Element(name);
			g.addContent("    "); //$NON-NLS-1$
			g.addContent(n);
			g.addContent("\n"); //$NON-NLS-1$
		}
		n.setText(value);
		FileOutputStream output = new FileOutputStream(path);
		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);
		outputter.output(doc, output);
		output.close();
	}
	
	public String get(String group, String name, String defaultValue) {
		Element g = root.getChild(group);
		if ( g == null) {
			return defaultValue;
		} 
		Element n = g.getChild(name);
		if ( n == null) {
			return defaultValue;
		} 		
		return n.getText();
	}

	public void save(String group, Hashtable<String, String> table) throws UnsupportedEncodingException, IOException {
		Enumeration<String> keys = table.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			save(group, key, table.get(key));
		}
	}

	public Hashtable<String, String> get(String group) {
		Hashtable<String,String> result = new Hashtable<String,String>();
		Element g = root.getChild(group);
		if (g != null) {
			List<Element> list = g.getChildren();
			Iterator<Element> i = list.iterator();
			while (i.hasNext()) {
				Element e = i.next();
				result.put(e.getName(), e.getText());
			}
		}
		return result;
	}

	public void remove(String string) throws UnsupportedEncodingException, IOException {
		root.removeChild(string);
		FileOutputStream output = new FileOutputStream(path);
		XMLOutputter outputter = new XMLOutputter();
		outputter.preserveSpace(true);
		outputter.output(doc, output);
		output.close();
	}
}

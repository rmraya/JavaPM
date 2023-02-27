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
package com.maxprograms.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLNode;

public class TextUtil {

    private static Document doc;
    private static Element root;
    private static Hashtable<String, String> descriptions;
    private static Hashtable<String, String> isBidi;
	private static Hashtable<String, String> countries;
	private static Hashtable<String, String> ISOLang;

    public static String normalise(String string, boolean trim) {
		boolean repeat = false;
		String rs = ""; //$NON-NLS-1$
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char ch = string.charAt(i);
			if (!Character.isSpaceChar(ch)) {
				if (ch != '\n') {
					rs = rs + ch;
				} else {
					rs = rs + " "; //$NON-NLS-1$
					repeat = true;
				}
			} else {
				rs = rs + " "; //$NON-NLS-1$
				while (i < length - 1 && Character.isSpaceChar(string.charAt(i + 1))) {
					i++;
				}
			}
		}
		if (repeat == true) {
			return normalise(rs, trim);
		}
		if (trim) {
			return rs.trim();
		}
		return rs;
	}

    public static String normalise(String string) {
        return normalise(string,true);
    }
    
    public static String cleanString(String input) {
        input = input.replaceAll("&","&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
        input = input.replaceAll("<","&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
        input = input.replaceAll(">","&gt;" ); //$NON-NLS-1$ //$NON-NLS-2$
        return validChars(input);
    } // end cleanString

    public static String validChars(String input) {
        // Valid: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
        // [#x10000-#x10FFFF]
        // Discouraged: [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF]
        //
        StringBuffer buffer = new StringBuffer();
        char c;
        int length = input.length();
        for (int i = 0; i < length; i++) {
			c = input.charAt(i);
			if ( c == '\t' || c == '\n' || c == '\r'
				|| c >= '\u0020' && c <= '\uD7DF' 
				|| c >= '\uE000' && c <= '\uFFFD' )
			{
				// normal character
				buffer.append(c);
			} else  if   (c >= '\u007F' && c <= '\u0084'
					|| c >= '\u0086' && c <= '\u009F' 
					|| c >= '\uFDD0' && c <= '\uFDDF') 
			{
				// Control character
				buffer.append("&#x" + Integer.toHexString(c) + ";"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (c >= '\uDC00' && c <= '\uDFFF' || c >= '\uD800' && c <= '\uDBFF') {
                // Multiplane character
                buffer.append(input.substring(i,i+1));
            } 
        }    
        return buffer.toString();
    }
    public static boolean isBidiChar(char c) {
        if (c >= '\u0590' && c <= '\u05FF') {
            return true; // Hebrew
        }
        if (c >= '\u0600' && c <= '\u06FF') {
            return true; // basic Arabic shapes
        }
        if (c >= '\uFB50' && c <= '\uFDFF') {
            return true; // Arabic presentation form A
        }
        if (c >= '\uFE70' && c <= '\uFEFF') {
            return true; // Arabic presentation form B
        }
        return false;
    }

    public static String[] getPageCodes() {
    	TreeMap<String,Charset> charsets = new TreeMap<String, Charset>(Charset.availableCharsets());
		Set<String> keys = charsets.keySet();
		String[] codes = new String[keys.size()];
        
		Iterator<String> i = keys.iterator();
		int j=0;
		while (i.hasNext()) {
			Charset cset = charsets.get(i.next());
			codes[j++] = cset.displayName();			
		}
        return codes;
    }

    private static void loadLanguages() throws SAXException, IOException, ParserConfigurationException  {
        doc = null;
        root = null;
        descriptions = null;
        isBidi = null;
        SAXBuilder builder = new SAXBuilder();        
        doc = builder.build("lib/langCodes.xml"); //$NON-NLS-1$
        root = doc.getRootElement();
        List<Element> list = root.getChildren("lang"); //$NON-NLS-1$
        Iterator<Element> i = list.iterator();
        descriptions = new Hashtable<String, String>();
        isBidi = new Hashtable<String, String>();
        while (i.hasNext()) {
            Element e = i.next();
            descriptions.put(e.getAttributeValue("code"),e.getText()); //$NON-NLS-1$
            isBidi.put(e.getAttributeValue("code"),e.getAttributeValue("bidi")); //$NON-NLS-1$ //$NON-NLS-2$
        }        
    }
    
    public static String[] getLanguageNames() {

        try {
            loadLanguages();
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }

        TreeSet<String> set = new TreeSet<String>();
        Enumeration<String> keys = descriptions.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            set.add(key + " " + descriptions.get(key)); //$NON-NLS-1$
        }

        Iterator<String> i = set.iterator();
        int j = 0;
        String langs[] = new String[set.size()];
        while (i.hasNext()) {
            langs[j++] = i.next();
        }

        return langs;
    }

    public static String getLanguageCode(String language) {

        if (language.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        try {
            loadLanguages();
        } catch (Exception e) {
            e.printStackTrace();
            return ""; //$NON-NLS-1$
        }
        Enumeration<String> keys = descriptions.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (language.equals(key + " " + descriptions.get(key))) { //$NON-NLS-1$
                return key;
            }
        }
        return language;
    }

     public static String getLanguageName(String language)  {

        if (language.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }

        try {
            loadLanguages();
        } catch (Exception e) {
            e.printStackTrace();
            return ""; //$NON-NLS-1$
        }

		Enumeration<String> keys = descriptions.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
			if (key.toLowerCase().equals(language.toLowerCase())) {
	            return key + " " + descriptions.get(key); //$NON-NLS-1$
            }
        }
        // code not found on the list
        // check if it is possible to build an ISO name
        switch (language.length()) {
        case 2:        	
        	String iso = getISO639(language, "lib/ISO639-1.xml"); //$NON-NLS-1$
        	if ( iso.equals("")) { //$NON-NLS-1$
        		return language;
        	} 
        	return language + " " + ISOLang.get(language.toLowerCase()); //$NON-NLS-1$
        case 3:
        	iso = getISO639(language, "lib/ISO639-2.xml"); //$NON-NLS-1$
        	if ( iso.equals("")) { //$NON-NLS-1$
        		return language;
        	} 
        	return language + " " + ISOLang.get(language.toLowerCase());        	       	 //$NON-NLS-1$
        case 5:
        	language.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        	if (language.charAt(2) != '-' ) {
    	    	return language;
    		}
    		String lang = language.substring(0,2).toLowerCase();
    		if ( getISO639(lang,"lib/ISO639-1.xml").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
    	    	return language;    	    	
    		}
    		String country = language.substring(3).toUpperCase();
    		if ( getCountryName(country).equals("")) { //$NON-NLS-1$
    	    	return language;    	    	
    		}
    		return lang + "-" + country + " " + ISOLang.get(lang) + " (" + countries.get(country) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        case 6:
        	language.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        	if (language.charAt(3) != '-' ) {
    	    	return language;
    		}
    		lang = language.substring(0,3).toLowerCase();
    		if ( getISO639(lang,"lib/ISO639-2.xml").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
    	    	return language;    	    	
    		}
    		country = language.substring(4).toUpperCase();
    		if ( getCountryName(country).equals("")) { //$NON-NLS-1$
    	    	return language;    	    	
    		}
    		return lang + "-" + country + " " + ISOLang.get(lang) + " (" + countries.get(country) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        default: 
        	return language;
        }
    }

    /**
     * @return
     */
    public static Vector<String> getBidirectionalLangs() {
        Vector<String> result = new Vector<String>();
        try {
            loadLanguages();
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        Enumeration<String> keys = isBidi.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if ( "Yes".equals(isBidi.get(key))) { //$NON-NLS-1$
                result.add(key);
            }            
        }
        return result;
    }
    
    public static String extractText(Element src) {
        if (src == null) {
            return ""; //$NON-NLS-1$
        }
        String text = ""; //$NON-NLS-1$
        List<XMLNode> l = src.getContent();
        Iterator<XMLNode> i = l.iterator();
        while (i.hasNext()) {
        	XMLNode o = i.next();
            if (o.getNodeType() == XMLNode.TEXT_NODE) {
                if (text.length() > 0) {
                    if ( src.getAttributeValue("xml:space","default").equals("default")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        text = text + " " + o.toString().trim(); //$NON-NLS-1$
                    } else {
                        text = text + o.toString().trim();
                    }
                } else {
                    text = text + o.toString().trim();
                }
                text = TextUtil.normalise(text);
            } else if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element)o;
				// check for term entries and extract the text
				if (e.getName().equals("mrk") && e.getAttributeValue("mtype", "").equals("term")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	                if (text.length() > 0) {
	                    if ( src.getAttributeValue("xml:space","default").equals("default")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                        text = text + " " + TextUtil.cleanString(e.getText().trim()); //$NON-NLS-1$
	                    } else {
	                        text = text + TextUtil.cleanString(e.getText().trim());
	                    }
	                } else {
	                    text = text + TextUtil.cleanString(e.getText().trim());
	                }
	                text = TextUtil.normalise(text);					
				}
            }
        }
        return text;
    }
    
    private static void loadCountries() throws SAXException, IOException, ParserConfigurationException  {
    	countries = new Hashtable<String, String>();
    	SAXBuilder builder = new SAXBuilder();
    	Document d = builder.build("lib/ISO3166-1.xml"); //$NON-NLS-1$
    	Element r = d.getRootElement();
    	List<Element> children = r.getChildren();
    	Iterator<Element> it = children.iterator();
    	while (it.hasNext()) {
    		Element e = it.next();
    		countries.put(e.getChild("ISO_3166-1_Alpha-2_Code_element").getText().trim(), //$NON-NLS-1$
    				e.getChild("ISO_3166-1_Country_name").getText().trim()); //$NON-NLS-1$
    	}
    }

    public static String getCountryName(String country)  {

        if (country.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }

        try {
            loadCountries();
        } catch (Exception e) {
            e.printStackTrace();
            return ""; //$NON-NLS-1$
        }

        if ( countries.containsKey(country.toUpperCase())) {
        	return countries.get(country.toUpperCase());
        }
        
        return ""; //$NON-NLS-1$
    }

    public static String getISO639(String code, String langFile)  {

        if (code.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }

        try {
        	loadISOLang(langFile);
        } catch (Exception e) {
            e.printStackTrace();
            return ""; //$NON-NLS-1$
        }

        if ( ISOLang.containsKey(code.toLowerCase())) {
        	return ISOLang.get(code.toLowerCase());
        }
        
        return ""; //$NON-NLS-1$
    }


    private static void loadISOLang(String langFile) throws SAXException, IOException, ParserConfigurationException {
    	ISOLang = new Hashtable<String, String>();
    	SAXBuilder builder = new SAXBuilder();
    	Document d = builder.build(langFile);
    	Element r = d.getRootElement();
    	List<Element> children = r.getChildren();
    	Iterator<Element> it = children.iterator();
    	while (it.hasNext()) {
    		Element e = it.next();
    		ISOLang.put(e.getAttributeValue("code").trim(), //$NON-NLS-1$
    				e.getText().trim());
    	}
    }

	public static String calcTime(long l) {
		if ( l< 0 ) {
    		return "-:--:--:"; //$NON-NLS-1$
    	}
    	long seconds = l/1000;
    	long minutes = seconds / 60;
    	long hours = minutes / 60;
    	minutes = minutes - hours * 60;
    	seconds = seconds - minutes * 60 - hours * 3600;
    	String sec;
    	if (seconds < 10) {
    		sec = "0" + seconds; //$NON-NLS-1$
    	} else {
    		sec = "" + seconds; //$NON-NLS-1$
    	}
    	String min;
    	if (minutes < 10) {
    		min = "0" + minutes; //$NON-NLS-1$
    	} else {
    		min = "" + minutes; //$NON-NLS-1$
    	}
    	return hours + ":" + min + ":" + sec; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isBiDiLanguage(String language) {
		try {
            loadLanguages();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // normalize language code
        language = getLanguageCode(getLanguageName(language));
        if (!isBidi.containsKey(language)) {
        	return false;
        }
        return isBidi.get(language).equals("Yes"); //$NON-NLS-1$
	}

	public static String[] split(String string, String separator) {
		Vector<String> parts = new Vector<String>();
		int index = string.indexOf(separator);
		while (index != -1) {
			parts.add(string.substring(0,index));
			string = string.substring(index + separator.length());
			index = string.indexOf(separator);
		}
		parts.add(string);
		String[] result = new String[parts.size()];
		for (int i=0 ; i<parts.size() ; i++) {
			result[i] = parts.get(i);
		}
		return result;
	}

}

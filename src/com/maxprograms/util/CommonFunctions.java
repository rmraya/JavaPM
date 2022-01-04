package com.maxprograms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;


import com.maxprograms.xml.Element;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;

public class CommonFunctions {
	
    public static final String SEPARATORS = " \r\n\f\t\u2028\u2029,.;\":<>?!()[]{}=+-/*\u00AB\u00BB\u201C\u201D\u201E\uFF00"; //$NON-NLS-1$
    
    public static boolean contains(String[] list, String s){
        for (int i=0; i<list.length; i++){
            if (list[i].equals(s)){
                return true;
            }
        }
        return false;
    }
    	
	public static String[] getItemBySeparator(String s, char separator) {
	    
		if (s == null) {
			return new String[0];
		}
		String separators = "" + separator; //$NON-NLS-1$
		StringTokenizer tokenizer = new StringTokenizer(s,separators);
	    
		int size = tokenizer.countTokens();
		String[] result = new String[size];
		for (int i = 0; i < size; i++) {
		    result[i] = tokenizer.nextToken();
		}

		return result;
	}
    
    
	public static String[] Vector2StringArray(Vector<String> vector){
		String[] result = new String[vector.size()];
		for (int i=0; i<vector.size(); i++){
			result[i] = vector.get(i);
		}
		return result;
	}
	
	public static void StringArray2Vector(String[] stringArray, Vector<String> vector){
		for (int i=0; i<stringArray.length; i++){
			vector.add(stringArray[i]);
		}
	}
	
	public static int indexOf(Vector<String> vector, String string){
		for (int i=0; i<vector.size(); i++){
			if (vector.get(i).equals(string)){
				return i;
			}
		}
		return -1;
		
	}
	
	public static int indexOf(String[] array, String string){
		for (int i=0; i<array.length; i++){
			if (array[i].equals(string)){
				return i;
			}
		}
		return -1;
	}
	
	public static String[] getWords(String s) {
		if (s == null) {
			return new String[0];
		}
		StringTokenizer tokenizer = new StringTokenizer(s,SEPARATORS,true);
	    
		Vector<String> result = new Vector<String>();
		while (tokenizer.hasMoreTokens()) {
		    String token = tokenizer.nextToken().trim();
		    if ( !token.equals("")) { //$NON-NLS-1$
		        result.add(token);
		    }
		}
		return Vector2StringArray(result);
	}	
	
	public static String retTMXDate() {
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		String sec =
			(calendar.get(Calendar.SECOND) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ calendar.get(Calendar.SECOND);
		String min =
			(calendar.get(Calendar.MINUTE) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ calendar.get(Calendar.MINUTE);
		String hour =
			(calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ calendar.get(Calendar.HOUR_OF_DAY);
		String mday =
			(calendar.get(Calendar.DATE) < 10 ? "0" : "") + calendar.get(Calendar.DATE); //$NON-NLS-1$ //$NON-NLS-2$
		String mon =
			(calendar.get(Calendar.MONTH) < 9 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
		+ (calendar.get(Calendar.MONTH) + 1);
		String longyear = "" + calendar.get(Calendar.YEAR); //$NON-NLS-1$

		String date = longyear + mon + mday + "T" + hour + min + sec + "Z"; //$NON-NLS-1$ //$NON-NLS-2$
		return date;
	}

	public static String retGMTdate(String TMXDate){
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		try{
			int second = Integer.parseInt(TMXDate.substring(13,15));
			int minute = Integer.parseInt(TMXDate.substring(11,13));
			int hour = Integer.parseInt(TMXDate.substring(9,11));		
			int date = Integer.parseInt(TMXDate.substring(6,8));		
			int month = Integer.parseInt(TMXDate.substring(4,6)) - 1;
			int year = Integer.parseInt(TMXDate.substring(0,4));		
			calendar.set(year, month, date, hour, minute, second);
			DateFormat dt = DateFormat.getDateTimeInstance();
			return dt.format(calendar.getTime());
		} catch (Exception e) {
			//e.printStackTrace();
			return ""; //$NON-NLS-1$
		}
	}
	
	public static long getGMTtime(String TMXDate){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		try{
			int second = Integer.parseInt(TMXDate.substring(13,15));
			int minute = Integer.parseInt(TMXDate.substring(11,13));
			int hour = Integer.parseInt(TMXDate.substring(9,11));		
			int date = Integer.parseInt(TMXDate.substring(6,8));		
			int month = Integer.parseInt(TMXDate.substring(4,6)) - 1;
			int year = Integer.parseInt(TMXDate.substring(0,4));		
			calendar.set(year, month, date, hour, minute, second);
			return calendar.getTimeInMillis();
		} catch (Exception e) {
			//e.printStackTrace();
			return 0l; 
		}
	}
	
    public static void copyFile(File in, File out) throws IOException {
    	FileInputStream fis  = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while((i=fis.read(buf))!=-1) {
          fos.write(buf, 0, i);
          }
        fis.close();
        fos.close();
    }
 
    /**
	 * break a path down into individual elements and add to a list.
	 * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
	 * @param file input file
	 * @return a Vector with the individual elements of the path in reverse order
	 */
	private static Vector<String> getPathList(File file) throws IOException{
		Vector<String> list = new Vector<String>();
		File r;
		r = file.getCanonicalFile();
		while(r != null) {
			list.add(r.getName());
			r = r.getParentFile();
		}
		return list;
	}

	/**
	 * figure out a string representing the relative path of
	 * 'f' with respect to 'r'
	 * @param r home path
	 * @param f path of file
	 */
	private static String matchPathLists(Vector<String> r, Vector<String> f) {
		int i;
		int j;
		String s = ""; //$NON-NLS-1$
		// start at the beginning of the lists
		// iterate while both lists are equal
		i = r.size()-1;
		j = f.size()-1;

		// first eliminate common root
		while(i >= 0&&j >= 0&&r.get(i).equals(f.get(j))) {
			i--;
			j--;
		}

		// for each remaining level in the home path, add a ..
		for(;i>=0;i--) {
			s += ".." + File.separator; //$NON-NLS-1$
		}

		// for each level in the file path, add the path
		for(;j>=1;j--) {
			s += f.get(j) + File.separator;
		}

		// file name
		if ( j>=0 && j<f.size()) {
			s += f.get(j);
		}
		return s;
	}

	/**
	 * get relative path of File 'f' with respect to 'home' directory example :
	 * home = /a/b/c f = /a/d/e/x.txt s = getRelativePath(home,f) =
	 * ../../d/e/x.txt
	 * 
	 * @param home
	 *            base path, should be a directory, not a file, or it doesn't
	 *            make sense
	 * @param f
	 *            file to generate path for
	 * @return path from home to f as a string
	 */
	public static String getRelativePath(String homeFile, String filename) throws Exception {
		File home = new File(homeFile);
		// If home is a file, get the parent
		if (!home.isDirectory()) {
			if (home.getParent() != null) {
				home = new File(home.getParent());	
			} else {
				home = new File(System.getProperty("user.dir")); //$NON-NLS-1$
			}
			
		}
		File file = new File(filename);
		if (!file.isAbsolute()) {
			return filename;
		}
		// Check for relative path
		if (!home.isAbsolute()) {
			throw new Exception(Messages.getString("CommonFunctions.6")); //$NON-NLS-1$
		}
		Vector<String> homelist;
		Vector<String> filelist;

		homelist = getPathList(home);
		filelist = getPathList(file);
		return matchPathLists(homelist, filelist);
	}    
 
	public static String getAbsolutePath(String homeFile, String relative) throws IOException{
	   	File home = new File(homeFile);
	   	// If home is a file, get the parent
	   	File result;
	   	if (!home.isDirectory()){
	   		home = home.getParentFile();
	   	}
	   	result = new File(home, relative);	   		
	   	return result.getCanonicalPath();
	}
	
	public static String getElementText(Element src, String spc) {
		// this version does not convert < > and & to entities
        if (src == null) {
            return ""; //$NON-NLS-1$
        }
        String text = ""; //$NON-NLS-1$
        List<XMLNode> l = src.getContent();
        if (l == null) {
            return ""; //$NON-NLS-1$
        }
        Iterator<XMLNode> i = l.iterator();
        while (i.hasNext()) {
        	XMLNode o = i.next();
            if (o.getNodeType() == XMLNode.TEXT_NODE) {
                if (spc.equals("default")) { //$NON-NLS-1$
                    if (text.length() > 0) {
                        text = text + " " + ((TextNode)o).getText(); //$NON-NLS-1$
                    } else {
                        text = text + ((TextNode)o).getText();
                    }
                } else {
                    text = text + ((TextNode)o).getText();
                }
            } else if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
                Element el = (Element)o;
                String type = el.getName();

                if (!type.equals("bpt") //$NON-NLS-1$
                        && !type.equals("ept") //$NON-NLS-1$
                        && !type.equals("it") //$NON-NLS-1$
                        && !type.equals("g") //$NON-NLS-1$
                        && !type.equals("ph") //$NON-NLS-1$
                        && !type.equals("x")) { //$NON-NLS-1$
                    text = text + " " + getElementText(el, spc); //$NON-NLS-1$
                }
                el = null;
            }
            o = null;
        }
        i = null;
        l = null;
        return text;
    }	
}

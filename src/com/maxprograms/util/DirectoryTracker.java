/*
 * Created on Dec 3, 2004
 *
 */
package com.maxprograms.util;


/**
 * @author Rodolfo M. Raya
 *
 */
public class DirectoryTracker {

	public static String lastDirectory(String preferences) {
		try {
			Preferences prefs = new Preferences(preferences);
			return prefs.get("lastDirectory", "lastDir", System.getProperty("user.home")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (Exception e) {
			return System.getProperty("user.home"); //$NON-NLS-1$
		}
	}
	
	public static void saveDirectory(String directory, String preferences) {
		try {
			Preferences prefs = new Preferences(preferences);
			prefs.save("lastDirectory", "lastDir", directory); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}

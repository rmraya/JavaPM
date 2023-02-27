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

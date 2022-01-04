/*
 * Created on Aug 10, 2004
 *
 */
package com.maxprograms.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Rodolfo M. Raya
 *
 */
public class Messages {
    private static final String BUNDLE_NAME = "com.maxprograms.util.util";//$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
        // private constructor
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
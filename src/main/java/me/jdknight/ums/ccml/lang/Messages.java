/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.lang;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Plugin internationalization message accessor.
 */
public class Messages
{
	/**
	 * Bundle area for messages.
	 **/
	private static final String BUNDLE_NAME = "me.jdknight.ums.ccml.lang.Messages"; //$NON-NLS-1$

	/**
	 * Bundle's message source.
	 **/
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Private constructor.
	 **/
	private Messages() {}

	/**
	 * Return the locale value for the provided language key.
	 * 
	 * @param key The language key.
	 * @return    The locale value for this key.
	 */
	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}

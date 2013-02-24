/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core;

import java.io.File;

import net.pms.PMS;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;

/**
 * Compatibility utility class.
 * 
 * <p>
 * In order to support previous versions of the media server builds, but lazily do it in 
 * to ignore making different branches/etc, this method will provide a proxy for 
 * API calls to the media server methods. To adjust for a previous version build, correctly 
 * uncomment the respective code below and build for that version.
 * </p>
 */
public final class LazyCompatibility
{
	/**
	 * Private class.
	 **/
	private LazyCompatibility() {}
	
	/**
	 * Return the format from provided file path.
	 * 
	 * @param path The file path.
	 * @return     The format.
	 */
	public static Format getAssociatedExtension(String path)
	{
		Format format = null;

		// PMS Versions: 1.25.1, 1.40.0, 1.50.0, 1.51.0
		/*
		format = PMS.get().getAssociatedExtension(sPath);
		*/

		// UMS Versions: 2.50.0+
		// PMS Versions: 1.52.0+
		// /*
		format = FormatFactory.getAssociatedExtension(path);
		// */
		
		return format;
	}
	
	/**
	 * Return the shared directories.
	 * 
	 * @return The shared directories.
	 */
	public static File[] getSharedDirectories()
	{
		File[] sharedDirectories = null;
		
		// PMS Versions: 1.25.1, 1.40.0
		/*
		try
		{
			sharedDirectories = PMS.get().loadFoldersConf(PMS.getConfiguration().getFolders(), true);
		}
		catch (IOException e) {}
		 */

		// UMS Versions: 2.50.0+
		// PMS Versions: 1.50.0+
		// /*
		sharedDirectories =  PMS.get().getFoldersConf(true);
		// */
		
		return sharedDirectories;
	}
}

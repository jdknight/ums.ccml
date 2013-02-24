/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core.interfaces;

/**
 * CCML plugin configuration utility.
 */
public interface ICcmlConfiguration
{
	/**
	 * Load the configuration file.
	 * 
	 * @return True, if the configuration file was loaded; false otherwise.
	 */
	public boolean load();
	
	/**
	 * Persist current settings to the configuration file.
	 * 
	 * @return True, if the configuration file was persisted; false otherwise.
	 */
	public boolean persist();

	/**
	 * Return the alternative meta folder.
	 * 
	 * <p>
	 * If defined, the folder will be used to match meta files for media resources.
	 * </p>
	 * 
	 * @return The path to the alternative meta folder; may be null.
	 */
	public String getAlternativeMetaFolder();

	/**
	 * Set the alternative meta folder.
	 * 
	 * <p>
	 * If defined, the folder will be used to match meta files for media resources.
	 * </p>
	 * 
	 * @param folderPath The path to the alternative meta folder.
	 * 
	 * @throws IllegalArgumentException Thrown if the provided folder is invalid.
	 */
	public void setAlternativeMetaFolder(String folderPath);
	
	/**
	 * Return whether or not the client wishes to hide parsing/building library notifications.
	 * 
	 * @return True, if the client wishes to hide the notifications; false otherwise.
	 */
	public boolean isHidingParsingBuildingNotification();
	
	/**
	 * Set whether or not the client wishes to hide parsing/building library notifications.
	 * 
	 * @param shouldNotify True, if the client wishes to hide the notifications; false otherwise.
	 */
	public void toggleHidingParsingBuildingNotification(boolean shouldNotify);
}

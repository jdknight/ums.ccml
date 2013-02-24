/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core.interfaces;

import java.util.EnumSet;

import me.jdknight.ums.ccml.core.enumerators.EMediaType;
import net.pms.dlna.virtual.VirtualFolder;

/**
 * A virtual folder containing media resources.
 */
public interface IVirtualFolderMediaResources
{
	/**
	 * Return the virtual folder.
	 * 
	 * @return The virtual folder.
	 */
	public VirtualFolder getVirtualFolder();
	
	/**
	 * Return the set of media types found in this virtual folder.
	 * 
	 * @return The set of media types.
	 */
	public EnumSet<EMediaType> getMediaType();
}

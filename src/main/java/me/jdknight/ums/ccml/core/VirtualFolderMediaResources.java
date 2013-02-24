/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core;

import java.util.EnumSet;

import me.jdknight.ums.ccml.core.enumerators.EMediaType;
import me.jdknight.ums.ccml.core.interfaces.IVirtualFolderMediaResources;
import net.pms.dlna.virtual.VirtualFolder;

/**
 * A virtual folder containing media resources.
 */
public class VirtualFolderMediaResources implements IVirtualFolderMediaResources
{
	/**
	 * The virtual folder.
	 **/
	private VirtualFolder _virtualFolder;
	
	/**
	 * The set of media types found in this virtual folder.
	 **/
	private EnumSet<EMediaType> _mediaTypes;
	
	/**
	 * Initializes a new instance of VirtualFolderMediaResources.
	 * 
	 * @param virtualFolder The virtual folder containing the media resources.
	 * @param mediaTypes    The set of media types found in this virtual folder.
	 */
	public VirtualFolderMediaResources(VirtualFolder virtualFolder, EnumSet<EMediaType> mediaTypes)
	{
		_virtualFolder = virtualFolder;
		_mediaTypes = mediaTypes;
	}
	
	/**
	 * Return the virtual folder.
	 * 
	 * @return The virtual folder.
	 */
	@Override
	public VirtualFolder getVirtualFolder()
	{
		return _virtualFolder;
	}

	/**
	 * Return the set of media types found in this virtual folder.
	 * 
	 * @return The set of media types.
	 */
	@Override
	public EnumSet<EMediaType> getMediaType()
	{
		return _mediaTypes;
	}
}

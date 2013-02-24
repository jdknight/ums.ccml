/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.tmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

import net.pms.PMS;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.VirtualFolder;

/**
 * A DLNA real file with virtual folder support for thumbnails.
 * 
 * <p>
 * This class attempts to mimic the DNLA real file from the media server core. It overrides the thumbnail-finder method
 * in to attempts to handle finding thumbnails for media in the same fashion as the media server does but in virtual folders.
 * </p>
 */
public class RealFileWithVirtualFolderThumbnails extends RealFile
{
	/**
	 * Initializes a new instance of RealFileWithVirtualFolderThumbnails with the provided file.
	 * 
	 * @param file The file to handle.
	 */
	public RealFileWithVirtualFolderThumbnails(File file)
	{
		super(file);
	}

	/**
	 * Initializes a new instance of RealFileWithVirtualFolderThumbnails with the provided file 
	 * and name for this file.
	 * 
	 * @param file The file to handle.
	 * @param sName The name to set for this file.
	 */
	public RealFileWithVirtualFolderThumbnails(File file, String sName)
	{
		super(file, sName);
	}
	
	/**
	 * Initializes a new instance of RealFileWithVirtualFolderThumbnails with values copied from 
	 * another reference.
	 * 
	 * @param otherReference The entity to copy.
	 */
	public RealFileWithVirtualFolderThumbnails(RealFileWithVirtualFolderThumbnails otherReference)
	{
		this(otherReference.getFile(), otherReference.getDisplayName());
	}

	/**
	 * Return the input stream to use for this file's thumbnail.
	 * 
	 * @return The input stream.
	 * 
	 * @throws IOException Thrown when the creation of an input stream has failed. 
	 */
	@Override
	public InputStream getThumbnailInputStream() throws IOException
	{
		// Virtual folder support.
		if ((getParent() instanceof VirtualFolder) == false)
		{
			return super.getThumbnailInputStream();
		}
	
		File file = getFile();
		File foundThumbnail = null;
	
		// Attempt to find the thumbnail for this resource:
		//  -Check for local thumbnail.
		//  -Check for thumbnail in alternate directory.
		boolean shouldCheckForAlternatives = true;
		String resourcePath = file.getParent() + File.separator;
		do
		{
			String resourceBaseName = FilenameUtils.getBaseName(file.getName());
		
			foundThumbnail = new File(resourcePath + resourceBaseName + ".png"); //$NON-NLS-1$
			if (foundThumbnail.isFile() == false) foundThumbnail = new File(resourcePath + resourceBaseName + ".jpg"); //$NON-NLS-1$
			if (foundThumbnail.isFile() == false) foundThumbnail = new File(resourcePath + file.getName() + ".cover.png"); //$NON-NLS-1$
			if (foundThumbnail.isFile() == false) foundThumbnail = new File(resourcePath + file.getName() + ".cover.jpg"); //$NON-NLS-1$

			if (shouldCheckForAlternatives == true && foundThumbnail.isFile() == false)
			{
				String alternativeThumbnailFolder = PMS.getConfiguration().getAlternateThumbFolder();
				if (new File(alternativeThumbnailFolder).isDirectory() == false)
				{
					resourcePath = alternativeThumbnailFolder + File.separator;
					shouldCheckForAlternatives = false;
				}
			}
		
			if (shouldCheckForAlternatives == false)
			{
				break;
			}
			shouldCheckForAlternatives = false;
		} while(true);
	
		// No thumbnail found? Try to grab the local folder's thumbnail.
		if (foundThumbnail == null || foundThumbnail.isFile() == false)
		{
			foundThumbnail = new File(file.getParent() + File.separator + "folder.png"); //$NON-NLS-1$

			if (foundThumbnail.isFile() == false)
			{
				foundThumbnail = new File(file.getParent() + File.separator + "folder.jpg"); //$NON-NLS-1$
			}
		}
	
		// Still no thumbnail found? Just resort to default in original method.
		if (foundThumbnail == null || foundThumbnail.isFile() == false)
		{
			return super.getThumbnailInputStream();
		}
		else
		{
			return new FileInputStream(foundThumbnail);
		}
	}
}

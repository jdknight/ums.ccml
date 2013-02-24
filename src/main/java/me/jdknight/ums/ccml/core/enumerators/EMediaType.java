/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core.enumerators;

import net.pms.formats.Format;

import me.jdknight.ums.ccml.lang.Messages;

/**
 * Media types.
 */
public enum EMediaType
{
	/**
	 * A video type.
	 **/
	VIDEO("Video", Messages.getString("EMediaType.VIDEO")), //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * An audio type.
	 **/
	AUDIO("Audio", Messages.getString("EMediaType.AUDIO")), //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * An image type.
	 **/
	IMAGE("Image", Messages.getString("EMediaType.PHOTO")), //$NON-NLS-1$ //$NON-NLS-2$
	
	/** 
	 * Unknown type.
	 **/
	UNKNOWN("Unknown", "?"); //$NON-NLS-1$ //$NON-NLS-2$
	
	/** 
	 * The display name for this media type.
	 **/
	private String _displayName;
	
	/**
	 * The English name for this media type.
	 **/
	private String _englishName;

	/**
	 * Initializes a new instance of EMediaType.
	 * 
	 * @param englishName The English name for this media type.
	 * @param displayName The display name for this media type.
	 */
	private EMediaType(String englishName, String displayName)
	{
		_englishName = englishName;
		_displayName = displayName;
	}
	
	/**
	 * Return the display name for this media type.
	 * 
	 * @return The display name.
	 */
	public String getDisplayName()
	{
		return _displayName;
	}
	
	/**
	 * Return the English name for this media type.
	 * 
	 * <p>
	 * This name is used to provide a 'standard' text mapping for a media type.
	 * </p>
	 * 
	 * @return The English name.
	 */
	public String getEnglishName()
	{
		return _englishName;
	}
	
	/**
	 * Return the media type based on the format.
	 * 
	 * @param format The format.
	 * @return        The media type.
	 */
	public static EMediaType get(Format format)
	{
		EMediaType type = EMediaType.UNKNOWN;
		if (format != null)
		{
			switch(format.getType())
			{
				case Format.VIDEO:
					type = EMediaType.VIDEO;
					break;
	
				case Format.AUDIO:
					type = EMediaType.AUDIO;
					break;
	
				case Format.IMAGE:
					type = EMediaType.IMAGE;
					break;
			}
		}

		return type;
	}
}
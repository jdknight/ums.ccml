/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Byte order mark utility class.
 * 
 * <p>
 * Used to help interpret a file's encoding.
 * </p>
 */
public final class ByteOrderMarkHelper
{
	/**
	 * File encoding type: UTF-8.
	 */
	private static final String FILE_FORMAT_UTF8 = "UTF-8"; //$NON-NLS-1$

	/**
	 * File encoding type: UTF-16 (Little Endian).
	 */
	private static final String FILE_FORMAT_UTF16LE = "UTF-16LE"; //$NON-NLS-1$

	/**
	 * File encoding type: UTF-16 (Big Endian).
	 */
	private static final String FILE_FORMAT_UTF16BE = "UTF-16BE"; //$NON-NLS-1$

	/**
	 * File encoding type: UTF-32 (Little Endian).
	 */
	private static final String FILE_FORMAT_UTF32LE = "UTF-32LE"; //$NON-NLS-1$

	/**
	 * File encoding type: UTF-32 (Big Endian).
	 */
	private static final String FILE_FORMAT_UTF32BE = "UTF-32BE"; //$NON-NLS-1$
	
	/**
	 * Private constructor.
	 **/
	private ByteOrderMarkHelper() {}
	
	/**
	 * Interpret the encoding of the provided file.
	 * 
	 * @param file The file.
	 * @return     The character encoding; null if unknown.
	 */
	public static String getEncoding(File file)
	{
		// Read the first initial bytes of a file and attempt to interpret it's encoding.
		//
		// See: http://en.wikipedia.org/wiki/Byte_order_mark
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			switch(reader.read())
			{
				case 0xEF:
					if (reader.read() == 0xBB && reader.read() == 0xBF)
					{
							return FILE_FORMAT_UTF8;
					}
					break;
					
				case 0xFE:
					if ( reader.read() == 0xFF )
					{
							return FILE_FORMAT_UTF16BE;
					}
					break;
					
				case 0xFF:
					if ( reader.read() == 0xFE )
					{
						if ( reader.read() == 0x00 && reader.read() == 0x00 )
						{
							return FILE_FORMAT_UTF32LE;
						}
						else
						{
							return FILE_FORMAT_UTF16LE;
						}
					}
					break;
					
				case 0x00:
					if ( reader.read() == 0x00 && reader.read() == 0xFE && reader.read() == 0xFF )
					{
						return FILE_FORMAT_UTF32BE;
					}
					break;
			}
		}
		catch (IOException e)
		{
			// Ignore issues with opening a file.
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					// Ignore issues with closing a file.
				}
			}
		}
		
		return null;
	}
}
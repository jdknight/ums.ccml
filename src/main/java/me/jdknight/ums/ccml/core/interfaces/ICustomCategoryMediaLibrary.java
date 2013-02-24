/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core.interfaces;

import java.util.List;
import java.util.Map;

import me.jdknight.ums.ccml.core.enumerators.EMediaType;
import net.pms.dlna.virtual.VirtualFolder;

/**
 * A custom category media library.
 * 
 * <p>
 * This library is used to keep track of categories while scanning meta information. The category types 
 * will be tracked when interacting with this library. Once all meta information has been tracked, one 
 * can acquire a compiled list of category types from this library.
 * </p>
 */
public interface ICustomCategoryMediaLibrary
{
	/**
	 * Acquire the category type for the provided media type and category type name.
	 * 
	 * <p>
	 * Usage of this method will result in a unique category type for each unique 
	 * category type name. For example, calling this method twice with the same 
	 * category type name will return the same category type.
	 * </p>
	 * 
	 * <p>
	 * Assumes an uncategorized master category type.
	 * </p>
	 * 
	 * @param mediaType        The media type.
	 * @param categoryTypeName The name of the category type.
	 * @return                 The category type.
	 */
	public IMediaCategoryType acquireCategoryType(EMediaType mediaType, String categoryTypeName);

	/**
	 * Acquire the category type for the provided media type and category type name.
	 * 
	 * <p>
	 * Usage of this method will result in a unique category type for each unique 
	 * category type name. For example, calling this method twice with the same 
	 * category type name will return the same category type.
	 * </p>
	 * 
	 * @param mediaType              The media type.
	 * @param masterCategoryTypeName The name of the master category type.
	 * @param categoryTypeName       The name of the category type.
	 * @return                       The category type.
	 */
	public IMediaCategoryType acquireCategoryType(EMediaType mediaType, String masterCategoryTypeName, String categoryTypeName);
	
	/**
	 * Return whether or not the media library has any content of the provided media type.
	 * 
	 * @param mediaType The media type.
	 * @return          True, if this library has content of this media type; false otherwise.
	 */
	public boolean hasContent(EMediaType mediaType);

	/**
	 * Return a list of master categories for a given media type.
	 * 
	 * @param mediaType The media type.
	 * @return          A list of master categories.
	 */
	public List<String> getMasterCategories(EMediaType mediaType);
	
	/**
	 * Return a list of media categories for a given media type and master category type.
	 * 
	 * @param mediaType          The media type.
	 * @param masterCategoryType The master category type.
	 * @return                   A list of media categories; null if the no category exists for the provided 
	 *                           master category type.
	 */
	public List<String> getMediaCategoriesForMasterCategoryType(EMediaType mediaType, String masterCategoryType);

	/**
	 * Return the master category map for a given type.
	 * 
	 * @param mediaType The media type.
	 * @return           A map for master categories.
	 */
	public Map<String, Map<String, IMediaCategoryType>> getMasterCategoryMapByType(EMediaType mediaType);
	
	/**
	 * Build a virtual folder of sorted category types for the provided media type.
	 * 
	 * <p>
	 * All resources tied to this category type will be added as children. Master categories, if present, 
	 * will also be generated.
	 * </p>
	 * 
	 * @param mediaType              The media type.
	 * @param mediaTypeVirtualFolder The virtual folder to add sorted category types to for the provided media type. 
	 */
	public void buildVirtualFolder(EMediaType mediaType, VirtualFolder mediaTypeVirtualFolder);
	
	/**
	 * Reset all category information on this library.
	 */
	public void resetCategories();
}

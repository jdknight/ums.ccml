/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.jdknight.ums.ccml.core.enumerators.EMediaType;
import me.jdknight.ums.ccml.core.interfaces.ICustomCategoryMediaLibrary;
import me.jdknight.ums.ccml.core.interfaces.IMediaCategoryType;
import me.jdknight.ums.ccml.lang.Messages;
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
public class CustomCategoryMediaLibrary implements ICustomCategoryMediaLibrary
{
	/**
	 * A map of category types for a video media type.
	 **/
	private Map<String, Map<String, IMediaCategoryType>> _masterCategoriesVideos;
	
	/**
	 * A map of category types for a audio media type.
	 **/
	private Map<String, Map<String, IMediaCategoryType>> _masterCategoriesAudio;
	
	/**
	 * A map of category types for a image media type.
	 **/
	private Map<String, Map<String, IMediaCategoryType>> _masterCategoriesImages;
	
	/**
	 * A map of category types for unknown media type.
	 **/
	private Map<String, Map<String, IMediaCategoryType>> _masterCategoriesUnknown;
	
	/**
	 * Initializes a new instance of CustomCategoryMediaLibrary.
	 */
	public CustomCategoryMediaLibrary()
	{
		_masterCategoriesVideos = new HashMap<String, Map<String, IMediaCategoryType>>();
		_masterCategoriesAudio = new HashMap<String, Map<String, IMediaCategoryType>>();
		_masterCategoriesImages = new HashMap<String, Map<String, IMediaCategoryType>>();
		_masterCategoriesUnknown = new HashMap<String, Map<String, IMediaCategoryType>>();
	}

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
	@Override
	public IMediaCategoryType acquireCategoryType(EMediaType mediaType, String categoryTypeName)
	{
		return acquireCategoryType(mediaType, null, categoryTypeName);
	}

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
	@Override
	public IMediaCategoryType acquireCategoryType(EMediaType mediaType, String masterCategoryTypeName, String categoryTypeName)
	{
		// Null master category provided? Reference 'Uncategorized' master name.
		if (masterCategoryTypeName == null) 
		{
			masterCategoryTypeName = Messages.getString("CCustomCategoryMediaLibrary.UNCATEGORIZED0"); //$NON-NLS-1$;
		}
		
		// Find the respective map for this master category type.
		Map<String, Map<String, IMediaCategoryType>> masterCategoryMap;
		switch(mediaType)
		{
			case VIDEO:
				masterCategoryMap = _masterCategoriesVideos;
				break;
			
			case AUDIO:
				masterCategoryMap = _masterCategoriesAudio;
				break;

			case IMAGE:
				masterCategoryMap = _masterCategoriesImages;
				break;
				
			default:
				masterCategoryMap = _masterCategoriesUnknown;
				break;
		}

		// Find the master category.
		Map<String, IMediaCategoryType> categoryMap = masterCategoryMap.get(masterCategoryTypeName);
		
		// If the master category does not exist, create a new one.
		if (categoryMap == null)
		{
			categoryMap = new HashMap<String, IMediaCategoryType>();
			masterCategoryMap.put(masterCategoryTypeName, categoryMap);
		}
		
		// Find the category.
		IMediaCategoryType category = categoryMap.get(categoryTypeName);
		
		// If the category does not exist, create a new one.
		if (category == null)
		{
			category = new MediaCategoryType(categoryTypeName);
			categoryMap.put(categoryTypeName, category);
		}

		return category;
	}

	/**
	 * Return whether or not the media library has any content of the provided media type.
	 * 
	 * @param mediaType The media type.
	 * @return          True, if this library has content of this media type; false otherwise.
	 */
	@Override
	public boolean hasContent(EMediaType mediaType)
	{
		switch(mediaType)
		{
			case AUDIO:   return ( _masterCategoriesAudio.size()   > 0 );
			case IMAGE:   return ( _masterCategoriesImages.size()  > 0 );
			case VIDEO:   return ( _masterCategoriesVideos.size()  > 0 );
			case UNKNOWN: return ( _masterCategoriesUnknown.size() > 0 );
		}
		
		return false;
	}
	
	/**
	 * Return a list of master categories for a given media type.
	 * 
	 * @param mediaType The media type.
	 * @return          A list of master categories.
	 */
	@Override
	public List<String> getMasterCategories(EMediaType mediaType)
	{
		// Find the respective map for this category type.
		Map<String,Map<String,IMediaCategoryType>> masterCategoryMap = getMasterCategoryMapByType(mediaType);

		// Get a sorted list of master category types.
		Set<String> masterCategoryTypes =  masterCategoryMap.keySet();
		assert(masterCategoryTypes.size() != 0);
		List<String> sortedMasterCategoryTypes = new ArrayList<String>(masterCategoryTypes);
		Collections.sort(sortedMasterCategoryTypes);
		
		return sortedMasterCategoryTypes;
	}
	
	/**
	 * Return a list of media categories for a given media type and master category type.
	 * 
	 * @param mediaType          The media type.
	 * @param masterCategoryType The master category type.
	 * @return                    A list of media categories; null if the no category exists for the provided 
	 *                            master category type.
	 */
	@Override
	public List<String> getMediaCategoriesForMasterCategoryType(EMediaType mediaType, String masterCategoryType)
	{
		// Find the category types for this master category type.
		Map<String, IMediaCategoryType> categoryTypeMap = getMediaCategoryMapByType(mediaType, masterCategoryType);
		if (categoryTypeMap == null)
		{
			return null;
		}

		// Get a sorted list of category types.
		Set<String> categoryTypes = categoryTypeMap.keySet();
		List<String> sortedCategoryTypes = new ArrayList<String>(categoryTypes);
		Collections.sort(sortedCategoryTypes);
		
		return sortedCategoryTypes;
	}
	
	/**
	 * Return the master category map for a given type.
	 * 
	 * @param mediaType The media type.
	 * @return          A map for master categories.
	 */
	@Override
	public Map<String, Map<String, IMediaCategoryType>> getMasterCategoryMapByType(EMediaType mediaType)
	{
		// Find the respective map for this category type.
		Map<String, Map<String, IMediaCategoryType>> masterCategoryMap;
		switch(mediaType)
		{
			case VIDEO:
				masterCategoryMap = _masterCategoriesVideos;
				break;
			
			case AUDIO:
				masterCategoryMap = _masterCategoriesAudio;
				break;

			case IMAGE:
				masterCategoryMap = _masterCategoriesImages;
				break;

			default:
				masterCategoryMap = _masterCategoriesUnknown;
				break;
		}
		
		return masterCategoryMap;
	}

	/**
	 * Return the master category map for a given type.
	 * 
	 * @param mediaType          The media type.
	 * @param masterCategoryType The master category type.
	 * @return                   A map for media categories; null if a category map does not exist for this type.
	 */
	private Map<String, IMediaCategoryType> getMediaCategoryMapByType(EMediaType mediaType, String masterCategoryType)
	{
		// Find the category types for this master category type.
		Map<String, Map<String, IMediaCategoryType>> masterCategoryMap = getMasterCategoryMapByType(mediaType);
		Map<String, IMediaCategoryType> categoryTypeMap = masterCategoryMap.get(masterCategoryType);
		return categoryTypeMap;
	}
	
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
	@Override
	public void buildVirtualFolder(EMediaType mediaType, VirtualFolder mediaTypeVirtualFolder)
	{
		// Get a sorted list of master category types.
		List<String> sortedMasterCategoryTypes = getMasterCategories(mediaType);
		for(String masterCategoryType : sortedMasterCategoryTypes)
		{
			VirtualFolder baseVirtualFolder;
			
			// Do we have more than one (1) master category? If so, create the master category virtual
			// folders as a base for this sections category types. If not, just reference the 
			// respective media type virtual folder.
			if (sortedMasterCategoryTypes.size() > 1)
			{
				// Create a master category type folder.
				VirtualFolder masterCategoryTypeVirtualFolder = new VirtualFolder(masterCategoryType, null);
				mediaTypeVirtualFolder.addChild(masterCategoryTypeVirtualFolder);
				
				baseVirtualFolder = masterCategoryTypeVirtualFolder;
			}
			else
			{
				baseVirtualFolder = mediaTypeVirtualFolder;
			}
			
			// Find the category types for this master category type.
			Map<String, IMediaCategoryType> categoryTypeMap = getMediaCategoryMapByType(mediaType, masterCategoryType);
			assert(categoryTypeMap != null);
			
			List<String> sortedCategoryTypes = getMediaCategoriesForMasterCategoryType(mediaType, masterCategoryType);
			for(String categoryType : sortedCategoryTypes)
			{
				IMediaCategoryType mediaCategoryType = categoryTypeMap.get(categoryType);
				
				// Create a category type folder.
				VirtualFolder categoryTypeVirtualFolder = mediaCategoryType.generateVirtualFolder();
				baseVirtualFolder.addChild(categoryTypeVirtualFolder);
			}
		}
	}
	
	/**
	 * Reset all media category types on this library.
	 */
	@Override
	public void resetCategories()
	{
		_masterCategoriesVideos.clear();
		_masterCategoriesAudio.clear();
		_masterCategoriesImages.clear();
		_masterCategoriesUnknown.clear();
	}
}

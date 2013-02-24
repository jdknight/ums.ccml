/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jdknight.ums.ccml.core.interfaces.IMediaCategoryType;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;

/**
 * A media category type.
 * 
 * <p>
 * A media category type belongs to a known media type (such as a video type). 
 * A media type may have many categories and these category may have resources belonging to them.
 * </p>
 * 
 * <pre>
 * For example:
 *  -> Video                      (Media Type)
 *     -> Genre                   (Media Category Type)
 *        -> Action               (Media Category)
 *           -> MyActionMovie.avi (Media)
 * </pre>
 */
public class MediaCategoryType implements IMediaCategoryType
{
	/**
	 * The name of this category type.
	 **/
	private String _name;

	/**
	 * The map of DLNA resources for known categories in master categories.
	 **/
	private Map<String, List<DLNAResource>> _resources = new HashMap<String, List<DLNAResource>>();

	/**
	 * Logger.
	 **/
	private static final Logger _logger = LoggerFactory.getLogger(MediaCategoryType.class);

	/**
	 * Initializes a new instance of MediaCategoryType.
	 * 
	 * @param name The name of this category type.
	 */
	public MediaCategoryType(String name)
	{
		_name = name;
	}

	/**
	 * Return the name of this category type.
	 * 
	 * @return The name.
	 */
	@Override
	public String getCategoryTypeName()
	{
		return _name;
	}

	/**
	 * Add a resource to this media category type.
	 * 
	 * @param resource     The resource to add.
	 * @param categoryName The name of this category to add the resource to.
	 * 
	 * @throws NullPointerException     Thrown if the provided resource is null or the provided category is null.
	 * @throws IllegalArgumentException Thrown if the provided category is empty.
	 */
	@Override
	public void addResource(DLNAResource resource, String categoryName)
	{
		if (resource == null || categoryName == null)
		{
			throw new NullPointerException();
		}

		if (categoryName.isEmpty() == true)
		{
			throw new IllegalArgumentException();
		}

		// Find the category to add this resource to.
		List<DLNAResource> categoryResources = _resources.get(categoryName);
		if (categoryResources == null)
		{
			// Create new list.
			categoryResources = new ArrayList<DLNAResource>();
			_resources.put(categoryName, categoryResources);
		}
		else
		{
			// Ignore duplicates.
			if (categoryResources.contains(resource) == true)
			{
				return;
			}
		}

		_logger.trace("[CCML] Adding the following resource to category '" + categoryName + "': " + resource.getDisplayName()); //$NON-NLS-1$ //$NON-NLS-2$
		categoryResources.add(resource);
	}

	/**
	 * Return a map of all resources added to this category type.
	 * 
	 * @return The map of resources.
	 */
	@Override
	public Map<String, List<DLNAResource>> getResources()
	{
		return _resources;
	}

	/**
	 * Generate a virtual folder for this media category.
	 * 
	 * <p>
	 * All resources tied to this category type will be added as children.
	 * </p>
	 * 
	 * @return The generated virtual folder.
	 */
	@Override
	public VirtualFolder generateVirtualFolder()
	{
		Comparator<DLNAResource> dlnaResourceComparator = new Comparator<DLNAResource>()
		{
			@Override
			public int compare(DLNAResource argument1, DLNAResource argument2)
			{
				return argument1.getDisplayName().compareToIgnoreCase(argument2.getDisplayName());
			}
		};
		
		// Create the initial category type virtual folder.
		VirtualFolder virtualFolder = new VirtualFolder(_name, null);

		// Sort category names.
		Set<String> categoryTypes = _resources.keySet();
		List<String> sortedCategoryTypes = new ArrayList<String>(categoryTypes);
		Collections.sort(sortedCategoryTypes);

		for(String categoryType : sortedCategoryTypes)
		{
			// Create a category folder.
			VirtualFolder categoryVirtualFolder = new VirtualFolder(categoryType, null);
			virtualFolder.addChild(categoryVirtualFolder);

			// Sort all resources for this category by name.
			List<DLNAResource> categoryResources = _resources.get(categoryType);
			assert(categoryResources != null);
			Collections.sort(categoryResources, dlnaResourceComparator);

			// Add each resource to the category folder.
			for(DLNAResource resource : categoryResources)
			{
				categoryVirtualFolder.addChild(resource);
			}
		}
			
		return virtualFolder;
	}
}

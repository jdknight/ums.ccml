/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core.interfaces;

import java.util.List;
import java.util.Map;

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
public interface IMediaCategoryType
{
	/**
	 * Return the name of this category type.
	 * 
	 * @return The name.
	 */
	public String getCategoryTypeName();
	
	/**
	 * Add a resource to this media category type.
	 * 
	 * @param resource     The resource to add.
	 * @param categoryName The name of this category to add the resource to.
	 * 
	 * @throws NullPointerException     Thrown if the provided resource is null or the provided category is null.
	 * @throws IllegalArgumentException Thrown if the provided category is empty.
	 */
	public void addResource(DLNAResource resource, String categoryName);
	
	/**
	 * Return a map of all resources added to this category type.
	 * 
	 * @return The map of resources.
	 */
	public Map<String, List<DLNAResource>> getResources();

	/**
	 * Generate a virtual folder for this media category.
	 * 
	 * <p>
	 * All resources tied to this category type will be added as children.
	 * </p>
	 * 
	 * @return The generated virtual folder.
	 */
	public VirtualFolder generateVirtualFolder();
}

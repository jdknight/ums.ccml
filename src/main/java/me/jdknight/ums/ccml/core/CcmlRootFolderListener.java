/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jdknight.ums.ccml.core.enumerators.EMediaType;
import me.jdknight.ums.ccml.core.interfaces.ICustomCategoryMediaLibrary;
import me.jdknight.ums.ccml.core.interfaces.IMediaCategoryType;
import me.jdknight.ums.ccml.core.interfaces.IVirtualFolderMediaResources;
import me.jdknight.ums.ccml.tmp.RealFileWithVirtualFolderThumbnails;
import me.jdknight.ums.ccml.ui.CcmlPanel;
import me.jdknight.ums.ccml.ui.ParsingBuildingWarningDialog;
import me.jdknight.ums.ccml.util.ByteOrderMarkHelper;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.formats.Format;

/**
 * Root folder external listener for the media server.
 */
public class CcmlRootFolderListener implements AdditionalFolderAtRoot
{
	/**
	 * Name for the root folder.
	 **/
	public final static String ROOT_FOLDER_NAME = "#- CCML -#"; //$NON-NLS-1$
	
	/**
	 * Name of folder meta file.
	 **/
	public final static String FOLDER_FOLDER_NAME = "folder.meta"; //$NON-NLS-1$

	/**
	 * Special category type name - master.
	 **/
	final String SPECIAL_CATEGORY_TYPE_NAME_MASTER = "Master"; //$NON-NLS-1$

	/**
	 * Special category type name - filter.
	 **/
	final String SPECIAL_CATEGORY_TYPE_NAME_FILTER = "Filter"; //$NON-NLS-1$
	
	/**
	 * The library.
	 **/
	private ICustomCategoryMediaLibrary _library;

	/**
	 * The base we generate at the root.
	 **/
	private VirtualFolder _baseFolder;
	
	/** Logger. */
	private static final Logger _logger = LoggerFactory.getLogger(CcmlRootFolderListener.class);
	
	/**
	 * Instance loading.
	 */
	static
	{
		_logger.info("[CCML] Loading plugin."); //$NON-NLS-1$
		
		// Load configuration.
		CcmlConfiguration.getInstance().load();
	}
	
	/**
	 * Initializes a new instance of CcmlRootFolderListener.
	 */
	public CcmlRootFolderListener()
	{
		// Pre-information.
		long startTimestamp = System.currentTimeMillis();
		_logger.info("[CCML] Parsing library (this may take awhile)..."); //$NON-NLS-1$
		ParsingBuildingWarningDialog dialog = null;

		// Notify user if parsing takes awhile.
		if (CcmlConfiguration.getInstance().isHidingParsingBuildingNotification() == false)
		{
			dialog = ParsingBuildingWarningDialog.queueParsingWarning();
		}

		// Parse meta library.
		_library = parseMetaLibrary();

		// Post-information.
		long endTimestamp = System.currentTimeMillis();
		long totalOffset = endTimestamp - startTimestamp;
		_logger.info("[CCML] Completed parsing library (" + getOffsetDisplay(totalOffset) + ")."); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog != null)
		{
			dialog.terminateDialog();
		}
	}

	/**
	 * Invoked when the plugin component is requested.
	 */
	@Override
	public JComponent config()
	{
		return new CcmlPanel(_library);
	}

	/**
	 * Return the name of this plugin.
	 */
	@Override
	public String name()
	{
		return "Custom Category Media Library"; //$NON-NLS-1$
	}

	/**
	 * Invoked when the media server is shutdown.
	 */
	@Override
	public void shutdown()
	{
		// Do nothing.
	}

	/**
	 * Create our custom root virtual folder.
	 * 
	 * @return The virtual folder DLNA resource.
	 */
	@Override
	public DLNAResource getChild()
	{
		// Already generated the base folder?
		if (_baseFolder != null)
		{
			return _baseFolder;
		}

		// Pre-information.
		long startTimestamp = System.currentTimeMillis();
		_logger.info("[CCML] Building virtual library (this may take awhile)..."); //$NON-NLS-1$
		ParsingBuildingWarningDialog dialog = null;

		// Notify user if parsing takes awhile.
		if (CcmlConfiguration.getInstance().isHidingParsingBuildingNotification() == false)
		{
			dialog = ParsingBuildingWarningDialog.queueBuildingWarning();
		}
		
		_baseFolder = new VirtualFolder(ROOT_FOLDER_NAME, null);
		buildBaseFolder();

		// Post-information.
		long endTimestamp = System.currentTimeMillis();
		long totalOffset = endTimestamp - startTimestamp;
		_logger.info("[CCML] Completed building virtual library (" + getOffsetDisplay(totalOffset) + ")."); //$NON-NLS-1$ //$NON-NLS-2$
		if (dialog != null)
		{
			dialog.terminateDialog();
		}
		
		return _baseFolder;
	}
	
	/**
	 * Parse the meta library for categorizations.
	 */
	private ICustomCategoryMediaLibrary parseMetaLibrary()
	{
		// Compile a list of shared directories to scan.
		File[] sharedDirectories = LazyCompatibility.getSharedDirectories();
		if (sharedDirectories == null || sharedDirectories.length == 0)
		{
			sharedDirectories = File.listRoots();
		}

		// Create library.
		ICustomCategoryMediaLibrary library = new CustomCategoryMediaLibrary();
		if (sharedDirectories != null && sharedDirectories.length > 0)
		{
			for(File sourceDirectory : sharedDirectories)
			{
				if (sourceDirectory.isDirectory() == true)
				{
					buildMediaLibrary(library, sourceDirectory);
				}
			}
		}
		
		return library;
	}
	
	/**
	 * Build the contents of the base folder.
	 */
	private void buildBaseFolder()
	{
		assert(_library != null);

		// Count how many base sections that we are using.
		// 
		// If the count is only one (1), prevent building a base section for 
		// the single media type we are using.
		int usedSectionsCount = 0;
		EMediaType[] mediaTypes = EMediaType.values();
		for(EMediaType mediaType : mediaTypes)
		{
			if (mediaType == EMediaType.UNKNOWN)
			{
				continue;
			}
			
			if (_library.hasContent(mediaType) == true)
			{
				usedSectionsCount++;
			}
		}

		// Create section(s).
		for(EMediaType mediaType : mediaTypes)
		{
			if (mediaType == EMediaType.UNKNOWN)
			{
				continue;
			}

			if (_library.hasContent(mediaType) == true)
			{
				VirtualFolder section;
				if (usedSectionsCount > 1)
				{
					section = new VirtualFolder(mediaType.getDisplayName(), null);
					_baseFolder.addChild(section);
				}
				else
				{
					section = _baseFolder;
				}
				
				_library.buildVirtualFolder(mediaType, section);
			}
		}
	}
	
	/**
	 * Build the media library on the provided library based off the provided base directory. 
	 * 
	 * @param library   The library to add to.
	 * @param directory The directory to scan.
	 */
	private void buildMediaLibrary(ICustomCategoryMediaLibrary library, File directory)
	{
		assert(directory.isDirectory() == true);

		File[] directoryChildren = directory.listFiles();
		if (directoryChildren != null)
		{
			for(File child : directoryChildren)
			{
				if (child.isFile() == true)
				{
					// Find if this file is a supported media type.
					RealFileWithVirtualFolderThumbnails mediaResource = new RealFileWithVirtualFolderThumbnails(child);
	
					Format mediaFormat = LazyCompatibility.getAssociatedExtension(child.getPath());
					EMediaType mediaType = EMediaType.get(mediaFormat);
					
					// Ignore unsupported media types.
					if (mediaType == EMediaType.UNKNOWN)
					{
						continue;
					}
	
					// Check if a meta file exists.
					String metaFilePath = mediaResource.getFile().getPath() + ".meta"; //$NON-NLS-1$
					File metaFile = new File(metaFilePath);
					
					// No meta file? Check the alternative folder (if any is provided).
					if (metaFile.isFile() == false)
					{
						String alternativeFolderPath = CcmlConfiguration.getInstance().getAlternativeMetaFolder();
						if (alternativeFolderPath != null)
						{
							File alternativeFolder = new File(alternativeFolderPath);
							if (alternativeFolder.isDirectory() == true)
							{
								String originalFileName = metaFile.getName();
								metaFile = new File(alternativeFolder, originalFileName);
							}
						}
						
					}

					// Still no meta file? Ignore.
					if (metaFile.isFile() == false)
					{
						continue;
					}
	
					_logger.trace("[CCML] Parsing meta file: " + metaFile); //$NON-NLS-1$
					
					Map<String, List<String>> mapOfCategories = parseMetaFile(metaFile);
					if (mapOfCategories == null)
					{
						continue;
					}
	
					// Attempt to find this meta file's master reference(s).
					String[] masterSections = stripSpecialValues(mapOfCategories, metaFile, SPECIAL_CATEGORY_TYPE_NAME_MASTER);
					
					// Strip out any filter entries; they are not used on single meta file.
					stripSpecialValues(mapOfCategories, metaFile, SPECIAL_CATEGORY_TYPE_NAME_FILTER);
					
					// Add resources to a respective media category type.
					Set<Entry<String, List<String>>> categorySet = mapOfCategories.entrySet();
					if (categorySet.isEmpty() == false)
					{
						for(String masterSection : masterSections)
						{
							for(Entry<String, List<String>> categoryReference : categorySet)
							{
								// Find/create category.
								String categoryName = categoryReference.getKey();
								IMediaCategoryType category = library.acquireCategoryType(mediaType, masterSection, categoryName);
		
								// Add resource to it.
								List<String> categoryValues = categoryReference.getValue();
								for(String categoryValue : categoryValues)
								{
									category.addResource(new RealFileWithVirtualFolderThumbnails(mediaResource), categoryValue);
		
									_logger.trace("[CCML] Adding resource to category a '" + categoryName + "' with a value of '" + categoryValue + "'" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											( masterSection != null ? " (Master: " + masterSection + ")" : "" ) + ": " + metaFile); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								}
							}
						}
					}
					else
					{
						_logger.warn("[CCML] The following meta file does not have any defined categories: " + metaFile); //$NON-NLS-1$
					}
				}
				else
				{
					// Recursive - scan folder for more resources.
					buildMediaLibrary(library, child);
				}
			}
		}

		// We will check if this folder has a meta file for its contents.
		//
		// Check if a folder meta file exists.
		File folderMetaFile = new File(directory, FOLDER_FOLDER_NAME);
		
		// No meta file? Ignore.
		if (folderMetaFile.isFile() == false)
		{
			return;
		}
		
		_logger.trace("[CCML] Parsing folder meta file: " + folderMetaFile); //$NON-NLS-1$
		
		Map<String, List<String>> mapOfCategories = parseMetaFile(folderMetaFile);
		if (mapOfCategories == null)
		{
			return;
		}

		// Attempt to find this meta file's master reference(s).
		String[] masterSections = stripSpecialValues(mapOfCategories, folderMetaFile, SPECIAL_CATEGORY_TYPE_NAME_MASTER);

		// Find if this meta file is specific to any media types.
		String[] filterValues = stripSpecialValues(mapOfCategories, folderMetaFile, SPECIAL_CATEGORY_TYPE_NAME_FILTER);

		boolean isFirstFilterAdded = false;
		EnumSet<EMediaType> mediaTypeFilter = EnumSet.allOf(EMediaType.class);
		for(String filterValue : filterValues)
		{
			if (filterValue != null)
			{
				EMediaType[] mediaTypes = EMediaType.values();
				for(EMediaType mediaType : mediaTypes)
				{
					if (mediaType.getEnglishName().equalsIgnoreCase(filterValue) == true || mediaType.getDisplayName().equalsIgnoreCase(filterValue) == true)
					{
						// If we have actual content to filter, start fresh.
						if (isFirstFilterAdded == false)
						{
							mediaTypeFilter = EnumSet.noneOf(EMediaType.class);
							isFirstFilterAdded = true;
						}
						
						mediaTypeFilter.add(mediaType);
						break;
					}
				}
			}
		}

		// Compile a list of media resources for this folder.
		IVirtualFolderMediaResources mediaResourcePoint = getVirtualFolderForDirectoryMedia(directory);
		DLNAResource resource = mediaResourcePoint.getVirtualFolder();

		// Add resources to a respective media category type.
		Set<Entry<String, List<String>>> categorySet = mapOfCategories.entrySet();
		if (categorySet.isEmpty() == false)
		{
			for(String masterSection : masterSections)
			{
				for(Entry<String, List<String>> categoryReference : categorySet)
				{
					EnumSet<EMediaType> mediaTypes = mediaResourcePoint.getMediaType();
					if (mediaTypes.isEmpty() == false)
					{
						for(EMediaType mediaType : mediaTypes)
						{
							// This media type filtered? If so, next.
							if (mediaTypeFilter.contains(mediaType) == false)
							{
								continue;
							}
							
							// Find/create category.
							String categoryName = categoryReference.getKey();
							IMediaCategoryType category = library.acquireCategoryType(mediaType, masterSection, categoryName);
	
							// Add resource to it.
							List<String> categoryValues = categoryReference.getValue();
							for(String categoryValue : categoryValues)
							{
								category.addResource(resource, categoryValue);
	
								_logger.trace("[CCML] Adding resource to category a '" + categoryName + "' with a value of '" + categoryValue + "'" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										( masterSection != null ? " (Master: " + masterSection + ")" : "" ) + ": " + folderMetaFile); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							}
						}
					}
					else
					{
						_logger.warn("[CCML] The following folder meta file does not have any content to reference: " + folderMetaFile); //$NON-NLS-1$
					}
				}
			}
		}
		else
		{
			_logger.warn("[CCML] The following folder meta file does not have any defined categories: " + folderMetaFile); //$NON-NLS-1$
		}
	}

	/**
	 * Return a virtual folder for all media resources in the provided directory.
	 * 
	 * @param directory The directory to search on.
	 */
	private IVirtualFolderMediaResources getVirtualFolderForDirectoryMedia(File directory)
	{
		assert(directory.isDirectory() == true);

		VirtualFolder folderSection = new VirtualFolder(directory.getName(), null);
		EnumSet<EMediaType> mediaTypes = EnumSet.noneOf(EMediaType.class);

		File[] directoryChildren = directory.listFiles();
		if (directoryChildren != null)
		{
			for(File child : directoryChildren)
			{
				if (child.isFile() == true)
				{
					// Find if this file is a supported media type.
					EMediaType mediaType = EMediaType.UNKNOWN;
					RealFileWithVirtualFolderThumbnails mediaResource = new RealFileWithVirtualFolderThumbnails(child);
	
					Format mediaFormat = LazyCompatibility.getAssociatedExtension(child.getPath());
					if (mediaFormat == null)
					{
						continue;
					}
					
					switch(mediaFormat.getType())
					{
						case Format.VIDEO:
							mediaType = EMediaType.VIDEO;
							break;
	
						case Format.AUDIO:
							mediaType = EMediaType.AUDIO;
							break;
	
						case Format.IMAGE:
							mediaType = EMediaType.IMAGE;
							break;
					}
	
					// Ignore unsupported media types.
					if (mediaType == EMediaType.UNKNOWN)
					{
						continue;
					}
					
					mediaTypes.add(mediaType);
					folderSection.addChild(mediaResource);
				}
				else
				{
					IVirtualFolderMediaResources subFolderSection = getVirtualFolderForDirectoryMedia(child);
	
					mediaTypes.addAll(subFolderSection.getMediaType());
					folderSection.addChild(subFolderSection.getVirtualFolder());
				}
			}
		}
		
		return new VirtualFolderMediaResources(folderSection, mediaTypes);
	}
	
	/**
	 * Parse a meta value for category type mappings.
	 * 
	 * @param metaFile The meta file to parse.
	 * @return         The map of category types to category values.
	 */
	private Map<String,List<String>> parseMetaFile(File metaFile)
	{
		BufferedReader reader = null;
		try
		{
			// Load file based on known encoding.
			String characterEncoding = ByteOrderMarkHelper.getEncoding(metaFile);
			if (characterEncoding != null)
			{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(metaFile), characterEncoding));
				reader.read();
			}
			else
			{
				reader = new BufferedReader(new FileReader(metaFile));
			}
			
			Map<String, List<String>> mapOfCategories = new HashMap<String, List<String>>();
			
			// Parse the meta file for category types to category mappings.
			String rawCategoryDefine;
			while ((rawCategoryDefine = reader.readLine()) != null)
			{
				String[] propertyAndValue = rawCategoryDefine.split("=", 2); //$NON-NLS-1$
				if (propertyAndValue.length != 2)
				{
					continue;
				}

				String categoryName = WordUtils.capitalize(propertyAndValue[0]);
				String categoryValue = WordUtils.capitalize(propertyAndValue[1]);
				if (categoryName.isEmpty() == true || categoryValue.isEmpty() == true)
				{
					continue;
				}

				List<String> categoryValuesList = mapOfCategories.get(categoryName);
				if (categoryValuesList == null)
				{
					categoryValuesList = new ArrayList<String>();
					mapOfCategories.put(categoryName, categoryValuesList);
				}
				
				categoryValuesList.add(categoryValue);
			}
			
			return mapOfCategories;
		}
		catch (FileNotFoundException e)
		{
			_logger.error("[CCML] Error opening meta file: " + metaFile); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			_logger.error("[CCML] Error parsing meta file: " + metaFile); //$NON-NLS-1$
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
	
	/**
	 * Extract and remove the special category type values from a map of categories.
	 * 
	 * @param mapOfCategories         The map of categories to scan and alter if found.
	 * @param metaFile                The meta file this map of categories was generated from.
	 * @param specialCategoryTypeName The special category type name.
	 * @return                        The special category type values; if no special category types are defined 
	 *                                an array containing a single null value will be returned.
	 */
	private String[] stripSpecialValues(Map<String,List<String>> mapOfCategories, File metaFile, String specialCategoryTypeName)
	{
		String[] specialCategories = new String[] { null };
		List<String> masterSection = mapOfCategories.get(specialCategoryTypeName);
		if (masterSection != null)
		{
			specialCategories = masterSection.toArray(new String[0]);
			
			mapOfCategories.remove(specialCategoryTypeName);
		}
		
		return specialCategories;
	}
	
	/**
	 * Return a display value for the provided offset.
	 * 
	 * @param offset The offset.
	 * @return       The display value.
	 */
	private String getOffsetDisplay(long offset)
	{
		String timeValue;
		if (offset < 1000)
		{
			timeValue = "less than a second"; //$NON-NLS-1$
		}
		else
		{
			offset /= 1000;
			if (offset < 60)
			{
				timeValue = (offset == 1 ? "one second" : offset + " seconds"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				offset /= 60;
				timeValue = (offset == 1 ? "one minute" : offset + " minutes"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		return timeValue;
	}
}

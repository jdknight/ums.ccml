/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jdknight.ums.ccml.core.LazyCompatibility;
import me.jdknight.ums.ccml.core.CcmlRootFolderListener;
import me.jdknight.ums.ccml.core.enumerators.EMediaType;
import me.jdknight.ums.ccml.core.interfaces.ICustomCategoryMediaLibrary;
import me.jdknight.ums.ccml.core.interfaces.IMediaCategoryType;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.TranscodeVirtualFolder;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;

/**
 * Panel to display currently parsed meta data.
 */
@SuppressWarnings("serial")
public class MetaStructurePanel extends JPanel
{
	/**
	 * Logger.
	 **/
	private static final Logger _logger = LoggerFactory.getLogger(MetaStructurePanel.class);
	
	/**
	 * Initializes a new instance of MetaStructurePanel.
	 * 
	 * @param library The library.
	 */
	public MetaStructurePanel(ICustomCategoryMediaLibrary library)
	{
        GridBagConstraints constraints;
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        
		// Build the meta category structure.
	    DefaultMutableTreeNode rootFolder = new DefaultMutableTreeNode(CcmlRootFolderListener.ROOT_FOLDER_NAME);
	    buildTree(library, rootFolder);
        
        // Create the tree to render the meta structure.
        JTree rootFolderTree = new JTree(rootFolder);
        JScrollPane rootFolderTreeScroller = new JScrollPane(rootFolderTree);

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		add(rootFolderTreeScroller, constraints);
		
		// Support a double-click operation to open a resource path using the systems file manager.
		rootFolderTree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent event)
			{
				// Only listen for double-clicks.
				if (event.getClickCount() != 2)
				{
					return;
				}
				
				// Attempt to find the file resource for this node.
				MediaResource resource = null;
				JTree tree = (JTree) event.getSource();
				TreePath treePath = tree.getPathForLocation(event.getX(), event.getY());
				if (treePath != null)
				{
					Object lastComponent = treePath.getLastPathComponent();
					if (lastComponent instanceof DefaultMutableTreeNode)
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) lastComponent;
						Object o = node.getUserObject();
						if (o instanceof MediaResource)
						{
							resource = (MediaResource) o;
						}
					}
				}
				
				// No resource? Ignore.
				if (resource == null)
				{
					return;
				}

				// Attempt to find an open action for this OS.
				String openAction;
				String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
				     if (osName.contains("win") == true) openAction = "explorer /select," + resource.getResourcePath(); //$NON-NLS-1$ //$NON-NLS-2$
				else if (osName.contains("mac") == true) openAction = "open -R "          + resource.getResourcePath(); //$NON-NLS-1$ //$NON-NLS-2$
				else if (osName.contains("nix") == true) openAction = "nautilus "         + resource.getResourcePath(); //$NON-NLS-1$ //$NON-NLS-2$ // May not always work.
				else
				{
					return;
				}
				
				try
				{
					_logger.debug("[CCML] Attempting to open path: " + resource.getResourcePath()); //$NON-NLS-1$
					Runtime.getRuntime().exec(openAction);
				}
				catch (IOException e)
				{
					// Ignore if the file could not be explored to.
				}
			}
		});
	}
	
	/**
	 * Build the descriptive tree to reflect the current library.
	 * 
	 * @param library  The library.
	 * @param rootNode The root to attach onto.
	 */
	private void buildTree(ICustomCategoryMediaLibrary library, DefaultMutableTreeNode rootNode)
	{
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
			
			if (library.hasContent(mediaType) == true)
			{
				usedSectionsCount++;
			}
		}

		// Create sections.
		for(EMediaType mediaType : mediaTypes)
		{
			if (mediaType == EMediaType.UNKNOWN)
			{
				continue;
			}
			
			DefaultMutableTreeNode sectionNode = rootNode;
			if (library.hasContent(mediaType) == true)
			{
				if (usedSectionsCount > 1)
				{
					sectionNode = new DefaultMutableTreeNode(mediaType.getDisplayName());
					rootNode.add(sectionNode);
				}
				
				// Master categories.
				Map<String, Map<String, IMediaCategoryType>> masterMap = library.getMasterCategoryMapByType(mediaType);
				Set<String> masterCategories = masterMap.keySet();
				List<String> sortedMasterCategories = new ArrayList<String>(masterCategories);
				Collections.sort(sortedMasterCategories);

				for(String masterCategory : sortedMasterCategories)
				{
					DefaultMutableTreeNode masterCategoryNode = new DefaultMutableTreeNode(masterCategory);
					sectionNode.add(masterCategoryNode);

					// Category types.
					Map<String, IMediaCategoryType> categoryTypeMap = masterMap.get(masterCategory);
					Set<String> categoryTypes = categoryTypeMap.keySet();
					List<String> sortedCategoryTypes = new ArrayList<String>(categoryTypes);
					Collections.sort(sortedCategoryTypes);

					for(String mediaCategoryType : sortedCategoryTypes)
					{
						DefaultMutableTreeNode categoryTypeNode = new DefaultMutableTreeNode(mediaCategoryType);
						masterCategoryNode.add(categoryTypeNode);

						// Categories.
						IMediaCategoryType categoryType = categoryTypeMap.get(mediaCategoryType);
						Map<String, List<DLNAResource>> categoryTypeResourcesMap = categoryType.getResources();
						Set<String> categories = categoryTypeResourcesMap.keySet();
						List<String> sortedCategories = new ArrayList<String>(categories);
						Collections.sort(sortedCategories);

						for(String mediaCategory : sortedCategories)
						{
							DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(mediaCategory);
							categoryTypeNode.add(categoryNode);
							
							// Resources.
							List<DLNAResource> categoryResources = categoryTypeResourcesMap.get(mediaCategory);
							buildTree_subSectionDlnaResources(library, categoryNode, mediaType, categoryResources);
						}
					}
				}
			}
		}
	}

	/**
	 * Extend the descriptive tree to include the following DLNA resources.
	 * 
	 * @param library   The library.
	 * @param rootNode  The root to attach onto.
	 * @param mediaType The media type we are handling.
	 * @param resources List of resources to render.
	 * @return          The total count of resources added.
	 */
	private int buildTree_subSectionDlnaResources(ICustomCategoryMediaLibrary library, DefaultMutableTreeNode rootNode, EMediaType mediaType, List<DLNAResource> resources)
	{
		int resourcesAdded = 0;
		
		for(DLNAResource resource : resources)
		{
			if (resource instanceof RealFile)
			{
				File resourceFile = ((RealFile)resource).getFile();

				// Determine the type of resource we found in this virtual folder. If it doesn't 
				// match the resource we are parsing, ignore it.
				Format mediaFormat = LazyCompatibility.getAssociatedExtension(resourceFile.getPath());
				EMediaType resourceType = EMediaType.get(mediaFormat);
				if (resourceType != mediaType)
				{
					continue;
				}

				MediaResource reference = new MediaResource(resource.getName(), resourceFile.getAbsolutePath());
				DefaultMutableTreeNode resourceNode = new DefaultMutableTreeNode(reference);
				rootNode.add(resourceNode);
				
				resourcesAdded++;
			}
			else if (resource instanceof VirtualFolder && (resource instanceof TranscodeVirtualFolder) == false)
			{
				DefaultMutableTreeNode virtualFolderNode = new DefaultMutableTreeNode(resource.getDisplayName());
				
				VirtualFolder virtualFolder = (VirtualFolder) resource;
				List<DLNAResource> folderResources = virtualFolder.getChildren();
				int ownResourcesAdded = buildTree_subSectionDlnaResources(library, virtualFolderNode, mediaType, folderResources);
				resourcesAdded += ownResourcesAdded;
				
				// If we did not add any resources, do not add this virtual folder.
				if (ownResourcesAdded > 0)
				{
					rootNode.add(virtualFolderNode);
				}
			}
		}
		
		return resourcesAdded;
	}
	
	/**
	 * A media resource to render on a tree node.
	 */
	private class MediaResource
	{
		/** The display name for this node. */
		private String _displayName;
		
		/** The absolute path for this resource. */
		private String _absolutePath;
		
		/**
		 * Initializes a new instance of MediaResource.
		 * 
		 * @param displayName  The display name for this node.
		 * @param absolutePath The absolute path for this resource.
		 */
		public MediaResource(String displayName, String absolutePath)
		{
			_displayName = displayName;
			_absolutePath = absolutePath;
		}
		
		/**
		 * Return the resource path for this resource.
		 * 
		 * @return The resource.
		 */
		public String getResourcePath()
		{
			return _absolutePath;
		}
		
		/**
		 * Return the name of the media resource.
		 * 
		 * @return The name.
		 */
		@Override
		public String toString()
		{
			return _displayName;
		}
	}
}

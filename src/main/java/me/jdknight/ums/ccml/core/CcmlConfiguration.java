/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jdknight.ums.ccml.core.interfaces.ICcmlConfiguration;
import me.jdknight.ums.ccml.util.ByteOrderMarkHelper;
import net.pms.PMS;

/**
 * CCML plugin configuration utility.
 */
public class CcmlConfiguration implements ICcmlConfiguration
{
	/**
	 * Supported CCML configuration file version.
	 **/
	private final static int CCML_CONFIGURATION_VERSION = 1;
	
	/**
	 * Name of the CCML configuration file.
	 **/
	private final static String CCML_CONFIGURATION_FILENAME = "PLUGIN_CCML.conf"; //$NON-NLS-1$
	
	/**
	 * Configuration key - configuration version.
	 **/
	private final static String CCML_CONFIGURATION_KEY_VERSION = "version"; //$NON-NLS-1$
	
	/**
	 * Configuration key - alternative media folder.
	 **/
	private final static String CCML_CONFIGURATION_KEY_ALTERNATIVE_MEDIA_FOLDER = "alternative_media_folder"; //$NON-NLS-1$
	
	/**
	 * Configuration key - never show parsing/building library notification.
	 **/
	private final static String CCML_CONFIGURATION_KEY_HIDE_PARSING_BUILDING_NOTIFICATION = "hide_parsing_building_notification"; //$NON-NLS-1$
	
	/**
	 * Logger.
	 **/
	private static final Logger _logger = LoggerFactory.getLogger(CcmlConfiguration.class);
	
	/**
	 * Flag used to indicate whether or not it is safe to save over any existing configuration file.
	 **/
	private boolean _isSafeToSave;
	
	/**
	 * The configured alternative media folder, may be null.
	 **/
	private String _alternativeMetaFolder;
	
	/**
	 * Flag to track whether or not the client wishes to hide the parsing/building notification.
	 **/
	private boolean _isHideParsingBuildingNotification;
	
	/**
	 * Configuration instance.
	 **/
	private static ICcmlConfiguration _instance;

	/**
	 * Return the configuration instance.
	 * 
	 * @return The instance.
	 */
	public static ICcmlConfiguration getInstance()
	{
		if (_instance == null)
		{
			_instance = new CcmlConfiguration();
			
			legacySupport();
		}
		
		return _instance;
	}
	
	/**
	 * Return the CCML configuration file.
	 * 
	 * @return The configuration file; null if the configuration file cannot be determined.
	 */
	private File getConfigurationFile()
	{
		try
		{
			// Attempt to get the CCML configuration file from the profile directory.
			File profileDirectory = new File(PMS.getConfiguration().getProfileDirectory());
			File pluginConfigurationFile = new File(profileDirectory, CCML_CONFIGURATION_FILENAME);
			
			// Directory exists instead? Ignore.
			if (pluginConfigurationFile.isDirectory() == true)
			{
				_logger.error("[CCML] Cannot prepare use for configuration file as its path is a directory."); //$NON-NLS-1$
				return null;
			}
			
			return pluginConfigurationFile;
		}
		catch (NullPointerException e)
		{
			_logger.error("[CCML] Cannot acquire configuration file as profile directory is not defined."); //$NON-NLS-1$
		}
		
		return null;
	}

	/**
	 * Load the configuration file.
	 * 
	 * @return True, if the configuration file was loaded or no configuration file exists; false otherwise.
	 */
	@Override
	public boolean load()
	{
		return load(this);
	}

	/**
	 * Load the configuration file into the provided configuration.
	 * 
	 * @param configuration The configuration to load into.
	 * @return              True, if the configuration file was loaded or no configuration file exists; false otherwise.
	 */
	private boolean load(ICcmlConfiguration configuration)
	{
		// Get the configuration file.
		File configurationFile = getConfigurationFile();
		if (configurationFile == null)
		{
			return false;
		}

		// No configuration file? Nothing to attempt to load, flag as success.
		if (configurationFile.isFile() == false)
		{
			return true;
		}

		// File exists, handle.
		BufferedReader reader = null;
		try
		{
			// Load configuration file based on known encoding.
			String characterEncoding = ByteOrderMarkHelper.getEncoding(configurationFile);
			if (characterEncoding != null)
			{
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(configurationFile), characterEncoding));
				reader.read();
			}
			else
			{
				reader = new BufferedReader(new FileReader(configurationFile));
			}

			// Load the file into our properties reader.
			Properties properties = new Properties();
			properties.load(reader);
			
			// Attempt to find the version.
			String rawVersion = properties.getProperty(CCML_CONFIGURATION_KEY_VERSION);
			if ( rawVersion != null )
			{
				try
				{
					int version = Integer.parseInt(rawVersion);
					
					// Newer version? Attempt to update as much as possible.
					if (version > CCML_CONFIGURATION_VERSION)
					{
						updateConfiguration(configuration, properties);
						return true;
					}
					// Old configuration file? Upgrade.
					else if (version < CCML_CONFIGURATION_VERSION)
					{
						// No upgrades supported yet.
					}
					// Same configuration version.
					else
					{
						updateConfiguration(configuration, properties);
						_isSafeToSave = true;
						return true;
					}
				}
				catch(NumberFormatException e)
				{
					_logger.error("[CCML] Ignoring configuration file as version is not a valid number."); //$NON-NLS-1$
				}
			}
			else
			{
				_logger.error("[CCML] Ignoring configuration file as version is not specified."); //$NON-NLS-1$
			}
		}
		catch (IOException e)
		{
			_logger.error("[CCML] Unable to read configuration file due to an I/O error."); //$NON-NLS-1$
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
					// Ignore if closing has failed.
				}
			}
		}

		return false;
	}
	
	/**
	 * Update the provided configuration file with settings found in the properties file.
	 * 
	 * @param configuration The configuration file.
	 * @param properties    The properties.
	 */
	private void updateConfiguration(ICcmlConfiguration configuration, Properties properties)
	{
		// Load known configuration properties over.
		_alternativeMetaFolder = properties.getProperty(CCML_CONFIGURATION_KEY_ALTERNATIVE_MEDIA_FOLDER);
		String sRaw = properties.getProperty(CCML_CONFIGURATION_KEY_HIDE_PARSING_BUILDING_NOTIFICATION);
		_isHideParsingBuildingNotification = Boolean.parseBoolean(sRaw);
	}
	
	/**
	 * Persist current settings to the configuration file.
	 * 
	 * @return True, if the configuration file was persisted; false otherwise.
	 */
	@Override
	public boolean persist()
	{
		return persist(this);
	}	

	/**
	 * Persist current settings to the configuration file.
	 * 
	 * @param configuration The configuration to load into.
	 * @return              True, if the configuration file was persisted; false otherwise.
	 */
	private boolean persist(ICcmlConfiguration configuration)
	{
		// Get the configuration file.
		File configurationFile = getConfigurationFile();
		if (configurationFile == null)
		{
			return false;
		}
		
		// If the configuration file does not exist, always mark as safe to save.
		if (configurationFile.isFile() == false)
		{
			_isSafeToSave = true;
		}

		Properties properties = new Properties();
		
		// Not safe to save?
		if (_isSafeToSave == false)
		{
			// Attempt to load the configuration. If we do not have a successful load, deny the save.
			CcmlConfiguration temporaryConfiguration = new CcmlConfiguration();
			if (temporaryConfiguration.load() == false)
			{
				_logger.error("[CCML] Cannot save configuration file since persisted configuration settings may be lost."); //$NON-NLS-1$
				return false;
			}

			// Update configuration from current file before applying new settings.
			updateConfiguration(temporaryConfiguration, properties);
		}

		updateProperties(configuration, properties);
		
		// Write configuration file.
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(configurationFile));
			
			properties.store(writer, "CCML Configuration File"); //$NON-NLS-1$
			return true;
		}
		catch (IOException e)
		{
			_logger.error("[CCML] Unable to write configuration file due to an I/O error."); //$NON-NLS-1$
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (IOException e)
				{
					// Ignore if closing has failed.
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Update the provided properties file with settings found in the configuration file.
	 * 
	 * @param configuration The configuration file.
	 * @param properties    The properties.
	 */
	private void updateProperties(ICcmlConfiguration configuration, Properties properties)
	{
		// Load known configuration properties over.
		properties.setProperty(CCML_CONFIGURATION_KEY_VERSION, String.valueOf(CCML_CONFIGURATION_VERSION));
		if ( _alternativeMetaFolder != null )
		{
			properties.setProperty(CCML_CONFIGURATION_KEY_ALTERNATIVE_MEDIA_FOLDER, _alternativeMetaFolder);
		}
		properties.setProperty(CCML_CONFIGURATION_KEY_HIDE_PARSING_BUILDING_NOTIFICATION, String.valueOf(_isHideParsingBuildingNotification));
	}
	
	/**
	 * Return the alternative meta folder.
	 * 
	 * <p>
	 * If defined, the folder will be used to match meta files for media resources.
	 * </p>
	 * 
	 * @return The path to the alternative meta folder; may be null.
	 */
	@Override
	public String getAlternativeMetaFolder()
	{
		return _alternativeMetaFolder;
	}

	/**
	 * Set the alternative meta folder.
	 * 
	 * <p>
	 * If defined, the folder will be used to match meta files for media resources.
	 * </p>
	 * 
	 * @param folderPath The path to the alternative meta folder; null to clear the alternative meta folder.
	 * 
	 * @throws IllegalArgumentException Thrown if the provided folder is invalid.
	 */
	@Override
	public void setAlternativeMetaFolder(String folderPath) throws IllegalArgumentException
	{
		if (folderPath != null)
		{
			File alternativeMetaFolder = new File(folderPath);
			if (alternativeMetaFolder.isDirectory() == false)
			{
				throw new IllegalArgumentException("A valid folder must be provided."); //$NON-NLS-1$
			}
		}

		_alternativeMetaFolder = folderPath;
	}

	/**
	 * Return whether or not the client wishes to hide parsing/building library notifications.
	 * 
	 * @return True, if the client wishes to hide the notifications; false otherwise.
	 */
	@Override
	public boolean isHidingParsingBuildingNotification()
	{
		return _isHideParsingBuildingNotification;
	}
	
	/**
	 * Set whether or not the client wishes to hide parsing/building library notifications.
	 * 
	 * @param shouldNotify True, if the client wishes to hide the notifications; false otherwise.
	 */
	@Override
	public void toggleHidingParsingBuildingNotification(boolean shouldNotify)
	{
		_isHideParsingBuildingNotification = shouldNotify;
	}
	
	/**
	 * Migrate old CCML configuration file to new location.
	 * 
	 * <p>
	 * First implementation of the CCML configuration file stored the file in the plugins directory. 
	 * Instead, it is desired to be located in the profile directory. If the file does exist, move 
	 * it over.
	 * </p>
	 */
	private static void legacySupport()
	{
		// If the current configuration file exists, ignore.
		File profileDirectory = new File(PMS.getConfiguration().getProfileDirectory());
		File pluginConfigurationFile = new File(profileDirectory, CCML_CONFIGURATION_FILENAME);
		if (pluginConfigurationFile.isFile() == true)
		{
			return;
		}

		// Attempt to find the CCML configuration file from the plugin directory based on first implementation.
		String firstImplementationFileName = "CCML.cfg"; //$NON-NLS-1$
		File pluginDirectory = new File(PMS.getConfiguration().getPluginDirectory());
		File oldPluginConfigurationFile = new File(pluginDirectory, firstImplementationFileName);
		if (oldPluginConfigurationFile.isFile() == true)
		{
			// File exists! Attempt to move it over.
			if (oldPluginConfigurationFile.renameTo(pluginConfigurationFile) == false)
			{
				_logger.error("[CCML] Attempt to move old configuration file has failed."); //$NON-NLS-1$
			}
		}
	}
}

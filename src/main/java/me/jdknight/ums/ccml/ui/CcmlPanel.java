/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.jdknight.ums.ccml.core.CcmlConfiguration;
import me.jdknight.ums.ccml.core.interfaces.ICustomCategoryMediaLibrary;
import me.jdknight.ums.ccml.lang.Messages;
import net.pms.PMS;
import net.pms.newgui.RestrictedFileSystemView;

/**
 * Main panel for CCML plugin customization.
 */
@SuppressWarnings("serial")
public class CcmlPanel extends JPanel
{
	/**
	 * Logger.
	 **/
	private static final Logger _logger = LoggerFactory.getLogger(CcmlPanel.class);
	
	/**
	 * The library.
	 **/
	private ICustomCategoryMediaLibrary _library;
	
	/**
	 * Flag to track whether or not the client has changed settings.
	 **/
	private boolean _isSettingsChanged;
	
	/**
	 * Currently set alternative meta folder directory.
	 **/
	private String _alternativeMetaFolder;
	
	/**
	 * Main panel for CCML plugin customization.
	 * 
	 * @param library The library.
	 */
	public CcmlPanel(ICustomCategoryMediaLibrary library)
	{
		_library = library;

        GridBagConstraints constraints;
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        
		// Description.
		JLabel alternativeMetaFolderLabel = new JLabel(Messages.getString("CCcmlPanel.ALTERNATIVE_META_FOLDER")); //$NON-NLS-1$
		
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 0;
		add(alternativeMetaFolderLabel, constraints);
		
		// Alternative meta folder text.
		_alternativeMetaFolder = CcmlConfiguration.getInstance().getAlternativeMetaFolder();
		if (_alternativeMetaFolder == null)
		{
			_alternativeMetaFolder = ""; //$NON-NLS-1$
		}

		final JTextField alternativeMetaFolderTextInput = new JTextField(_alternativeMetaFolder);
		alternativeMetaFolderTextInput.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent oEvent)
			{
				// Cache the users input.
				_isSettingsChanged = true;
				_alternativeMetaFolder = ((JTextField) oEvent.getSource()).getText();
			}
		});

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 2;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(alternativeMetaFolderTextInput, constraints);

		// Dialog to select an alternative meta folder.
		JButton alternativeMetaFolderButton = new JButton("..."); //$NON-NLS-1$
		alternativeMetaFolderButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				JFileChooser fileChooser = null;
				try
				{
					fileChooser = new JFileChooser();
				}
				catch (Exception e)
				{
					fileChooser = new JFileChooser(new RestrictedFileSystemView());
				}
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				// Open to alternative folder location, if any.
				File currentFolder = new File(_alternativeMetaFolder);
				if (currentFolder.isDirectory() == true)
				{
					fileChooser.setCurrentDirectory(currentFolder);
				}
				
				int returnValue = fileChooser.showDialog((Component) event.getSource(), Messages.getString("CCcmlPanel.CHOOSE_A_FOLDER")); //$NON-NLS-1$
				if (returnValue == JFileChooser.APPROVE_OPTION)
				{
					String folderPath = fileChooser.getSelectedFile().getAbsolutePath();

					// Cache the users input.
					_isSettingsChanged = true;
					_alternativeMetaFolder = folderPath;
					alternativeMetaFolderTextInput.setText(folderPath);
					
					_logger.trace("[CCML] Configured new alternative meta folder: " + folderPath); //$NON-NLS-1$
				}
			}
		});

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		constraints.gridy = 1;
		add(alternativeMetaFolderButton, constraints);

		// Description.
		JLabel restartRequiredDescriptionLabel = new JLabel(Messages.getString("CCcmlPanel.RESTART_REQUIRED_NOTICE")); //$NON-NLS-1$

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.insets = new Insets(10, 10, 10, 10);
		add(restartRequiredDescriptionLabel, constraints);

		// Dialog to show current meta structure.
		JButton showMetaStructureButton = new JButton("[+]"); //$NON-NLS-1$
		showMetaStructureButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				JOptionPane optionPane = new JOptionPane(new MetaStructurePanel(_library), JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION);
				JDialog dialog = optionPane.createDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())), Messages.getString("CCcmlPanel.META_STRUCTURE")); //$NON-NLS-1$
				dialog.setResizable(true);
				dialog.setVisible(true);
			}
		});

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridwidth = 2;
		constraints.gridx = 1;
		constraints.gridy = 2;
		add(showMetaStructureButton, constraints);
		
		// Listen for the panel closing.
		this.addAncestorListener(new AncestorListener()
		{
			@Override
			public void ancestorRemoved(AncestorEvent oEvent)
			{
				if (_isSettingsChanged)
				{
					// [SETTING] - Alternative Media Folder - Start
					
					// Get previous folder.
					String previousAlternativeMediaFolder = CcmlConfiguration.getInstance().getAlternativeMetaFolder();
					if (previousAlternativeMediaFolder == null)
					{
						previousAlternativeMediaFolder = ""; //$NON-NLS-1$
					}
					
					// Validate the clients input. If it is a valid directory, update the path
					if (_alternativeMetaFolder.isEmpty() == false && _alternativeMetaFolder.equalsIgnoreCase(previousAlternativeMediaFolder) == false)
					{
						if (new File(_alternativeMetaFolder).isDirectory() == true)
						{
							CcmlConfiguration.getInstance().setAlternativeMetaFolder(_alternativeMetaFolder);

							_logger.trace("[CCML] Configured new alternative meta folder: " + _alternativeMetaFolder); //$NON-NLS-1$
							return;
						}
					}

					// Invalid entry; assume a null alternative meta folder.
					CcmlConfiguration.getInstance().setAlternativeMetaFolder(null);
					_isSettingsChanged = true;
					
					if (previousAlternativeMediaFolder.isEmpty() == false)
					{
						_logger.trace("[CCML] Clearing alternative meta folder."); //$NON-NLS-1$
					}
					
					// [SETTING] - Alternative Media Folder - End
					
					// Persist changes.
					if (CcmlConfiguration.getInstance().persist() == true)
					{
						_logger.info("[CCML] Configuration file saved."); //$NON-NLS-1$
					}
					else
					{
						_logger.info("[CCML] Unable to save configuration."); //$NON-NLS-1$
					}
				}
			}
			
			@Override
			public void ancestorAdded(AncestorEvent oEvent)
			{
				// Ignore.
			}

			@Override
			public void ancestorMoved(AncestorEvent oEvent)
			{
				// Ignore.
			}
		});
	}
}

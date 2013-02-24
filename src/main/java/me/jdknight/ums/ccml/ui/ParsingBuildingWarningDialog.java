/** 
 * @file
 * Copyright (c) 2011-2013 jdknight. All rights reserved.
 * GNU General Public License, Version 2
 **/

package me.jdknight.ums.ccml.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import me.jdknight.ums.ccml.core.CcmlConfiguration;
import me.jdknight.ums.ccml.lang.Messages;
import net.pms.PMS;

/**
 * Dialog to inform a user of a parsing/building notification.
 */
@SuppressWarnings("serial")
public class ParsingBuildingWarningDialog extends JOptionPane
{
	/**
	 * Time, in milliseconds, to wait before rending a parsing warning dialog.
	 **/
	private static final int PARSING_WAIT_INTERVAL = 5000;

	/**
	 * Time, in milliseconds, to wait before rending a building warning dialog.
	 **/
	private static final int BUILDING_WAIT_INTERVAL = 7000;
	
	/**
	 * Dialog options.
	 **/
	private static final Object[] DIALOG_OPTIONS = { 
		Messages.getString("CParsingBuildingWarningDialog.NEVER_SHOW_AGAIN"), //$NON-NLS-1$
		Messages.getString("CParsingBuildingWarningDialog.OK") }; //$NON-NLS-1$
	
	/**
	 * The dialog.
	 **/
	private JDialog _dialog;
	
	/**
	 * Dialog consumption flag.
	 **/
	private boolean _isConsumed;

	/**
	 * Initializes a new instance of ParsingBuildingWarningDialog.
	 * 
	 * @param isParsing True, if we are parsing; otherwise flagging for building.
	 */
	public ParsingBuildingWarningDialog(boolean isParsing)
	{
		super(
				(isParsing == true ? Messages.getString("CParsingBuildingWarningDialog.LIBRARY_BEING_PARSED") : Messages.getString("CParsingBuildingWarningDialog.LIBRARY_BEING_BUILT")), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.PLAIN_MESSAGE,
				JOptionPane.YES_NO_OPTION, 
				null, 
				DIALOG_OPTIONS, DIALOG_OPTIONS[1]);

		String dialogTitle = "CCML - " + (isParsing == true ? Messages.getString("CParsingBuildingWarningDialog.PARSING_LIBRARY") : Messages.getString("CParsingBuildingWarningDialog.BUILDING_LIBRARY")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		_dialog = createDialog(((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame()))), dialogTitle);
	}
	
	/**
	 * Show the dialog.
	 */
	public void showDialog()
	{
		// Consumed or visible? Ignore.
		if (_isConsumed == true || _dialog.isVisible() == true)
		{
			return;
		}

		// Open the dialog.
		_dialog.setVisible(true);
		
		// When the dialog closes, check if the user selected never to see it again.
		Object valueSelected = getValue();
		if (valueSelected == DIALOG_OPTIONS[0])
		{
			CcmlConfiguration.getInstance().toggleHidingParsingBuildingNotification(true);
			CcmlConfiguration.getInstance().persist();
		}
	}
	
	/**
	 * Terminate the dialog.
	 * 
	 * <p>
	 * Once a dialog has been terminated, it cannot be opened again.
	 * </p>
	 */
	public void terminateDialog()
	{
		// Consume and close, if not already done.
		_isConsumed = true;
		_dialog.setVisible(false);
	}

	/**
	 * Queue a parsing warning dialog to be opened.
	 * 
	 * @return The dialog.
	 */
	public static ParsingBuildingWarningDialog queueParsingWarning()
	{
		return queueDialog(true);
	}

	/**
	 * Queue a building warning dialog to be opened.
	 * 
	 * @return The dialog.
	 */
	public static ParsingBuildingWarningDialog queueBuildingWarning()
	{
		return queueDialog(false);
	}
	
	/**
	 * Queue a warning dialog to be opened.
	 * 
	 * @param isParsing True, if we are parsing; else wise flagging for building.
	 * @return          The dialog.
	 */
	private static ParsingBuildingWarningDialog queueDialog(final boolean isParsing)
	{
		final ParsingBuildingWarningDialog dialog = new ParsingBuildingWarningDialog(isParsing);

		// Open dialog after waiting the timeout.
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				ActionListener task = new ActionListener()
				{
					public void actionPerformed(ActionEvent event)
					{
						dialog.showDialog();
					}
				};

				Timer timer = new Timer((isParsing == true ? PARSING_WAIT_INTERVAL : BUILDING_WAIT_INTERVAL), task);
				timer.setRepeats(false);
				timer.start();
			}
		});

		return dialog;
	}
}

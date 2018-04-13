package com.ibm.ServerWizard2.utility;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.ServerWizard2.ServerWizard2;

public class ServerwizMessageDialog {
	
	public static void openError(Shell parent, String title, String message) {
		if(ServerWizard2.updateOnlyMode)
		{
			ServerWizard2.LOGGER.severe(message);
		} else {
			MessageDialog.openError(parent, title, message);
		}
	}
	
	public static void openInformation(Shell parent, String title, String message) {
		if(ServerWizard2.updateOnlyMode)
		{
			ServerWizard2.LOGGER.severe(message);
		} else {
			MessageDialog.openInformation(parent, title, message);
		}
	}
}

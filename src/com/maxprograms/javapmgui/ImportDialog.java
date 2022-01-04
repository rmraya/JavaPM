package com.maxprograms.javapmgui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.util.DirectoryTracker;
import com.maxprograms.util.TextUtil;

public class ImportDialog extends Dialog{

	Shell shell;
	private Display display;
	boolean cancelled;
	String file;
	String language;



	public ImportDialog(Shell parent) {
		super(parent,SWT.NONE);
		cancelled = true;
		
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout());
		shell.setText(Messages.getString("ImportDialog.2")); //$NON-NLS-1$
		display = shell.getDisplay();
		
		Composite fileComposite = new Composite(shell,SWT.NONE);
		fileComposite.setLayout(new GridLayout(3,false));
		fileComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));

		Label fileLabel = new Label(fileComposite,SWT.NONE);
		fileLabel.setText(Messages.getString("ImportDialog.0")); //$NON-NLS-1$
		
		final Text fileText = new Text(fileComposite,SWT.BORDER);
		GridData data =new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL);
		data.widthHint = 250;
		fileText.setLayoutData(data);
		
		Button browse = new Button(fileComposite,SWT.PUSH);
		browse.setText(Messages.getString("ImportDialog.1")); //$NON-NLS-1$
		browse.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(shell,SWT.OPEN);
				String[] extensions = {"*.properties","*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
				String[] names = {Messages.getString("ImportDialog.4"),Messages.getString("ImportDialog.5")}; //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterExtensions(extensions);
				fd.setFilterNames(names);
				fd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
				String output = fd.open();
				if (output != null) {
					fileText.setText(output);
					DirectoryTracker.saveDirectory(fd.getFilterPath(),Constants.preferences);
				}
			}
		});
		
		Composite langComposite = new Composite(shell,SWT.NONE);
		langComposite.setLayout(new GridLayout(2,false));
		langComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));

		Label languageLabel = new Label(langComposite,SWT.NONE);
		languageLabel.setText(Messages.getString("ImportDialog.6")); //$NON-NLS-1$

		final Combo langCombo = new Combo(langComposite,SWT.DROP_DOWN|SWT.READ_ONLY);
		langCombo.setItems(TextUtil.getLanguageNames());
		langCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
		
		Composite bottom = new Composite(shell,SWT.BORDER);
		bottom.setLayout(new GridLayout(2,true));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));

		Button okButton = new Button(bottom, SWT.PUSH);
		okButton.setText(Messages.getString("ImportDialog.7")); //$NON-NLS-1$
		okButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
		okButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (fileText.getText().equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("ImportDialog.9")); //$NON-NLS-1$
					box.open();
					return;
				}
				if (langCombo.getText().equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell,SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("ImportDialog.11")); //$NON-NLS-1$
					box.open();
					return;
				}
				file = fileText.getText();
				language = TextUtil.getLanguageCode(langCombo.getText());
				cancelled = false;
				shell.close();
			}
		});

		
		Button cancel = new Button(bottom, SWT.PUSH|SWT.CANCEL);
		cancel.setText(Messages.getString("ImportDialog.12")); //$NON-NLS-1$
		cancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
		cancel.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {				
				shell.close();
			}
		});
		
		shell.pack();
	}

	public void show() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public boolean wasCancelled() {
		return cancelled;
	}

	public String getLanguage() {
		return language;
	}

	public String getFile() {
		return file;
	}

}

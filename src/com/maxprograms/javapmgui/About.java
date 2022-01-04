package com.maxprograms.javapmgui;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class About extends Dialog {

	private Shell shell;
	private Display display;
	static final String VERSION = "1.0-3"; //$NON-NLS-1$
	
	
	public About(Shell parent) {
		super(parent,SWT.NONE);
		
		shell = new Shell(parent,SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setLayout(new GridLayout());
		display = shell.getDisplay();
		
		Color white = new Color(display,255,255,255);
		shell.setBackground(white);
		shell.setText(Messages.getString("About.1")); //$NON-NLS-1$
		
		
		Label logo = new Label(shell,SWT.NONE);
		logo.setImage(new Image(display,"images/Icons/Orange_squares.png")); //$NON-NLS-1$
		logo.setBackground(white);
		logo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER));
		
		Label name = new Label(shell,SWT.CENTER);
		MessageFormat mf = new MessageFormat(Messages.getString("About.3")); //$NON-NLS-1$
		Object[] args = {VERSION};
		name.setText(mf.format(args));
		name.setBackground(white);
		name.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER));
		
		Label empty = new Label(shell,SWT.NONE);
		empty.setText(""); //$NON-NLS-1$
		
		Label copyright = new Label(shell,SWT.CENTER);
		copyright.setText(Messages.getString("About.5")); //$NON-NLS-1$
		copyright.setBackground(white);
		copyright.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER));
		
		Label website = new Label(shell,SWT.CENTER);
		website.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER));
		website.setText("http://www.maxprograms.com"); //$NON-NLS-1$
		website.setBackground(white);
		website.setForeground(new Color(display,0x00,0x00,0xFF));
		website.addMouseListener(new MouseListener(){
			 
			 public void mouseDoubleClick(MouseEvent arg0) {
				 // do nothing				
			 }
			 
			 public void mouseDown(MouseEvent arg0) {
				 Program.launch("http://www.maxprograms.com");	 //$NON-NLS-1$
			 }
			 
			 public void mouseUp(MouseEvent arg0) {
				 // do nothing				
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

}

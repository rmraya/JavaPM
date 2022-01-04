package com.maxprograms.javapmgui;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.maxprograms.javapm.JavaPM;
import com.maxprograms.util.DirectoryTracker;
import com.maxprograms.util.Preferences;

public class JavaPMGUI {

	private static Display display;
	Shell shell;
	JavaPM javapm;
	Table table;
	private String projectFile;

	public JavaPMGUI() {
		
		String lang = loadPreferences("language"); //$NON-NLS-1$
        if (lang.equals("") ) { //$NON-NLS-1$
        	lang = Locale.getDefault().toString();
        	if (lang.toLowerCase().startsWith("en")) { //$NON-NLS-1$
        		lang = "en"; //$NON-NLS-1$
        	} else if (lang.toLowerCase().startsWith("es")) { //$NON-NLS-1$
        		lang = "es"; //$NON-NLS-1$
        	}
        }
		Locale.setDefault(new Locale(lang));
		
		display = new Display();
		shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(Messages.getString("JavaPMGUI.0")); //$NON-NLS-1$
		shell.setLayout(new GridLayout());
		shell.setImage(new Image(display,"images/Icons/Orange_squares.png")); //$NON-NLS-1$
		shell.addListener(SWT.Close, new Listener(){

			public void handleEvent(Event arg0) {
				if (javapm != null && javapm.isDirty()) {
					MessageBox box = new MessageBox(shell,SWT.ICON_QUESTION|SWT.YES|SWT.NO);
					box.setMessage(Messages.getString("JavaPMGUI.1")); //$NON-NLS-1$
					if (box.open() == SWT.YES) {
						try {
							if (projectFile.equals("Untitled.jpm")) { //$NON-NLS-1$
								saveProject();
							} else {
								javapm.saveProject();
							}
						} catch (Exception e) {
							MessageBox box1 = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
							box1.setMessage(e.getMessage());
							box1.open();
						}
					}
				}
			}
		});
		
		Menu mainMenu = new Menu(shell,SWT.BAR);
		createMenu(mainMenu);
		shell.setMenuBar(mainMenu);
	
		Composite holder = new Composite(shell,SWT.NONE);
		holder.setLayout(new GridLayout(3, false));
		holder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
		
		ToolBar bar = new ToolBar(holder,SWT.FLAT);
		//bar.setBackground(new Color(display,255,255,255));
		populateToolbar(bar);	
		
		Label filler = new Label(holder,SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
		
		ToolBar helpbar = new ToolBar(holder,SWT.FLAT);
		ToolItem helpItem = new ToolItem(helpbar,SWT.PUSH);
		helpItem.setImage(new Image(display,"images/Normal/help.png")); //$NON-NLS-1$
		helpItem.setToolTipText(Messages.getString("JavaPMGUI.11")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				displayHelp();
			}
		});

		table = new Table(shell,SWT.READ_ONLY|SWT.H_SCROLL|SWT.V_SCROLL|SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL|GridData.FILL_BOTH);
		tableData.widthHint = 450;
		tableData.heightHint = 350;
		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		final TableColumn column = new TableColumn(table,SWT.NONE);
		column.setText(Messages.getString("JavaPMGUI.2")); //$NON-NLS-1$
		column.setWidth(450);
		
		shell.pack();
		
		shell.addPaintListener(new PaintListener(){

			public void paintControl(PaintEvent arg0) {
				column.setWidth(table.getClientArea().width);
			}
		});
	}

	private void displayHelp() {
		String helpFile = null;
		// English by default
		if (!System.getProperty("file.separator").equals("\\")) {   //$NON-NLS-1$ //$NON-NLS-2$
			helpFile = "docs/javapm.pdf";  //$NON-NLS-1$
		} else {
			helpFile = "docs\\javapm.pdf";  //$NON-NLS-1$
		}

		try {
			Program.launch(new File(helpFile).toURI().toURL().toString());
		} catch (MalformedURLException e) {
			MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			if (e.getMessage() != null) {
				box.setMessage(e.getMessage());
			} else {
				e.printStackTrace();
				box.setMessage(Messages.getString("JavaPMGUI.17")); //$NON-NLS-1$
			}
			box.open();
			return;
		}
	}

	private static String loadPreferences(String type) {
        try {
    		Preferences prefs = new Preferences(Constants.preferences);
    		return prefs.get("prefs", type, ""); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            // ignore 
        }
        return ""; //$NON-NLS-1$
    }

	private void populateToolbar(ToolBar bar) {
		ToolItem newItem = new ToolItem(bar,SWT.NONE);
		newItem.setImage(new Image(display,"images/Normal/new.png")); //$NON-NLS-1$
		newItem.setToolTipText(Messages.getString("JavaPMGUI.4")); //$NON-NLS-1$
		newItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				newProject();				
			}

		});
		
		ToolItem openItem = new ToolItem(bar,SWT.PUSH);
		openItem.setImage(new Image(display,"images/Normal/open.png")); //$NON-NLS-1$
		openItem.setToolTipText(Messages.getString("JavaPMGUI.6")); //$NON-NLS-1$
		openItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				openProject();
			}
		});
		
		
		ToolItem saveItem = new ToolItem(bar,SWT.PUSH);
		saveItem.setImage(new Image(display,"images/Normal/save.png")); //$NON-NLS-1$
		saveItem.setToolTipText(Messages.getString("JavaPMGUI.8")); //$NON-NLS-1$
		saveItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				saveProject();
			}
		});
		
		new ToolItem(bar,SWT.SEPARATOR);

		ToolItem addFolder = new ToolItem(bar,SWT.PUSH);
		addFolder.setImage(new Image(display,"images/Normal/folder.png")); //$NON-NLS-1$
		addFolder.setToolTipText(Messages.getString("JavaPMGUI.10")); //$NON-NLS-1$
		addFolder.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				addFolder();
			}
		});
		
		ToolItem add = new ToolItem(bar,SWT.PUSH);
		add.setImage(new Image(display,"images/Normal/plus.png")); //$NON-NLS-1$
		add.setToolTipText(Messages.getString("JavaPMGUI.12")); //$NON-NLS-1$
		add.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				addFile();
			}
		});
		
		ToolItem remove = new ToolItem(bar,SWT.PUSH);
		remove.setImage(new Image(display,"images/Normal/minus.png")); //$NON-NLS-1$
		remove.setToolTipText(Messages.getString("JavaPMGUI.14")); //$NON-NLS-1$
		remove.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				removeFile();
			}
		});

		new ToolItem(bar,SWT.SEPARATOR);
		
		ToolItem exportItem = new ToolItem(bar,SWT.PUSH);
		exportItem.setImage(new Image(display,"images/Normal/export.png")); //$NON-NLS-1$
		exportItem.setToolTipText(Messages.getString("JavaPMGUI.16")); //$NON-NLS-1$
		exportItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				exportChanged();
			}
		});
		
		ToolItem importItem = new ToolItem(bar,SWT.PUSH);
		importItem.setImage(new Image(display,"images/Normal/import.png")); //$NON-NLS-1$
		importItem.setToolTipText(Messages.getString("JavaPMGUI.18")); //$NON-NLS-1$
		importItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				importTranslations();
			}
		});
		
		new ToolItem(bar,SWT.SEPARATOR);

		ToolItem exportXliff = new ToolItem(bar,SWT.PUSH);
		exportXliff.setImage(new Image(display,"images/Normal/document_out.png")); //$NON-NLS-1$
		exportXliff.setToolTipText(Messages.getString("JavaPMGUI.59")); //$NON-NLS-1$
		exportXliff.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				exportXLIFF();
			}
		});
		
		ToolItem importXliff = new ToolItem(bar,SWT.PUSH);
		importXliff.setImage(new Image(display,"images/Normal/document_in.png")); //$NON-NLS-1$
		importXliff.setToolTipText(Messages.getString("JavaPMGUI.61")); //$NON-NLS-1$
		importXliff.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				importXLIFF();
			}
		});
		
		
		new ToolItem(bar,SWT.SEPARATOR);
		
		ToolItem markTranslated = new ToolItem(bar,SWT.PUSH);
		markTranslated.setImage(new Image(display,"images/Normal/processed.png")); //$NON-NLS-1$
		markTranslated.setToolTipText(Messages.getString("JavaPMGUI.20")); //$NON-NLS-1$
		markTranslated.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				markTranslated();
			}
		});
	}

	void importXLIFF() {
		if (javapm == null) {
			return;
		}
		FileDialog fd = new FileDialog(shell,SWT.OPEN);
		String[] extensions = {"*.xlf","*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
		String[] names = {Messages.getString("JavaPMGUI.64"),Messages.getString("JavaPMGUI.65")}; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		fd.setFilterNames(names);
		fd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
		String xliff = fd.open(); 
		if ( xliff != null ) {
			display.update();
			DirectoryTracker.saveDirectory(fd.getFilterPath(),Constants.preferences);
			try {
				shell.setCursor(new Cursor(display,SWT.CURSOR_WAIT));
				javapm.importXLIFF(xliff);
				shell.setCursor(new Cursor(display,SWT.CURSOR_ARROW));
			} catch (Exception e) {
				shell.setCursor(new Cursor(display,SWT.CURSOR_ARROW));
				e.printStackTrace();
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}
	}

	void exportXLIFF() {
		if (javapm == null) {
			return;
		}
		ExportDialog dialog = new ExportDialog(shell);
		dialog.show();
		if (!dialog.wasCancelled()) {
			try {
				javapm.exportXLIFF(dialog.getFile(), dialog.getLanguage());
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}
	}

	void markTranslated() {
		if (javapm == null) {
			return;
		}
		try {
			MessageBox box = new MessageBox(shell,SWT.ICON_QUESTION|SWT.YES|SWT.NO);
			box.setMessage(Messages.getString("JavaPMGUI.9")); //$NON-NLS-1$
			if (box.open() == SWT.YES) {
				javapm.reloadAllStrings();
			}
		} catch (IOException e) {
			MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
			box.setMessage(e.getMessage());
			box.open();
		}
	}

	void addFolder() {
		if (javapm == null) {
			return;
		}
		DirectoryDialog dd = new DirectoryDialog(shell,SWT.OPEN);
		dd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
		String folder = dd.open();
		try {
			javapm.addFolder(folder);
			updateTable();
			DirectoryTracker.saveDirectory(folder,Constants.preferences);
		} catch (Exception e) {
			MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
			box.setMessage(e.getMessage());
			box.open();
		}
	}

	void importTranslations() {
		if (javapm == null) {
			return;
		}
		ImportDialog dialog = new ImportDialog(shell);
		dialog.show();
		if (!dialog.wasCancelled()) {
			try {
				javapm.importTranslations(dialog.getFile(), dialog.getLanguage());
			} catch (Exception e) {
				e.printStackTrace();
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}
	}

	void exportChanged() {
		if (javapm == null) {
			return;
		}
		FileDialog fd = new FileDialog(shell,SWT.SAVE);
		String[] extensions = {"*.properties","*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
		String[] names = {Messages.getString("JavaPMGUI.23"),Messages.getString("JavaPMGUI.24")}; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		fd.setFilterNames(names);
		fd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
		fd.setFileName(new File(projectFile).getName()+ ".properties"); //$NON-NLS-1$
		String output = fd.open();
		if (output != null) {
			try {
				if (!output.endsWith(".properties")) { //$NON-NLS-1$
					output = output + ".properties"; //$NON-NLS-1$
				}
				javapm.exportChanged(output);
			} catch (Exception e) {
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}
	}

	void removeFile() {
		if (javapm == null) {
			return;
		}
		if (table.getSelectionCount() == 0) {
			MessageBox box = new MessageBox(shell,SWT.ICON_WORKING|SWT.OK);
			box.setMessage(Messages.getString("JavaPMGUI.26")); //$NON-NLS-1$
			box.open();
			return;
		}
		int[] rows = table.getSelectionIndices();
		for (int i=0; i<rows.length ; i++) {
			String file = table.getItem(rows[i]).getText();
			javapm.remove(file);
		}
		updateTable();
	}

	void addFile() {
		if (javapm == null) {
			return;
		}
		FileDialog fd = new FileDialog(shell,SWT.OPEN);
		String[] names = {Messages.getString("JavaPMGUI.27"), Messages.getString("JavaPMGUI.28")}; //$NON-NLS-1$ //$NON-NLS-2$
		String[] extensions = {"*.properties", "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		fd.setFilterNames(names);
		fd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
		String file = fd.open();
		if (file != null) {
			try {
				javapm.addFile(file);
				updateTable();
				DirectoryTracker.saveDirectory(fd.getFilterPath(),Constants.preferences);
			} catch (Exception e) {
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}
	}

	private void createMenu(Menu mainMenu) {
		Menu fileMenu = new Menu(mainMenu);
		MenuItem file = new MenuItem(mainMenu,SWT.CASCADE);
		file.setText(Messages.getString("JavaPMGUI.31")); //$NON-NLS-1$
		file.setMenu(fileMenu);
		
		MenuItem newItem = new MenuItem(fileMenu,SWT.PUSH);
		newItem.setText(Messages.getString("JavaPMGUI.32")); //$NON-NLS-1$
		newItem.setImage(new Image(display, "images/Small/new.png")); //$NON-NLS-1$
		newItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				newProject();				
			}

		});
		
		MenuItem openItem = new MenuItem(fileMenu,SWT.NONE);
		openItem.setText(Messages.getString("JavaPMGUI.33")); //$NON-NLS-1$
		openItem.setImage(new Image(display, "images/Small/open.png")); //$NON-NLS-1$
		openItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				openProject();
			}
		});
		
		
		MenuItem saveItem = new MenuItem(fileMenu,SWT.NONE);
		saveItem.setText(Messages.getString("JavaPMGUI.34")); //$NON-NLS-1$
		saveItem.setImage(new Image(display, "images/Small/save.png")); //$NON-NLS-1$
		saveItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				saveProject();
			}
		});
		
		new MenuItem(fileMenu,SWT.SEPARATOR);
		
		MenuItem closeItem = new MenuItem(fileMenu,SWT.PUSH);
		if (System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			closeItem.setText(Messages.getString("JavaPMGUI.37")); //$NON-NLS-1$
			closeItem.setAccelerator(SWT.CTRL|SWT.F4);
		} else if (System.getProperty("os.name").startsWith("Mac")) { //$NON-NLS-1$ //$NON-NLS-2$
			closeItem.setText(Messages.getString("JavaPMGUI.40")); //$NON-NLS-1$
		} else {
			closeItem.setText(Messages.getString("JavaPMGUI.41")); //$NON-NLS-1$
			closeItem.setAccelerator(SWT.CTRL|'Q');
		}
		closeItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				close();				
			}
		});
		
		Menu optionsMenu = new Menu(mainMenu);
		MenuItem options = new MenuItem(mainMenu,SWT.CASCADE);
		options.setText(Messages.getString("JavaPMGUI.3")); //$NON-NLS-1$
		options.setMenu(optionsMenu);
		
		MenuItem languages = new MenuItem(optionsMenu,SWT.CASCADE);
		languages.setText(Messages.getString("JavaPMGUI.5")); //$NON-NLS-1$
		Menu langMenu = new Menu(languages);
		languages.setMenu(langMenu);
		
		MenuItem english = new MenuItem(langMenu,SWT.PUSH);
		english.setText(Messages.getString("JavaPMGUI.7")); //$NON-NLS-1$
		english.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent e) {
				saveLanguage("en"); //$NON-NLS-1$
			}
			
		});
		
		
		MenuItem spanish = new MenuItem(langMenu,SWT.PUSH);
		spanish.setText(Messages.getString("JavaPMGUI.13")); //$NON-NLS-1$
		spanish.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent e) {
				saveLanguage("es"); //$NON-NLS-1$ 
			}
			
		});
		
		
		Menu tasksMenu = new Menu(mainMenu);
		MenuItem tasks = new MenuItem(mainMenu,SWT.CASCADE);
		tasks.setText(Messages.getString("JavaPMGUI.42")); //$NON-NLS-1$
		tasks.setMenu(tasksMenu);

		MenuItem addFolder = new MenuItem(tasksMenu,SWT.PUSH);
		addFolder.setText(Messages.getString("JavaPMGUI.43")); //$NON-NLS-1$
		addFolder.setImage(new Image(display, "images/Small/folder.png")); //$NON-NLS-1$
		addFolder.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				addFolder();
			}
		});
		
		MenuItem add = new MenuItem(tasksMenu,SWT.PUSH);
		add.setText(Messages.getString("JavaPMGUI.44")); //$NON-NLS-1$
		add.setImage(new Image(display, "images/Small/plus.png")); //$NON-NLS-1$
		add.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				addFile();
			}
		});
		
		MenuItem remove = new MenuItem(tasksMenu,SWT.PUSH);
		remove.setText(Messages.getString("JavaPMGUI.45")); //$NON-NLS-1$
		remove.setImage(new Image(display, "images/Small/minus.png")); //$NON-NLS-1$
		remove.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				removeFile();
			}
		});
		
		new MenuItem(tasksMenu,SWT.SEPARATOR);
		
		MenuItem exportXliff = new MenuItem(tasksMenu,SWT.PUSH);
		exportXliff.setText(Messages.getString("JavaPMGUI.66")); //$NON-NLS-1$
		exportXliff.setImage(new Image(display, "images/Small/document_out.png")); //$NON-NLS-1$
		exportXliff.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				exportXLIFF();
			}
		});
		
		MenuItem importXliff = new MenuItem(tasksMenu,SWT.PUSH);
		importXliff.setText(Messages.getString("JavaPMGUI.67")); //$NON-NLS-1$
		importXliff.setImage(new Image(display, "images/Small/document_in.png")); //$NON-NLS-1$
		importXliff.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				importXLIFF();
			}
		});
		
		new MenuItem(tasksMenu,SWT.SEPARATOR);
		
		MenuItem exportItem = new MenuItem(tasksMenu,SWT.PUSH);
		exportItem.setText(Messages.getString("JavaPMGUI.46")); //$NON-NLS-1$
		exportItem.setImage(new Image(display, "images/Small/export.png")); //$NON-NLS-1$
		exportItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				exportChanged();				
			}
		});

		MenuItem importItem = new MenuItem(tasksMenu,SWT.PUSH);
		importItem.setText(Messages.getString("JavaPMGUI.47")); //$NON-NLS-1$
		importItem.setImage(new Image(display, "images/Small/import.png")); //$NON-NLS-1$
		importItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				importTranslations();				
			}
		});

		new MenuItem(tasksMenu,SWT.SEPARATOR);
		
		MenuItem markTranslated = new MenuItem(tasksMenu,SWT.PUSH);
		markTranslated.setText(Messages.getString("JavaPMGUI.48")); //$NON-NLS-1$
		markTranslated.setImage(new Image(display, "images/Small/processed.png")); //$NON-NLS-1$
		markTranslated.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				markTranslated();
			}
		});
		
		Menu helpMenu = new Menu(mainMenu);
		MenuItem help = new MenuItem(mainMenu,SWT.CASCADE);
		help.setText(Messages.getString("JavaPMGUI.68"));  //$NON-NLS-1$
		help.setMenu(helpMenu);

		MenuItem helpItem = new MenuItem(helpMenu,SWT.PUSH);
		helpItem.setText(Messages.getString("JavaPMGUI.19")); //$NON-NLS-1$
		helpItem.setImage(new Image(display, "images/Small/help.png")); //$NON-NLS-1$
		helpItem.setAccelerator(SWT.F1);
		helpItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				displayHelp();
			}
		});
		
		new MenuItem(helpMenu, SWT.SEPARATOR);
		
		MenuItem about = new MenuItem(helpMenu,SWT.PUSH);
		about.setText(Messages.getString("JavaPMGUI.69")); //$NON-NLS-1$
		about.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing				
			}

			public void widgetSelected(SelectionEvent arg0) {
				About box = new About(shell);
				box.show();
			}
		});
	}

	protected void saveLanguage(String string) {
		savePreferences("language",string); //$NON-NLS-1$
		MessageBox box = new MessageBox(shell,SWT.ICON_INFORMATION|SWT.OK);
		box.setMessage(Messages.getString("JavaPMGUI.15")); //$NON-NLS-1$
		box.open();
	}

	void savePreferences(String type,String value) {
		try {
    		Preferences prefs = new Preferences(Constants.preferences);
    		prefs.save("prefs", type, value); //$NON-NLS-1$
		} catch (Exception e) {
			MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
			if (e.getLocalizedMessage() != null) {
				box.setMessage(e.getLocalizedMessage());
			} else {
				box.setMessage(e.getMessage());
			}
			box.open();
		}
	}

	void close() {
		shell.close();
	}

	void saveProject() {
		if (javapm != null) {
			if (projectFile.equals("Untitled.jpm")) { //$NON-NLS-1$
				FileDialog fd = new FileDialog(shell,SWT.SAVE);
				String[] extensions = {"*.jpm", "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
				String[] names = {Messages.getString("JavaPMGUI.55"),Messages.getString("JavaPMGUI.56")}; //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterExtensions(extensions);
				fd.setFilterNames(names);
				fd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
				String file = fd.open();
				if (file != null) {
					if (!file.endsWith(".jpm")) { //$NON-NLS-1$
						file = file + ".jpm"; //$NON-NLS-1$
					}
					projectFile = file;
					javapm.setProjectFile(projectFile);
					DirectoryTracker.saveDirectory(fd.getFilterPath(), Constants.preferences);
				} else {
					return;
				}
			}
			try {
				javapm.saveProject();
			} catch (Exception e) {
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}
	}

	void openProject() {
		FileDialog fd = new FileDialog(shell,SWT.OPEN);
		String[] extensions = {"*.jpm", "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
		String[] names = {Messages.getString("JavaPMGUI.51"),Messages.getString("JavaPMGUI.52")}; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		fd.setFilterNames(names);
		fd.setFilterPath(DirectoryTracker.lastDirectory(Constants.preferences));
		String file = fd.open();
		if (file != null) {
			try {
				javapm = new JavaPM();
				javapm.openProject(file);
				DirectoryTracker.saveDirectory(fd.getFilterPath(),Constants.preferences);
				updateTable();
				projectFile = file;
			} catch (Exception e) {
				MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				box.setMessage(e.getMessage());
				box.open();
			}
		}		
	}

	void newProject() {
		javapm = new JavaPM();
		updateTable();
		projectFile = "Untitled.jpm"; //$NON-NLS-1$
	}

	private void updateTable() {
		table.removeAll();
		String[] files = javapm.getFiles();
		for (int i=0 ; i<files.length  ; i++) {
			TableItem item = new TableItem(table,SWT.NONE);
			item.setText(files[i]);
		}
	}

	public static void main(String[] args) {
		try {
			JavaPMGUI gui = new JavaPMGUI();
			gui.show();
		} catch (Exception e) {
			try {
				File tmp = File.createTempFile("error", ".log", new File("logs")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				PrintWriter writer = new PrintWriter(tmp);
				e.printStackTrace(writer);
				writer.close();
			} catch (Exception i) {
				i.printStackTrace();
			}
		}
	}

	private void show() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

}

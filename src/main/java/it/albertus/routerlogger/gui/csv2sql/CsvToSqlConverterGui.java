package it.albertus.routerlogger.gui.csv2sql;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.decoration.ControlValidatorDecoration;
import it.albertus.jface.listener.ByteVerifyListener;
import it.albertus.jface.validation.ByteTextValidator;
import it.albertus.jface.validation.ControlValidator;
import it.albertus.jface.validation.StringTextValidator;
import it.albertus.jface.validation.Validator;
import it.albertus.routerlogger.engine.RouterLoggerConfig;
import it.albertus.routerlogger.gui.Images;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.routerlogger.writer.CsvWriter;
import it.albertus.routerlogger.writer.DatabaseWriter;
import it.albertus.routerlogger.writer.csv2sql.CsvToSqlConverter;
import it.albertus.util.Configuration;
import it.albertus.util.ISupplier;
import it.albertus.util.logging.LoggerFactory;

class CsvToSqlConverterGui implements IShellProvider {

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlConverterGui.class);

	private final Configuration configuration = RouterLoggerConfig.getInstance();

	private final Shell shell;

	private List sourceFilesList;
	private Button removeButton;
	private Button clearButton;
	private Text csvSeparatorText;
	private Text csvTimestampPatternText;
	private Button csvResponseTimeFlag;
	private Text destinationDirectoryText;
	private Text sqlTableNameText;
	private Text sqlColumnNamesPrefixText;
	private Text sqlMaxLengthColumnNamesText;
	private Button processButton;

	private final Set<Validator> validators = new HashSet<Validator>();

	private final ModifyListener textModifyListener = new ModifyListener() {
		@Override
		public void modifyText(final ModifyEvent me) {
			updateProcessButtonStatus();
		}
	};

	/**
	 * Constructs a new instance of the <em>CSV to SQL converter</em> window,
	 * based on the provided shell.
	 * 
	 * @param shell the shell in which all the controls will be created (cannot
	 *        be null)
	 * 
	 * @see #open()
	 */
	CsvToSqlConverterGui(final Shell shell) {
		if (shell == null) {
			throw new NullPointerException("shell cannot be null");
		}
		else if (shell.isDisposed()) {
			throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
		}
		this.shell = shell;
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	/** Opens this <em>CSV to SQL converter</em> window. */
	void open() {
		shell.setText(Messages.get("lbl.csv2sql.dialog.title"));
		shell.setImages(Images.getMainIcons());
		createContents(shell);
		constrainShellSize(shell);
		shell.open();
	}

	private void constrainShellSize(final Shell shell) {
		shell.pack();
		final Point minSize = shell.getSize();
		shell.setMinimumSize(minSize);
		shell.setSize(SwtUtils.convertHorizontalDLUsToPixels(sourceFilesList, 280), minSize.y);
	}

	private void createContents(final Shell shell) {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(shell);
		createSourceGroup(shell);
		createDestinationGroup(shell);
		createButtonBar(shell);
	}

	private void createSourceGroup(final Shell parent) {
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.get("lbl.csv2sql.source"));
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);
		createSourceFilesList(group);
		createCsvSeparatorField(group);
		createCsvDatePatternField(group);
		createCsvResponseTimeFlag(group);
	}

	private void createSourceFilesList(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.source.files"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		sourceFilesList = new List(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 3).grab(true, true).applyTo(sourceFilesList);

		createSourceAddButton(parent);
		createSourceRemoveButton(parent);
		createSourceClearButton(parent);

		final ControlValidator<List> validator = new ControlValidator<List>(sourceFilesList) {
			@Override
			public boolean isValid() {
				return sourceFilesList != null && !sourceFilesList.isDisposed() && sourceFilesList.getItemCount() > 0;
			}
		};
		validators.add(validator);

		sourceFilesList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e != null) {
					if (SWT.NONE == e.stateMask && SwtUtils.KEY_DELETE == e.keyCode && sourceFilesList.getSelectionCount() > 0) {
						removeSelectedItemsFromList();
					}
					else if (SWT.MOD1 == e.stateMask && SwtUtils.KEY_SELECT_ALL == e.keyCode) {
						sourceFilesList.selectAll();
					}
				}
			}
		});

		createSourceFilesListMenu(sourceFilesList);
	}

	private void createSourceAddButton(final Composite parent) {
		final Button addButton = new Button(parent, SWT.PUSH);
		addButton.setText(Messages.get("lbl.csv2sql.source.add"));
		final int addButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(addButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(addButtonWidth, SWT.DEFAULT).applyTo(addButton);

		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final Set<String> files = selectSourceFiles(parent.getShell());
				if (!files.isEmpty()) {
					files.addAll(Arrays.asList(sourceFilesList.getItems())); // merge
					sourceFilesList.setItems(files.toArray(new String[files.size()]));
					if (sourceFilesList.getItemCount() > 0) {
						removeButton.setEnabled(true);
						clearButton.setEnabled(true);
					}
					if (destinationDirectoryText != null && !destinationDirectoryText.isDisposed() && destinationDirectoryText.getCharCount() == 0 && sourceFilesList.getItemCount() > 0) {
						final String lastItem = sourceFilesList.getItem(sourceFilesList.getItemCount() - 1);
						destinationDirectoryText.setText(new File(lastItem).getParent());
					}
				}
				updateProcessButtonStatus();
			}
		});
	}

	private void createSourceRemoveButton(final Composite parent) {
		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setEnabled(false);
		removeButton.setText(Messages.get("lbl.csv2sql.source.remove"));
		final int removeButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(removeButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(removeButtonWidth, SWT.DEFAULT).applyTo(removeButton);

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeSelectedItemsFromList();
			}
		});
	}

	private void createSourceClearButton(final Composite parent) {
		clearButton = new Button(parent, SWT.PUSH);
		clearButton.setEnabled(false);
		clearButton.setText(Messages.get("lbl.csv2sql.source.clear"));
		final int clearButtonWidth = SwtUtils.convertHorizontalDLUsToPixels(clearButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(clearButtonWidth, SWT.DEFAULT).applyTo(clearButton);

		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeAllItemsFromList();
			}
		});
	}

	private void createSourceFilesListMenu(final List sourceFilesList) {
		final Menu contextMenu = new Menu(sourceFilesList);

		// Remove...
		final MenuItem deleteMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		deleteMenuItem.setText(Messages.get("lbl.csv2sql.source.remove") + SwtUtils.getShortcutLabel(Messages.get("lbl.menu.item.delete.key")));
		deleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // dummy
		deleteMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeSelectedItemsFromList();
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Select all...
		final MenuItem selectAllMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		selectAllMenuItem.setText(JFaceMessages.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		selectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL); // dummy
		selectAllMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				sourceFilesList.selectAll();
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Clear...
		final MenuItem clearMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		clearMenuItem.setText(Messages.get("lbl.csv2sql.source.clear"));
		clearMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeAllItemsFromList();
			}
		});

		sourceFilesList.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(final MenuDetectEvent e) {
				deleteMenuItem.setEnabled(sourceFilesList.getSelectionCount() > 0);
				selectAllMenuItem.setEnabled(sourceFilesList.getItemCount() > 0);
				clearMenuItem.setEnabled(sourceFilesList.getItemCount() > 0);
				contextMenu.setVisible(true);
			}
		});
	}

	private void createCsvSeparatorField(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.source.csv.separator"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		csvSeparatorText = new Text(parent, SWT.BORDER);
		csvSeparatorText.setText(configuration.getString("csv.field.separator", CsvWriter.Defaults.FIELD_SEPARATOR));
		csvSeparatorText.setTextLimit(Byte.MAX_VALUE);
		csvSeparatorText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(csvSeparatorText);

		final ControlValidator<Text> validator = new StringTextValidator(csvSeparatorText, false);
		new ControlValidatorDecoration(validator, new ISupplier<String>() {
			@Override
			public String get() {
				return Messages.get("err.csv2sql.source.csv.separator");
			}
		});
		validators.add(validator);
	}

	private void createCsvDatePatternField(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.source.csv.date.pattern"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		csvTimestampPatternText = new Text(parent, SWT.BORDER);
		csvTimestampPatternText.setText(CsvWriter.DATE_PATTERN);
		csvTimestampPatternText.setTextLimit(Byte.MAX_VALUE);
		csvTimestampPatternText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(csvTimestampPatternText);

		final ControlValidator<Text> validator = new ControlValidator<Text>(csvTimestampPatternText) {
			@Override
			public boolean isValid() {
				if (getControl().getText().trim().isEmpty()) {
					return false;
				}
				try {
					new SimpleDateFormat(getControl().getText());
					return true;
				}
				catch (final Exception e) {
					return false;
				}
			}
		};
		new ControlValidatorDecoration(validator, new ISupplier<String>() {
			@Override
			public String get() {
				return Messages.get("err.csv2sql.source.csv.date.pattern");
			}
		});
		validators.add(validator);
	}

	private void createCsvResponseTimeFlag(final Composite parent) {
		csvResponseTimeFlag = new Button(parent, SWT.CHECK);
		csvResponseTimeFlag.setText(Messages.get("lbl.csv2sql.source.csv.responseTime"));
		csvResponseTimeFlag.setSelection(true);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(csvResponseTimeFlag);
	}

	private void createDestinationGroup(final Shell shell) {
		final Group group = new Group(shell, SWT.NONE);
		group.setText(Messages.get("lbl.csv2sql.destination"));
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);
		createDestinationDirectoryField(group);
		createDatabaseTableNameField(group);
		createDatabaseColumnNamePrefixField(group);
		createDatabaseMaxLengthColumnNamesField(group);
	}

	private void createDestinationDirectoryField(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.destination.directory"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		destinationDirectoryText = new Text(parent, SWT.BORDER);
		destinationDirectoryText.setEditable(false);
		destinationDirectoryText.addModifyListener(textModifyListener);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(destinationDirectoryText);

		final ControlValidator<Text> validator = new StringTextValidator(destinationDirectoryText, false);
		validators.add(validator);

		final Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceMessages.get("lbl.button.browse"));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).hint(buttonWidth, SWT.DEFAULT).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final String dir = selectDestinationPath(parent.getShell());
				if (dir != null) {
					destinationDirectoryText.setText(dir);
				}
			}
		});
	}

	private void createDatabaseTableNameField(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.destination.table.name"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		sqlTableNameText = new Text(parent, SWT.BORDER);
		sqlTableNameText.setTextLimit(Byte.MAX_VALUE);
		sqlTableNameText.setText(configuration.getString("database.table.name", DatabaseWriter.Defaults.TABLE_NAME));
		sqlTableNameText.addModifyListener(textModifyListener);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlTableNameText);

		final ControlValidator<Text> validator = new StringTextValidator(sqlTableNameText, false) {
			@Override
			public boolean isValid() {
				return super.isValid() && !sqlTableNameText.getText().trim().isEmpty();
			}
		};
		new ControlValidatorDecoration(validator, new ISupplier<String>() {
			@Override
			public String get() {
				return Messages.get("err.csv2sql.destination.table.name");
			}
		});
		validators.add(validator);
	}

	private void createDatabaseColumnNamePrefixField(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.destination.column.name.prefix"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		sqlColumnNamesPrefixText = new Text(parent, SWT.BORDER);
		sqlColumnNamesPrefixText.setTextLimit(Byte.MAX_VALUE);
		sqlColumnNamesPrefixText.setText(configuration.getString("database.column.name.prefix", DatabaseWriter.Defaults.COLUMN_NAME_PREFIX));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlColumnNamesPrefixText);
	}

	private void createDatabaseMaxLengthColumnNamesField(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.get("lbl.csv2sql.destination.column.name.max.length"));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

		sqlMaxLengthColumnNamesText = new Text(parent, SWT.BORDER);
		sqlMaxLengthColumnNamesText.setTextLimit(String.valueOf(Byte.MAX_VALUE).length());
		sqlMaxLengthColumnNamesText.setText(Integer.toString(configuration.getInt("database.column.name.max.length", DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH)));
		sqlMaxLengthColumnNamesText.addModifyListener(textModifyListener);
		sqlMaxLengthColumnNamesText.addVerifyListener(new ByteVerifyListener(false));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(sqlMaxLengthColumnNamesText);

		final ControlValidator<Text> validator = new ByteTextValidator(sqlMaxLengthColumnNamesText, false, (byte) 8, Byte.MAX_VALUE);
		new ControlValidatorDecoration(validator, new ISupplier<String>() {
			@Override
			public String get() {
				return JFaceMessages.get("err.preferences.integer.range", 8, Byte.MAX_VALUE);
			}
		});
		validators.add(validator);
	}

	private void createButtonBar(final Shell shell) {
		createProcessButton(shell);
		createCloseButton(shell);
	}

	private void createProcessButton(final Shell shell) {
		processButton = new Button(shell, SWT.PUSH);
		processButton.setEnabled(false);
		processButton.setText(Messages.get("lbl.csv2sql.button.convert"));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(processButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(processButton);
		processButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				process(shell);
			}
		});
	}

	private void createCloseButton(final Shell shell) {
		final Button button = new Button(shell, SWT.PUSH);
		button.setText(JFaceMessages.get("lbl.button.close"));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				shell.close();
			}
		});
	}

	private void updateProcessButtonStatus() {
		if (processButton != null && !processButton.isDisposed()) {
			processButton.setEnabled(isValid());
		}
	}

	private boolean isValid() {
		for (final Validator validator : validators) {
			if (!validator.isValid()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Converts the selected CSV files to SQL scripts.
	 * 
	 * @param shell the parent shell, needed to open the progress monitor dialog
	 */
	private void process(final Shell shell) {
		try {
			final String sqlTableName = sqlTableNameText.getText().trim();
			final String sqlColumnNamesPrefix = sqlColumnNamesPrefixText.getText().trim();
			final String sqlTimestampColumnName = DatabaseWriter.TIMESTAMP_BASE_COLUMN_NAME;
			final String sqlResponseTimeColumnName = csvResponseTimeFlag.getSelection() ? DatabaseWriter.RESPONSE_TIME_BASE_COLUMN_NAME : null;
			final int sqlMaxLengthColumnNames = Integer.parseInt(sqlMaxLengthColumnNamesText.getText().trim());
			final String csvSeparator = csvSeparatorText.getText();
			final String csvTimestampPattern = csvTimestampPatternText.getText().trim();

			final CsvToSqlConverter converter = new CsvToSqlConverter(csvSeparator, csvTimestampPattern, sqlTableName, sqlColumnNamesPrefix, sqlTimestampColumnName, sqlResponseTimeColumnName, sqlMaxLengthColumnNames);

			final CsvToSqlConverterRunnable runnable = new CsvToSqlConverterRunnable(converter, sourceFilesList.getItems(), destinationDirectoryText.getText());

			ProgressMonitorDialog.setDefaultImages(shell.getImages());
			final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell) {
				@Override
				protected void createCancelButton(final Composite parent) {
					super.createCancelButton(parent);
					cancel.setText(JFaceMessages.get("lbl.button.cancel"));
				}

				@Override
				protected void configureShell(final Shell shell) {
					super.configureShell(shell);
					shell.setText(Messages.get("lbl.csv2sql.progress.text"));
				}
			};

			dialog.run(true, true, runnable);

			final MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
			box.setText(shell.getText());
			box.setMessage(Messages.get("msg.csv2sql.dialog.result.message.success"));
			box.open();
		}
		catch (final InvocationTargetException e) {
			final String message = Messages.get("err.csv2sql.invocationTargetException");
			logger.log(Level.WARNING, message, e);
			EnhancedErrorDialog.openError(shell, shell.getText(), message, IStatus.WARNING, e.getCause() != null ? e.getCause() : e, shell.getDisplay().getSystemImage(SWT.ICON_WARNING));
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.log(Level.FINE, e.toString(), e);
			final MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION);
			box.setText(shell.getText());
			box.setMessage(Messages.get("msg.csv2sql.dialog.result.message.cancelled"));
			box.open();
		}
		catch (final Exception e) {
			final String message = Messages.get("err.csv2sql.exception");
			logger.log(Level.SEVERE, message, e);
			EnhancedErrorDialog.openError(shell, shell.getText(), message, IStatus.ERROR, e, shell.getDisplay().getSystemImage(SWT.ICON_ERROR));
		}
	}

	/**
	 * Opens the file dialog to set the source files.
	 * 
	 * @param parent the parent shell
	 * @return the selected file names
	 */
	private Set<String> selectSourceFiles(final Shell parent) {
		final FileDialog openDialog = new FileDialog(parent, SWT.OPEN | SWT.MULTI);
		openDialog.setFilterExtensions(new String[] { "*.CSV;*.csv" });
		openDialog.open();
		final Set<String> fileNames = new TreeSet<String>();
		for (final String fileName : openDialog.getFileNames()) {
			fileNames.add(openDialog.getFilterPath() + File.separator + fileName);
		}
		return fileNames;
	}

	/**
	 * Opens the directory dialog to set destination directory.
	 * 
	 * @param parent the parent shell
	 * @return the selected directory
	 */
	private String selectDestinationPath(final Shell parent) {
		final DirectoryDialog saveDialog = new DirectoryDialog(parent, SWT.NONE);
		saveDialog.setText(Messages.get("lbl.csv2sql.destination.dialog.text"));
		saveDialog.setMessage(Messages.get("lbl.csv2sql.destination.dialog.message"));
		return saveDialog.open();
	}

	private void removeSelectedItemsFromList() {
		if (sourceFilesList.getSelectionCount() > 0) {
			sourceFilesList.remove(sourceFilesList.getSelectionIndices());
			if (sourceFilesList.getItemCount() == 0) {
				removeButton.setEnabled(false);
				clearButton.setEnabled(false);
			}
			updateProcessButtonStatus();
		}
	}

	private void removeAllItemsFromList() {
		if (sourceFilesList.getItemCount() > 0) {
			sourceFilesList.removeAll();
			removeButton.setEnabled(false);
			clearButton.setEnabled(false);
			updateProcessButtonStatus();
		}
	}

}

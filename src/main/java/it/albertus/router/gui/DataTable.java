package it.albertus.router.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import it.albertus.jface.DisplayThreadExecutor;
import it.albertus.jface.SwtUtils;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.ClearDataTableSelectionListener;
import it.albertus.router.gui.listener.CopyDataTableSelectionListener;
import it.albertus.router.gui.listener.DataTableContextMenuDetectListener;
import it.albertus.router.gui.listener.DeleteDataTableSelectionListener;
import it.albertus.router.gui.listener.SelectAllDataTableSelectionListener;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

public class DataTable {

	private static final Logger logger = LoggerFactory.getLogger(DataTable.class);

	private static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	private static final char SAMPLE_CHAR = '9';
	private static final char FIELD_SEPARATOR = '\t';

	private static final String CFG_KEY_GUI_TABLE_COLUMNS_PADDING_RIGHT = "gui.table.columns.padding.right";
	private static final String CFG_KEY_GUI_TABLE_COLUMNS_PACK = "gui.table.columns.pack";
	private static final String CFG_KEY_GUI_IMPORTANT_KEYS_COLOR_BACKGROUND = "gui.important.keys.color.background";
	private static final String CFG_KEY_GUI_THRESHOLDS_REACHED_COLOR_FOREGROUND = "gui.thresholds.reached.color.foreground";

	private static final String FONT_KEY_TABLE_BOLD = "tableBold";

	private static final DateFormat dateFormatTable = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	public static class Defaults {
		public static final int MAX_ITEMS = 2000;
		public static final boolean COLUMNS_PACK = false;
		public static final byte COLUMNS_PADDING_RIGHT = 0;
		public static final String IMPORTANT_KEYS_COLOR_BACKGROUND = "255,255,0";
		public static final String THRESHOLDS_REACHED_COLOR_FOREGROUND = "255,0,0";

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	enum TableDataKey {
		INITIALIZED(Boolean.class);

		private final Class<?> type;

		private TableDataKey(final Class<?> type) {
			this.type = type;
		}

		public Class<?> getType() {
			return type;
		}
	}

	private int iteration;

	private final TableViewer tableViewer;

	private final Menu contextMenu;
	private final MenuItem copyMenuItem;
	private final MenuItem deleteMenuItem;
	private final MenuItem selectAllMenuItem;
	private final MenuItem clearMenuItem;

	/**
	 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
	 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri
	 * casi (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
	 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
	 * combinazioni di tasti saranno ignorate.
	 */
	protected DataTable(final Composite parent, final Object layoutData, final RouterLoggerGui gui) {
		tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		final Table table = tableViewer.getTable();
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setData(TableDataKey.INITIALIZED.toString(), false);

		contextMenu = new Menu(table);

		// Copy...
		copyMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		copyMenuItem.setText(Messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		copyMenuItem.addSelectionListener(new CopyDataTableSelectionListener(gui));
		copyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY); // Finto!

		// Delete...
		deleteMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		deleteMenuItem.setText(Messages.get("lbl.menu.item.delete") + SwtUtils.getShortcutLabel(Messages.get("lbl.menu.item.delete.key")));
		deleteMenuItem.addSelectionListener(new DeleteDataTableSelectionListener(gui));
		deleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // Finto!

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Select all...
		selectAllMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		selectAllMenuItem.setText(Messages.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		selectAllMenuItem.addSelectionListener(new SelectAllDataTableSelectionListener(gui));
		selectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL); // Finto!

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Clear...
		clearMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		clearMenuItem.setText(Messages.get("lbl.menu.item.clear"));
		clearMenuItem.addSelectionListener(new ClearDataTableSelectionListener(gui));

		table.addMenuDetectListener(new DataTableContextMenuDetectListener(gui));
	}

	/** Copies the current selection to the clipboard. */
	public void copy() {
		if (tableViewer != null) {
			final Table table = tableViewer.getTable();
			if (table != null && !table.isDisposed() && table.getColumns() != null && table.getColumns().length != 0 && table.getSelectionCount() > 0) {
				StringBuilder data = new StringBuilder();

				// Testata...
				for (final TableColumn column : table.getColumns()) {
					data.append(column.getText()).append(FIELD_SEPARATOR);
				}
				data.replace(data.length() - 1, data.length(), NewLine.SYSTEM_LINE_SEPARATOR);
				if (data.length() > configuration.getInt(RouterLoggerGui.CFG_KEY_GUI_CLIPBOARD_MAX_CHARS, RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS)) {
					final MessageBox messageBox = new MessageBox(table.getShell(), SWT.ICON_WARNING);
					messageBox.setText(Messages.get("lbl.window.title"));
					messageBox.setMessage(Messages.get("err.clipboard.cannot.copy"));
					messageBox.open();
					return;
				}

				// Dati selezionati (ogni TableItem rappresenta una riga)...
				boolean limited = false;
				final int columnCount = table.getColumnCount();
				for (final TableItem item : table.getSelection()) {
					final StringBuilder row = new StringBuilder();
					for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
						row.append(item.getText(columnIndex));
						if (columnIndex != columnCount - 1) {
							row.append(FIELD_SEPARATOR);
						}
						else {
							row.append(NewLine.SYSTEM_LINE_SEPARATOR);
						}
					}
					if (row.length() + data.length() > configuration.getInt(RouterLoggerGui.CFG_KEY_GUI_CLIPBOARD_MAX_CHARS, RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS)) {
						limited = true;
						break;
					}
					else {
						data.append(row);
					}
				}

				// Inserimento dati negli appunti...
				final Clipboard clipboard = new Clipboard(table.getDisplay());
				clipboard.setContents(new String[] { data.toString() }, new TextTransfer[] { TextTransfer.getInstance() });
				clipboard.dispose();
				if (limited) {
					final MessageBox messageBox = new MessageBox(table.getShell(), SWT.ICON_INFORMATION);
					messageBox.setText(Messages.get("lbl.window.title"));
					messageBox.setMessage(Messages.get("err.clipboard.limited.copy", data.length()));
					messageBox.open();
				}
			}
		}
	}

	public void delete() {
		if (tableViewer != null) {
			final Table table = tableViewer.getTable();
			if (table != null && !table.isDisposed() && table.getColumns() != null && table.getColumns().length != 0 && table.getSelectionCount() > 0) {
				table.setRedraw(false);
				table.remove(table.getSelectionIndices());
				table.setRedraw(true);
			}
		}
	}

	public void clear() {
		if (tableViewer != null) {
			final Table table = tableViewer.getTable();
			if (table != null && !table.isDisposed() && table.getColumns() != null && table.getColumns().length != 0) {
				table.setRedraw(false);
				table.removeAll();
				table.setRedraw(true);
			}
		}
	}

	public void reset() {
		if (tableViewer != null) {
			final Table table = tableViewer.getTable();
			if (table != null && !table.isDisposed()) {
				table.setRedraw(false);
				table.removeAll();
				iteration = 0;
				for (final TableColumn tc : table.getColumns()) {
					tc.dispose();
				}
				table.setData(TableDataKey.INITIALIZED.toString(), false);
				table.setRedraw(true);
			}
		}
	}

	public boolean canDelete() {
		return canCopy();
	}

	public boolean canClear() {
		return canSelectAll();
	}

	public boolean canCopy() {
		return tableViewer != null && tableViewer.getTable() != null && tableViewer.getTable().getSelectionCount() > 0;
	}

	public boolean canSelectAll() {
		return tableViewer != null && tableViewer.getTable() != null && tableViewer.getTable().getItemCount() > 0;
	}

	public void addRow(final int iteration, final RouterData data, final Map<Threshold, String> thresholdsReached) {
		if (data != null && data.getData() != null && !data.getData().isEmpty()) {
			final Map<String, String> info = data.getData();
			final String timestamp = dateFormatTable.format(data.getTimestamp());
			final int maxItems = configuration.getInt("gui.table.items.max", Defaults.MAX_ITEMS);
			final Table table = tableViewer.getTable();
			new DisplayThreadExecutor(table).execute(new Runnable() {
				@Override
				public void run() {
					// Header (una tantum)...
					if (!(Boolean) table.getData(TableDataKey.INITIALIZED.toString())) {
						// Disattivazione ridisegno automatico...
						table.setRedraw(false);

						// Iterazione...
						TableColumn column = new TableColumn(table, SWT.NONE);
						column.setText(Messages.get("lbl.column.iteration.text"));
						column.setToolTipText(Messages.get("lbl.column.iteration.tooltip"));

						// Timestamp...
						column = new TableColumn(table, SWT.NONE);
						column.setText(Messages.get("lbl.column.timestamp.text"));
						column.setToolTipText(Messages.get("lbl.column.timestamp.tooltip"));

						// Tempo di risposta...
						column = new TableColumn(table, SWT.NONE);
						column.setText(Messages.get("lbl.column.response.time.text"));
						column.setToolTipText(Messages.get("lbl.column.response.time.tooltip"));

						// Tutte le altre colonne...
						for (String key : info.keySet()) {
							column = new TableColumn(table, SWT.NONE);
							column.setText(configuration.getBoolean(CFG_KEY_GUI_TABLE_COLUMNS_PACK, Defaults.COLUMNS_PACK) ? " " : key);
							column.setToolTipText(key);
						}
					}

					// Dati...
					int i = 0;
					final TableItem item = new TableItem(table, SWT.NONE, 0);
					item.setText(i++, Integer.toString(iteration));
					item.setText(i++, timestamp);
					item.setText(i++, Integer.toString(data.getResponseTime()));

					final Color importantKeyBackgroundColor = getImportantKeysBackgroundColor();
					for (final Entry<String, String> entry : info.entrySet()) {
						final String key = entry.getKey();
						// Grassetto...
						if (key != null && configuration.getGuiImportantKeys().contains(key.trim())) {
							FontRegistry fontRegistry = JFaceResources.getFontRegistry();
							if (!fontRegistry.hasValueFor(FONT_KEY_TABLE_BOLD)) {
								final Font tableFont = item.getFont();
								final FontData oldFontData = tableFont.getFontData()[0];
								fontRegistry.put(FONT_KEY_TABLE_BOLD, new FontData[] { new FontData(oldFontData.getName(), oldFontData.getHeight(), SWT.BOLD) });
							}
							item.setFont(i, fontRegistry.get(FONT_KEY_TABLE_BOLD));

							// Evidenzia cella...
							item.setBackground(i, importantKeyBackgroundColor);
						}

						// Colore per i valori oltre soglia...
						final Color thresholdsReachedForegroundColor = getThresholdsReachedForegroundColor();
						for (final Threshold threshold : thresholdsReached.keySet()) {
							if (key != null && key.equals(threshold.getKey())) {
								item.setForeground(i, thresholdsReachedForegroundColor);
								break;
							}
						}

						item.setText(i++, entry.getValue());
					}

					// Dimensionamento delle colonne (una tantum)...
					if (!(Boolean) table.getData(TableDataKey.INITIALIZED.toString())) {
						final TableItem iterationTableItem = table.getItem(0);
						final String originalIteration = iterationTableItem.getText(0);
						setSampleNumber(iterationTableItem, 4);
						final byte margin = configuration.getByte(CFG_KEY_GUI_TABLE_COLUMNS_PADDING_RIGHT, Defaults.COLUMNS_PADDING_RIGHT);
						for (int j = 0; j < table.getColumns().length; j++) {
							addRightMargin(iterationTableItem, j, margin);
							table.getColumn(j).pack();
							removeRightMargin(iterationTableItem, j, margin);
						}
						iterationTableItem.setText(0, originalIteration);

						if (configuration.getBoolean(CFG_KEY_GUI_TABLE_COLUMNS_PACK, Defaults.COLUMNS_PACK)) {
							table.getColumn(2).setWidth(table.getColumn(0).getWidth());
							final String[] stringArray = new String[info.keySet().size()];
							final TableColumn[] columns = table.getColumns();
							final int startIndex = 3;
							for (int k = startIndex; k < columns.length; k++) {
								final TableColumn column = columns[k];
								column.setText(info.keySet().toArray(stringArray)[k - startIndex]);
							}
						}
						table.setData(TableDataKey.INITIALIZED.toString(), true);

						// Attivazione ridisegno automatico...
						table.setRedraw(true);
					}

					// Limitatore righe in tabella...
					if (table.getItemCount() == maxItems + 1) {
						table.remove(table.getItemCount() - 1);
					}
					else if (table.getItemCount() > maxItems) {
						table.setRedraw(false);
						do {
							table.remove(table.getItemCount() - 1);
						}
						while (table.getItemCount() > maxItems);
						table.setRedraw(true);
					}
					if (Util.isGtk()) {
						table.setTopIndex(table.getTopIndex() - 1);
					}
				}
			});
			this.iteration = iteration;
		}
	}

	public Color getThresholdsReachedForegroundColor() {
		return getColor(CFG_KEY_GUI_THRESHOLDS_REACHED_COLOR_FOREGROUND, Defaults.THRESHOLDS_REACHED_COLOR_FOREGROUND);
	}

	public Color getImportantKeysBackgroundColor() {
		return getColor(CFG_KEY_GUI_IMPORTANT_KEYS_COLOR_BACKGROUND, Defaults.IMPORTANT_KEYS_COLOR_BACKGROUND);
	}

	private Color getColor(final String configurationKey, final String defaultColorKey) {
		String colorKey = configuration.getString(configurationKey, defaultColorKey);
		RGB rgbColorData;
		try {
			rgbColorData = StringConverter.asRGB(colorKey);
		}
		catch (final RuntimeException e) {
			logger.log(Level.FINER, e.toString(), e);
			logger.log(Level.INFO, Messages.get("err.invalid.color"), colorKey);
			colorKey = defaultColorKey;
			rgbColorData = StringConverter.asRGB(colorKey);
		}
		final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		if (!colorRegistry.hasValueFor(colorKey)) {
			colorRegistry.put(colorKey, rgbColorData);
		}
		return colorRegistry.get(colorKey);
	}

	/** Consente la determinazione automatica della larghezza del campo. */
	private static void setSampleNumber(final TableItem tableItem, final int size) {
		final char[] sample = new char[size];
		for (int i = 0; i < size; i++) {
			sample[i] = SAMPLE_CHAR;
		}
		tableItem.setText(String.valueOf(sample));
	}

	private static void addRightMargin(final TableItem item, final int index, final byte margin) {
		if (margin > 0) {
			final StringBuilder text = new StringBuilder(item.getText(index));
			for (byte i = 0; i < margin; i++) {
				text.append(' ');
			}
			item.setText(index, text.toString());
		}
	}

	private static void removeRightMargin(final TableItem item, final int index, final byte margin) {
		if (margin > 0) {
			final String text = item.getText(index);
			item.setText(index, text.substring(0, text.length() - margin));
		}
	}

	public void updateTexts() {
		copyMenuItem.setText(Messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		deleteMenuItem.setText(Messages.get("lbl.menu.item.delete") + SwtUtils.getShortcutLabel(Messages.get("lbl.menu.item.delete.key")));
		selectAllMenuItem.setText(Messages.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		clearMenuItem.setText(Messages.get("lbl.menu.item.clear"));
	}

	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public Table getTable() {
		return tableViewer != null ? tableViewer.getTable() : null;
	}

	public Menu getContextMenu() {
		return contextMenu;
	}

	public MenuItem getCopyMenuItem() {
		return copyMenuItem;
	}

	public MenuItem getDeleteMenuItem() {
		return deleteMenuItem;
	}

	public MenuItem getSelectAllMenuItem() {
		return selectAllMenuItem;
	}

	public MenuItem getClearMenuItem() {
		return clearMenuItem;
	}

	public int getIteration() {
		return iteration;
	}

}

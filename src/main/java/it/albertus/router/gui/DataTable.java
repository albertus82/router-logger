package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.ClearDataTableSelectionListener;
import it.albertus.router.gui.listener.CopyDataTableSelectionListener;
import it.albertus.router.gui.listener.DataTableContextMenuDetectListener;
import it.albertus.router.gui.listener.DeleteDataTableSelectionListener;
import it.albertus.router.gui.listener.SelectAllDataTableSelectionListener;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class DataTable {

	private static final char SAMPLE_CHAR = '9';
	private static final char FIELD_SEPARATOR = '\t';

	private static final DateFormat dateFormatTable = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	public interface Defaults {
		int GUI_TABLE_MAX_ITEMS = 2000;
		boolean GUI_TABLE_COLUMNS_PACK = false;
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

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	private int iteration;

	private final Table table;

	private final Menu contextMenu;
	private final MenuItem copyMenuItem;
	private final MenuItem deleteMenuItem;
	private final MenuItem selectAllMenuItem;
	private final MenuItem clearMenuItem;

	private final Color importantKeyBackgroundColor;
	private final Color thresholdReachedForegroudColor;

	/**
	 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
	 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri
	 * casi (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
	 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
	 * combinazioni di tasti saranno ignorate.
	 */
	public DataTable(final Composite parent, final Object layoutData, final RouterLoggerGui gui) {
		table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setData(TableDataKey.INITIALIZED.toString(), false);

		contextMenu = new Menu(table);

		// Copy...
		copyMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		copyMenuItem.setText(Resources.get("lbl.menu.item.copy") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_COPY));
		copyMenuItem.addSelectionListener(new CopyDataTableSelectionListener(gui));
		copyMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_COPY); // Finto!

		// Delete...
		deleteMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		deleteMenuItem.setText(Resources.get("lbl.menu.item.delete") + GuiUtils.getShortcutLabel(Resources.get("lbl.menu.item.delete.key")));
		deleteMenuItem.addSelectionListener(new DeleteDataTableSelectionListener(gui));
		deleteMenuItem.setAccelerator(GuiUtils.KEY_DELETE); // Finto!

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Select all...
		selectAllMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		selectAllMenuItem.setText(Resources.get("lbl.menu.item.select.all") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_SELECT_ALL));
		selectAllMenuItem.addSelectionListener(new SelectAllDataTableSelectionListener(gui));
		selectAllMenuItem.setAccelerator(SWT.MOD1 | GuiUtils.KEY_SELECT_ALL); // Finto!

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Clear...
		clearMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		clearMenuItem.setText(Resources.get("lbl.menu.item.clear"));
		clearMenuItem.addSelectionListener(new ClearDataTableSelectionListener(gui));

		table.addMenuDetectListener(new DataTableContextMenuDetectListener(gui));

		importantKeyBackgroundColor = table.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		thresholdReachedForegroudColor = table.getDisplay().getSystemColor(SWT.COLOR_RED);
	}

	/** Copies the current selection to the clipboard. */
	public void copy() {
		if (table != null && !table.isDisposed() && table.getColumns() != null && table.getColumns().length != 0 && table.getSelectionCount() > 0) {
			if (table.getSelectionCount() > 1) {
				System.gc(); // La copia puo' richiedere molta memoria!
			}
			StringBuilder data = new StringBuilder();

			// Testata...
			for (final TableColumn column : table.getColumns()) {
				data.append(column.getText()).append(FIELD_SEPARATOR);
			}
			data.replace(data.length() - 1, data.length(), NewLine.SYSTEM_LINE_SEPARATOR);

			// Dati selezionati (ogni TableItem rappresenta una riga)...
			for (final TableItem item : table.getSelection()) {
				for (int i = 0; i < table.getColumnCount(); i++) {
					data.append(item.getText(i)).append(FIELD_SEPARATOR);
				}
				data.replace(data.length() - 1, data.length(), NewLine.SYSTEM_LINE_SEPARATOR);
			}

			// Inserimento dati negli appunti...
			final Clipboard clipboard = new Clipboard(table.getDisplay());
			clipboard.setContents(new String[] { data.toString() }, new TextTransfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
		}
	}

	public void delete() {
		if (table != null && !table.isDisposed() && table.getColumns() != null && table.getColumns().length != 0 && table.getSelectionCount() > 0) {
			table.setRedraw(false);
			table.remove(table.getSelectionIndices());
			table.setRedraw(true);
		}
	}

	public void clear() {
		if (table != null && !table.isDisposed() && table.getColumns() != null && table.getColumns().length != 0) {
			table.setRedraw(false);
			table.removeAll();
			table.setRedraw(true);
		}
	}

	public void reset() {
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

	public void addRow(final int iteration, final RouterData data, final Map<Threshold, String> thresholdsReached) {
		if (data != null && data.getData() != null && !data.getData().isEmpty()) {
			final Map<String, String> info = data.getData();
			final String timestamp = dateFormatTable.format(data.getTimestamp());
			final int maxItems = configuration.getInt("gui.table.items.max", Defaults.GUI_TABLE_MAX_ITEMS);
			new GuiThreadExecutor(table) {
				@Override
				protected void run() {
					// Header (una tantum)...
					if (!(Boolean) table.getData(TableDataKey.INITIALIZED.toString())) {
						// Disattivazione ridisegno automatico...
						table.setRedraw(false);

						// Iterazione...
						TableColumn column = new TableColumn(table, SWT.NONE);
						column.setText(Resources.get("lbl.column.iteration.text"));
						column.setToolTipText(Resources.get("lbl.column.iteration.tooltip"));

						// Timestamp...
						column = new TableColumn(table, SWT.NONE);
						column.setText(Resources.get("lbl.column.timestamp.text"));
						column.setToolTipText(Resources.get("lbl.column.timestamp.tooltip"));

						// Tempo di risposta...
						column = new TableColumn(table, SWT.NONE);
						column.setText(Resources.get("lbl.column.response.time.text"));
						column.setToolTipText(Resources.get("lbl.column.response.time.tooltip"));

						// Tutte le altre colonne...
						for (String key : info.keySet()) {
							column = new TableColumn(table, SWT.NONE);
							column.setText(configuration.getBoolean("gui.table.columns.pack", Defaults.GUI_TABLE_COLUMNS_PACK) ? " " : key);
							column.setToolTipText(key);
						}
					}

					// Dati...
					int i = 0;
					final TableItem item = new TableItem(table, SWT.NONE, 0);
					item.setText(i++, Integer.toString(iteration));
					item.setText(i++, timestamp);
					item.setText(i++, Integer.toString(data.getResponseTime()));

					for (final String key : info.keySet()) {
						// Grassetto...
						if (key != null && configuration.getGuiImportantKeys().contains(key.trim())) {
							FontRegistry fontRegistry = JFaceResources.getFontRegistry();
							if (!fontRegistry.hasValueFor("tableBold")) {
								final Font tableFont = item.getFont();
								final FontData oldFontData = tableFont.getFontData()[0];
								fontRegistry.put("tableBold", new FontData[] { new FontData(oldFontData.getName(), oldFontData.getHeight(), SWT.BOLD) });
							}
							item.setFont(i, fontRegistry.get("tableBold"));

							// Evidenzia cella...
							item.setBackground(i, importantKeyBackgroundColor);
						}

						// Colore per i valori oltre soglia...
						for (final Threshold threshold : thresholdsReached.keySet()) {
							if (key.equals(threshold.getKey())) {
								item.setForeground(i, thresholdReachedForegroudColor);
								break;
							}
						}

						item.setText(i++, info.get(key));
					}

					// Dimesionamento delle colonne (una tantum)...
					if (!(Boolean) table.getData(TableDataKey.INITIALIZED.toString())) {
						final TableItem iterationTableItem = table.getItem(0);
						final String originalIteration = iterationTableItem.getText();
						setSampleNumber(iterationTableItem, 4);
						for (int j = 0; j < table.getColumns().length; j++) {
							table.getColumn(j).pack();
						}
						iterationTableItem.setText(originalIteration);

						if (configuration.getBoolean("gui.table.columns.pack", Defaults.GUI_TABLE_COLUMNS_PACK)) {
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
					if (table.getItemCount() > maxItems) {
						table.remove(maxItems);
					}
				}
			}.start();
			this.iteration = iteration;
		}
	}

	/** Consente la determinazione automatica della larghezza del campo. */
	private void setSampleNumber(final TableItem tableItem, final int size) {
		final char[] sample = new char[size];
		for (int i = 0; i < size; i++) {
			sample[i] = SAMPLE_CHAR;
		}
		tableItem.setText(String.valueOf(sample));
	}

	public Table getTable() {
		return table;
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

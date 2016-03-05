package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class DataTable {

	private interface Defaults {
		int GUI_TABLE_MAX_ITEMS = 2000;
		boolean GUI_TABLE_COLUMNS_PACK = false;
	}

	private static class Singleton {
		private static final DataTable TABLE = new DataTable();
	}

	public static DataTable getInstance() {
		return Singleton.TABLE;
	}

	private RouterLoggerGui gui;

	private Menu contextMenu;
	private MenuItem copyMenuItem;
	private MenuItem selectAllMenuItem;

	private DataTable() {}

	public void init(final RouterLoggerGui gui, final Object layoutData) {
		if (this.table == null) {
			this.gui = gui;
			this.table = createTable(gui.getShell(), layoutData);
			createContextMenu();
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	private Table createTable(final Composite container, final Object layoutData) {
		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLayoutData(layoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		return table;
	}

	private void createContextMenu() {
		contextMenu = new Menu(table);

		// Copia...
		copyMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		copyMenuItem.setText(Resources.get("lbl.menu.item.copy") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_COPY));
		copyMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				copySelection();
			}
		});

		new MenuItem(contextMenu, SWT.SEPARATOR);

		// Seleziona tutto...
		selectAllMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		selectAllMenuItem.setText(Resources.get("lbl.menu.item.select.all") + GuiUtils.getMod1KeyLabel() + Character.toUpperCase(GuiUtils.KEY_SELECT_ALL));
		selectAllMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.selectAll();
			}
		});

		table.addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent e) {
				copyMenuItem.setEnabled(gui.canCopyDataTable());
				selectAllMenuItem.setEnabled(gui.canSelectAllDataTable());
				contextMenu.setVisible(true);
			}
		});
	}

	public void copySelection() {
		if (table.getColumns() != null && table.getColumns().length != 0 && table.getSelectionCount() > 0) {
			if (table.getSelectionCount() > 1) {
				System.gc(); // La copia puo' richiedere molta memoria!
			}
			final StringBuilder data = new StringBuilder();

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

	private static final char FIELD_SEPARATOR = '\t';
	private static final DateFormat DATE_FORMAT_TABLE_GUI = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private Table table = null;
	private boolean tableInitialized = false;
	private final boolean packColumns = configuration.getBoolean("gui.table.columns.pack", Defaults.GUI_TABLE_COLUMNS_PACK);

	public void addRow(final RouterData data, final Map<Threshold, String> thresholdsReached, final int iteration) {
		if (data != null && data.getData() != null && !data.getData().isEmpty()) {
			final Map<String, String> info = data.getData();
			final String timestamp = DATE_FORMAT_TABLE_GUI.format(data.getTimestamp());
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						if (table != null && !table.isDisposed()) {
							// Header (una tantum)...
							if (!tableInitialized) {
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
									column.setText(packColumns ? " " : key);
									column.setToolTipText(key);
								}
							}

							// Dati...
							int i = 0;
							final TableItem item = new TableItem(table, SWT.NONE, 0);
							item.setText(i++, Integer.toString(iteration));
							item.setText(i++, timestamp);
							item.setText(i++, Integer.toString(data.getResponseTime()));

							for (String key : info.keySet()) {
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
									item.setBackground(i, item.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
								}

								// Colore per i valori oltre soglia...
								for (final Threshold threshold : thresholdsReached.keySet()) {
									if (key.equals(threshold.getKey())) {
										item.setForeground(i, item.getDisplay().getSystemColor(SWT.COLOR_RED));
										break;
									}
								}

								item.setText(i++, info.get(key));
							}

							// Dimesionamento delle colonne (una tantum)...
							if (!tableInitialized) {
								for (int j = 0; j < table.getColumns().length; j++) {
									table.getColumn(j).pack();
								}
								table.getColumn(0).setWidth((int) (table.getColumn(0).getWidth() * 1.3));

								if (packColumns) {
									table.getColumn(2).setWidth((int) (table.getColumn(0).getWidth()));
									final String[] stringArray = new String[info.keySet().size()];
									final TableColumn[] columns = table.getColumns();
									final int startIndex = 3;
									for (int k = startIndex; k < columns.length; k++) {
										final TableColumn column = columns[k];
										column.setText(info.keySet().toArray(stringArray)[k - startIndex]);
									}
								}
								tableInitialized = true;
							}

							// Limitatore righe in tabella...
							final int maxItems = configuration.getInt("gui.table.items.max", Defaults.GUI_TABLE_MAX_ITEMS);
							if (table.getItemCount() > maxItems) {
								table.remove(maxItems);
							}
						}
					}
					catch (IllegalArgumentException iae) {}
					catch (SWTException se) {}
				}
			});
		}
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

	public MenuItem getSelectAllMenuItem() {
		return selectAllMenuItem;
	}

}

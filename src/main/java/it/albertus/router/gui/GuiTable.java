package it.albertus.router.gui;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GuiTable {

	private interface Defaults {
		int MAX_ITEMS = 5000;
		String GUI_IMPORTANT_KEYS_SEPARATOR = ",";
		boolean GUI_TABLE_COLUMNS_PACK = false;
	}

	private static class Singleton {
		private static final GuiTable table = new GuiTable();
	}

	public static GuiTable getInstance() {
		return Singleton.table;
	}

	private GuiTable() {}

	public void init(final Composite container) {
		if (this.table == null) {
			this.table = createTable(container);

			// Caricamento chiavi importanti da evidenziare...
			for (String importantKey : configuration.getString("gui.important.keys", "").split(configuration.getString("gui.important.keys.separator", Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).trim())) {
				if (StringUtils.isNotBlank(importantKey)) {
					importantKeys.add(importantKey.trim());
				}
			}
		}
		else {
			throw new IllegalStateException(this.getClass().getSimpleName() + " already initialized.");
		}
	}

	private Table createTable(final Composite container) {
		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.minimumHeight = 200;
		gridData.heightHint = 200;
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		return table;
	}

	private static final DateFormat DATE_FORMAT_TABLE_GUI = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	private static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	private Table table = null;
	private boolean tableInitialized = false;
	private final boolean packColumns = configuration.getBoolean("gui.table.columns.pack", Defaults.GUI_TABLE_COLUMNS_PACK);
	private final int maxItems = configuration.getInt("gui.table.items.max", Defaults.MAX_ITEMS);
	private final Set<String> importantKeys = new HashSet<String>();

	public void addRow(final Map<String, String> info, final int iteration) {
		if (table != null && !table.isDisposed() && info != null && !info.isEmpty()) {
			final String timestamp = DATE_FORMAT_TABLE_GUI.format(new Date());
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						// Header (una tantum)...
						if (!tableInitialized) {
							TableColumn column = new TableColumn(table, SWT.NONE);
							column.setText("#");
							column.setToolTipText("Iteration");
							column = new TableColumn(table, SWT.NONE);
							column.setText("Timestamp");
							column.setToolTipText("Timestamp");
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

						for (String key : info.keySet()) {
							// Grassetto...
							if (key != null && importantKeys.contains(key.trim())) {
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
							if (configuration.getThresholds().getReachedKeys(info).contains(key)) {
								item.setForeground(i, item.getDisplay().getSystemColor(SWT.COLOR_RED));
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
								final String[] stringArray = new String[info.keySet().size()];
								final TableColumn[] columns = table.getColumns();
								final int startIndex = 2;
								for (int k = startIndex; k < columns.length; k++) {
									final TableColumn column = columns[k];
									column.setText(info.keySet().toArray(stringArray)[k - startIndex]);
								}
							}
							tableInitialized = true;
						}

						// Limitatore righe in tabella...
						if (table.getItemCount() > maxItems) {
							table.remove(maxItems);
						}
					}
					catch (IllegalArgumentException iae) {}
					catch (SWTException se) {}
				}
			});
		}
	}

}

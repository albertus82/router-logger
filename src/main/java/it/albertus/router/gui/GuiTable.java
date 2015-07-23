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
		String GUI_BOLD_KEYS_SEPARATOR = ",";
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

			// Caricamento chiavi da mostrare in grassetto...
			for (String boldKey : configuration.getString("gui.bold.keys", "").split(configuration.getString("gui.bold.keys.separator", Defaults.GUI_BOLD_KEYS_SEPARATOR).trim())) {
				if (StringUtils.isNotBlank(boldKey)) {
					boldColumns.add(boldKey.trim());
				}
			}
		}
		else {
			throw new IllegalStateException(this.getClass().getSimpleName() + " already initialized.");
		}
	}

	private Table createTable(final Composite container) {
		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
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
	private boolean tablePacked = false;
	private final Set<String> boldColumns = new HashSet<String>();

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
							column = new TableColumn(table, SWT.NONE);
							column.setText("Timestamp");
							for (String key : info.keySet()) {
								column = new TableColumn(table, SWT.NONE);
								column.setText(key);
							}
							tableInitialized = true;
						}

						// Dati...
						int i = 0;
						TableItem item = new TableItem(table, SWT.NONE, 0);
						item.setText(i++, Integer.toString(iteration));
						item.setText(i++, timestamp);

						for (String key : info.keySet()) {
							// Grassetto...
							if (key != null && boldColumns.contains(key.trim())) {
								FontRegistry fontRegistry = JFaceResources.getFontRegistry();
								if (!fontRegistry.hasValueFor("tableBold")) {
									final Font tableFont = item.getFont();
									final FontData oldFontData = tableFont.getFontData()[0];
									fontRegistry.put("tableBold", new FontData[] { new FontData(oldFontData.getName(), oldFontData.getHeight(), SWT.BOLD) });
								}
								item.setFont(i, fontRegistry.get("tableBold"));
							}

							item.setText(i++, info.get(key));
						}

						// Dimesionamento delle colonne (una tantum)...
						if (!tablePacked) {
							for (int j = 0; j < i; j++) {
								table.getColumn(j).pack();
							}
							table.getColumn(0).setWidth((int) (table.getColumn(0).getWidth() * 1.3));
							tablePacked = true;
						}

						// Limitatore righe in tabella...
						final int maxItems = configuration.getInt("gui.table.items.max", Defaults.MAX_ITEMS);
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

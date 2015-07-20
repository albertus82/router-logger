package it.albertus.router.gui;

import it.albertus.router.RouterLoggerConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GuiTable {

	private interface Defaults {
		int MAX_ITEMS = 10000;
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

	private boolean tableInitialized = false;
	private boolean tablePacked = false;
	private Table table = null;

	public void addRow(final Map<String, String> info, final int iteration) {
		final String timestamp = DATE_FORMAT_TABLE_GUI.format(new Date());
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (info != null && !info.isEmpty() && table != null && !table.isDisposed()) {

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
						item.setText(i++, info.get(key));
					}

					// Dimesionamento delle colonne (una tantum)...
					if (!tablePacked) {
						while (i > 0) {
							table.getColumn(--i).pack();
						}
						table.getColumn(0).setWidth((int) (table.getColumn(0).getWidth() * 1.3));
						tablePacked = true;
					}

					// Limitatore righe in tabella...
					final int maxItems = RouterLoggerConfiguration.getInstance().getInt("gui.table.items.max", Defaults.MAX_ITEMS);
					if (table.getItemCount() > maxItems) {
						table.remove(maxItems);
					}

					// Forza ridisegno tabella ad ogni aggiornamento...
					table.redraw();
				}
			}

		});
	}

}

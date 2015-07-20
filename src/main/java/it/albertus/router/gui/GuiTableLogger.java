package it.albertus.router.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GuiTableLogger implements Runnable {

	private static final DateFormat DATE_FORMAT_TABLE_GUI = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	private static boolean tableInitialized = false;
	private static boolean tablePacked = false;

	private final Table table;
	private final Map<String, String> info;
	private final int iteration;

	public GuiTableLogger(Table table, Map<String, String> info, int iteration) {
		this.table = table;
		this.info = info;
		this.iteration = iteration;
	}

	@Override
	public void run() {
		if (info != null && !info.isEmpty() && table != null && !table.isDisposed()) {
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

			int i = 0;
			TableItem item = new TableItem(table, SWT.NONE, 0);
			item.setText(i++, String.valueOf(iteration));
			item.setText(i++, DATE_FORMAT_TABLE_GUI.format(new Date()));
			for (String key : info.keySet()) {
				item.setText(i++, info.get(key));
			}

			if (!tablePacked) {
				while (i > 0) {
					table.getColumn(--i).pack();
				}
				table.getColumn(0).setWidth((int) (table.getColumn(0).getWidth() * 1.3));
				tablePacked = true;
			}

			// table.setSize(table.computeSize(SWT.DEFAULT, 200));

			table.redraw();
		}
	}

}

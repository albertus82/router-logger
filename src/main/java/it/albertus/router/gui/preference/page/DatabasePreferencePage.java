package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.StaticNamesAndValues;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class DatabasePreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.database.header"));
		return header;
	}

	protected enum DatabaseDriverComboData {
		DB2("com.ibm.db2.jcc.DB2Driver"),
		MYSQL("com.mysql.jdbc.Driver"),
		ORACLE("oracle.jdbc.OracleDriver"),
		POSTGRESQL("org.postgresql.Driver"),
		SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		private final String driverClassName;

		private DatabaseDriverComboData(final String driverClassName) {
			this.driverClassName = driverClassName;
		}

		public String getDriverClassName() {
			return driverClassName;
		}
	}

	public static StaticNamesAndValues getDatabaseComboOptions() {
		final DatabaseDriverComboData[] values = DatabaseDriverComboData.values();
		final StaticNamesAndValues options = new StaticNamesAndValues(values.length);
		for (final DatabaseDriverComboData comboData : values) {
			final String value = comboData.driverClassName;
			options.put(value, value);
		}
		return options;
	}

}

package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

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

	protected enum DatabaseDriverClass {
		DB2("com.ibm.db2.jcc.DB2Driver"),
		MYSQL("com.mysql.jdbc.Driver"),
		ORACLE("oracle.jdbc.OracleDriver"),
		POSTGRESQL("org.postgresql.Driver"),
		SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		private final String driverClassName;

		private DatabaseDriverClass(final String driverClassName) {
			this.driverClassName = driverClassName;
		}

		public String getDriverClassName() {
			return driverClassName;
		}
	}

	public static LocalizedComboEntryNamesAndValues getDatabaseComboOptions() {
		final int length = DatabaseDriverClass.values().length;
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues();
		for (int i = 0; i < length; i++) {
			final int index = i;
			final String value = DatabaseDriverClass.values()[index].getDriverClassName();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return DatabaseDriverClass.values()[index].getDriverClassName();
				}
			};
			options.add(name, value);
		}
		return options;
	}

}

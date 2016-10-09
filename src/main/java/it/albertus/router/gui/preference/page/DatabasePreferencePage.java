package it.albertus.router.gui.preference.page;

import org.eclipse.swt.widgets.Control;

import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.router.resources.Messages;

public class DatabasePreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		return createNoteComposite(null, getFieldEditorParent(), Messages.get("lbl.preferences.database.header"), "");
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

	public static StaticLabelsAndValues getDatabaseComboOptions() {
		final DatabaseDriverComboData[] values = DatabaseDriverComboData.values();
		final StaticLabelsAndValues options = new StaticLabelsAndValues(values.length);
		for (final DatabaseDriverComboData comboData : values) {
			final String value = comboData.driverClassName;
			options.put(value, value);
		}
		return options;
	}

}

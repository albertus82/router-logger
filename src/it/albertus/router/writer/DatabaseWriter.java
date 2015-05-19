package it.albertus.router.writer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter extends Writer {

	private interface Defaults {
		String TABLE_NAME = "ROUTER_LOG";
		String COLUMN_TYPE = "VARCHAR";
		String COLUMN_LENGTH = "250";
		int CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS = 2000;
	}

	private static final String CONFIGURATION_KEY_DATABASE_PASSWORD = "database.password";
	private static final String CONFIGURATION_KEY_DATABASE_USERNAME = "database.username";
	private static final String CONFIGURATION_KEY_DATABASE_URL = "database.url";
	private static final String CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME = "database.driver.class.name";

	private static final String TIMESTAMP_COLUMN_NAME = "log_timestamp";

	private Connection connection = null;
	private boolean showMessage = true;
	private final int connectionValidationTimeoutInMillis;

	public DatabaseWriter() {
		if (!configuration.contains(CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME) || !configuration.contains(CONFIGURATION_KEY_DATABASE_URL) || !configuration.contains(CONFIGURATION_KEY_DATABASE_USERNAME) || !configuration.contains(CONFIGURATION_KEY_DATABASE_PASSWORD)) {
			throw new RuntimeException("Database configuration error. Review your " + configuration.getFileName() + " file.");
		}
		try {
			Class.forName(configuration.getString(CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME));
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("Missing database driver library (JAR) or misspelled class name \"" + configuration.getString(CONFIGURATION_KEY_DATABASE_DRIVER_CLASS_NAME) + "\" in your " + configuration.getFileName() + " file.", e);
		}
		connectionValidationTimeoutInMillis = configuration.getInt("database.connection.validation.timeout.ms", Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS);
	}

	@Override
	public void saveInfo(final Map<String, String> info) {
		// Connessione al database...
		try {
			if (connection == null || !connection.isValid(connectionValidationTimeoutInMillis)) {
				connection = DriverManager.getConnection(configuration.getString(CONFIGURATION_KEY_DATABASE_URL), configuration.getString(CONFIGURATION_KEY_DATABASE_USERNAME), configuration.getString(CONFIGURATION_KEY_DATABASE_PASSWORD));
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException se) {
			throw new RuntimeException(se);
		}

		// Verifica esistenza tabella ed eventuale creazione...
		final String tableName = configuration.getString("database.table.name", Defaults.TABLE_NAME).replaceAll("[^A-Za-z0-9_]+", "");
		if (!tableExists(tableName)) {
			out.println("Creating database table: " + tableName + "...");
			createTable(tableName, info);
		}

		// Inserimento dati...
		if (showMessage) {
			out.println("Logging into database table: " + tableName + "...");
			showMessage = false;
		}

		Map<Integer, String> columns = new HashMap<Integer, String>();
		StringBuilder dml = new StringBuilder("INSERT INTO ").append(tableName).append(" (").append(TIMESTAMP_COLUMN_NAME);
		int index = 2;
		for (String key : info.keySet()) {
			columns.put(index++, key);
			dml.append(", ").append(cleanColumnName(key));
		}
		dml.append(") VALUES (?");
		for (int i = 0; i < info.size(); i++) {
			dml.append(", ?");
		}
		dml.append(')');

		PreparedStatement insert = null;
		try {
			insert = connection.prepareStatement(dml.toString());
			insert.setTimestamp(1, new Timestamp(new Date().getTime()));
			for (int parameterIndex : columns.keySet()) {
				insert.setString(parameterIndex, info.get(columns.get(parameterIndex)));
			}
			insert.executeUpdate();
		}
		catch (SQLException se) {
			se.printStackTrace();
		}
		finally {
			try {
				insert.close();
			}
			catch (Exception e) {}
		}
	}

	private boolean tableExists(final String tableName) {
		PreparedStatement statement = null;
		try {
			// Verifica esistenza tabella...
			statement = connection.prepareStatement("SELECT 1 FROM " + tableName);
			statement.setFetchSize(1);
			statement.executeQuery();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
		finally {
			try {
				statement.close();
			}
			catch (Exception e) {}
		}
	}

	private void createTable(final String tableName, final Map<String, String> info) {
		// Creazione tabella...
		StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(tableName).append(" (").append(TIMESTAMP_COLUMN_NAME).append(" TIMESTAMP");
		for (String key : info.keySet()) {
			ddl.append(", ").append(cleanColumnName(key)).append(' ').append(configuration.getString("database.column.type", Defaults.COLUMN_TYPE)).append('(').append(configuration.getString("database.column.length", Defaults.COLUMN_LENGTH)).append(')');
		}
		ddl.append(", CONSTRAINT pk_routerlogger PRIMARY KEY (").append(TIMESTAMP_COLUMN_NAME).append("))");

		PreparedStatement createTable = null;
		try {
			createTable = connection.prepareStatement(ddl.toString());
			createTable.executeUpdate();
		}
		catch (SQLException se) {
			throw new RuntimeException(se);
		}
		finally {
			try {
				createTable.close();
			}
			catch (Exception e) {}
		}
	}

	private String cleanColumnName(String key) {
		return key.replaceAll("[^A-Za-z0-9_]+", "");
	}

	@Override
	public void release() {
		closeDatabaseConnection();
	}

	private void closeDatabaseConnection() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					out.println("Closing database connection.");
					connection.close();
					connection = null;
				}
			}
			catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}

}

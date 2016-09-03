package it.albertus.router.writer;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Messages;
import it.albertus.util.ConfigurationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DatabaseWriter extends Writer {

	public static final String DESTINATION_KEY = "lbl.writer.destination.database";

	public interface Defaults {
		String TABLE_NAME = "router_log";
		String COLUMN_NAME_PREFIX = "rl_";
		String TIMESTAMP_COLUMN_TYPE = "TIMESTAMP";
		String RESPONSE_TIME_COLUMN_TYPE = "INTEGER";
		String INFO_COLUMN_TYPE = "VARCHAR(250)";
		int COLUMN_NAME_MAX_LENGTH = 30;
		int CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS = 2000;
	}

	protected static final String CFG_KEY_DB_PASSWORD = "database.password";
	protected static final String CFG_KEY_DB_USERNAME = "database.username";
	protected static final String CFG_KEY_DB_URL = "database.url";
	protected static final String CFG_KEY_DB_DRIVER_CLASS_NAME = "database.driver.class.name";

	protected Connection connection = null;
	protected boolean showMessage = true;

	public DatabaseWriter() {
		if (!configuration.contains(CFG_KEY_DB_DRIVER_CLASS_NAME)) {
			throw new ConfigurationException(Messages.get("err.database.cfg.error") + ' ' + Messages.get("err.review.cfg", configuration.getFileName()), CFG_KEY_DB_DRIVER_CLASS_NAME);
		}
		if (!configuration.contains(CFG_KEY_DB_URL)) {
			throw new ConfigurationException(Messages.get("err.database.cfg.error") + ' ' + Messages.get("err.review.cfg", configuration.getFileName()), CFG_KEY_DB_URL);
		}
		if (!configuration.contains(CFG_KEY_DB_USERNAME)) {
			throw new ConfigurationException(Messages.get("err.database.cfg.error") + ' ' + Messages.get("err.review.cfg", configuration.getFileName()), CFG_KEY_DB_USERNAME);
		}
		if (!configuration.contains(CFG_KEY_DB_PASSWORD)) {
			throw new ConfigurationException(Messages.get("err.database.cfg.error") + ' ' + Messages.get("err.review.cfg", configuration.getFileName()), CFG_KEY_DB_PASSWORD);
		}
		try {
			Class.forName(configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME));
		}
		catch (ClassNotFoundException e) {
			throw new ConfigurationException(Messages.get("err.database.jar", configuration.getString(CFG_KEY_DB_DRIVER_CLASS_NAME), configuration.getFileName()), e, CFG_KEY_DB_DRIVER_CLASS_NAME);
		}
	}

	@Override
	public synchronized void saveInfo(final RouterData data) {
		final Map<String, String> info = data.getData();

		try {
			// Connessione al database...
			try {
				if (connection == null || !connection.isValid(configuration.getInt("database.connection.validation.timeout.ms", Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS))) {
					connection = DriverManager.getConnection(configuration.getString(CFG_KEY_DB_URL), configuration.getString(CFG_KEY_DB_USERNAME), configuration.getString(CFG_KEY_DB_PASSWORD));
					connection.setAutoCommit(true);
				}
			}
			catch (SQLException se) {
				throw new RuntimeException(se);
			}

			// Verifica esistenza tabella ed eventuale creazione...
			final String tableName = getTableName();
			if (!tableExists(tableName)) {
				out.println(Messages.get("msg.creating.database.table", tableName), true);
				createTable(tableName, info);
			}

			// Inserimento dati...
			if (showMessage) {
				out.println(Messages.get("msg.logging.into.database", tableName), true);
				showMessage = false;
			}

			Map<Integer, String> columns = new HashMap<Integer, String>();

			StringBuilder dml = new StringBuilder("INSERT INTO ").append(tableName).append(" (").append(getTimestampColumnName());
			dml.append(", ").append(getResponseTimeColumnName());
			int index = 3;
			for (String key : info.keySet()) {
				columns.put(index++, key);
				dml.append(", ").append(getColumnName(key));
			}
			dml.append(") VALUES (?, ?");
			for (int i = 0; i < info.size(); i++) {
				dml.append(", ?");
			}
			dml.append(')');

			PreparedStatement insert = null;
			try {
				insert = connection.prepareStatement(dml.toString());
				insert.setTimestamp(1, new Timestamp(data.getTimestamp().getTime()));
				insert.setInt(2, data.getResponseTime());
				for (int parameterIndex : columns.keySet()) {
					insert.setString(parameterIndex, info.get(columns.get(parameterIndex)));
				}
				insert.executeUpdate();
			}
			catch (SQLException se) {
				logger.log(se);
			}
			finally {
				try {
					insert.close();
				}
				catch (Exception e) {}
			}
		}
		catch (final ConfigurationException ce) {
			logger.log(ce);
			closeDatabaseConnection();
		}
	}

	protected String getResponseTimeColumnName() {
		return getColumnName("response_time_ms");
	}

	protected String getTimestampColumnName() {
		return getColumnName("timestamp");
	}

	protected boolean tableExists(final String tableName) {
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

	protected void createTable(final String tableName, final Map<String, String> info) {
		final String timestampColumnType = configuration.getString("database.timestamp.column.type", Defaults.TIMESTAMP_COLUMN_TYPE);
		final String responseTimeColumnType = configuration.getString("database.response.column.type", Defaults.RESPONSE_TIME_COLUMN_TYPE);
		final String infoColumnType = configuration.getString("database.info.column.type", Defaults.INFO_COLUMN_TYPE);

		// Creazione tabella...
		StringBuilder ddl = new StringBuilder("CREATE TABLE ").append(tableName).append(" (").append(getTimestampColumnName()).append(' ').append(timestampColumnType);
		ddl.append(", ").append(getResponseTimeColumnName()).append(' ').append(responseTimeColumnType); // Response time
		for (String key : info.keySet()) {
			ddl.append(", ").append(getColumnName(key)).append(' ').append(infoColumnType);
		}
		ddl.append(", CONSTRAINT pk_routerlogger PRIMARY KEY (").append(getTimestampColumnName()).append("))");

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

	protected String getTableName() {
		return configuration.getString("database.table.name", Defaults.TABLE_NAME).replaceAll("[^A-Za-z0-9_]+", "");
	}

	protected String getColumnName(String name) {
		name = configuration.getString("database.column.name.prefix", Defaults.COLUMN_NAME_PREFIX) + name;
		name = name.replaceAll("[^A-Za-z0-9_]+", "");
		final int maxLength = configuration.getInt("database.column.name.max.length", Defaults.COLUMN_NAME_MAX_LENGTH);
		if (name.length() > maxLength) {
			name = name.substring(0, maxLength);
		}
		return name;
	}

	@Override
	public void release() {
		closeDatabaseConnection();
	}

	protected void closeDatabaseConnection() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					out.println(Messages.get("msg.closing.database.connection"), true);
					connection.close();
					connection = null;
				}
			}
			catch (SQLException se) {
				logger.log(se);
			}
		}
	}

}

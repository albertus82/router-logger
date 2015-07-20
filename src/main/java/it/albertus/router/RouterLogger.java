package it.albertus.router;

import it.albertus.router.gui.GuiConsole;
import it.albertus.router.gui.GuiTableLogger;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.util.Logger;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.StringUtils;
import it.albertus.util.ThreadUtils;
import it.albertus.util.Version;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class RouterLogger extends ApplicationWindow {

	private interface Defaults {
		int ITERATIONS = -1;
		long INTERVAL_FAST_IN_MILLIS = 1000L;
		long INTERVAL_NORMAL_IN_MILLIS = 5000L;
		long HYSTERESIS_IN_MILLIS = 10000L;
		int RETRIES = 3;
		long RETRY_INTERVAL_IN_MILLIS = 30000L;
		boolean WRITER_THREAD = false;
//		boolean CONSOLE_ANIMATION = true;
		boolean CONSOLE_SHOW_CONFIGURATION = false;
//		String CONSOLE_SHOW_KEYS_SEPARATOR = ",";
		Class<? extends Writer> WRITER_CLASS = CsvWriter.class;
		Class<? extends Reader> READER_CLASS = TpLink8970Reader.class;
	}

//	private static final char[] ANIMATION = { '-', '\\', '|', '/' };
	

	private static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private static final GuiConsole out = GuiConsole.getInstance();
	private static final Logger logger = Logger.getInstance();

	private final Reader reader;
	private final Writer writer;

	private Reader initReader() {
		final String configurationKey = "reader.class.name";
		String readerClassName = configuration.getString(configurationKey, Defaults.READER_CLASS.getName()).trim();

		try {
			Class.forName(readerClassName); // Default package.
		}
		catch (ClassNotFoundException e) {
			readerClassName = Reader.class.getPackage().getName() + '.' + readerClassName;
		}

		final Reader reader;
		try {
			reader = (Reader) Class.forName(readerClassName).newInstance();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid \"" + configurationKey + "\" property. Review your " + configuration.getFileName() + " file.", e);
		}
		return reader;
	}

	private Writer initWriter() {
		final String configurationKey = "writer.class.name";
		String writerClassName = configuration.getString(configurationKey, Defaults.WRITER_CLASS.getName()).trim();

		try {
			Class.forName(writerClassName); // Default package.
		}
		catch (ClassNotFoundException e) {
			writerClassName = Writer.class.getPackage().getName() + '.' + writerClassName;
		}

		final Writer writer;
		try {
			writer = (Writer) Class.forName(writerClassName).newInstance();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid \"" + configurationKey + "\" property. Review your " + configuration.getFileName() + " file.", e);
		}
		return writer;
	}

	private void run() {
		welcome();

		boolean exit = false;

		final int retries = configuration.getInt("logger.retry.count", Defaults.RETRIES);

		// Gestione chiusura console (CTRL+C)...
		final Thread hook = new Thread() {
			public void run() {
				reader.disconnect();
				release();
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);

		for (int index = 0; index <= retries && !exit; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				final long retryIntervalInMillis = configuration.getLong("logger.retry.interval.ms", Defaults.RETRY_INTERVAL_IN_MILLIS);
				out.println("Waiting for reconnection " + index + '/' + retries + " (" + retryIntervalInMillis + " ms)...", true);
				try {
					Thread.sleep(retryIntervalInMillis);
				}
				catch (InterruptedException ie) {
					throw new RuntimeException(ie);
				}
			}

			// Avvio della procedura...
			final boolean connected = reader.connect();

			// Log in...
			if (connected) {
				boolean loggedIn = false;
				try {
					loggedIn = reader.login();
				}
				catch (Exception e) {
					logger.log(e);
				}

				// Loop...
				if (loggedIn) {
					index = 0;
					try {
						loop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (Exception e) {
						logger.log(e);
					}
					finally {
						// In ogni caso, si esegue la disconnessione dal server...
						try {
							reader.logout();
						}
						catch (Exception e) {
							logger.log(e);
						}
						reader.disconnect();
					}
				}
				else {
					// In caso di autenticazione fallita, si esce subito per evitare il blocco dell'account.
					exit = true;
					reader.disconnect();
				}
			}
		}

		Runtime.getRuntime().removeShutdownHook(hook);

		release();
		out.println("Bye!", true);
	}

	private void welcome() {
		// Preparazione numero di versione (se presente)...
		final Version version = Version.getInstance();
		final StringBuilder versionInfo = new StringBuilder();
		final String versionNumber = version.getNumber();
		if (versionNumber != null && !"".equals(versionNumber.trim())) {
			versionInfo.append('v').append(versionNumber.trim()).append(' ');
		}
		String versionDate = version.getDate();
		if (versionDate != null && !"".equals(versionDate.trim())) {
			versionInfo.append('(').append(versionDate.trim()).append(") ");
		}

		out.println("********** ADSL Modem Router Logger " + versionInfo.toString() + "**********");
		out.println();
		boolean lineBreak = false;
		if (StringUtils.isNotBlank(reader.getDeviceModel())) {
			out.println("Device model: " + reader.getDeviceModel().trim() + '.');
			lineBreak = true;
		}
		if (!configuration.getThresholds().isEmpty()) {
			out.println("Thresholds: " + configuration.getThresholds().toString());
			lineBreak = true;
		}
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			out.println("Settings: " + configuration.toString());
			lineBreak = true;
		}
		if (lineBreak) {
			out.println();
		}
	}

	private final void loop() throws IOException, InterruptedException {
		// Determinazione numero di iterazioni...
		int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS);
		if (iterations <= 0) {
			iterations = Integer.MAX_VALUE;
		}

		long hysteresis = 0;

		// Iterazione...
		for (int iteration = 1/*, lastLogLength = 0*/; iteration <= iterations; iteration++) {
			// Chiamata alle implementazioni specifiche...
			final Map<String, String> info = reader.readInfo();
			saveInfo(info);
			// Fine implementazioni specifiche.

			// Scrittura indice dell'iterazione in console...
			/*
			final StringBuilder clean = new StringBuilder();
			while (lastLogLength-- > 0) {
				clean.append('\b').append(' ').append('\b');
			}
			final StringBuilder log = new StringBuilder();
			final boolean animate = configuration.getBoolean("console.animation", Defaults.CONSOLE_ANIMATION);
			if (animate) {
				log.append(ANIMATION[iteration & 3]).append(' ');
			}
			log.append(iteration);
			if (iterations != Integer.MAX_VALUE) {
				log.append('/').append(iterations);
			}
			log.append(' ');
			if (animate) {
				log.append(ANIMATION[iteration & 3]).append(' ');
			}
			// Fine scrittura indice.

			// Scrittura informazioni aggiuntive richieste...
			if (info != null && !info.isEmpty()) {
				final StringBuilder infoToShow = new StringBuilder();
				for (String keyToShow : configuration.getString("console.show.keys", "").split(configuration.getString("console.show.keys.separator", Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).trim())) {
					if (keyToShow != null && !"".equals(keyToShow.trim())) {
						keyToShow = keyToShow.trim();
						for (final String key : info.keySet()) {
							if (key != null && key.trim().equals(keyToShow)) {
								if (infoToShow.length() == 0) {
									infoToShow.append('[');
								}
								else {
									infoToShow.append(", ");
								}
								infoToShow.append(keyToShow + ": " + info.get(key));
							}
						}
					}
				}
				if (infoToShow.length() != 0) {
					infoToShow.append("] ");
				}
				log.append(infoToShow);
			}
			// Fine scrittura informazioni aggiuntive.

			lastLogLength = log.length();
			out.print(clean.toString() + log.toString());
			*/
			Display.getDefault().syncExec(new GuiTableLogger(table, info, iteration));


			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iteration != iterations) {
				final long waitTimeInMillis;
				final boolean thresholdReached = configuration.getThresholds().isReached(info);
				if (thresholdReached || System.currentTimeMillis() - hysteresis < configuration.getLong("logger.hysteresis.ms", Defaults.HYSTERESIS_IN_MILLIS)) {
					waitTimeInMillis = configuration.getLong("logger.interval.fast.ms", Defaults.INTERVAL_FAST_IN_MILLIS);
					if (thresholdReached) {
						hysteresis = System.currentTimeMillis();
					}
				}
				else {
					waitTimeInMillis = configuration.getLong("logger.interval.normal.ms", Defaults.INTERVAL_NORMAL_IN_MILLIS);
				}
				Thread.sleep(waitTimeInMillis);
			}
		}
	}

	private void saveInfo(final Map<String, String> info) {
		if (configuration.getBoolean("logger.writer.thread", Defaults.WRITER_THREAD)) {
			new Thread() {
				@Override
				public void run() {
					writer.saveInfo(info);
				}
			}.start();
		}
		else {
			writer.saveInfo(info);
		}
	}

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	private void release() {
		try {
			reader.release();
		}
		catch (Exception e) {}
		try {
			writer.release();
		}
		catch (Exception e) {}
	}
	
	private Table table;
//	private StyledText styledText;


	public RouterLogger() {
		super(null);
//		createActions();
//		addToolBar(SWT.FLAT | SWT.WRAP);
//		addMenuBar();
//		addStatusLine();
		// Inizializzazione del Reader...
		reader = initReader();

		// Inizializzazione del Writer...
		writer = initWriter();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, true);
		container.setLayout(layout);
//
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
//			label.setText("1");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("2");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("3");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("4");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("5");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("6");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("7");
//		}
//		{
//			Label label = new Label(container, SWT.NONE);
//			label.setText("8");
//		}

		// Tabella
		table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Console
		StyledText styledText = new StyledText(container, SWT.BORDER);
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor("console")) {
			Font terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
			fontRegistry.put("console", new FontData[] { new FontData(terminalFont.getFontData()[0].getName(), 10, SWT.NORMAL) });
		}
		styledText.setFont(fontRegistry.get("console"));
		out.init(styledText); // Aggancio la console alla casella di testo della GUI.
		
		return container;
	}

//	private void createActions() {
//		// Create the actions
//	}
//
//	@Override
//	protected MenuManager createMenuManager() {
//		MenuManager menuManager = new MenuManager("menu");
//		return menuManager;
//	}
//
//	@Override
//	protected ToolBarManager createToolBarManager(int style) {
//		ToolBarManager toolBarManager = new ToolBarManager(style);
//		return toolBarManager;
//	}
//
//	@Override
//	protected StatusLineManager createStatusLineManager() {
//		StatusLineManager statusLineManager = new StatusLineManager();
//		return statusLineManager;
//	}

	public static void main(String args[]) {
		try {
			final RouterLogger window = new RouterLogger();

			window.setBlockOnOpen(true);

			Thread updateThread = new Thread() {
				@Override
		        public void run() {
		        	// Per evitare che l'aggiornamento della tabella avvenga prima che essa sia stata creata.
					ThreadUtils.sleep(1000);
					
		        	window.run();
		        }
		    };

		    updateThread.setDaemon(true);
		    updateThread.start();
			window.open();

			Display.getCurrent().dispose();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Router Logger");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(750, 550);
	}
	
//	public boolean close() {
//
//	    final Shell grandShell = this.getShell();
//	    grandShell.setVisible(false);
//
//	    Display display = Display.getCurrent();
//
//	    Tray tray = display.getSystemTray();
//	    if(tray != null) {
//	        TrayItem item = new TrayItem(tray, SWT.NONE);
////	        item.setImage(ArecaImages.ICO_SMALL);
//	        final Menu menu = new Menu(getShell(), SWT.POP_UP);
//	        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
//	        menuItem.setText("Areca");
//	        menuItem.addListener (SWT.Selection, new Listener () {
//	            public void handleEvent (Event event) {
//	                grandShell.setVisible(true);
//	            }
//	        });
//	        item.addListener (SWT.MenuDetect, new Listener () {
//	            public void handleEvent (Event event) {
//	                menu.setVisible (true);
//	            }
//	        });
//
//	    }
//
//	    return true;
//	}
}

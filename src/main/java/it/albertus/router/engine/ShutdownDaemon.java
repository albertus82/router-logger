package it.albertus.router.engine;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ShutdownDaemon extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(ShutdownDaemon.class);

	private final RouterLoggerEngine engine;
	private final int timeoutInSecs;
	private final Date shutdownTime;
	private final Date creationDate = new Date();

	public ShutdownDaemon(final RouterLoggerEngine engine, final int timeoutInSecs) {
		setDaemon(true);
		this.engine = engine;
		this.timeoutInSecs = timeoutInSecs;
		final Calendar shutdownCalendar = Calendar.getInstance();
		shutdownCalendar.add(Calendar.SECOND, timeoutInSecs);
		this.shutdownTime = shutdownCalendar.getTime();
	}

	public int getTimeoutInSecs() {
		return timeoutInSecs;
	}

	public Date getShutdownTime() {
		return shutdownTime;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void run() {
		try {
			logger.log(Level.WARNING, Messages.get("msg.close.schedule"), new String[] { DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Messages.getLanguage().getLocale()).format(shutdownTime), Integer.toString(timeoutInSecs) });
			TimeUnit.SECONDS.sleep(timeoutInSecs);
			logger.log(Level.INFO, Messages.get("msg.close.schedule.execute"), DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Messages.getLanguage().getLocale()).format(creationDate));
			engine.close();
		}
		catch (final InterruptedException e) {
			logger.log(Level.INFO, Messages.get("msg.close.schedule.canceled"), new String[] { DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Messages.getLanguage().getLocale()).format(shutdownTime), Integer.toString(timeoutInSecs) });
			Thread.currentThread().interrupt();
		}
	}

}

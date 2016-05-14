package it.albertus.router.email;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.NewLine;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThresholdsEmailSender {

	public interface Defaults {
		int THRESHOLDS_EMAIL_SEND_INTERVAL_SECS = 600;
	}

	private static class Singleton {
		private static final ThresholdsEmailSender instance = new ThresholdsEmailSender();
	}

	public static ThresholdsEmailSender getInstance() {
		return Singleton.instance;
	}

	private ThresholdsEmailSender() {}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final Queue<ThresholdEmailItem> queue = new ConcurrentLinkedQueue<ThresholdEmailItem>();
	protected volatile Thread daemon;

	private final Object lock = new Object();

	public void send(final Map<Threshold, String> thresholdsReached, final RouterData routerData) {
		if (thresholdsReached != null && !thresholdsReached.isEmpty() && routerData != null) {
			synchronized (lock) {
				queue.add(new ThresholdEmailItem(thresholdsReached, routerData));
				if (this.daemon == null) {
					daemon = new Thread(new ThresholdsEmailRunnable(), "thresholdsEmailDaemon");
					daemon.setDaemon(true);
					daemon.start();
				}
			}
		}
	}

	protected class ThresholdsEmailRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				// Exit if there is nothing to do...
				synchronized (lock) {
					if (queue == null || queue.isEmpty()) {
						daemon = null;
						break;
					}
				}

				try {
					final LinkedList<ThresholdEmailItem> sent = new LinkedList<ThresholdEmailItem>();

					// Build email message...
					final StringBuilder message = new StringBuilder();
					for (final ThresholdEmailItem item : queue) {
						message.append(item.toString()).append(NewLine.CRLF.toString()).append(NewLine.CRLF.toString()).append(NewLine.CRLF.toString());
						sent.add(item);
					}

					// Build email subject...
					final String subject;
					if (sent.size() == 1) {
						subject = Resources.get("msg.threshold.email.subject.single", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Resources.getLanguage().getLocale()).format(sent.getFirst().getDate()));
					}
					else {
						subject = Resources.get("msg.threshold.email.subject.multiple", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Resources.getLanguage().getLocale()).format(sent.getFirst().getDate()), DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Resources.getLanguage().getLocale()).format(sent.getLast().getDate()));
					}

					// Send email...
					EmailSender.getInstance().reserve(subject, message.toString().trim());

					queue.removeAll(sent);
				}
				catch (final Exception exception) {
					Logger.getInstance().log(exception, Destination.CONSOLE);
				}

				try {
					Thread.sleep(1000 * configuration.getInt("thresholds.email.send.interval.secs", Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS));
				}
				catch (final InterruptedException ie) {
					break;
				}
			}
		}
	}

}

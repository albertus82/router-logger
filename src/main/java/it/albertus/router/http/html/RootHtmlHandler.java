package it.albertus.router.http.html;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.router.http.HttpServerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

@Path("/")
public class RootHtmlHandler extends AbstractHtmlHandler {

	private static final Logger logger = LoggerFactory.getLogger(RootHtmlHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	static final String CFG_KEY_ENABLED = "server.handler.root.enabled";

	private static final String RESOURCE_BASE_PATH = '/' + HttpServerConfiguration.class.getPackage().getName().toLowerCase().replace('.', '/') + '/';

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		if (requestedStaticResource(exchange)) {
			sendStaticResource(exchange, RESOURCE_BASE_PATH + getPathInfo(exchange), "no-transform, public, max-age=86400, s-maxage=259200");
		}
		else {
			// Response...
			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.home")));
			html.append("<div class=\"row\">").append(NewLine.CRLF);
			html.append("<div class=\"col-lg-6 col-lg-offset-3 col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2 col-xs-8 col-xs-offset-2\">").append(NewLine.CRLF);
			html.append("<img class=\"img-responsive img-hp\" src=\"img/61uIpBXY7nL._SL1280_.jpg\" alt=\"Router\" />").append(NewLine.CRLF);
			html.append("</div>").append(NewLine.CRLF);
			html.append("</div>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());
			sendResponse(exchange, html.toString());
		}
	}

	@Override
	protected void setContentTypeHeader(final HttpExchange exchange) {
		if (existsStaticResource(RESOURCE_BASE_PATH + getPathInfo(exchange)) && !exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			setContentTypeHeader(exchange, getContentType(exchange.getRequestURI().getPath())); // extension based
		}
		else {
			super.setContentTypeHeader(exchange); // text/html
		}
	}

	@Override
	protected void log(final HttpExchange exchange) {
		if (requestedStaticResource(exchange)) {
			Level level = Level.OFF;
			try {
				level = Level.parse(getHttpServerConfiguration().getRequestLoggingLevel());
			}
			catch (final RuntimeException e) {
				logger.log(Level.WARNING, e.toString(), e);
			}
			if (level.intValue() > Level.FINE.intValue()) {
				level = Level.FINE;
			}
			doLog(exchange, level);
		}
		else {
			super.log(exchange);
		}
	}

	@Override
	public boolean isEnabled(final HttpExchange exchange) {
		if (requestedStaticResource(exchange)) {
			return true; // always serve static resources
		}
		else {
			return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED); // configuration based
		}
	}

	private boolean requestedStaticResource(final HttpExchange exchange) {
		return !exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath());
	}

}

package it.albertus.routerlogger.http.html;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.net.MimeTypes;
import it.albertus.net.httpserver.annotation.Path;
import it.albertus.net.httpserver.config.IHttpServerConfig;
import it.albertus.routerlogger.engine.RouterLoggerEngine;
import it.albertus.routerlogger.http.HttpServer;
import it.albertus.routerlogger.reader.AsusDslN12EReader;
import it.albertus.routerlogger.reader.AsusDslN14UReader;
import it.albertus.routerlogger.reader.DLinkDsl2750Reader;
import it.albertus.routerlogger.reader.IReader;
import it.albertus.routerlogger.reader.TpLink8970Reader;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.NewLine;

@Path("/")
public class RootHtmlHandler extends AbstractHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;
		public static final boolean LOG_INCLUDE_STATIC = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	static final String CFG_KEY_ENABLED = "server.handler.root.enabled";

	private static final String RESOURCE_BASE_PATH = '/' + HttpServer.class.getPackage().getName().replace('.', '/') + "/static/";

	private final RouterLoggerEngine engine;

	public RootHtmlHandler(final IHttpServerConfig config, final RouterLoggerEngine engine) {
		super(config);
		this.engine = engine;
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		if (requestedStaticResource(exchange)) {
			sendStaticResource(exchange, RESOURCE_BASE_PATH + getPathInfo(exchange), false, "no-transform, public, max-age=2592000");
		}
		else {
			// Response...
			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.home")));
			html.append("<div class=\"row\">").append(NewLine.CRLF);
			html.append("<div class=\"col-lg-6 col-lg-offset-3 col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2 col-xs-8 col-xs-offset-2\">").append(NewLine.CRLF);
			html.append("<img class=\"img-responsive img-hp\" src=\"img/").append(getImageFileName(engine.getReader())).append("\" alt=\"Router\" />").append(NewLine.CRLF);
			html.append("</div>").append(NewLine.CRLF);
			html.append("</div>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());
			sendResponse(exchange, html.toString());
		}
	}

	String getImageFileName(final IReader reader) {
		if (reader instanceof AsusDslN12EReader) {
			return "asus_dsl_n12e.png";
		}
		if (reader instanceof AsusDslN14UReader) {
			return "asus_dsl_n14u.png";
		}
		if (reader instanceof DLinkDsl2750Reader) {
			return "dlink_dsl_2750b.png";
		}
		if (reader instanceof TpLink8970Reader) {
			return "tplink_td_w8970v1.png";
		}
		return "applications-internet.png";
	}

	@Override
	protected void setContentTypeHeader(final HttpExchange exchange) {
		if (requestedStaticResource(exchange) && getStaticResource(RESOURCE_BASE_PATH + getPathInfo(exchange)) != null) {
			setContentTypeHeader(exchange, MimeTypes.getContentType(exchange.getRequestURI().getPath())); // extension based
		}
		else {
			super.setContentTypeHeader(exchange); // text/html
		}
	}

	@Override
	protected void setContentLanguageHeader(final HttpExchange exchange) {
		if (!requestedStaticResource(exchange) || getStaticResource(RESOURCE_BASE_PATH + getPathInfo(exchange)) == null) {
			super.setContentLanguageHeader(exchange);
		}
	}

	@Override
	protected void logRequest(final HttpExchange exchange) {
		if (!requestedStaticResource(exchange) || configuration.getBoolean("server.log.include.static", Defaults.LOG_INCLUDE_STATIC)) {
			super.logRequest(exchange);
		}
	}

	@Override
	protected void logResponse(final HttpExchange exchange) {
		if (!requestedStaticResource(exchange) || configuration.getBoolean("server.log.include.static", Defaults.LOG_INCLUDE_STATIC)) {
			super.logResponse(exchange);
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

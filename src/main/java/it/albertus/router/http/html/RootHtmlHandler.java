package it.albertus.router.http.html;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.router.http.HttpServer;
import it.albertus.router.resources.Messages;

@Path("/")
public class RootHtmlHandler extends AbstractHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_ENABLED = "server.handler.root.enabled";

	private static final String RESOURCE_BASE_PATH = '/' + HttpServer.class.getPackage().getName().toLowerCase().replace('.', '/') + '/';

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		if (!exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			sendStaticResource(exchange, RESOURCE_BASE_PATH + getPathInfo(exchange));
		}
		else {
			// Response...
			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.home")));
			// TODO add some contents
			html.append(buildHtmlFooter());
			sendResponse(exchange, html.toString());
		}
	}

	@Override
	protected void addContentTypeHeader(final HttpExchange exchange) {
		if (existsStaticResource(RESOURCE_BASE_PATH + getPathInfo(exchange)) && !exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			exchange.getResponseHeaders().add("Content-Type", getContentType(exchange.getRequestURI().getPath())); // extension based
		}
		else {
			super.addContentTypeHeader(exchange); // text/html
		}
	}

	@Override
	public boolean isEnabled(final HttpExchange exchange) {
		if (!exchange.getRequestURI().getPath().equals(getPath()) && !exchange.getRequestURI().getRawPath().equals(getPath())) {
			return true; // always serve static resources
		}
		else {
			return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED); // configuration based
		}
	}

}

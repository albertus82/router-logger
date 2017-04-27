package it.albertus.router.http.html;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

@Path("/connect")
public class ConnectHandler extends AbstractHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	static final String CFG_KEY_ENABLED = "server.handler.connect.enabled";

	private final RouterLoggerEngine engine;

	public ConnectHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected void doPost(final HttpExchange exchange) throws IOException {
		final boolean accepted = engine.canConnect();
		if (accepted) {
			engine.connect();
		}

		// Headers...
		setCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.connect")));
		html.append("<div class=\"page-header\"><h2>").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.connect"))).append("</h2></div>").append(NewLine.CRLF);
		if (accepted) {
			html.append("<div class=\"alert alert-success alert-h4\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("msg.server.accepted"))).append("</div>").append(NewLine.CRLF);
		}
		else {
			html.append("<div class=\"alert alert-danger alert-h4\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("msg.server.not.acceptable"))).append("</div>").append(NewLine.CRLF);
		}
		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString(), accepted ? HttpURLConnection.HTTP_ACCEPTED : HttpURLConnection.HTTP_PRECON_FAILED);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}

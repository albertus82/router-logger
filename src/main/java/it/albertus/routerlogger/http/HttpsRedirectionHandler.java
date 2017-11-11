package it.albertus.routerlogger.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.net.httpserver.AbstractHttpHandler;
import it.albertus.net.httpserver.annotation.Path;
import it.albertus.util.logging.LoggerFactory;

@Path("/")
public class HttpsRedirectionHandler extends AbstractHttpHandler {

	private static final int DEFAULT_HTTPS_PORT = 443;

	private static final Logger logger = LoggerFactory.getLogger(HttpsRedirectionHandler.class);

	private final HttpServerConfig config;

	public HttpsRedirectionHandler(final HttpServerConfig config) {
		super(config);
		this.config = config;
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		String host = config.getSslRedirectionLocationHost();
		if (host == null || host.trim().isEmpty() || HttpServerConfig.DEFAULT_SSL_REDIRECTION_LOCATION_HOST.equals(host.trim())) {
			host = exchange.getRequestHeaders().getFirst("Host").split(":")[0]; // discard the port number
			logger.log(Level.CONFIG, "SSL host location not configured. Using the \"Host\" request header: \"{0}\".", host);
		}

		final int port = config.getSslRedirectionLocationPort();
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid port: " + port + ".");
		}

		final int responseCode = config.getSslRedirectionResponseCode();
		if (responseCode < HttpURLConnection.HTTP_MULT_CHOICE || responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new IllegalArgumentException("Invalid response code: " + responseCode + ".");
		}

		final URI uri = exchange.getRequestURI();
		final StringBuilder sb = new StringBuilder("https://").append(host);
		if (port != DEFAULT_HTTPS_PORT) {
			sb.append(':').append(port);
		}
		if (uri.getRawPath() != null) {
			sb.append(uri.getRawPath());
		}
		if (uri.getRawQuery() != null) {
			sb.append('?').append(uri.getRawQuery());
		}
		if (uri.getRawFragment() != null) {
			sb.append('#').append(uri.getRawFragment());
		}

		final String location = sb.toString();
		exchange.getResponseHeaders().set("Location", location);
		logger.log(Level.FINE, "Redirecting to: \"{0}\".", location);
		exchange.sendResponseHeaders(responseCode, -1);
		exchange.getResponseBody().close();
		exchange.close();
	}

}

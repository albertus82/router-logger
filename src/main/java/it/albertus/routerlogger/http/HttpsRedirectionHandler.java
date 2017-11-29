package it.albertus.routerlogger.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.net.httpserver.AbstractHttpHandler;
import it.albertus.net.httpserver.HttpException;
import it.albertus.net.httpserver.annotation.Path;
import it.albertus.util.logging.LoggerFactory;

@Path("/")
public class HttpsRedirectionHandler extends AbstractHttpHandler {

	private static final int DEFAULT_HTTPS_PORT = 443;

	private static final String HOST_HEADER_KEY = "Host";
	private static final String LOCATION_HEADER_KEY = "Location";

	private static final Logger logger = LoggerFactory.getLogger(HttpsRedirectionHandler.class);

	private final RedirectionServerConfig config;

	public HttpsRedirectionHandler(final RedirectionServerConfig config) {
		super(config);
		if (config == null) {
			throw new NullPointerException("config cannot be null");
		}
		this.config = config;
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		String host = config.getSslRedirectionLocationHost();
		if (host == null || host.trim().isEmpty() || RedirectionServerConfig.DEFAULT_SSL_REDIRECTION_LOCATION_HOST.equals(host.trim())) {
			host = getHostFromRequest(exchange);
		}

		final int port = config.getSslRedirectionLocationPort();
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Invalid TCP port: " + port + ".");
		}

		final int responseCode = config.getSslRedirectionResponseCode();
		if (responseCode < HttpURLConnection.HTTP_MULT_CHOICE || responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
			throw new IllegalArgumentException("Invalid HTTP redirection code: " + responseCode + ".");
		}

		final StringBuilder sb = new StringBuilder("https://").append(host);
		if (port != DEFAULT_HTTPS_PORT) {
			sb.append(':').append(port);
		}
		final URI uri = exchange.getRequestURI();
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
		exchange.getResponseHeaders().set(LOCATION_HEADER_KEY, location);
		logger.log(Level.FINE, "Redirecting to: \"{0}\".", location);
		exchange.sendResponseHeaders(responseCode, -1);
		exchange.getResponseBody().close();
	}

	private static String getHostFromRequest(final HttpExchange exchange) {
		if (!exchange.getRequestHeaders().containsKey(HOST_HEADER_KEY)) {
			throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "Missing " + HOST_HEADER_KEY + " header.");
		}
		else {
			final String hostHeaderValue = exchange.getRequestHeaders().getFirst(HOST_HEADER_KEY).trim();
			if (hostHeaderValue.isEmpty()) {
				throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "Empty " + HOST_HEADER_KEY + " header.");
			}
			else {
				final String host = hostHeaderValue.split(":")[0].trim(); // discard the port number, if present
				logger.log(Level.CONFIG, "SSL host location not configured. Using the \"Host\" request header: \"{0}\".", host);
				return host;
			}
		}
	}

}

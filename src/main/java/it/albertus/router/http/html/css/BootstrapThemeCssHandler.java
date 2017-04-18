package it.albertus.router.http.html.css;

import com.sun.net.httpserver.Headers;

import it.albertus.httpserver.annotation.Path;
import it.albertus.router.http.StaticResourceHandler;

@Path("/css/" + BootstrapThemeCssHandler.RESOURCE_NAME)
public class BootstrapThemeCssHandler extends StaticResourceHandler {

	protected static final String RESOURCE_NAME = "bootstrap-theme.min.css";

	public BootstrapThemeCssHandler() {
		super(RESOURCE_NAME, createHeaders());
	}

	private static Headers createHeaders() {
		final Headers headers = new Headers();
		headers.add("Content-Type", "text/css");
		headers.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=259200");
		return headers;
	}

}

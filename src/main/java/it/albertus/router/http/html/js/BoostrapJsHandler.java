package it.albertus.router.http.html.js;

import com.sun.net.httpserver.Headers;

import it.albertus.httpserver.annotation.Path;
import it.albertus.router.http.StaticResourceHandler;

@Path("/js/" + BoostrapJsHandler.RESOURCE_NAME)
public class BoostrapJsHandler extends StaticResourceHandler {

	protected static final String RESOURCE_NAME = "bootstrap.min.js";

	public BoostrapJsHandler() {
		super(RESOURCE_NAME, createHeaders());
	}

	private static Headers createHeaders() {
		final Headers headers = new Headers();
		headers.add("Content-Type", "text/javascript; charset=UTF-8");
		headers.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=259200");
		return headers;
	}

}

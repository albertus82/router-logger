package it.albertus.router.http.html;

import com.sun.net.httpserver.Headers;

import it.albertus.httpserver.annotation.Path;
import it.albertus.router.http.StaticResourceHandler;

@Path('/' + FaviconHandler.RESOURCE_NAME)
public class FaviconHandler extends StaticResourceHandler {

	protected static final String RESOURCE_NAME = "favicon.ico";

	public FaviconHandler() {
		super(RESOURCE_NAME, createHeaders());
	}

	private static Headers createHeaders() {
		final Headers faviconHeaders = new Headers();
		faviconHeaders.add("Content-Type", "image/x-icon");
		faviconHeaders.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=259200");
		return faviconHeaders;
	}

}

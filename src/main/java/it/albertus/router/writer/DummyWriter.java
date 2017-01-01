package it.albertus.router.writer;

import it.albertus.router.engine.RouterData;

public class DummyWriter extends Writer {

	public static final String DESTINATION_KEY = "lbl.writer.destination.dummy";

	@Override
	public void saveInfo(RouterData info) {
		// This writer does not save anything.
	}

	@Override
	public void release() {
		// Nothing to release.
	}

}

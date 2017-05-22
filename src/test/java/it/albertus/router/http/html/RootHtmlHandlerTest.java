package it.albertus.router.http.html;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.DummyReader;
import it.albertus.router.reader.TpLink8970Reader;

public class RootHtmlHandlerTest {

	@Test
	public void testImageFileName() {
		final RootHtmlHandler h = new RootHtmlHandler(null);
		Assert.assertEquals("asus_dsl_n12e.png", h.getImageFileName(new AsusDslN12EReader()));
		Assert.assertEquals("asus_dsl_n14u.png", h.getImageFileName(new AsusDslN14UReader()));
		Assert.assertEquals("dlink_dsl_2750b.png", h.getImageFileName(new DLinkDsl2750Reader()));
		Assert.assertEquals("tplink_td_w8970v1.png", h.getImageFileName(new TpLink8970Reader()));
		Assert.assertEquals("tplink_td_w8970v1.png", h.getImageFileName(new DummyReader()));
	}

}

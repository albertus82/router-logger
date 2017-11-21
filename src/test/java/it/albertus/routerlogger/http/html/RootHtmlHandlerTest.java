package it.albertus.routerlogger.http.html;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.routerlogger.reader.AsusDslN12EReader;
import it.albertus.routerlogger.reader.AsusDslN14UReader;
import it.albertus.routerlogger.reader.DLinkDsl2750Reader;
import it.albertus.routerlogger.reader.DummyReader;
import it.albertus.routerlogger.reader.TpLink8970Reader;

public class RootHtmlHandlerTest {

	@Test
	public void testImageFileName() {
		Assert.assertEquals("asus_dsl_n12e.png", new AsusDslN12EReader().getImageFileName());
		Assert.assertEquals("asus_dsl_n14u.png", new AsusDslN14UReader().getImageFileName());
		Assert.assertEquals("dlink_dsl_2750b.png", new DLinkDsl2750Reader().getImageFileName());
		Assert.assertEquals("tplink_td_w8970v1.png", new TpLink8970Reader().getImageFileName());
		Assert.assertEquals("applications-internet.png", new DummyReader().getImageFileName());
	}

}

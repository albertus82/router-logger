package it.albertus.router.server;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.router.server.html.BaseHtmlHandler;

public class BaseHtmlHandlerTest {

	private static final String petrarca = "e 'l viso di pietosi color farsi,\r\nnon so se vero o falso, mi parea:\r\ni' che l'esca amorosa al petto avea,\r\nqual meraviglia se di subito arsi?";
	private static final String specials = "'qwertyuiop' & <<asdfghjkl>> && \"\"\"zxcvbnm\"\"\"";

	@Test
	public void escapeHtmlTest() {
		String escaped = BaseHtmlHandler.escapeHtml(petrarca);
		System.out.println(escaped);
		Assert.assertEquals(petrarca, BaseHtmlHandler.unescapeHtml(escaped));

		escaped = BaseHtmlHandler.escapeHtml(specials);
		System.out.println(escaped);
		Assert.assertEquals(specials, BaseHtmlHandler.unescapeHtml(escaped));
	}

}

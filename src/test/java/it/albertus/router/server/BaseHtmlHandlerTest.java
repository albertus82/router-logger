package it.albertus.router.server;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.router.server.html.BaseHtmlHandler;

public class BaseHtmlHandlerTest {

	private static final String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed rutrum, mi porttitor lacinia cursus, elit lectus vehicula ante, vitae molestie lacus lectus nec nulla. Cras aliquet mi vitae dui porttitor dignissim. Mauris varius, sem eget tempus finibus, magna odio ultricies ipsum, a luctus turpis lorem nec elit. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Sed quis neque non turpis scelerisque sagittis ut eget purus. Etiam bibendum, lectus eget interdum rutrum, est diam tristique neque, vitae consectetur elit lorem sit amet augue. Vivamus in sodales metus. Vivamus arcu quam, rutrum vitae ullamcorper laoreet, condimentum sit amet metus. Duis quis ligula eleifend, maximus est in, tristique justo. Morbi felis purus, luctus et tincidunt vel, ultrices viverra dui.";
	private static final String specials = "'qwertyuiop' & <<asdfghjkl>> && \"zxcvbnm,._-\"";

	@Test
	public void escapeHtmlTest() {
		String escaped = BaseHtmlHandler.escapeHtml(lorem);
		System.out.println(escaped);
		Assert.assertEquals(lorem, BaseHtmlHandler.unescapeHtml(escaped));

		escaped = BaseHtmlHandler.escapeHtml(specials);
		System.out.println(escaped);
		Assert.assertEquals(specials, BaseHtmlHandler.unescapeHtml(escaped));
	}

}

package it.albertus.router.gui.preference.field;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

public class TextFormatter {

	private static final String SAMPLE_CHAR = "9";

	private static final FontRegistry fontRegistry = JFaceResources.getFontRegistry();

	public static void updateFontStyle(final Text text, final String defaultValue) {
		if (text != null && !text.isDisposed() && text.getFont() != null && !text.getFont().isDisposed() && text.getFont().getFontData() != null && text.getFont().getFontData().length != 0) {
			if (!defaultValue.equals(text.getText())) {
				if (text.getFont().getFontData()[0].getStyle() != SWT.BOLD) {
					setBoldFontStyle(text);
				}
			}
			else {
				if (text.getFont().getFontData()[0].getStyle() != SWT.NORMAL) {
					setNormalFontStyle(text);
				}
			}
		}
	}

	/** Calls {@code updateFontStyle(String.valueOf(defaultValue))}. */
	public static void updateFontStyle(final Text text, final Object defaultValue) {
		updateFontStyle(text, String.valueOf(defaultValue));
	}

	public static void setNormalFontStyle(final Text text) {
		final FontData fontData = text.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("defaultProperty")) {
			fontData.setStyle(SWT.NORMAL);
			fontRegistry.put("defaultProperty", new FontData[] { fontData });
		}
		text.setFont(fontRegistry.get("defaultProperty"));
	}

	public static void setBoldFontStyle(final Text text) {
		final FontData fontData = text.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("customProperty")) {
			fontData.setStyle(SWT.BOLD);
			fontRegistry.put("customProperty", new FontData[] { fontData });
		}
		text.setFont(fontRegistry.get("customProperty"));
	}

	public static int getWidthHint(final Text text, final int size) {
		return getWidthHint(text, size, SAMPLE_CHAR);
	}

	public static int getWidthHint(final Text text, final int size, final String string) {
		int widthHint = SWT.DEFAULT;
		final GC gc = new GC(text);
		try {
			final Point extent = gc.textExtent(string);
			widthHint = size * extent.x;
		}
		finally {
			gc.dispose();
		}
		return widthHint;
	}

}

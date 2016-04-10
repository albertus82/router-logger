package it.albertus.router.gui;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public final class TextFormatter {

	private TextFormatter() {}

	private static final char SAMPLE_CHAR = '9';

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

	public static void setNormalFontStyle(final Control control) {
		final FontData fontData = control.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("defaultProperty")) {
			fontData.setStyle(SWT.NORMAL);
			fontRegistry.put("defaultProperty", new FontData[] { fontData });
		}
		control.setFont(fontRegistry.get("defaultProperty"));
	}

	public static void setBoldFontStyle(final Control control) {
		final FontData fontData = control.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("customProperty")) {
			fontData.setStyle(SWT.BOLD);
			fontRegistry.put("customProperty", new FontData[] { fontData });
		}
		control.setFont(fontRegistry.get("customProperty"));
	}

	public static int getWidthHint(final Control control, final int size, final int weight) {
		return getWidthHint(control, size, weight, Character.toString(SAMPLE_CHAR));
	}

	public static int getWidthHint(final Control control, final int size, final int weight, final char character) {
		return getWidthHint(control, size, weight, Character.toString(character));
	}

	public static int getWidthHint(final Control control, final int weight, final String string) {
		return getWidthHint(control, 1, weight, string);
	}

	private static int getWidthHint(final Control control, final int multiplier, final int weight, final String string) {
		int widthHint = SWT.DEFAULT;
		if (control != null && !control.isDisposed()) {
			final Font font = control.getFont(); // Backup initial font.
			if (weight == SWT.BOLD) {
				setBoldFontStyle(control);
			}
			else {
				setNormalFontStyle(control);
			}
			final GC gc = new GC(control);
			try {
				final Point extent = gc.textExtent(string);
				widthHint = (int) (multiplier * extent.x * 1.1);
			}
			finally {
				gc.dispose();
			}
			control.setFont(font); // Restore initial font.
		}
		return widthHint;
	}

}

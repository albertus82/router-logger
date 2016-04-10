package it.albertus.router.gui.preference.field;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Text;

public class TextFormatter {

	private final Text text;
	private final FontRegistry fontRegistry;

	public TextFormatter(final Text text) {
		this.text = text;
		this.fontRegistry = JFaceResources.getFontRegistry();
	}

	public void updateFontStyle(final String defaultValue) {
		if (text != null && text.getFont() != null && text.getFont().getFontData() != null && text.getFont().getFontData().length != 0) {
			if (!defaultValue.equals(text.getText())) {
				if (text.getFont().getFontData()[0].getStyle() != SWT.BOLD) {
					setBoldFontStyle();
				}
			}
			else {
				if (text.getFont().getFontData()[0].getStyle() != SWT.NORMAL) {
					setNormalFontStyle();
				}
			}
		}
	}

	/** Calls {@code updateFontStyle(String.valueOf(defaultValue))}. */
	public void updateFontStyle(final Object defaultValue) {
		updateFontStyle(String.valueOf(defaultValue));
	}

	public void setNormalFontStyle() {
		final FontData fontData = text.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("defaultProperty")) {
			fontData.setStyle(SWT.NORMAL);
			fontRegistry.put("defaultProperty", new FontData[] { fontData });
		}
		text.setFont(fontRegistry.get("defaultProperty"));
	}

	public void setBoldFontStyle() {
		final FontData fontData = text.getFont().getFontData()[0];
		if (!fontRegistry.hasValueFor("customProperty")) {
			fontData.setStyle(SWT.BOLD);
			fontRegistry.put("customProperty", new FontData[] { fontData });
		}
		text.setFont(fontRegistry.get("customProperty"));
	}

}

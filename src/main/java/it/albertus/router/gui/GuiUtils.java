package it.albertus.router.gui;

import org.eclipse.swt.SWT;

public class GuiUtils {

	public static final char KEY_SELECT_ALL = 'a';
	public static final char KEY_COPY = 'c';

	public static String getMod1ShortcutLabel(final char key) {
		if (SWT.MOD1 != SWT.COMMAND) {
			return "\tCtrl+" + Character.toUpperCase(key);
		}
		else {
			return "";
		}
	}

}

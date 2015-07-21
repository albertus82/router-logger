package it.albertus.router.gui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class GuiImages {

	public static final Image[] ICONS = initIcons();

	private static Image[] initIcons() {
		InputStream is = GuiImages.class.getResourceAsStream("router.ico");
		ImageData[] images = new ImageLoader().load(is);
		try {
			is.close();
		}
		catch (IOException ioe) {}
		Image[] icons = new Image[images.length];
		int i = 0;
		for (ImageData id : images) {
			icons[i++] = new Image(Display.getCurrent(), id);
		}
		return icons;
	}

}

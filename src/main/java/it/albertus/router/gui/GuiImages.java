package it.albertus.router.gui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class GuiImages {

	public static final Image[] ICONS;
	
	static {
		ImageLoader loader = new ImageLoader();
		ImageData[] images = loader.load(GuiImages.class.getResourceAsStream("router.ico"));
		ICONS = new Image[images.length];
		int i = 0;
		for(ImageData id : images) {
			ICONS[i++] = new Image(Display.getCurrent(), id);
		}
	}
	
}

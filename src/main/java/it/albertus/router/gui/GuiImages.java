package it.albertus.router.gui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

public class GuiImages {

	public static final Image[] ICONS_ROUTER = initRouterIcons();
	public static final Image[] ICONS_WARNING = initWarningIcons();

	public static final Image TRAY_ICON_ROUTER_OK = GuiImages.ICONS_ROUTER[1];
	public static final Image TRAY_ICON_ROUTER_WARNING = new DecorationOverlayIcon(TRAY_ICON_ROUTER_OK, ImageDescriptor.createFromImage(GuiImages.ICONS_WARNING[0]), IDecoration.BOTTOM_RIGHT).createImage();

	private static Image[] initRouterIcons() {
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

	private static Image[] initWarningIcons() {
		InputStream is = GuiImages.class.getResourceAsStream("warning.ico");
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

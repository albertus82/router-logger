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

	public static final Image[] ICONS_ROUTER = loadIcons("router.ico");
	public static final Image[] ICONS_WARNING = loadIcons("warning.ico");
	public static final Image[] ICONS_ERROR = loadIcons("error.ico");
	public static final Image[] ICONS_LOCK = loadIcons("lock.ico");

	public static final Image TRAY_ICON_ROUTER_OK = GuiImages.ICONS_ROUTER[1];
	public static final Image TRAY_ICON_ROUTER_WARNING = new DecorationOverlayIcon(TRAY_ICON_ROUTER_OK, ImageDescriptor.createFromImage(GuiImages.ICONS_WARNING[0]), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ROUTER_ERROR = new DecorationOverlayIcon(TRAY_ICON_ROUTER_OK, ImageDescriptor.createFromImage(GuiImages.ICONS_ERROR[0]), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ROUTER_LOCK = new DecorationOverlayIcon(TRAY_ICON_ROUTER_OK, ImageDescriptor.createFromImage(GuiImages.ICONS_LOCK[0]), IDecoration.BOTTOM_RIGHT).createImage();

	private static Image[] loadIcons(final String fileName) {
		final InputStream is = GuiImages.class.getResourceAsStream(fileName);
		final ImageData[] images = new ImageLoader().load(is);
		try {
			is.close();
		}
		catch (IOException ioe) {}
		final Image[] icons = new Image[images.length];
		int i = 0;
		for (final ImageData id : images) {
			icons[i++] = new Image(Display.getCurrent(), id);
		}
		return icons;
	}

}

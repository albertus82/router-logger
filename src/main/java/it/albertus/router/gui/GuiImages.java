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

	/* Icona principale dell'applicazione (in vari formati) */
	public static final Image[] ICONS_ROUTER_BLUE = loadIcons("blue.ico");

	/* Icone base per l'area di notifica (16x16) */
	public static final Image TRAY_ICON_ROUTER_BLUE = GuiImages.ICONS_ROUTER_BLUE[1];
	public static final Image TRAY_ICON_ROUTER_GREY = loadIcons("grey.ico")[0];

	/* Simboli in sovraimpressione per l'area di notifica (16x16, non utilizzabili da soli) */
	private static final Image TRAY_ICON_OVERLAY_CLOCK = loadIcons("clock.ico")[0];
	private static final Image TRAY_ICON_OVERLAY_ERROR = loadIcons("error.ico")[0];
	private static final Image TRAY_ICON_OVERLAY_LOCK = loadIcons("lock.ico")[0];
	private static final Image TRAY_ICON_OVERLAY_WARNING = loadIcons("warning.ico")[0];

	/* Icone composte per l'area di notifica (16x16) */
	public static final Image TRAY_ICON_ROUTER_BLUE_WARNING = new DecorationOverlayIcon(TRAY_ICON_ROUTER_BLUE, ImageDescriptor.createFromImage(GuiImages.TRAY_ICON_OVERLAY_WARNING), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ROUTER_BLUE_LOCK = new DecorationOverlayIcon(TRAY_ICON_ROUTER_BLUE, ImageDescriptor.createFromImage(GuiImages.TRAY_ICON_OVERLAY_LOCK), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ROUTER_GREY_CLOCK = new DecorationOverlayIcon(TRAY_ICON_ROUTER_GREY, ImageDescriptor.createFromImage(GuiImages.TRAY_ICON_OVERLAY_CLOCK), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ROUTER_GREY_ERROR = new DecorationOverlayIcon(TRAY_ICON_ROUTER_GREY, ImageDescriptor.createFromImage(GuiImages.TRAY_ICON_OVERLAY_ERROR), IDecoration.BOTTOM_RIGHT).createImage();

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

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

public class Images {

	/* Icona principale dell'applicazione (in vari formati) */
	static final Image[] MAIN_ICONS = loadIcons("main.ico");

	/* Icone base per l'area di notifica (16x16) */
	static final Image TRAY_ICON_ACTIVE = Images.MAIN_ICONS[2];
	static final Image TRAY_ICON_INACTIVE = loadIcons("inactive.ico")[0];

	/* Simboli in sovraimpressione per l'area di notifica (16x16, non utilizzabili da soli) */
	private static final Image TRAY_ICON_OVERLAY_CLOCK = loadIcons("clock.ico")[0];
	private static final Image TRAY_ICON_OVERLAY_ERROR = loadIcons("error.ico")[0];
	private static final Image TRAY_ICON_OVERLAY_LOCK = loadIcons("lock.ico")[0];
	private static final Image TRAY_ICON_OVERLAY_WARNING = loadIcons("warning.ico")[0];

	/* Icone composte per l'area di notifica (16x16) */
	static final Image TRAY_ICON_ACTIVE_WARNING = new DecorationOverlayIcon(TRAY_ICON_ACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_WARNING), IDecoration.BOTTOM_RIGHT).createImage();
	static final Image TRAY_ICON_ACTIVE_LOCK = new DecorationOverlayIcon(TRAY_ICON_ACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_LOCK), IDecoration.BOTTOM_RIGHT).createImage();
	static final Image TRAY_ICON_INACTIVE_CLOCK = new DecorationOverlayIcon(TRAY_ICON_INACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_CLOCK), IDecoration.BOTTOM_RIGHT).createImage();
	static final Image TRAY_ICON_INACTIVE_ERROR = new DecorationOverlayIcon(TRAY_ICON_INACTIVE, ImageDescriptor.createFromImage(Images.TRAY_ICON_OVERLAY_ERROR), IDecoration.BOTTOM_RIGHT).createImage();

	private static Image[] loadIcons(final String fileName) {
		final InputStream is = Images.class.getResourceAsStream(fileName);
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

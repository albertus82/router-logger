package it.albertus.routerlogger.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import it.albertus.util.IOUtils;

public class Images {

	// Main application icon (in various formats)
	private static final List<Image> mainIcons = load("main.ico");

	// Icone base per l'area di notifica (16x16)
	public static final Image TRAY_ICON_ACTIVE = mainIcons.get(2);
	public static final Image TRAY_ICON_INACTIVE = load("inactive.ico").get(0);

	// Simboli in sovraimpressione per l'area di notifica (16x16, non utilizzabili da soli)
	private static final Image TRAY_ICON_OVERLAY_CLOCK = load("clock.ico").get(0);
	private static final Image TRAY_ICON_OVERLAY_ERROR = load("error.ico").get(0);
	private static final Image TRAY_ICON_OVERLAY_LOCK = load("lock.ico").get(0);
	private static final Image TRAY_ICON_OVERLAY_WARNING = load("warning.ico").get(0);

	// Icone composte per l'area di notifica (16x16)
	public static final Image TRAY_ICON_ACTIVE_WARNING = new DecorationOverlayIcon(TRAY_ICON_ACTIVE, ImageDescriptor.createFromImage(TRAY_ICON_OVERLAY_WARNING), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_ACTIVE_LOCK = new DecorationOverlayIcon(TRAY_ICON_ACTIVE, ImageDescriptor.createFromImage(TRAY_ICON_OVERLAY_LOCK), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_INACTIVE_CLOCK = new DecorationOverlayIcon(TRAY_ICON_INACTIVE, ImageDescriptor.createFromImage(TRAY_ICON_OVERLAY_CLOCK), IDecoration.BOTTOM_RIGHT).createImage();
	public static final Image TRAY_ICON_INACTIVE_ERROR = new DecorationOverlayIcon(TRAY_ICON_INACTIVE, ImageDescriptor.createFromImage(TRAY_ICON_OVERLAY_ERROR), IDecoration.BOTTOM_RIGHT).createImage();

	private Images() {
		throw new IllegalAccessError();
	}

	private static List<Image> load(final String fileName) {
		InputStream is = null;
		try {
			is = Images.class.getResourceAsStream(fileName);
			final List<Image> images = new ArrayList<Image>();
			for (final ImageData id : new ImageLoader().load(is)) {
				images.add(new Image(Display.getCurrent(), id));
			}
			return images;
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static Image[] getMainIcons() {
		return mainIcons.toArray(new Image[mainIcons.size()]);
	}

}

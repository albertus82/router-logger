package it.albertus.routerlogger.gui.preference.field;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.JFaceMessages;
import it.albertus.jface.preference.field.EnhancedListEditor;
import it.albertus.routerlogger.engine.RouterLoggerEngine;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.routerlogger.writer.IWriter;
import it.albertus.util.logging.LoggerFactory;

public class WriterListEditor extends EnhancedListEditor {

	public static final String CLASS_NAMES_SPLIT_REGEX = "[,;\\s]+";
	public static final char SEPARATOR = ',';

	private static final Logger logger = LoggerFactory.getLogger(WriterListEditor.class);

	private final Image[] images;
	private final String[][] entryNamesAndValues;

	public WriterListEditor(final String name, final String labelText, final Composite parent, final Integer horizontalSpan, final String[][] entryNamesAndValues, final Image... images) {
		super(name, labelText, parent, horizontalSpan);
		this.entryNamesAndValues = entryNamesAndValues;
		this.images = images;
	}

	@Override
	protected void createButtons(final Composite box) {
		createAddButton(box);
		createEditButton(box);
		createRemoveButton(box);
	}

	@Override
	protected String createList(final String[] classNames) {
		final StringBuilder list = new StringBuilder();
		if (classNames != null) {
			for (int index = 0; index < classNames.length; index++) {
				list.append(classNames[index].trim());
				if (index != classNames.length - 1) {
					list.append(SEPARATOR);
				}
			}
		}
		return list.toString();
	}

	@Override
	protected String getNewInputObject() {
		final ClassNameDialog classNameDialog = new ClassNameDialog(getShell());
		classNameDialog.create(JFaceMessages.get("lbl.preferences.email.dialog.add.title"));
		if (classNameDialog.open() == Window.OK) {
			return classNameDialog.getClassName();
		}
		return null;
	}

	@Override
	protected String getModifiedInputObject(final String value) {
		final ClassNameDialog emailAddressDialog = new ClassNameDialog(getShell());
		emailAddressDialog.create(JFaceMessages.get("lbl.preferences.email.dialog.edit.title"));
		emailAddressDialog.comboClassName.setText(value);
		if (emailAddressDialog.open() == Window.OK) {
			return emailAddressDialog.getClassName();
		}
		return null;
	}

	@Override
	protected String[] parseString(final String stringList) {
		if (stringList != null && !stringList.isEmpty()) {
			return stringList.trim().split(CLASS_NAMES_SPLIT_REGEX);
		}
		else {
			return new String[] {};
		}
	}

	protected class ClassNameDialog extends TitleAreaDialog {

		private Combo comboClassName;
		private Button okButton;
		private String className;

		public ClassNameDialog(final Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			if (images != null && images.length > 0) {
				newShell.setImages(images);
			}
		}

		public void create(final String title) {
			super.create();
			getShell().setText(title);
			setTitle(title);
			setMessage(JFaceMessages.get("lbl.preferences.email.dialog.message"), IMessageProvider.INFORMATION);
		}

		@Override
		protected Composite createDialogArea(final Composite parent) {
			final Composite area = (Composite) super.createDialogArea(parent);
			final Composite container = new Composite(area, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(container);

			final Label labelName = new Label(container, SWT.NONE);
			labelName.setText(JFaceMessages.get("lbl.preferences.email.dialog.address"));
			GridDataFactory.swtDefaults().applyTo(labelName);

			comboClassName = new Combo(container, SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(comboClassName);
			comboClassName.setTextLimit(Preferences.MAX_VALUE_LENGTH);
			for (int i = 0; i < entryNamesAndValues.length; i++) {
				comboClassName.add(entryNamesAndValues[i][0], i);
			}
			comboClassName.addModifyListener(new TextModifyListener());

			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);

			okButton = getButton(IDialogConstants.OK_ID);
			okButton.setText(JFaceMessages.get("lbl.button.ok"));
			okButton.setEnabled(false);

			final Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
			cancelButton.setText(JFaceMessages.get("lbl.button.cancel"));
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		public String getClassName() {
			return className;
		}

		private class TextModifyListener implements ModifyListener {

			@Override
			public void modifyText(final ModifyEvent me) {
				String text = comboClassName.getText().trim();

				for (int i = 0; i < entryNamesAndValues.length; i++) {
					if (text.equals(entryNamesAndValues[i][0])) {
						text = entryNamesAndValues[i][1];
						break;
					}
				}

				className = text;

				if (text.isEmpty() || !checkState(text)) {
					if (okButton.isEnabled()) {
						okButton.setEnabled(false);
					}
				}
				else {
					if (!okButton.isEnabled()) {
						okButton.setEnabled(true);
					}
				}
			}

			private boolean checkState(final String value) {
				try {
					final Class<?> writerClass = Class.forName(RouterLoggerEngine.getWriterClassName(value), false, this.getClass().getClassLoader());
					if (IWriter.class.isAssignableFrom(writerClass) && !Modifier.isAbstract(writerClass.getModifiers())) {
						setErrorMessage(null);
						return true;
					}
					else {
						setErrorMessage(Messages.get("err.preferences.combo.class.writer.invalid"));
						return false;
					}
				}
				catch (final Exception e) {
					logger.log(Level.FINE, e.toString(), e);
					setErrorMessage(Messages.get("err.preferences.combo.class.writer.missing"));
					return false;
				}
				catch (final LinkageError e) {
					logger.log(Level.FINE, e.toString(), e);
					setErrorMessage(Messages.get("err.preferences.combo.class.writer.missing"));
					return false;
				}
			}
		}
	}

}

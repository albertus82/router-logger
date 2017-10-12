package it.albertus.routerlogger.gui.preference.field;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.preference.field.EnhancedListEditor;
import it.albertus.routerlogger.engine.RouterLoggerConfig.Thresholds;
import it.albertus.routerlogger.gui.Images;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.StringUtils;

public class ThresholdsListEditor extends EnhancedListEditor {

	private static final char DELIMITER = '=';

	public ThresholdsListEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent, Integer.valueOf(Short.MAX_VALUE));
	}

	@Override
	protected void createButtons(final Composite box) {
		createAddButton(box);
		createEditButton(box);
		createRemoveButton(box);
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		return (PreferenceStore) super.getPreferenceStore();
	}

	@Override
	protected void doLoad() {
		final List list = getList();
		if (list != null) {
			final Set<String> thresholds = new TreeSet<String>();
			for (final String preferenceName : getPreferenceStore().preferenceNames()) {
				if (preferenceName.startsWith(Thresholds.CFG_PREFIX + '.')) {
					thresholds.add(preferenceName.substring(preferenceName.indexOf('.') + 1) + DELIMITER + getPreferenceStore().getString(preferenceName));
				}
			}
			for (final String threshold : thresholds) {
				list.add(threshold);
			}
		}
	}

	@Override
	public void store() {
		if (getPreferenceStore() != null) {
			doStore();
		}
	}

	@Override
	protected void doStore() {
		// Cleanup...
		for (final String preferenceName : getPreferenceStore().preferenceNames()) {
			if (preferenceName.startsWith(Thresholds.CFG_PREFIX + '.')) {
				getPreferenceStore().setToDefault(preferenceName);
			}
		}

		// Store...
		if (getList() != null) {
			for (final String item : getList().getItems()) {
				final String name = Thresholds.CFG_PREFIX + '.' + item.substring(0, item.indexOf(DELIMITER));
				final String value = item.substring(item.indexOf(DELIMITER) + 1);
				getPreferenceStore().setValue(name, value);
			}
		}
	}

	@Override
	protected String getNewInputObject() {
		final ThresholdDialog thresholdDialog = new ThresholdDialog(getShell());
		thresholdDialog.create(Messages.get("lbl.preferences.thresholds.expressions.add.title"));
		if (thresholdDialog.open() == Window.OK) {
			return thresholdDialog.getIdentifier() + DELIMITER + thresholdDialog.getExpression();
		}
		return null;
	}

	@Override
	protected String getModifiedInputObject(final String value) {
		final ThresholdDialog thresholdDialog = new ThresholdDialog(getShell());
		thresholdDialog.create(Messages.get("lbl.preferences.thresholds.expressions.edit.title"));
		thresholdDialog.textIdentifier.setText(StringUtils.substringBefore(value, Character.toString(DELIMITER)));
		thresholdDialog.textExpression.setText(StringUtils.substringAfter(value, Character.toString(DELIMITER)));
		if (thresholdDialog.open() == Window.OK) {
			return thresholdDialog.getIdentifier() + DELIMITER + thresholdDialog.getExpression();
		}
		return null;
	}

	protected class ThresholdDialog extends TitleAreaDialog {
		private static final int TEXT_LIMIT = 255;
		private static final String REGEX_IDENTIFIER = "[0-9A-Za-z\\.]*";

		private Text textIdentifier;
		private Text textExpression;
		private Button okButton;
		private Button cancelButton;
		private String identifier;
		private String expression;

		public ThresholdDialog(final Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.setImages(Images.getMainIcons());
		}

		public void create(final String title) {
			super.create();
			getShell().setText(title);
			setTitle(title);
			setMessage(Messages.get("lbl.preferences.thresholds.expressions.message"), IMessageProvider.INFORMATION);
		}

		@Override
		protected Composite createDialogArea(final Composite parent) {
			final Composite area = (Composite) super.createDialogArea(parent);
			final Composite container = new Composite(area, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(container);

			final Label labelName = new Label(container, SWT.NONE);
			labelName.setText(Messages.get("lbl.preferences.thresholds.expressions.identifier"));
			GridDataFactory.swtDefaults().applyTo(labelName);

			textIdentifier = new Text(container, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textIdentifier);
			textIdentifier.setTextLimit(TEXT_LIMIT);
			textIdentifier.addVerifyListener(new IdentifierVerifyListener());

			final Label labelExpression = new Label(container, SWT.NONE);
			labelExpression.setText(Messages.get("lbl.preferences.thresholds.expressions.expression"));
			GridDataFactory.swtDefaults().applyTo(labelExpression);

			textExpression = new Text(container, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textExpression);
			textExpression.setTextLimit(TEXT_LIMIT);

			final TextModifyListener textModifyListener = new TextModifyListener();
			textIdentifier.addModifyListener(textModifyListener);
			textExpression.addModifyListener(textModifyListener);

			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);

			okButton = getButton(IDialogConstants.OK_ID);
			okButton.setText(Messages.get("lbl.button.ok"));
			okButton.setEnabled(false);

			cancelButton = getButton(IDialogConstants.CANCEL_ID);
			cancelButton.setText(Messages.get("lbl.button.cancel"));
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		@Override
		protected void okPressed() {
			identifier = textIdentifier.getText();
			expression = textExpression.getText();
			super.okPressed();
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getExpression() {
			return expression;
		}

		private class TextModifyListener implements ModifyListener {
			@Override
			public void modifyText(final ModifyEvent me) {
				if (textIdentifier.getText().isEmpty() || textExpression.getText().trim().isEmpty()) {
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
		}

		private class IdentifierVerifyListener implements VerifyListener {
			@Override
			public void verifyText(final VerifyEvent ve) {
				if (!ve.text.matches(REGEX_IDENTIFIER)) {
					ve.doit = false;
				}
			}
		}
	}

	/**
	 * @deprecated This <tt>ListEditor</tt> manages its properties internally.
	 */
	@Override
	@Deprecated
	protected String[] parseString(final String stringList) {
		return new String[] {};
	}

	/**
	 * @deprecated This <tt>ListEditor</tt> manages its properties internally.
	 */
	@Override
	@Deprecated
	protected String createList(final String[] items) {
		return null;
	}

}

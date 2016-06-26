package it.albertus.router.gui.preference.field;

import it.albertus.jface.preference.field.LocalizedListEditor;
import it.albertus.router.engine.RouterLoggerConfiguration.Thresholds;
import it.albertus.router.gui.Images;
import it.albertus.router.resources.Resources;

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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ThresholdsFieldEditor extends LocalizedListEditor {

	public ThresholdsFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent, Integer.valueOf(Short.MAX_VALUE));
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		return (PreferenceStore) super.getPreferenceStore();
	}

	@Override
	protected void doLoad() {
		if (getList() != null) {
			final Set<String> thresholds = new TreeSet<String>();
			for (final String preferenceName : getPreferenceStore().preferenceNames()) {
				if (preferenceName.startsWith(Thresholds.CFG_PREFIX + '.')) {
					thresholds.add(preferenceName.substring(preferenceName.indexOf('.') + 1) + '=' + getPreferenceStore().getString(preferenceName));
				}
			}
			for (final String threshold : thresholds) {
				getList().add(threshold);
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
				final String name = Thresholds.CFG_PREFIX + '.' + item.substring(0, item.indexOf('='));
				final String value = item.substring(item.indexOf('=') + 1);
				getPreferenceStore().setValue(name, value);
			}
		}
	}

	@Override
	protected String getNewInputObject() {
		final ThresholdDialog thresholdDialog = new ThresholdDialog(getShell());
		thresholdDialog.create();
		if (thresholdDialog.open() == Window.OK) {
			return thresholdDialog.getIdentifier() + '=' + thresholdDialog.getExpression();
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
			newShell.setText(Resources.get("lbl.preferences.thresholds.expressions.title"));
			newShell.setImages(Images.MAIN_ICONS);
		}

		@Override
		public void create() {
			super.create();
			setTitle(Resources.get("lbl.preferences.thresholds.expressions.title"));
			setMessage(Resources.get("lbl.preferences.thresholds.expressions.message"), IMessageProvider.INFORMATION);
		}

		@Override
		protected Composite createDialogArea(final Composite parent) {
			final Composite area = (Composite) super.createDialogArea(parent);
			final Composite container = new Composite(area, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(container);

			final Label labelName = new Label(container, SWT.NONE);
			labelName.setText(Resources.get("lbl.preferences.thresholds.expressions.identifier"));
			GridDataFactory.swtDefaults().applyTo(labelName);

			textIdentifier = new Text(container, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textIdentifier);
			textIdentifier.setTextLimit(TEXT_LIMIT);
			textIdentifier.addVerifyListener(new IdentifierVerifyListener());

			final Label labelExpression = new Label(container, SWT.NONE);
			labelExpression.setText(Resources.get("lbl.preferences.thresholds.expressions.expression"));
			GridDataFactory.swtDefaults().applyTo(labelExpression);

			textExpression = new Text(container, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textExpression);
			textExpression.setTextLimit(TEXT_LIMIT);

			final TextKeyListener textKeyListener = new TextKeyListener();
			textIdentifier.addKeyListener(textKeyListener);
			textExpression.addKeyListener(textKeyListener);

			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);

			okButton = getButton(IDialogConstants.OK_ID);
			okButton.setText(Resources.get("lbl.button.ok"));
			okButton.setEnabled(false);

			cancelButton = getButton(IDialogConstants.CANCEL_ID);
			cancelButton.setText(Resources.get("lbl.button.cancel"));
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

		private class TextKeyListener extends KeyAdapter {
			@Override
			public void keyReleased(final KeyEvent ke) {
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

	@Override
	@Deprecated
	protected String[] parseString(final String stringList) {
		return new String[] {};
	}

	@Override
	@Deprecated
	protected String createList(final String[] items) {
		return null;
	}

}

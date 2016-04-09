package it.albertus.router.gui.preferences;

import it.albertus.router.engine.RouterLoggerConfiguration.Thresholds;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ThresholdsFieldEditor extends ListEditor {

	public ThresholdsFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	public PreferenceStore getPreferenceStore() {
		return (PreferenceStore) super.getPreferenceStore();
	}

	@Override
	protected void doLoad() {
		if (getList() != null) {
			for (final String preferenceName : getPreferenceStore().preferenceNames()) {
				if (preferenceName.startsWith(Thresholds.CFG_PREFIX + '.')) {
					getList().add(preferenceName.substring(preferenceName.indexOf('.') + 1) + '=' + getPreferenceStore().getString(preferenceName));
				}
			}
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
	@Deprecated
	protected String createList(String[] items) {
		return null;
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

	@Override
	@Deprecated
	protected String[] parseString(final String stringList) {
		return new String[] {};
	}

	public class ThresholdDialog extends TitleAreaDialog {
		private Text textIdentifier;
		private Text textExpression;
		private String identifier;
		private String expression;

		public ThresholdDialog(final Shell parent) {
			super(parent);
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

			textIdentifier = new Text(container, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textIdentifier);
			textIdentifier.addVerifyListener(new IdentifierVerifyListener());

			final Label labelExpression = new Label(container, SWT.NONE);
			labelExpression.setText(Resources.get("lbl.preferences.thresholds.expressions.expression"));

			textExpression = new Text(container, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textExpression);

			return area;
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

		private class IdentifierVerifyListener implements VerifyListener {
			private static final String REGEX_IDENTIFIER = "[0-9A-Za-z\\.]*";

			@Override
			public void verifyText(final VerifyEvent ve) {
				if (!ve.text.matches(REGEX_IDENTIFIER)) {
					ve.doit = false;
				}
			}
		}
	}

}

package it.albertus.router.gui.preferences;

import it.albertus.router.engine.RouterLoggerConfiguration.Thresholds;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ThresholdsFieldEditor extends ListEditor {

	public ThresholdsFieldEditor(String name, String labelText, Composite parent) {
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
			for (String item : getList().getItems()) {
				final String name = Thresholds.CFG_PREFIX + '.' + item.substring(0, item.indexOf('='));
				final String value = item.substring(item.indexOf('=') + 1);
				getPreferenceStore().setValue(name, value);
			}
		}
	}

	@Override
	protected String createList(String[] items) {
		return null;
	}

	@Override
	protected String getNewInputObject() {
		final ThresholdDialog dialog = new ThresholdDialog(getShell());
		dialog.create();
		if (dialog.open() == Window.OK) {
			return dialog.getId() + '=' + dialog.getExpression();
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return new String[] {};
	}

	public class ThresholdDialog extends TitleAreaDialog {
		private Text textId; // TODO impedire ' ' e '='
		private Text textExpression;
		private String id;
		private String expression;

		public ThresholdDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		public void create() {
			super.create();
			setTitle("Title");
			setMessage("Message", IMessageProvider.INFORMATION);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(2, false);
			container.setLayout(layout);

			createFirstName(container);
			createLastName(container);

			return area;
		}

		private void createFirstName(Composite container) {
			Label lbtFirstName = new Label(container, SWT.NONE);
			lbtFirstName.setText("ID");

			GridData dataFirstName = new GridData();
			dataFirstName.grabExcessHorizontalSpace = true;
			dataFirstName.horizontalAlignment = GridData.FILL;

			textId = new Text(container, SWT.BORDER);
			textId.setLayoutData(dataFirstName);
		}

		private void createLastName(Composite container) {
			Label lbtLastName = new Label(container, SWT.NONE);
			lbtLastName.setText("Expression");

			GridData dataLastName = new GridData();
			dataLastName.grabExcessHorizontalSpace = true;
			dataLastName.horizontalAlignment = GridData.FILL;
			textExpression = new Text(container, SWT.BORDER);
			textExpression.setLayoutData(dataLastName);
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		private void saveInput() {
			id = textId.getText();
			expression = textExpression.getText();
		}

		@Override
		protected void okPressed() {
			saveInput();
			super.okPressed();
		}

		public String getId() {
			return id;
		}

		public String getExpression() {
			return expression;
		}
	}

}

package it.albertus.router.gui;

import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {

	private String message = "";
	private String link = "";

	public AboutDialog(Shell parent) {
		this(parent, SWT.SHEET); // SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
	}

	public AboutDialog(Shell parent, int style) {
		super(parent, style);
	}

	public void open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, false));

		final Label icon = new Label(shell, SWT.NONE);
		icon.setImage(shell.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
		GridData gridData = new GridData();
		gridData.verticalSpan = 2;
		icon.setLayoutData(gridData);

		final Label info = new Label(shell, SWT.NONE);
		info.setText(this.message);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		info.setLayoutData(gridData);

		final Link link = new Link(shell, SWT.NONE);
		link.setText("<a href=\"" + getLink() + "\">" + getLink() + "</a>");
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		link.setLayoutData(gridData);
		link.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(e.text);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(Resources.get("lbl.button.ok"));
		gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gridData.horizontalSpan = 2;
		okButton.setLayoutData(gridData);

		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});
		shell.setDefaultButton(okButton);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}

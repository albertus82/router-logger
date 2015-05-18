package it.albertus.router;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenCharacterStyle;
import com.googlecode.lanterna.screen.ScreenWriter;
import com.googlecode.lanterna.terminal.TerminalPosition;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import com.googlecode.lanterna.terminal.swing.TerminalAppearance;
import com.googlecode.lanterna.terminal.swing.TerminalPalette;

public class LoggerTerminal {

	private static final int COLUMNS = 80;
	private static final int ROWS = 25;

	private static final LoggerTerminal terminal = new LoggerTerminal();

	public static LoggerTerminal getInstance() {
		return terminal;
	}

	private final Screen screen;
	private final ScreenWriter writer;
	private int col = 0;
	private int row = 0;

	private LoggerTerminal() {
		final Font font = new Font("Consolas", Font.BOLD, 14);
		TerminalAppearance ta = new TerminalAppearance(font, font, TerminalPalette.WINDOWS_XP_COMMAND_PROMPT, true);
		SwingTerminal terminal = TerminalFacade.createSwingTerminal(ta, COLUMNS, ROWS);
		screen = TerminalFacade.createScreen(terminal);
		writer = new ScreenWriter(screen);
		writer.setForegroundColor(com.googlecode.lanterna.terminal.Terminal.Color.GREEN);
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public Screen getScreen() {
		return screen;
	}

	public ScreenWriter getWriter() {
		return writer;
	}

	private void putstr(String str) {
		if (str != null) {
			List<String> stringheDaStampare = new ArrayList<String>();
			int count = col;
			StringBuilder curStr = new StringBuilder();
			for (char c : str.toCharArray()) {
				if ((count != 0 && count % COLUMNS == 0) || c == '\n') {
					count = 0;
					stringheDaStampare.add(curStr.toString());
					curStr = new StringBuilder();
				}
				if (c == '\r') {
					count = 0;
					col = 0;
				}
				else if (c == '\n') {

				}
				else if (c == '\b') {
					if (count > 0) {
						count--;
						col--;
					}
				}
				else {
					curStr.append(c);
					count++;
				}

			}
			stringheDaStampare.add(curStr.toString());

			for (String s : stringheDaStampare) {
				writer.drawString(col, row, s, ScreenCharacterStyle.Bold);
				col = 0;
				row++;
			}
			row--;
			col = stringheDaStampare.get(stringheDaStampare.size() - 1).length();
			screen.setCursorPosition(new TerminalPosition(col, row));
			screen.refresh();
		}
	}

	public void print(String str) {
		putstr(str);
		screen.refresh();
	}

	public void println(String str) {
		putstr(str);
		row++;
		col = 0;
		screen.setCursorPosition(new TerminalPosition(col, row));
		screen.refresh();
	}

	public void println() {
		row++;
		col = 0;
		screen.setCursorPosition(new TerminalPosition(col, row));
		screen.refresh();
	}

}

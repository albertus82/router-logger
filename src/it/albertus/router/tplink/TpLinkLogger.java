package it.albertus.router.tplink;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TpLinkLogger extends RouterLogger {
	
	private static final DateFormat dateFormatLog = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyymmdd");


	public static void main(String... args) throws Exception {
		RouterLogger logger = new TpLinkLogger();

		while (true) {
			logger.login();
			while (true) {
				try {
					logger.info();
					logger.save();
					Thread.sleep(1000);
				}
				catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			logger.logout();
		}
	}

	public TpLinkLogger() throws IOException {
		BufferedInputStream reader = new BufferedInputStream(TpLinkLogger.class.getResourceAsStream("/logger.cfg"));
		configuration.load(reader);
		reader.close();
	}

	@Override
	protected void login() {
		try {
			telnet.connect(configuration.getProperty("router.address"), Integer.parseInt(configuration.getProperty("router.port")));
			telnet.setConnectTimeout(30000);
			telnet.setSoTimeout(30000);
			in = telnet.getInputStream();
			out = telnet.getOutputStream();
			read(':', true);
			write(configuration.getProperty("router.username"));
			read(':', true);
			write(configuration.getProperty("router.password"));
			read('#', true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void info() throws IOException {
		write("adsl show info");
		read('{', true);
		StringReader sr = new StringReader(read('}', false));
		info.load(sr);
		sr.close();
	}

	@Override
	protected void logout() {
		try {
			telnet.disconnect();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void save() {
		Date sysdate = new Date();
		FileWriter output;
		try {
			output = new FileWriter(new File("c:/users/alberto/desktop/router_"+dateFormatFileName.format(sysdate)+".log"), true);
			output.append(dateFormatLog.format(sysdate)).append(' ').append( info.toString()).append("\r\n");
			output.flush();
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

}
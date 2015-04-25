package it.albertus.router.tplink;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;


public class Main {

	public static void main(String... args) throws SocketException, IOException, InvalidTelnetOptionException, InterruptedException {
		TelnetClient telnetClient = new TelnetClient();
		telnetClient.connect("192.168.1.1", 23);
		telnetClient.setConnectTimeout(60000);
		telnetClient.setSoTimeout(60000);
		
		InputStream is = telnetClient.getInputStream();
		
		char bt;
		while ((bt = (char)is.read()) != -1 ) {
			if ( bt == ':')
				break;
		}

		telnetClient.getOutputStream().write('a');
		telnetClient.getOutputStream().write('d');
		telnetClient.getOutputStream().write('m');
		telnetClient.getOutputStream().write('i');
		telnetClient.getOutputStream().write('n');
		telnetClient.getOutputStream().write('\n');
		telnetClient.getOutputStream().flush();
		
		while ((bt = (char)is.read()) != -1 ) {
			if (bt == ':')
				break;
		}
		
		telnetClient.getOutputStream().write('a');
		telnetClient.getOutputStream().write('d');
		telnetClient.getOutputStream().write('m');
		telnetClient.getOutputStream().write('i');
		telnetClient.getOutputStream().write('n');
		telnetClient.getOutputStream().flush();
		Thread.sleep(70);
		telnetClient.getOutputStream().write('\n');
		telnetClient.getOutputStream().flush();

		while ((bt = (char)is.read()) != -1 ) {
			System.out.print(bt);
			if (bt == '#')
				break;
		}

		telnetClient.disconnect();
	}
}

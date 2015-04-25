package it.albertus.router.tplink;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.net.telnet.TelnetClient;

@Deprecated
public class Main {

	@Deprecated
	public static void main(String... args) throws Exception {
//		int ok = 0;
//		int err = 0;
		
//		for (int i = 0; i < 50; i++) { 
			
//			try {
//				Thread.sleep(100);
				
				
		TelnetClient telnetClient = new TelnetClient();
		telnetClient.connect("192.168.1.1", 23);
		telnetClient.setConnectTimeout(30000);
		telnetClient.setSoTimeout(30000);
		
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
//		Thread.sleep(40);
		telnetClient.getOutputStream().write('\n');
		telnetClient.getOutputStream().flush();

		while ((bt = (char)is.read()) != -1 ) {
			System.out.print(bt);
			if (bt == '#')
				break;
		}

		telnetClient.getOutputStream().write('a');
		telnetClient.getOutputStream().write('d');
		telnetClient.getOutputStream().write('s');
		telnetClient.getOutputStream().write('l');
		telnetClient.getOutputStream().write(' ');
		telnetClient.getOutputStream().write('s');
		telnetClient.getOutputStream().write('h');
		telnetClient.getOutputStream().write('o');
		telnetClient.getOutputStream().write('w');
		telnetClient.getOutputStream().write(' ');
		telnetClient.getOutputStream().write('i');
		telnetClient.getOutputStream().write('n');
		telnetClient.getOutputStream().write('f');
		telnetClient.getOutputStream().write('o');
		telnetClient.getOutputStream().write('\n');
		telnetClient.getOutputStream().flush();

		while ((bt = (char)is.read()) != -1 ) {
			if (bt == '{') {
				break;
			}
		}
		
		
		StringBuilder props = new StringBuilder();
		while ((bt = (char)is.read()) != -1 ) {
			if (bt == '}') {
				break;
			}
			props.append(bt);
		}
		StringReader sr = new StringReader(props.toString().trim());
		Properties p = new Properties();
		p.load(sr);
		
		System.out.println(p);
		
		while ((bt = (char)is.read()) != -1 ) {
			if (bt == '#') {
				break;
			}
		}
		
		telnetClient.disconnect();
//		ok++;
//			}catch(Exception e) {
//				err++;
////				continue;
//			}
//			finally {
//				System.out.println("OK: " + ok + " - ERR: " + err);
//			}
		}
//		System.out.println("OK: " + ok + " - ERR: " + err);
		
//	}
}

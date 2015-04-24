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
//			System.out.print(bt);
			if ( bt == ':')
				break;
		}


//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(telnetClient.getOutputStream()));
//		writer.write("admin\r");
//		writer.newLine();
//		writer.flush();
		telnetClient.getOutputStream().write('a');
		telnetClient.getOutputStream().write('d');
		telnetClient.getOutputStream().write('m');
		telnetClient.getOutputStream().write('i');
		telnetClient.getOutputStream().write('n');
		telnetClient.getOutputStream().write('\n');
		telnetClient.getOutputStream().flush();
//		Thread.sleep(1000);
		int i = 0;
		while ((bt = (char)is.read()) != -1 ) {
//			System.out.print(bt);
			
//			System.out.println("index: " + i + " - available:" + is.available());
			if (bt == ':')
				break;
		}
//		System.out.println(is.read());
//		System.out.println(is.read());
//		System.out.println(is.read());
//		writer = new OutputStreamWriter(telnetClient.getOutputStream());
		telnetClient.getOutputStream().write('a');
		telnetClient.getOutputStream().flush();
		telnetClient.getOutputStream().write('d');
		telnetClient.getOutputStream().flush();
		telnetClient.getOutputStream().write('m');
		telnetClient.getOutputStream().flush();
		telnetClient.getOutputStream().write('i');
		telnetClient.getOutputStream().flush();
		telnetClient.getOutputStream().write('n');
		telnetClient.getOutputStream().flush();
		telnetClient.getOutputStream().write('\n');
		Thread.sleep(100);
		telnetClient.getOutputStream().flush();
		
//		writer.write("admin");
//		writer.newLine();
//		writer.flush();

		while ((bt = (char)is.read()) != -1 ) {
			System.out.print(bt);
//			if ( (char)bt == ':')
//				break;
		}
		
		
//		InputStreamReader isr = new InputStreamReader(telnetClient.getInputStream());
//		BufferedReader br = new BufferedReader(isr);
////		System.out.println(br.readLine());
////		  IOUtils.readWrite(telnetClient.getInputStream(),telnetClient.getOutputStream(),System.in,System.out);
//		PrintWriter outPrint=new PrintWriter(telnetClient.getOutputStream(),true);
//		System.out.println(br.readLine());System.out.println(br.readLine());
////		outPrint.write("admin\n");
////		outPrint.write("admin\n");
//		System.out.println(br.readLine());
//		outPrint.

		telnetClient.disconnect();
	}
}

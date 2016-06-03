import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {

	public static void main(String[] args) {
		new TestOauth2().sendMessage();
	}

	private static String TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
	//	private JavaMailSender sender;

	//	private String accessToken = "fixme";
		private long tokenExpires = 1458168133864L;

	public void sendMessage() {
		if (System.currentTimeMillis() > tokenExpires) {
			try {
				final String request = "client_id=" + URLEncoder.encode(oauthClientId, "UTF-8") + "&client_secret=" + URLEncoder.encode(oauthSecret, "UTF-8") + "&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8") + "&grant_type=refresh_token";
				final HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				final PrintWriter out = new PrintWriter(conn.getOutputStream());
				out.print(request); // note: println causes error
				out.close();
				conn.connect();
				BufferedReader resp = null;
				final Map<String, String> rsp = new HashMap<String, String>();
				try {
					String line;
					resp = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					while ((line = resp.readLine()) != null) {
						if (!line.contains("{") && !line.contains("}")) {
							String key = line.substring(line.indexOf('"') + 1, line.indexOf("\":")).trim();
							String value = line.substring(line.indexOf("\":") + 2).trim();
							if (value.startsWith("\"")) {
								value = value.substring(1);
							}
							if (value.endsWith(",")) {
								value = value.substring(0, value.length() - 1);
							}
							if (value.endsWith("\"")) {
								value = value.substring(0, value.length() - 1);
							}
							rsp.put(key, value);
						}
					}
				}
				catch (final IOException ioe) {
					ioe.printStackTrace();
				}
				finally {
					if (resp != null) {
						try {
							resp.close();
						}
						catch (Exception e) {/* Ignore */}
					}
				}
				System.out.println(rsp);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Now send mail like normal
	}

}

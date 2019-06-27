package testMail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class TestMail {

	public static void main(String[] args) {
		final long startTime = System.currentTimeMillis();
		// TODO Auto-generated method stub
		try {
			InputStream flux = new FileInputStream("mail.txt");
			InputStreamReader lecture = new InputStreamReader(flux);
			BufferedReader buff = new BufferedReader(lecture);
			String ligne;
			String mot1 = null;

			while ((ligne = buff.readLine()) != null) {
				System.out.println("===========\n" + ligne);
				StringTokenizer st = new StringTokenizer(ligne, "@");
				String domain = null;
				while (st.hasMoreTokens()) {
					System.out.println(st.nextToken());
					domain = st.nextToken();
				}
				System.out.println("===========\n" + domain);
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "nslookup -type=mx " + domain);
				builder.redirectErrorStream(true);
				Process p = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				String[] mot;

				line = r.readLine();

				try {
					while (line != null) {

						// System.out.println("===========\n" + line);
						mot = line.split("\\s");

						if (mot[0].equals(domain)) {

							Socket skt = new Socket(mot[8], 25);
							skt.setSoTimeout(10 * 1000);
							BufferedReader bufferedReader = new BufferedReader(
									new InputStreamReader(skt.getInputStream()));
							BufferedWriter bufferedWriter = new BufferedWriter(
									new OutputStreamWriter(skt.getOutputStream()));

							send(bufferedWriter, "HELO " + domain);

							send(bufferedWriter, "MAIL FROM: " + ligne);

							send(bufferedWriter, "RCPT TO: " + ligne);
							receive(bufferedReader);
							// status = receive(bufferedReader);
							System.out.println(mot[8]);
							skt.close();

							break;
						}
						line = r.readLine();
					}
				} catch (SocketException exception) {
					System.out.println("Echec connexion " + exception);
				} catch (java.net.SocketTimeoutException exception) {
					// Output unexpected IOExceptions.
					System.out.println("Timeout " + exception);
				} catch (java.net.UnknownHostException exception) {
					// Output unexpected IOExceptions.
					System.out.println("Server non reconnu " + exception);
				}

			}
			buff.close();
			// System.out.println(mot1);
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public static void send(BufferedWriter wr, String text) throws IOException {
		wr.write(text + "\r\n");
		wr.flush();
	}

	public static void receive(BufferedReader in) throws IOException {
		String line = null;

		while (true) {
			line = in.readLine();

			if (line == null) {
				break;
			}
			System.out.println(line);
		}
	}

}

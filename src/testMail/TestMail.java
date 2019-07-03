package testMail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class TestMail {

	public static void main(String[] args) throws IOException {
		// creation du flux de sorti
		String path = "thaicreate.txt";
		FileWriter writer;
		File file = new File(path);
		writer = new FileWriter(file, false);

		// TODO Auto-generated method stub
		try {
			// initialisation du flux d'entrée
			InputStream flux = new FileInputStream("mail.txt");
			InputStreamReader lecture = new InputStreamReader(flux);
			BufferedReader buff = new BufferedReader(lecture);
			String ligne;

			while ((ligne = buff.readLine()) != null) {
				System.out.println("===========\n" + ligne);
				// ecriture du mail
				writer.write(ligne + "||");
				writer.flush();
				// séparation du nom de domaine et du user
				StringTokenizer st = new StringTokenizer(ligne, "@");
				String domain = null;
				// récupération du domain
				while (st.hasMoreTokens()) {
					System.out.println(st.nextToken());
					domain = st.nextToken();
				}
				System.out.println("===========\n" + domain);

				// exécution de la commande nslookup sur le domain
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "nslookup -type=mx " + domain);
				builder.redirectErrorStream(true);
				Process p = builder.start();
				// flux de lecture sur le process builder
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				String[] mot;

				line = r.readLine();

				try {
					while (line != null) {

						// System.out.println("===========\n" + line);
						mot = line.split("\\s");

						if (mot[0].equals(domain)) {
							// ping vers le server mail associer au domaine
							Socket skt = new Socket(mot[8], 25);
							skt.setSoTimeout(10 * 1000); // durée d'attende
							BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(skt.getInputStream()));
							BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
							// exécution des commande d'intérrogation 
							send(bufferedWriter, "HELO " + domain);
							send(bufferedWriter, "MAIL FROM: " + ligne);
							send(bufferedWriter, "RCPT TO: " + ligne);
							receive(bufferedReader, file, writer);
							send(bufferedWriter, "quit");
							receive(bufferedReader, file, writer);//affichage et ecriture sur fichier des reponse du serveur
							
							writer.write("\r\n");
							writer.flush();
							skt.close();

							System.out.println(mot[8]);
							break;
						}
						line = r.readLine();
					}
				} catch (SocketException exception) {
					System.out.println("Echec connexion " + exception);
					writer.write("Echec connexion " + exception);
					writer.write("\r\n");
					writer.flush();
				} catch (java.net.SocketTimeoutException exception) {
					// Output unexpected IOExceptions.
					System.out.println("Timeout " + exception);
					writer.write("Timeout " + exception);
					writer.write("\r\n");
					writer.flush();
				} catch (java.net.UnknownHostException exception) {
					// Output unexpected IOExceptions.
					System.out.println("Server non reconnu " + exception);
					writer.write("Server non reconnu " + exception);
					writer.write("\r\n");
					writer.flush();
				}
			}			
			writer.close();
			buff.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public static void send(BufferedWriter wr, String text) throws IOException {
		wr.write(text + "\r\n");
		wr.flush();
	}

	public static void receive(BufferedReader in, File file, FileWriter writer) throws IOException {
		String line=null;
		
		while (true) {
			line = in.readLine();
			if(line == null) {
				break;
			}
			try {
				writer.write(line);
				writer.write("||");
				writer.flush();
				System.out.println("Write success!");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(line);
			line = in.readLine();
		}
	}

}

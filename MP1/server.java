import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

class server {
	public static void main(String argv[]) throws Exception {
		String clientSentence;
		int portNumber = 6789;

		// creating welcome socket (the door to knock) -- only used to accept
		// connections
		ServerSocket welcomeSocket = new ServerSocket(portNumber);
		System.out.println("Initialising Server with port: " + portNumber);
		// HashMap to convert month to integer
		HashMap<String, Integer> months = initialize();

		while (true) {
			// creating connection socket -- one per TCP connection
			Socket connectionSocket = welcomeSocket.accept();

			// creating input and output streams (to receive from/send to client socket)
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			// reads request from client
			StringBuilder requestBuilder = new StringBuilder();
			String line = "";
			while ((line = inFromClient.readLine()) != null && !line.equals(""))
				requestBuilder.append(line + "\r\n");
			clientSentence = requestBuilder.toString();

			String[] sentenceArraySpaces = clientSentence.split(" |\r\n");

			boolean clientSentenceGet = clientSentence.startsWith("GET");

			boolean clientSentenceHTTP = sentenceArraySpaces[2].equals("HTTP/1.1");

			StringBuilder answerBuilder = new StringBuilder();

			File file = new File(sentenceArraySpaces[1].substring(1));

			String[] firstSentence = sentenceArraySpaces[0].split(" ");

			// checks for errors
			if (firstSentence.length == 0 || firstSentence.length > 3 || !clientSentenceGet && !clientSentenceHTTP) {
				answerBuilder.append("HTTP/1.1 400 Bad Request");

			} else if (!clientSentenceHTTP) {
				answerBuilder.append("HTTP/1.1 505 HTTP version not implemented");

			} else if (!clientSentenceGet) {
				answerBuilder.append("HTTP/1.1 501 method unimplemented");

			} else if (!file.exists()) {
				answerBuilder.append("HTTP/1.1 404 not found");

				// create an OK response
			} else {

				boolean isOK = false;
				boolean containsModified = false;
				int index = 0;
				for (int i = 0; i < sentenceArraySpaces.length && !containsModified; i++) {
					containsModified = sentenceArraySpaces[i].equals("If-Modified-Since:");
					if (containsModified)
						index = i;
				}

				// checks if request has If-Modified-Since field
				if (!containsModified) {
					answerBuilder.append("HTTP/1.1 200 OK\r\n");
					isOK = true;
				} else {
					try {
						// reads date of request
						long fileTime = file.lastModified();
						int year = Integer.parseInt(sentenceArraySpaces[index + 4]);
						int month = months.get(sentenceArraySpaces[index + 3]);
						int day = Integer.parseInt(sentenceArraySpaces[index + 2]);
						int hour = Integer.parseInt(sentenceArraySpaces[index + 5].substring(0, 2)) + 1;
						int minute = Integer.parseInt(sentenceArraySpaces[index + 5].substring(3, 5));
						int second = Integer.parseInt(sentenceArraySpaces[index + 5].substring(6));
						Calendar cal = Calendar.getInstance();
						cal.clear();
						cal.set(year, month, day, hour, minute, second);
						long wantedTime = cal.getTimeInMillis();

						// checks if file was modified after wantedTime
						if (fileTime > wantedTime) {
							answerBuilder.append("HTTP/1.1 200 OK\r\n");
							isOK = true;
						} else
							answerBuilder.append("HTTP/1.1 304 Not Modified");
					} catch (Exception e) {
						answerBuilder.append("HTTP/1.1 400 Bad Request");
					}
				}

				// adds Set-Cookie field
				if (isOK) {
					answerBuilder.append("Set-Cookie: ");
					// cookie being sent
					answerBuilder.append("12345\r\n\r\n");
					Scanner sc = new Scanner(file);
					while (sc.hasNextLine()) {
						answerBuilder.append(sc.nextLine() + "\r\n");
					}
					answerBuilder.append("\r\n\r\n");
					sc.close();
				}
			}

			// sends answer
			outToClient.writeBytes(answerBuilder.toString());

			// closing the I/O streams and the socket
			connectionSocket.close();
			inFromClient.close();
			outToClient.close();

		}
	}

	// initializing hashmap
	public static HashMap<String, Integer> initialize() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("Jan", 0);
		map.put("Feb", 1);
		map.put("Mar", 2);
		map.put("Apr", 3);
		map.put("May", 4);
		map.put("Jun", 5);
		map.put("Jul", 6);
		map.put("Aug", 7);
		map.put("Sep", 8);
		map.put("Oct", 9);
		map.put("Nov", 10);
		map.put("Dec", 11);
		return map;
	}
}

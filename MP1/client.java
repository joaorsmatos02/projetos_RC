import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class client {
	public static void main(String argv[]) throws Exception {
		String sentence;
		String modifiedSentence;
		boolean switcher = true;
		int serverPort = 6789;

		// creating client socket
		Socket clientSocket = new Socket("localhost", serverPort);

		if (clientSocket.isConnected())
			System.out.println("Connected to localhost:" + serverPort);

		// creating streams for stdin, to output characters to the server, and to
		// receive from the socket
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		// can read sentence from stdin or as an argument
		if (argv.length == 0) {
			System.out.println("Please Type:");
			sentence = inFromUser.readLine();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(argv[0]);
			for (int i = 1; i < argv.length; i++)
				sb.append(" " + argv[i]);
			sentence = sb.toString();
		}
		boolean moreThanOneWord = sentence.contains(" ");
		StringBuilder sb = new StringBuilder();

		// prepare request
		if (!moreThanOneWord) {
			sb.append("GET /" + sentence + " HTTP/1.1");
			File cookie = new File("cookie.txt");
			if (!cookie.createNewFile()) {
				switcher = false;
				sb.append("\r\nCookie: ");
				Scanner reader = new Scanner(cookie);
				while (reader.hasNextLine())
					sb.append(reader.nextLine());
				reader.close();
			}
		} else {
			String[] words = sentence.split(" ");
			sb.append("GET /" + words[0] + " HTTP/1.1" + "\r\n" + "If-Modified-Since: ");
			for (int i = 1; i < words.length; i++)
				sb.append(words[i] + " ");
		}
		sb.append("\r\n\r\n");

		// Send request
		outToServer.writeBytes(sb.toString());

		// printing reply received from the server
		StringBuilder recieved = new StringBuilder();
		String line = "";
		while ((line = inFromServer.readLine()) != null)
			recieved.append(line + "\r\n");
		modifiedSentence = recieved.toString();

		// saving response
		if (!moreThanOneWord) {
			File response = new File("response.txt");
			response.createNewFile();
			PrintWriter writer = new PrintWriter("response.txt");
			writer.write(modifiedSentence);
			writer.close();
			if (switcher) {
				writer = new PrintWriter("cookie.txt");
				String cookieString = modifiedSentence.substring(modifiedSentence.indexOf("Cookie: ") + 8);
				writer.write(cookieString.substring(0, cookieString.indexOf("\r\n")));
			}
			writer.close();
		}
		System.out.println("FROM SERVER: " + modifiedSentence);

		// closing I/O streams and socket
		inFromUser.close();
		inFromServer.close();
		outToServer.close();
		clientSocket.close();

	}
}
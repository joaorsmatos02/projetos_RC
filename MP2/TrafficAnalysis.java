import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class TrafficAnalysis {
	public static void main(String argv[]) throws Exception {

		// Reading CSV
		File file = new File("MP2_Grupo14.csv");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String line = br.readLine(); // skip first line
		int ipv4 = 0; // number of packets using IPv4
		int ipv6 = 0; // and IPv6
		int maxSize = Integer.MIN_VALUE; // max packet size
		int minSize = Integer.MAX_VALUE; // min packet size
		int rst = 0; // number of failed attempts
		long size = 0; // total size (to calculate average)
		double packets = 0; // total number of packets (to calculate average)
		Set<String> ip = new HashSet<String>(); // set of unique IPv4 hosts
		Set<String> tcp = new HashSet<String>(); // set of unique tcp ports

		while ((line = br.readLine()) != null) {
			String[] contents = line.split("\",\"|\"");
			if (contents[3].contains(":") && contents[4].contains(":")) {
				ipv6++;
			} else {
				ipv4++;
				ip.add(contents[4]);
			}
			if (contents[7].equals("TCP"))
				tcp.add(contents[5]);
			if (contents.length > 9 && Integer.parseInt(contents[9].substring(2), 16) == 4) // check if rst flag is set
				rst++;
			int thisSize = Integer.parseInt(contents[8]);
			size += thisSize;
			maxSize = Math.max(maxSize, thisSize);
			minSize = Math.min(minSize, thisSize);
			packets = Integer.parseInt(contents[1]);
		}

		double avg = size / packets;
		DecimalFormat df = new DecimalFormat("#.##");
		String average = df.format(avg);
		average = average.replace(',', '.');

		System.out.println(ipv4 + " pacotes possuem emissor e recetor IPv4.");
		System.out.println("Os restantes " + ipv6 + " pacotes possuem emissor e recetor IPv6.");
		System.out.println(ip.size() + " hosts IPv4 �nicos receberam pacotes.");
		System.out.println("O trace contem " + tcp.size() + " portos TCP origem �nicos");
		System.out.println("Os tamanhos m�ximo e m�nimo de pacote s�o, respetivamente " + maxSize + " e " + minSize
				+ ", sendo o tamanho m�dio " + average + ".");
		System.out.println("Falharam " + rst + " tentativas de comunica��o TCP.");
		br.close();

		// writing to csv
		File csv = new File("Respostas_Grupo14.csv");
		csv.createNewFile();
		PrintWriter pw = new PrintWriter(csv);
		pw.write("Q1:" + ipv4 + "\r\nQ2:" + ipv6 + "\r\nQ3:" + ip.size() + "\r\nQ4:" + tcp.size() + "\r\nQ5:" + average
				+ "," + maxSize + "," + minSize + "\r\nQ6:" + rst);
		pw.close();
	}
}

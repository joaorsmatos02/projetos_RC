import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class TrafficAnalysis {
	public static void main(String[] args) throws IOException {

		File file = new File("MP3_Grupo14_B.csv");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		boolean SYNFlood = false;
		boolean UDPFlood = false;
		boolean PoD = false;

		HashMap<String, String[]> lastFrom = new HashMap<>(); // stores the last packet sent from and to the given ips

		HashMap<String, ArrayList<Double>> syn = new HashMap<>(); // stores times of unacknowledged syns
		HashMap<String, ArrayList<Double>> udp = new HashMap<>(); // stores times of udp packets
		HashMap<String, ArrayList<Double>> ping = new HashMap<>(); // stores times of pings

		String line = br.readLine(); // skip first line

		long UDPsize = 0;
		long UDPcount = 0;
		long ICMPsize = 0;
		long ICMPcount = 0;

		while ((line = br.readLine()) != null) { // measure average udp and ping sizes
			String[] contents = line.split("\",\"|\"");
			if (contents[7].equals("DNS")) {
				UDPcount++;
				UDPsize += Long.parseLong(contents[9]);
			} else if (contents[7].startsWith("ICMP")) {
				ICMPcount++;
				ICMPsize += Long.parseLong(contents[9]);
			}
		}

		double UDPavg = UDPsize / UDPcount;
		double ICMPavg = ICMPsize / ICMPcount;

		br.close(); // restart reading file
		fr.close();
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		line = br.readLine();

		String attackerSYN = ""; // ip of attacker (SYN)
		String attackedSYN = "";// attacked ip (SYN)

		String attackerUDP = ""; // ip of attacker (UDP)
		String attackedUDP = "";// attacked ip (UDP)

		String attackerPOD = ""; // ip of attacker (POD)
		String attackedPOD = "";// attacked ip (POD)

		while ((line = br.readLine()) != null) {
			String[] contents = line.split("\",\"|\""); // contents of trace line
			StringBuilder ip = new StringBuilder();
			ip.append(contents[3] + " -> " + contents[4]); // source and destination ips

			// SYNFlood
			if (!SYNFlood) {
				if (contents[7].equals("TCP")) {
					ArrayList<Double> newList = syn.getOrDefault(ip.toString(), new ArrayList<Double>());
					if (contents[10].equals("0x002")) {
						newList.add(Double.parseDouble(contents[2])); // if a new syn is made between two ips, the time
																		// is added to the list
						syn.put(ip.toString(), newList);
						if (syn.get(ip.toString()).size() >= 10) // if 10 or more syns are waiting for acknowledgement,
																	// it is considered a syn flood
							SYNFlood = true;
					} else if (contents[10].equals("0x010")) // when an ack is recieved, a syn is removed from the list
						if (newList.size() != 0)
							newList.remove(0);
				} else if (contents[7].equals("RST") && lastFrom.get(ip.toString())[10].equals("0x002"))
					// if an rst is sent after a syn, its also considered as a syn flood
					SYNFlood = true;
				if (SYNFlood) { // if a syn flood is detected, the ip adresses of the attacker and attacked are
								// stored
					attackerSYN = contents[3];
					attackedSYN = contents[4];
				}
			}

			// UDPFlood
			if (contents[7].equals("DNS") && !UDPFlood) {
				ArrayList<Double> list = udp.getOrDefault(ip.toString(), new ArrayList<Double>());
				list.add(Double.parseDouble(contents[2]));
				udp.put(ip.toString(), list);
				if ((list.size() > 10 && Double.parseDouble(contents[2]) - list.get(list.size() - 10) < 5)
						|| Integer.parseInt(contents[9]) < UDPavg / 5)
					UDPFlood = true;
				// if 10 or more pings are sent within 5 seconds or if a udp packet is 5 times
				// smaller than the average it is considered udp flood
				if (UDPFlood) { // if a udp flood is detected, the ip adresses of the attacker and attacked
								// are stored
					attackerUDP = contents[3];
					attackedUDP = contents[4];
				}
			}

			// PoD
			if (contents[7].startsWith("ICMP") && !PoD) {
				ArrayList<Double> list = ping.getOrDefault(ip.toString(), new ArrayList<Double>());
				list.add(Double.parseDouble(contents[2]));
				ping.put(ip.toString(), list);
				if ((list.size() >= 15 && Double.parseDouble(contents[2]) - list.get(list.size() - 15) < 3)
						|| Integer.parseInt(contents[9]) > ICMPavg * 15)
					PoD = true;
				// if 15 or more pings are sent within 3 seconds or if a ping is 15 times bigger
				// than the average it is considered ping of death
				if (PoD) { // if a ping of death is detected, the ip adresses of the attacker and attacked
							// are stored
					attackerPOD = contents[3];
					attackedPOD = contents[4];
				}
			}

			///////

			if (contents.length > 10 && SYNFlood && contents[3].equals(attackerSYN) && contents[4].equals(attackedSYN)
					&& contents[10].equals("0x002")) { // add all times of syns from attacker to the list
				ArrayList<Double> newList = syn.getOrDefault(ip.toString(), new ArrayList<Double>());
				newList.add(Double.parseDouble(contents[2]));
				syn.put(ip.toString(), newList);
			}
			if (UDPFlood && contents[3].equals(attackerUDP) && contents[4].equals(attackedUDP)) {
				// add all times of udp packets from attacker to the list// add all times of
				// pings from attacker to the list
				ArrayList<Double> list = udp.getOrDefault(ip.toString(), new ArrayList<Double>());
				list.add(Double.parseDouble(contents[2]));
				udp.put(ip.toString(), list);
			}
			if (PoD && contents[3].equals(attackerPOD) && contents[4].equals(attackedPOD)) {
				// add all times of pings from attacker to the list// add all times of pings
				// from attacker to the list
				ArrayList<Double> list = ping.getOrDefault(ip.toString(), new ArrayList<Double>());
				list.add(Double.parseDouble(contents[2]));
				ping.put(ip.toString(), list);
			}
			lastFrom.put(ip.toString(), contents);
		}

		br.close();
		fr.close();

		DecimalFormat df = new DecimalFormat("#.#");
		DecimalFormat df1 = new DecimalFormat("#.##");
		DecimalFormat df2 = new DecimalFormat("#.######");

		int SYNPackets = 0;
		int SYNRythms = 1;
		double SYNDuration = 0;

		if (SYNFlood) { // processing of SYN_FLOOD data
			File synData = new File("results_SYN.txt");
			FileWriter synDataWriter = new FileWriter(synData);
			StringBuilder ip = new StringBuilder();
			ip.append(attackerSYN + " -> " + attackedSYN); // source and destination ips
			ArrayList<Double> times = syn.get(ip.toString());
			SYNPackets = times.size();
			SYNDuration = Double
					.parseDouble(df.format((times.get(times.size() - 1) - times.get(0)) / 60).replace(',', '.'));
			for (int i = 1; i < times.size(); i++) {
				if (times.get(i) > times.get(i - 1) + 5)
					SYNRythms++;
				synDataWriter.append(df2.format(times.get(i)).replace(',', '.') + " "
						+ df1.format(times.get(i) - times.get(i - 1)).replace(',', '.') + "\r\n");
			}
			synDataWriter.close();
			// if there is a gap of 5+ seconds between packets its considered a different
			// rythm
			if (SYNRythms > times.size() / 2) { // if the packets are too spread out, the attack flag is removed
				SYNFlood = false;
				synData.delete();
			} else {
				System.out.println("This trace contains a SYN_FLOOD attack");
				System.out.println("The attacked ip is " + attackedSYN + " and the attacker is " + attackerSYN);
				System.out.println("The attacker sent " + SYNPackets + " packets to the victim");
				System.out.println("The attack had " + SYNRythms + " rythm(s)");
				System.out.println("The attack lasted for " + SYNDuration + " minutes");
				if (UDPFlood || PoD)
					System.out.println();
			}
		}

		int UDPPackets = 0;
		int UDPRythms = 1;
		double UDPDuration = 0;

		if (UDPFlood) { // processing of UDP_FLOOD data
			File udpData = new File("results_UDP.txt");
			FileWriter udpDataWriter = new FileWriter(udpData);
			StringBuilder ip = new StringBuilder();
			ip.append(attackerUDP + " -> " + attackedUDP); // source and destination ips
			ArrayList<Double> times = udp.get(ip.toString());
			UDPPackets = times.size();
			UDPDuration = Double
					.parseDouble(df.format((times.get(times.size() - 1) - times.get(0)) / 60).replace(',', '.'));
			for (int i = 1; i < times.size(); i++) {
				if (times.get(i) > times.get(i - 1) + 5)
					UDPRythms++;
				udpDataWriter.append(df2.format(times.get(i)).replace(',', '.') + " "
						+ df1.format(times.get(i) - times.get(i - 1)).replace(',', '.') + "\r\n");
			}
			udpDataWriter.close();
			// if there is a gap of 5+ seconds between packets its considered a different
			// rythm
			if (UDPRythms > times.size() / 2) { // if the packets are too spread out, the attack flag is removed
				UDPFlood = false;
				udpData.delete();
			} else {
				System.out.println("This trace contains a UDP_FLOOD attack");
				System.out.println("The attacked ip is " + attackedUDP + " and the attacker is " + attackerUDP);
				System.out.println("The attacker sent " + UDPPackets + " packets to the victim");
				System.out.println("The attack had " + UDPRythms + " rythm(s)");
				System.out.println("The attack lasted for " + UDPDuration + " minutes");
				if (PoD)
					System.out.println();
			}
		}

		int PODPackets = 0;
		int PODRythms = 1;
		double PODDuration = 0;

		if (PoD) { // processing of PoD data
			File podData = new File("results_POD.txt");
			FileWriter podDataWriter = new FileWriter(podData);
			StringBuilder ip = new StringBuilder();
			ip.append(attackerPOD + " -> " + attackedPOD); // source and destination ips
			ArrayList<Double> times = ping.get(ip.toString());
			PODPackets = times.size();
			PODDuration = Double
					.parseDouble(df.format((times.get(times.size() - 1) - times.get(0)) / 60).replace(',', '.'));
			for (int i = 1; i < times.size(); i++) {
				if (times.get(i) > times.get(i - 1) + 5)
					PODRythms++;
				podDataWriter.append(df2.format(times.get(i)).replace(',', '.') + " "
						+ df1.format(times.get(i) - times.get(i - 1)).replace(',', '.') + "\r\n");
			}
			podDataWriter.close();
			// if there is a gap of 5+ seconds between packets its considered a different
			// rythm
			if (PODRythms > times.size() / 2) { // if the packets are too spread out, the attack flag is removed
				PoD = false;
				podData.delete();
			} else {
				System.out.println("This trace contains a PoD attack");
				System.out.println("The attacked ip is " + attackedPOD + " and the attacker is " + attackerPOD);
				System.out.println("The attacker sent " + PODPackets + " packets to the victim");
				System.out.println("The attack had " + PODRythms + " rythm(s)");
				System.out.println("The attack lasted for " + PODDuration + " minutes");
			}
		}

		// writing answers
		if (PoD || SYNFlood || UDPFlood) {
			File answers = new File("Respostas_Grupo14.txt");
			FileWriter answerWriter = new FileWriter(answers);

			answerWriter.append("Q1: " + file.getName().charAt(file.getName().length() - 5));
			answerWriter.append("\r\nQ2: ");
			if (PoD) {
				answerWriter.append("PoD");
				if (SYNFlood || UDPFlood)
					answerWriter.append(", ");
			}
			if (SYNFlood) {
				answerWriter.append("SYN_FLOOD");
				if (UDPFlood)
					answerWriter.append(", ");
			}
			if (UDPFlood)
				answerWriter.append("UDP_FLOOD");
			answerWriter.append("\r\nQ3: ");
			if (PoD) {
				answerWriter.append("Victim: " + attackedPOD + " - Attacker: " + attackerPOD);
				if (SYNFlood || UDPFlood)
					answerWriter.append(", ");
			}
			if (SYNFlood) {
				answerWriter.append("Victim: " + attackedSYN + " - Attacker: " + attackerSYN);
				if (UDPFlood)
					answerWriter.append(", ");
			}
			if (UDPFlood)
				answerWriter.append("Victim: " + attackedUDP + " - Attacker: " + attackerUDP);
			answerWriter.append("\r\nQ4: ");
			if (PoD) {
				answerWriter.append(PODPackets + " packets");
				if (SYNFlood || UDPFlood)
					answerWriter.append(", ");
			}
			if (SYNFlood) {
				answerWriter.append(SYNPackets + " packets");
				if (UDPFlood)
					answerWriter.append(", ");
			}
			if (UDPFlood)
				answerWriter.append(UDPPackets + " packets");
			answerWriter.append("\r\nQ5: ");
			if (PoD) {
				answerWriter.append(PODRythms + "");
				if (SYNFlood || UDPFlood)
					answerWriter.append(", ");
			}
			if (SYNFlood) {
				answerWriter.append(SYNRythms + "");
				if (UDPFlood)
					answerWriter.append(", ");
			}
			if (UDPFlood)
				answerWriter.append(UDPRythms + "");
			answerWriter.append("\r\nQ6: ");
			if (PoD) {
				answerWriter.append(PODDuration + " minutes");
				if (SYNFlood || UDPFlood)
					answerWriter.append(", ");
			}
			if (SYNFlood) {
				answerWriter.append(SYNDuration + " minutes");
				if (UDPFlood)
					answerWriter.append(", ");
			}
			if (UDPFlood)
				answerWriter.append(UDPDuration + " minutes");
			answerWriter.close();
		}

	}
}

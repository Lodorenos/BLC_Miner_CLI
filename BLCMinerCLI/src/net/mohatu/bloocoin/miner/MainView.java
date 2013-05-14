/*
 * Copyright (C) 2013 Mohatu.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package net.mohatu.bloocoin.miner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class MainView {
	private static boolean mining = true;
	private static boolean isMining = false;
	private static long counter = 0;
	private static String addr = "";
	private static String key = "";
	private static int solved = 0;
	private static final String url = "server.bloocoin.org";
	private static final int port = 3122;
	private static long startTime = System.nanoTime();
	private static int threads = 5;
	private static boolean showHelp = true;
	private static double khs;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		start();
	}

	public static void mainLoop() {
		Scanner scan = new Scanner(System.in);
		String currentCommand = scan.next();
		// main switch:case
		switch (currentCommand) {
		case "help":
			displayHelp();
			mainLoop();
		case "mine":
			if (isMining == false) {
				isMining = true;
				startTime = System.nanoTime();
				Thread miner = new Thread(new MinerHandler());
				Thread khs = new Thread(new KhsClass());
				miner.start();
				khs.start();
				mining = true;
				updateStatusText("Mining started");
			} else {
				updateStatusText("Mining already!");
			}
			mainLoop();
			break;
		case "exit":
			System.exit(0);
			break;
		case "blc":
			Thread cc = new Thread(new CoinClass());
			cc.start();
			mainLoop();
			break;
		case "stop":
			mining = false;
			isMining = false;
			updateStatusText("Mining stopped");
			mainLoop();
			break;
		case "fromlist":
			Thread slc = new Thread(new SubmitListClass());
			slc.start();
			mainLoop();
			break;
		case "send":
			updateStatusText("Send to whom? :");
			String to = new String(scan.next());
			updateStatusText("How much? :");
			int amt = scan.nextInt();
			updateStatusText("Send " + amt + " to " + to + "? (true/false)");
			if(scan.nextBoolean()==true){
				Thread sc = new Thread(new SendClass(to, amt));
				sc.start();
				updateStatusText(amt + "BLC sent to " + to + ".");
			}else{
				updateStatusText("Transaction failed!");
			}
			mainLoop();
			break;
		case "setthreads":
			if(isMining == false){
				setThreads(scan.nextInt());
				updateStatusText("Updated threads to " + threads);
			}
			mainLoop();
			break;
			default:
				updateStatusText("Unknown command");
				mainLoop();
				break;
		}
		scan.close();
	}

	public static void displayHelp() {
		updateStatusText("Available commands: help, mine, stop, setthreads x, exit, blc");
	}

	public static void start() {
		updateStatusText("Program start time: " + startTime);
		initialize();
	}

	public static boolean getHelp() {
		return showHelp;
	}

	public static void setHelp(boolean b) {
		showHelp = false;
	}

	private static void initialize() {
		loadData();
	}

	public static void updateCounter() {
		counter++;
	}

	public static void updateKhs(double khss) {
		khs = khss;
	}

	public static void setTime(int hour, int minute, int second) {
		String hourString, minuteString, secondString;
		hourString = Integer.toString(hour);
		minuteString = Integer.toString(minute);
		secondString = Integer.toString(second);

		if (hour < 10) {
			hourString = "0" + hour;
		}

		if (minute < 10) {
			minuteString = "0" + minute;
		}
		if (second < 10) {
			secondString = "0" + second;
		}
		System.out.print("\r                                               ");
		System.out.print("\r" + hourString + ":" + minuteString + ":"
				+ secondString + "\tTried: " + counter + "\tKh/s: "+khs + "\tSolved:"+solved+"\n");
	}

	public static void updateSolved(String solution) {
		System.out.println("Solved: " + ++solved);
	}

	public static long getCounter() {
		return counter;
	}

	public static boolean getStatus() {
		return mining;
	}

	public static void updateStatusText(String status) {
		System.out.println(status);
	}

	public static void updateStatusTextOneLine(String status) {
		System.out.print("\r" + status);
	}

	public static String getAddr() {
		return addr;
	}

	public static String getKey() {
		return key;
	}

	public static String getURL() {
		return url;
	}

	public static int getPort() {
		return port;
	}

	public static void updateBLC(int blc) {
		updateStatusText("BLC: " + Integer.toString(blc));
	}

	private static void getCoins() {
		Thread gc = new Thread(new CoinClass());
		gc.start();
	}

	public static int getThreads() {
		return threads;
	}

	public static void setThreads(int thread) {
		threads = thread;
	}

	public static long getStartTime() {
		return startTime;
	}

	public static void loadDataPub() {
		loadData();
	}

	private static void loadData() {
		try {
			FileInputStream stream = new FileInputStream(new File("bloostamp"));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			String data = (Charset.defaultCharset().decode(bb).toString());
			addr = data.split(":")[0];
			key = data.split(":")[1];
			stream.close();
			MainView.updateStatusText("Bloostamp data loaded successfully");
			MainView.updateStatusText("Getting coin count.");
			setThreads((Runtime.getRuntime().availableProcessors() / 2) + 1);
			getCoins();
			mainLoop();
		} catch (FileNotFoundException fnfe) {
			MainView.updateStatusText("Could not find the bloostamp file!");
			System.out.println("Unable to find the bloostamp file");
			System.out
					.println("Bloostamp file not found. Generate a new one? (y/n)");
			Scanner reader = new Scanner(System.in);
			if (reader.next().equals("y")) {
				Thread reg = new Thread(new RegisterClass());
				reg.run();
			} else {
				System.exit(0);
			}
			reader.close();
		} catch (IOException ioe) {
			MainView.updateStatusText("IOException");
		}
	}
}

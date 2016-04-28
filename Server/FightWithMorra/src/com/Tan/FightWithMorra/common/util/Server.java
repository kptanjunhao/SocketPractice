package com.Tan.FightWithMorra.common.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import sun.nio.ch.ThreadPool;


public class Server {
	private static int ID = 100;
	
	/**
	 * TCP的监听端口
	 */
	public static final int TCP_PORT = 4700;
	
	/**
	 * UDP的监听端口
	 */
	public static final int UDP_PORT = 7400;

	List<Client> clients = new ArrayList<Client>();
	PlayingClients playingClients = new PlayingClients();
	
	/**
	 * 启动服务器
	 *
	 */
	@SuppressWarnings("resource")
	public void start() {

		new Thread(new UDPThread()).start();

		ServerSocket ss = null;
		try {
			ss = new ServerSocket(TCP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			Socket s = null;
			try {
				s = ss.accept();
				Reader reader = new InputStreamReader(s.getInputStream());
				char chars[] = new char[64];
				int len;
				StringBuilder sb = new StringBuilder();
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeInt(ID++);
				while ((len=reader.read(chars))!=-1){
					sb.append(new String(chars,0,len));
				}
				String IP = s.getInetAddress().getHostAddress();
				int udpPort = Integer.valueOf(sb.toString());
				Client c = new Client(IP, udpPort);
				boolean containsClients = false;
				for (Client temp:clients){
					if ((temp.IP.equals(c.IP)) && (temp.udpPort == c.udpPort)){
						containsClients = true;
					}
				}
				if (!containsClients){
					clients.add(c);
				}
				s.close();
				System.out.println("A Client Connect! Addr- "
						+ s.getInetAddress() + ":" + s.getPort()
						+ "----UDP Port:" + udpPort);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (s != null) {
					try {
						s.close();
						s = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new Server().start();
	}
	
	private class Client {
		String IP;

		int udpPort;

		public Client(String IP, int udpPort) {
			this.IP = IP;
			this.udpPort = udpPort;
		}
	}
	
	private class PlayingClients{
		List<Client> playingClients;
		
		public PlayingClients(){
			this.playingClients = new ArrayList<Client>();
		}
		public void addClient(Client client1,Client client2){
			playingClients.add(client1);
			playingClients.add(client2);
		}
		
		public Client findPairClient(Client client){
			int index = playingClients.indexOf(findByClient(client));
			Client pairClient;
			if (index%2 == 0){
				pairClient = playingClients.get(index+1);
			}else{
				pairClient = playingClients.get(index-1);
			}
			return pairClient;
		}
		
		private Client findByClient(Client client){
			for (Client c:playingClients){
				if (c.IP.equals(client.IP) && (c.udpPort == client.udpPort)){
					return c;
				}
			}
			return client;
		}
		
		public boolean containClient(Client client){
			
			for (Client temp:playingClients){
				if ((temp.IP.equals(client.IP)) & (temp.udpPort == client.udpPort)){
					return true;
				}
			}
			return false;
		}
	}

	private class UDPThread implements Runnable {

		byte[] buf = new byte[1024];
		byte[] buf1 = new byte[1024];
		byte[] buf2 = new byte[1024];
		
		public void run() {
			DatagramSocket ds = null;
			try {
				ds = new DatagramSocket(UDP_PORT);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			System.out.println("UDP thread started at port :" + UDP_PORT);
			while (ds != null) {
				DatagramPacket dp = new DatagramPacket(buf, buf.length);
				DatagramPacket dp1 = new DatagramPacket(buf1, buf1.length);
				DatagramPacket dp2 = new DatagramPacket(buf2, buf2.length);
				try {
					ds.receive(dp);
					String connectedIP;
					int connectport = 0;
					try {
						connectedIP = dp.getAddress().toString().replace("/", "");
						connectport = dp.getPort();
					}catch (Exception e){
						connectedIP = null;
					}
					
					Client connectedClient = new Client(connectedIP,connectport);
					if (playingClients.containClient(connectedClient)){
						Client pairClient = playingClients.findPairClient(connectedClient);
						dp.setSocketAddress(new InetSocketAddress(pairClient.IP, pairClient.udpPort));
						ds.send(dp);
					}else{
						Client temp1 = null;
						Client temp2 = null;
						for (Client c : clients) {
							if (temp1 == null) {
								temp1 = c;
							} else if (temp2 == null) {
								temp2 = c;
								playingClients.addClient(temp1, temp2);
								String str = "Find a match";
								dp1.setData(str.getBytes());
								dp1.setSocketAddress(new InetSocketAddress(temp1.IP, temp1.udpPort));
								dp2.setData(str.getBytes());
								dp2.setSocketAddress(new InetSocketAddress(temp2.IP, temp2.udpPort));
								ds.send(dp1);
								ds.send(dp2);
								temp1 = null;
								temp2 = null;
								dp1 = null;
								dp2 = null;
								
							}
						}
					}
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				buf = new byte[1024];
				buf1 = new byte[1024];
				buf2 = new byte[1024];
			}
			
		}

	}
}

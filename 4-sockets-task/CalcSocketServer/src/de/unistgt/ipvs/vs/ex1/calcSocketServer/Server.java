package de.unistgt.ipvs.vs.ex1.calcSocketServer;

import java.io.IOException;
import java.net.ServerSocket;



public class Server {
	public void main() {
		while (true){		
			try {
				ServerSocket srvSocket= new ServerSocket(12345);
				if (srvSocket.isBound()) {
					srvSocket.close();
				CalcSocketServer serverThread=new CalcSocketServer(12345);
				serverThread.start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
	}
}

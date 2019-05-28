package de.unistgt.ipvs.vs.ex1.calcSocketServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import de.unistgt.ipvs.vs.ex1.calculation.ICalculation;
import de.unistgt.ipvs.vs.ex1.calculationImpl.CalculationImpl;


/**
 * Extend the run-method of this class as necessary to complete the assignment.
 * You may also add some fields, methods, or further classes.
 */
public class CalcSocketServer extends Thread {
	private ServerSocket srvSocket;
	private int port;

	public CalcSocketServer(int port) {
		this.srvSocket = null;
		this.port = port;
	}
	
	@Override
	public void interrupt() {
		try {
			if (srvSocket != null)
				srvSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void run() {
		if (port <= 0) {
			System.err.println("SocketServer listen port not specified!");
			System.exit(-1);
		}
		
			try {
				srvSocket = new ServerSocket(this.port);
				Socket clientSocket = srvSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
				out.println("<08:RDY>");
				ICalculation calculation = new CalculationImpl();
				while(true){
					if (!in.readLine().isEmpty()) {
					out.println("<07:OK>");
					String str;				
					str=in.readLine();
					str=str.substring(str.indexOf('<'), str.indexOf('>'));
					String Result = calculate(str);
					out.println("<OK:st");
					}									    
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
			
				
		}
		
			
		
			

		// TODO
		// Start listening server socket ..
	}
}



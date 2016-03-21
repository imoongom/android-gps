/*------------------------------------------------------------------------------------------------------------------
-- SOURCE FILE: udps_sql.java - An application that will listen for datagrams
-- from the Android GPS app and update the database.
--
-- PROGRAM: udps_sql
--
-- FUNCTIONS:
-- public udps_sql(int port)
-- public void run()
-- public static void main (String [] args)
--
-- DATE: March 16, 2016
--
-- REVISIONS: 
--    March 16, 2016 - File created. Implement basic Java server.
--    March 20, 2016 - Fix JDBC driver dependency.  
--    March 20, 2016 - Comments added for the functions.
--
-- DESIGNER: Krystle Bulalakaw, Oscar Kwan, Gabriel Lee, Eunwon Moon
--
-- PROGRAMMER: Eunwon Moon, Gabriel Lee
--
-- NOTES:
-- The UDP server that gets the clients' data and updates the database with it 
-- before echoing back the data to the client.
-- Creates a thread to run the main loop that receives the datagrams.
----------------------------------------------------------------------------------------------------------------------*/
package udps_sql;

import java.net.*;
import java.io.*;
import java.sql.*;

public class udps_sql extends Thread
{
	String ClientString;
	int ClientPort;
	private DatagramSocket ListeningSocket;
	DatagramPacket dgram;
	static final int DgramSize = 2048;
	byte[] PacketData;
	InetAddress Addr;
	static Connection conn = null;
	static Statement stmt = null;
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/androidgps";
	
	// Database credentials
	static final String USER = "root";
	static final String PASS = "aman";
	
	/*------------------------------------------------------------------------------------------------------------------
    -- FUNCTION: udps_sql
    --
    -- DATE: March 16, 2016
    --
    -- REVISIONS: 
    --    March 16, 2016 - File created.
    --    March 20, 2016 - Comments added for the functions.
    --
    -- DESIGNER: Krystle Bulalakaw, Oscar Kwan, Gabriel Lee, Eunwon Moon
    --
    -- PROGRAMMER: Eunwon Moon, Gabriel Lee
    --
    -- INTERFACE: public udps_sql(int port)
    -- int port: the port number to use when running the server
    --
    -- RETURNS: n/a
    --
    -- NOTES:
    -- The constructor for the udps_sql class. Creates a listening socket with 
	-- the given port and set the socket timeout.
    ----------------------------------------------------------------------------------------------------------------------*/
	public udps_sql(int port) throws IOException
	{
		ListeningSocket = new DatagramSocket (port);
		ListeningSocket.setSoTimeout(100000); // set a 20 second timeout
	}
	
	/*------------------------------------------------------------------------------------------------------------------
    -- FUNCTION: run
    --
    -- DATE: March 16, 2016
    --
    -- REVISIONS: 
    --    March 16, 2016 - File created.
    --    March 20, 2016 - Comments added for the functions.
    --
    -- DESIGNER: Krystle Bulalakaw, Oscar Kwan, Gabriel Lee, Eunwon Moon
    --
    -- PROGRAMMER: Eunwon Moon, Gabriel Lee
    --
    -- INTERFACE: public void run()
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- The code to execute in the thread. A UDP server that listens for 
	-- datagrams and echoes it back to the client. It updates the database
	-- from the client's data before the echo.
    ----------------------------------------------------------------------------------------------------------------------*/
	public void run()
	{
		while(true)
		{
			PacketData = new byte[DgramSize];
			dgram = new DatagramPacket (PacketData, DgramSize);
			
			ClientString = null;
			
			try
			{
				// Listen for datagrams
				System.out.println ("Listening on port: " + ListeningSocket.getLocalPort());
				ListeningSocket.receive(dgram);
				
				Addr = dgram.getAddress();
				ClientPort = dgram.getPort();
				System.out.println ("Datagram from: "+ Addr +":"+ ClientPort);
				
				// Get the client data
				ClientString = new String (PacketData, 0, dgram.getLength());
				System.out.println ("Message: "+ ClientString.trim());
				
				int rs = stmt.executeUpdate(ClientString);
				// Echo it back
				dgram = new DatagramPacket(PacketData, DgramSize, Addr, ClientPort);
				PacketData = null;
				ClientString = null;
				
				try
				{
					ListeningSocket.send (dgram);
				}
				catch(IOException ex)
				{
					System.out.println(" Could not Send :"+ex.getMessage());
					System.exit(0);
				} 
			}	
			catch (SocketTimeoutException s)
			{
				System.out.println ("Socket timed out!");
				ListeningSocket.close();
				
				break;
			}
			catch(SQLException se)
			{
				//Handle errors for JDBC
				System.out.println("SQLException...");
				se.printStackTrace();
			}
			catch (IOException io)
			{
				System.out.println (io);
				ListeningSocket.close();
				
				break;
			}
		}
		
		try
		{
			if(stmt!=null)
			stmt.close();
		}
			catch(SQLException se2){
		}
		
		try
		{
			if(conn!=null)
			conn.close();
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
	}
    
    /*------------------------------------------------------------------------------------------------------------------
    -- FUNCTION: main
    --
    -- DATE: March 16, 2016
    --
    -- REVISIONS: 
    --    March 16, 2016 - File created.
    --    March 20, 2016 - Fix JDBC driver dependency.
    --    March 20, 2016 - Comments added for the functions.
    --
    -- DESIGNER: Krystle Bulalakaw, Oscar Kwan, Gabriel Lee, Eunwon Moon
    --
    -- PROGRAMMER: Eunwon Moon, Gabriel Lee
    --
    -- INTERFACE: public static void main (String [] args)
    -- String [] args: command-line arguments (used to get the port number)
    --
    -- RETURNS: void
    --
    -- NOTES:
    -- The main entry point for the application.
	-- Opens the connections to the database so that the runnable thread can
	-- update the database with the data from the client.
    ----------------------------------------------------------------------------------------------------------------------*/
	public static void main (String [] args)
	{
		if(args.length != 1)
		{
			System.out.println("Usage Error : java udps_sql <port>");
			System.exit(0);
		}   
		int port = Integer.parseInt(args[0]);
		
		try{
			// Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			
			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			stmt = conn.createStatement();
		}
		catch(SQLException se)
		{
			// Handle errors for JDBC
			System.out.println("SQLException...");
			se.printStackTrace();
		}
		catch(Exception e)
		{
			// Handle errors for Class.forName
			System.out.println("handle Exception class.forname...");
			e.printStackTrace();
		}
		
		try
		{
			// Create a thread and execute the run method.
			Thread t = new udps_sql (port);
			t.start();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}

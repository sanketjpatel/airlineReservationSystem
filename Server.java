package airlines;

import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sanket
 */


public class Server {

    private static ServerSocket serverSocket = null;
    private static Socket serverFront = null;
    
    private static Socket serverBack = null;    //for connection from server to backend

    static final String loginMessage = "Logged In";
    static final String logoutMessage = "Logged Out";
    private static final int maxClients = 10;   //Maximum number of clients allowed to connect to this server
    private static final clientThread[] threads = new clientThread[maxClients];    
    
    public static PrintWriter bos = null;
    public static BufferedReader bis = null;
    public static ObjectOutputStream boos = null;
    public static ObjectInputStream bois = null;
    
    public static void main(String[] args) {
        int serverFrontPort = 29456; // default port
        int serverBackPort = 11000;
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                
        
        
        /**** BASIC SERVER SETUP FOR COMMUNICATION WITH CLIENTS ****/
        
        if (args.length < 1) {
            System.out.println("Usage: java Server <portNumber>\n"
                    + "Now using port number=" + serverFrontPort);
        } else {
            serverFrontPort = Integer.parseInt(args[0]);
        }

        try {
            serverSocket = new ServerSocket(serverFrontPort);
            System.out.println("Server ready");
        } catch (IOException e) {
            System.out.println(e);
        }
        
        /**** BASIC SERVER SETUP FOR COMMUNICATION WITH THE BACKEND ****/
        
        try {
            String hostName = "localhost"; // use the default host name
            //System.out.println("What is the port number of the server host?");
            serverBack = new Socket(
                    InetAddress.getByName(hostName), serverBackPort);
            
            bis = new BufferedReader( new InputStreamReader(serverBack.getInputStream()));
            bos = new PrintWriter(serverBack.getOutputStream(), true);
            boos = new ObjectOutputStream(serverBack.getOutputStream()); 
            bois = new ObjectInputStream(serverBack.getInputStream());
            
            String initialBackMsg = bis.readLine();
            System.out.println(initialBackMsg);
            bos.println("Server ready");
            
            /******
             * 
             * Server Backend Code
             * 
             */
            
            
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the Backend");
        }



        // instantiates a stream socket for accepting connections        
        while (true) {  // forever loop

            try {
                // wait to accept a connection
                //System.out.println("Waiting for a connection..");
                serverFront = serverSocket.accept();
                PrintWriter fos = new PrintWriter(serverFront.getOutputStream(), true);
                //boolean done = false;

                int i=0;

                //code to handle new clients. Make a new thread if it does not exist
                for (i = 0; i < maxClients; i++) {
                    if (threads[i] == null) {
                        
                        (threads[i] = new clientThread(serverFront, threads)).start();                        
                        System.out.println("Connection accepted on thread "+(i+1));                        
                        break;                        
                    }
                }

                //Code to handle client limit
                if (i == maxClients) {
                    fos.println("Server too busy. Try later.");
                    fos.close();
                    serverFront.close();
                }

            } catch (IOException e) {
                System.out.println(e);
            }
        }   //end while-true		
    }	//end main
}   //end Server	

//class clientThread implements Runnable {
class clientThread extends Thread {

    private String clientName = null;
    private Socket serverFront = null;
        
    private PrintWriter fos = null;        
    private BufferedReader fis = null;
    private ObjectOutputStream foos = null;
    private ObjectInputStream fois = null;
        
    private final clientThread[] threads;
    private int maxClients;


    public clientThread(Socket serverFront, clientThread[] threads) {
        this.serverFront = serverFront;
        this.threads = threads;
        maxClients = threads.length;
    }

    public void run() {

        int maxClients = this.maxClients;
        clientThread[] threads = this.threads;

        try {
            //System.out.println("Generating input output streams");
            fos = new PrintWriter(serverFront.getOutputStream(), true);
            fis = new BufferedReader(new InputStreamReader(serverFront.getInputStream()));
            foos = new ObjectOutputStream(serverFront.getOutputStream()); 
            fois = new ObjectInputStream(serverFront.getInputStream());
            
            /*
             * Start client code handling
             */
            boolean doneAll = false;
            boolean flag = true;
            
            while (!doneAll) {
                fos.println("Connection established");

                while(flag) {
                    String flags = fis.readLine();
                    if (flags.equals("0")) {
                        flag = false;
                        break;
                    }
                    
                    final Customer cust = (Customer)fois.readObject();
                    clientName = cust.ID;
                    System.out.println("Request received from : "+clientName);
                    
                    Server.bos.println("LOGIN");
                    
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                //Frontend message relayed to Backend
                                Server.boos.writeObject(cust);  //Frontend message relayed to Backend
                            } catch (IOException ex) {
                                Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }, 500);
                    
                    String custRegHandling = Server.bis.readLine();
                    fos.println(custRegHandling);   //Backend reply relayed to Client
                }
                
                
                //System.out.println("Hurray!");
                
                /****
                 * 
                 * CUSTOMER MENU HANDLING
                 * 
                 */
                
                String nextRequest;    //for receiving a operation to perform
                
                boolean done = false;
                
                while(!done) {
                    
                    nextRequest = fis.readLine(); 
                    Server.bos.println("MENU");
                    Server.bos.println(nextRequest);
                    switch(nextRequest) {
                        
                        //Logout
                        case "0":
                            String clientID = fis.readLine();
                            done = true;
                            doneAll = true;
                            Server.bos.println(clientID);
                            fos.println("*** Bye " + clientName + " ***");
                            break;
                     
                        case "a0":
                            clientID = fis.readLine();
                            done = true;
                            doneAll = true;
                            Server.bos.println(clientID);
                            fos.println("*** Bye " + clientName + " ***");
                            break;
                     
                            
                            
                        //View Flights
                        case "1":
                            String origin = fis.readLine();
                            String dest = fis.readLine();
                            Server.bos.println(origin);
                            Server.bos.println(dest);
                            
                            //send ResultSet to client
                            foos.writeObject((DefaultTableModel)Server.bois.readObject());
                            break;
                            
                            
                        //View History
                        case "2":
                            Server.bos.println(fis.readLine());
                            foos.writeObject((DefaultTableModel)Server.bois.readObject());
                            break;
                            
                            
                        //Book Ticket
                        //Cancel Ticket
                        case "3":
                        case "4":
                            //read Transaction object and send it to the backend
                            Server.boos.writeObject((Transaction)fois.readObject());
                            
                            fos.println(Server.bis.readLine());
                            break;
                            
                                                         
                         //Update Profile
                         case "5":
                             Server.boos.writeObject((Customer)fois.readObject());
                             fos.println(Server.bis.readLine());
                             break;
                             
                         case "a1":
                             Server.boos.writeObject((Flight)fois.readObject());
                             Server.bos.println(fis.readLine());
                             fos.println(Server.bis.readLine());
                             break;
                             
                         case "a2":
                             Server.bos.println(fis.readLine());
                             Server.bos.println(fis.readLine());
                             fos.println(Server.bis.readLine());
                             break;
                    }
                
                } //end while(!done)

                
            }//end-while(true); client handling code
            
            
            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized (this) {
                for (int i = 0; i < maxClients; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                        System.out.println("Connection closed on thread " + (i + 1) + " for client " + clientName);
                    }
                }
            }

            /*
             * Close the output stream, close the input stream, close the socket.
             */
            fis.close();
            fos.close();
            serverFront.close();


        } //end-try-in-run()zd
        catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }	//end run()

}   //end class clientThread

/* END OF FILE */
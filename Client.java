package airlines;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sanket
 */

public class Client {

    private static Socket socket = null;
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
    private static BufferedReader is = null;
    private static PrintWriter os = null;
    private static ObjectInputStream ois = null;
    private static ObjectOutputStream oos = null;
    private static boolean done;
    private static String clientID;
    private static boolean isAR;

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        
        
//<editor-fold defaultstate="collapsed" desc="Connect to Server, Initiate streams">
        try {
            System.out.println("Welcome to the Airline Reservation Interface.\n"
                    + "What is the hostname?");
            String hostName = br.readLine();
            if (hostName.length() == 0) // if user did not enter a name
            {
                hostName = "localhost"; // use the default host name
            }
            System.out.println("What is the port number of the host?");
            String portNum = br.readLine();
            if (portNum.length() == 0) {
                portNum = "29456";  // default port number
            }
            socket = new Socket(
                    InetAddress.getByName(hostName), Integer.parseInt(portNum));
            
            is = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            os = new PrintWriter(socket.getOutputStream(), true);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host ");
        }
//</editor-fold>

        if (socket != null) {

            try {
                done = false;
                String echo = is.readLine();
                System.out.println(echo);
                                
                if(echo.equals("Server too busy. Try later.")) {                    
                    socket.close();
                    return;
                }
                
                while (!done) {
                    
                    //Start Login Process
                    loginProcess(0);
                    
                    //Logged in. Now show the menu.
                    menu();
                    
                    done = true;
                }   // end while
            }   // end try
            catch (IOException | NumberFormatException e) {
                System.err.println("IOException:  " + e);
            }   // end catch

        }   //end-if(socket!=null)
        
    }   //end main
    
//<editor-fold defaultstate="collapsed" desc="LOGIN PROCESS">
    /****
     *
     *  LOGIN PROCESS
     *
     * @param regStatus
     * @throws java.io.IOException
     */
    
    public static void loginProcess(int regStatus) throws IOException{
        
        switch (regStatus){
            case 0:
                boolean firstTimeUser = true;
                boolean firstTry = true;
                
                while(firstTry) {
                    System.out.println("\nAre you a first time user? (Y/N). Enter \"AR\" if you are an Airline Representative.");
                    String firstResp = br.readLine();
                    switch (firstResp.toUpperCase()) {
                        case "YES":
                        case "Y":
                            firstTimeUser = true;
                            firstTry = false;
                            break;
                        case "N":
                        case "NO":
                            firstTimeUser = false;
                            firstTry = false;
                            break;
                        case "AR":
                            isAR = true;
                            firstTimeUser = false;
                            firstTry = false;
                            break;
                        default:
                            System.out.println("Invalid Response. Enter either Y or N");
                    }
                }
                
                
                if(firstTimeUser){
                    registrationProcess();
                    //Register the user, sends a customer object to the server
                }
                else{
                    loginCredentials();
                    //Login Credentials, sends a customer object to the server
                }
                
                serverResponseAction();
                break;
                
            case 1:
                registrationProcess();
                serverResponseAction();
                break;                
            case 2:
                loginCredentials();
                serverResponseAction();
                break;
        }
    }
    
    public static void registrationProcess() throws IOException{
        System.out.println("\nRegister for a customer account. Choose your username:");
        String custID = br.readLine();
        System.out.println("\nEnter your desired password:");
        String custPass = br.readLine();
        System.out.println("\nEnter your First Name:");
        String firstName = br.readLine();
        System.out.println("\nEnter your Last Name:");
        String lastName = br.readLine();
        
        clientID = custID;
                
        final Customer cust;
        cust = new Customer(custID, custPass, firstName, lastName);
        cust.newCust = true;
        cust.isAR = isAR;
        os.println("1");    //flag = 1
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //Frontend message relayed to Backend
                    oos.writeObject(cust);      //send object Customer over to server
                } catch (IOException ex) {
                    Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 500);
        
    }   //end registrationProcess
    
    public static void loginCredentials() throws IOException{
        System.out.println("\nEnter your username:");
        String custID = br.readLine();
        System.out.println("\nEnter your password:");
        String custPass = br.readLine();
        
        clientID = custID;
        
        os.println("1");            //flag = 1
        
        final Customer cust;
        cust = new Customer(custID, custPass);
        cust.newCust = false;
        cust.isAR = isAR;
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //Frontend message relayed to Backend
                    oos.writeObject(cust);      //send object Customer over to server
                } catch (IOException ex) {
                    Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, 500);

    }   //end loginCredentials
    
    public static void serverResponseAction() throws IOException{
        String custRegHandling = is.readLine();
        System.out.println("\n"+custRegHandling);
        
        switch(custRegHandling){
            
            case "You are now registered":
            case "Correct login credentials":
            case "You are already registered. You are now logged in":
                //System.out.println("Sending false bool to server");                
                os.println("0");    //flag = 0
                
                break;
                
            case "Correct AR login credentials":                
                os.println("0");    //flag = 0
                break;
                
            case "You are already registered. Incorrect password. Try again":
                System.out.println("\nPlease login with correct details");
                loginProcess(2);
                break;
                
            case "No such customer found":
                loginProcess(1);
                break;
                
            case "You are already logged in":
                System.out.println("\nFirst logout from the other interface before logging in");
                loginProcess(2);
                break;
            
            case "Incorrect loginID or password":
                loginProcess(2);
                break;
                
            default:
                loginProcess(2);
                break;
        }   //end switch
    }   //end serverResponseAction
    
//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="MENU INVOKE">
    /*****
     * Menu Invoke
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    public static void menu() throws IOException, ClassNotFoundException, SQLException{
        
        if(isAR) {
            System.out.println("\n\nChoose from the following options: ");
            System.out.println("1. View flights");
            System.out.println("a1. Add flight");
            System.out.println("a2. Delete flight");
            System.out.println("a0. Logout");
            System.out.print("\nEnter your response: ");
        }
        
        else {
            System.out.println("\n\nChoose from the following options: ");
            System.out.println("1. View flights");
            System.out.println("2. View transaction history");
            System.out.println("3. Book tickets");
            System.out.println("4. Cancel tickets");
            System.out.println("5. Update Profile");
            System.out.println("0. Logout");
            System.out.print("\nEnter your response: ");
        }
        
        String response = br.readLine();
        switch(response) {            
            
            //Logout
            case "0":
            case "a0":
                System.out.println("Logging out..");
                done = true;
                os.println(response);
                os.println(clientID);
                System.out.println(is.readLine());
                socket.close();
                break;
                
                
            //View Flights
            case "1":
                os.println("1");
                System.out.print("Enter origin: ");
                String origin = br.readLine();
                System.out.print("Enter destination: ");
                String dest = br.readLine();
                origin = origin.toUpperCase();
                dest = dest.toUpperCase();
                os.println(origin);
                os.println(dest);
                                
                DefaultTableModel flightSearch = (DefaultTableModel)ois.readObject();
                
                System.out.println("\nHere are the flight details:");
                JOptionPane.showMessageDialog(null,new JScrollPane(new JTable(flightSearch)));                
                break;
                
                
            //View Transaction History
            case "2":
                os.println("2");
                os.println(clientID);
                
                DefaultTableModel history = (DefaultTableModel)ois.readObject();
                
                System.out.println("\nHere is your transaction history:");
                JOptionPane.showMessageDialog(null,new JScrollPane(new JTable(history)));
                break;
                
                
            //Book Tickets
            case "3":
                os.println("3");
                
                System.out.print("Enter flight number of this flight you want to book: ");
                String flightNo = br.readLine();
                flightNo = flightNo.toUpperCase();
                System.out.print("Enter number of seats: ");
                int seats = Integer.parseInt(br.readLine());
                
                String type = "BOOK";
                Transaction booking = new Transaction(clientID, flightNo, type, seats);
                
                oos.writeObject(booking);
                
                System.out.println(is.readLine());                
                break;
                
                
            //Cancel Tickets
            case "4":
                os.println("4");
                
                System.out.print("Enter flight number of the ticket(s) you want to cancel: ");
                flightNo = br.readLine().toUpperCase();
                System.out.print("Enter number of tickets: ");
                seats = 0 - Integer.parseInt(br.readLine());
                
                type = "CANCEL";
                Transaction cancellation = new Transaction(clientID, flightNo, type, seats);
                
                oos.writeObject(cancellation);
                
                System.out.println(is.readLine());
                break;
                
                
            //Update Profile    
            case "5":
                os.println("5");
                System.out.print("Change First Name to: ");
                String fName = br.readLine();
                System.out.print("Change Last Name to: ");
                String lName = br.readLine();
                System.out.print("Enter Password: ");
                String password = br.readLine();
                
                Customer modifiedCust = new Customer(clientID, password, fName, lName);
                oos.writeObject(modifiedCust);
                System.out.println(is.readLine());
                break;
                
                
            //Add Flight
            case "a1":
                os.println("a1");
                System.out.println("Enter flight number: ");
                flightNo = br.readLine().toUpperCase();
                System.out.println("Enter origin: ");
                origin = br.readLine().toUpperCase();
                System.out.println("Enter destination: ");
                dest = br.readLine().toUpperCase();
                System.out.println("Enter number of seats: ");
                int numSeats = Integer.parseInt(br.readLine());
                System.out.println("Enter price per ticket: ");
                int price = Integer.parseInt(br.readLine());
                
                Flight flight = new Flight(flightNo, origin, dest, numSeats, price);
                oos.writeObject(flight);
                os.println(clientID);
                System.out.println(is.readLine());
                break;
            
                
            //Delete Flight
            case "a2":
                os.println("a2");
                System.out.println("Enter flight number: ");
                flightNo = br.readLine().toUpperCase();
                os.println(flightNo);
                os.println(clientID);
                System.out.println(is.readLine());
                break;
                
            default:
                System.out.println("Invalid response. Try again!");
                break;
        }
        
        if(!(response.equals("0") || response.equals("a0")))
            menu();
        
        
    }
//</editor-fold>
    
    
    
    
} // end class
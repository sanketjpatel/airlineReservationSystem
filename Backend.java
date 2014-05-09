/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airlines;

import java.io.*;
import java.net.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author bhavs_000
 */
public class Backend {

    private static ServerSocket backendSocket = null;
    private static Socket serverSocket = null;    //for connection from server to backend

    static final String loginMessage = "Backend Logged In";
    static final String logoutMessage = "Backend Logged Out";
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        
        int backendPort = 11000; // default port

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter os = null;
        BufferedReader is = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        
        DBConnect database = new DBConnect();

        if (args.length < 1) {
            System.out.println("Usage: java Backend <portNumber>\n"
                    + "Now using port number=" + backendPort);
        } else {
            backendPort = Integer.parseInt(args[0]);
        }

        try {
            backendSocket = new ServerSocket(backendPort);
            String initialBackMsg = "Backend ready";
            
            System.out.println(initialBackMsg);
            
        } catch (IOException e) {
            System.out.println(e);
            database.logoutAll();
        }
        
        // instantiates a stream socket for accepting connections        
        try {
            // wait to accept a connection
            System.out.println("Waiting for a connection..");
            serverSocket = backendSocket.accept();

            os = new PrintWriter(serverSocket.getOutputStream(), true);
            is = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            oos = new ObjectOutputStream(serverSocket.getOutputStream());
            ois = new ObjectInputStream(serverSocket.getInputStream());

            os.println("Backend ready");
            System.out.println(is.readLine());  //server ready

            boolean flag = true;
            while (true) {  // forever loop
                
                //<editor-fold defaultstate="collapsed" desc="loginProcess">
                /**
                 * ****
                 *
                 * Login Checking/Registration
                 */
                String region = is.readLine();
                switch (region) {
                    //<editor-fold defaultstate="collapsed" desc="case "LOGIN"">
                    case "LOGIN":
                        Customer cust = (Customer) ois.readObject();
                        System.out.println("Request received from: " + cust.ID);
                        if (!cust.isAR) {                                   //Client is a Customer
                            
                            if (cust.newCust) {                             //Client claims he is a new Customer
                                if (!database.checkCustomer(cust.ID)) {     //Customer does not exist in database
                                    cust.setLogin();
                                    database.insertCustomer(cust);
                                    System.out.println("New registration: " + cust.ID);
                                    os.println("You are now registered");
                                } else {                                    //Customer exists in database
                                    if (database.credentialCheck(cust)) {    //Check Customer username and password
                                        cust.setLogin();                    //login Customer if correct credentials
                                        database.updateCustomer(cust);
                                        
                                        //send message that client already exists and has been logged in
                                        os.println("You are already registered. You are now logged in");
                                    } else {
                                        os.println("You are already registered. Incorrect password. Try again");
                                    }
                                }
                            } else {                                          //Client says existing Customer or AR
                                if (!database.checkCustomer(cust.ID)) {     //Customer does not exist in database
                                    os.println("No such customer found");
                                } else {
                                    if (database.credentialCheck(cust)) {   //Correct login credentials
                                        
                                        if (!database.loginCheck(cust.ID)) { //Not logged in
                                            cust.setLogin();
                                            database.custLogin(cust.ID);  //Set login to 1 in database
                                            System.out.println(cust.ID + " logged in");
                                            os.println("Correct login credentials");
                                        } else {
                                            os.println("You are already logged in");
                                        }
                                        
                                    } else {                                  //Incorrect login credentials
                                        os.println("Incorrect loginID or password");
                                    }
                                }
                            }
                        } else {
                            if (!database.checkAR(cust.ID)) {     //AR does not exist in database
                                os.println("No such Airline Representative found");
                            } else {
                                if (database.credentialARCheck(cust)) {   //Correct login credentials
                                    
                                    if (!database.loginARCheck(cust.ID)) { //Not logged in
                                        cust.setLogin();
                                        database.loginAR(cust.ID);  //Set login to 1 in database
                                        System.out.println(cust.ID + " logged in");
                                        os.println("Correct login credentials");
                                    } else {
                                        os.println("You are already logged in");
                                    }
                                    
                                } else {                                  //Incorrect login credentials
                                    os.println("Incorrect loginID or password");
                                }
                            }
                        }
                        break;
//</editor-fold>
                        
                        //<editor-fold defaultstate="collapsed" desc="case "MENU"">
                    case "MENU":
                        String nextRequest = is.readLine();
                        switch (nextRequest) {
                            
                            //Logout request
                            case "0":
                                String clientID = is.readLine();
                                //done = true;
                                database.custLogout(clientID, false);
                                System.out.println(clientID + " logged out");
                                break;
                                
                            case "a0":
                                clientID = is.readLine();
                                //done = true;
                                database.custLogout(clientID, true);
                                System.out.println(clientID + " logged out");
                                break;
                                
                                //View flights
                            case "1":
                                String origin = is.readLine();  //will be in lowercase
                                String dest = is.readLine();    //will be in lowercase
                                
                                DefaultTableModel viewFlights = database.getFlights(origin, dest);  //search flights
                                oos.writeObject(viewFlights);
                                break;
                                
                                //View Transaction History
                            case "2":
                                String custID = is.readLine();
                                
                                DefaultTableModel history = database.viewHistory(custID);
                                oos.writeObject(history);
                                break;
                                
                                //Book Tickets
                            case "3":
                                
                                Transaction booking = (Transaction) ois.readObject();
                                
                                String transID = booking.getTransID();
                                String flightNo = booking.getFlightNo();
                                int seats = booking.getSeats();
                                Flight fbook = database.retrieveFlight(flightNo);
                                
                                if (fbook.bookSeat(seats)) {
                                    booking.setCost(seats * fbook.getPrice());
                                    System.out.println("Flight " + flightNo + " has " + fbook.getAvail() + " seats left");
                                    database.insertTransaction(booking);
                                    database.updateFlight(fbook);
                                    String successMessage = "Successfully booked " + seats + " seats on " + flightNo;
                                    System.out.println(successMessage);
                                    os.println(successMessage);
                                } else {
                                    os.println("Only " + fbook.getAvail() + " seats available on flight " + flightNo);
                                }
                                
                                break;
                                
                                //Cancel Tickets
                            case "4":
                                
                                Transaction cancellation = (Transaction) ois.readObject();
                                
                                transID = cancellation.getTransID();
                                flightNo = cancellation.getFlightNo();
                                seats = cancellation.getSeats();    //negative number
                                Flight fcancel = database.retrieveFlight(flightNo);
                                custID = cancellation.getCustID();
                                if (-seats <= database.custFlightSeats(custID, flightNo)) {
                                    cancellation.setCost(seats * fcancel.getPrice());
                                    System.out.println("Flight " + flightNo + " has " + fcancel.getAvail() + " seats left");
                                    database.insertTransaction(cancellation);
                                    fcancel.cancelSeat(seats);
                                    database.updateFlight(fcancel);
                                    String successMessage = "Successfully cancelled " + (-seats) + " seats on " + flightNo;
                                    System.out.println(successMessage);
                                    os.println(successMessage);
                                } else {
                                    os.println("You have booked only " + database.custFlightSeats(custID, flightNo) + " seats on flight " + flightNo + " before");
                                }
                                
                                break;
                                
                                //Update Profile
                            case "5":
                                Customer modifiedCust = (Customer) ois.readObject();
                                database.updateCustomer(modifiedCust);
                                os.println("Successfully updated Customer Profile");
                                break;
                                
                            case "a1":
                                Flight flight = (Flight) ois.readObject();
                                String ARID = is.readLine();
                                System.out.println(database.checkFlight(flight.getFlightNo()));
                                if (!database.checkFlight(flight.getFlightNo())) {
                                    database.insertFlight(flight);
                                    Transaction addFlight = new Transaction(ARID, flight.getFlightNo(), "ADD", flight.getSeats());
                                    database.insertTransaction(addFlight);
                                    os.println("Successfully added flight number " + flight.getFlightNo());
                                } else {
                                    os.println("Error");
                                }
                                
                                break;
                                
                            case "a2":
                                flightNo = is.readLine();
                                ARID = is.readLine();
                                flight = database.retrieveFlight(flightNo);
                                if (database.checkFlightOwner(flightNo, ARID)) {    //if flight exists
                                    database.deleteFlight(flightNo);    //delete flight
                                    Transaction delFlight = new Transaction(ARID, flightNo, "DELETE", flight.getSeats());
                                    database.insertTransaction(delFlight);
                                    //cancel all transactions on that flight
                                    os.println("Successfully deleted flight number " + flightNo);
                                } else {
                                    os.println("Error");
                                }
                                break;
                                
                            default:
                                break;
                                
                        }
//</editor-fold>
                }

            }  //end while-true
        } catch (IOException e) {            
            database.logoutAll();
            System.out.println(e);
        }   //end try-catch		
    }   //end main

}   //end Backend

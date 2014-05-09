/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package airlines;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author sanket
 */
public class DBConnect {
    private Connection connect;    
    private Statement statement;
    
    public DBConnect() throws SQLException{
        
        try{
            Class.forName("com.mysql.jdbc.Driver");            
            connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/airlineDB","root","");
            statement = connect.createStatement();
            
        } catch(ClassNotFoundException e){
            System.out.println("Error: "+e);
        }
        
    }
    
    public ResultSet getData(String table){
        
        try {            
            String query = "select * from "+table;
            ResultSet returnTable = statement.executeQuery(query);
            return returnTable;            
        } catch (SQLException ex) {
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return null;
    }
    
    public boolean checkCustomer(String ID)throws SQLException{
        //<editor-fold defaultstate="collapsed" desc="true if a customer with this ID exists in the database">
        String searchQuery = "SELECT * from custDB WHERE username= \"" + ID +"\"";
        
        ResultSet rs = statement.executeQuery(searchQuery); // execute the query, and get a java resultset
        return rs.absolute(1);
//</editor-fold>
    }
    
    public boolean checkAR(String ID)throws SQLException{        
        //<editor-fold defaultstate="collapsed" desc="true if an AR with this ID exists in the database">
        String searchQuery = "SELECT * from ARDB WHERE username= \"" + ID +"\"";
        
        ResultSet rs = statement.executeQuery(searchQuery); // execute the query, and get a java resultset
        return rs.absolute(1);
//</editor-fold>        
    }

    
    public boolean credentialCheck(Customer cust)  throws SQLException{ 
        //<editor-fold defaultstate="collapsed" desc="true if a customer with correct credentials exists in database">
        String username = cust.ID;
        String password = cust.getPassword();
        
        String searchQuery = "SELECT * from custDB WHERE username= \"" + username +"\" AND password= \"" + password + "\"";
        ResultSet rs = statement.executeQuery(searchQuery);
        return rs.absolute(1);
//</editor-fold>
    }
    
    public boolean credentialARCheck(Customer cust)  throws SQLException{ 
        //<editor-fold defaultstate="collapsed" desc="true if an AR with correct credentials exists in database">
        String username = cust.ID;
        String password = cust.getPassword();
        
        String searchQuery = "SELECT * from ARDB WHERE username= \"" + username +"\" AND password= \"" + password + "\"";
        ResultSet rs = statement.executeQuery(searchQuery);
        return rs.absolute(1);
//</editor-fold>
    }
    
    public boolean checkFlightOwner(String flightNo, String ARID)  throws SQLException{ 
        //<editor-fold defaultstate="collapsed" desc="true if an AR with correct credentials exists in database">
        
        String searchQuery = "SELECT * from transDB WHERE custID= \"" + ARID +"\" AND type= \"ADD\" AND flightNo= \"" + flightNo + "\"";
        ResultSet rs = statement.executeQuery(searchQuery);
        return rs.absolute(1);
//</editor-fold>
    }
    
    public boolean loginCheck(String custID)  throws SQLException{
        //<editor-fold defaultstate="collapsed" desc="true if this customer is logged in the system">
        String searchQuery = "SELECT * from custDB WHERE username= \"" + custID +"\";";
        ResultSet rs = statement.executeQuery(searchQuery);
        if(rs.absolute(1)){
            return 1==rs.getInt("login");
        }
        else
            return false;
//</editor-fold>
    }
    
    public boolean loginARCheck(String ARID)  throws SQLException{
        //<editor-fold defaultstate="collapsed" desc="true if this customer is logged in the system">
        String searchQuery = "SELECT * from ARDB WHERE username= \"" + ARID +"\";";
        ResultSet rs = statement.executeQuery(searchQuery);
        if(rs.absolute(1)){
            return 1==rs.getInt("login");
        }
        else
            return false;
//</editor-fold>
    }
    
    
    
    public boolean checkFlight(String flightNo)throws SQLException{ 
        //returns true if this flightNo exists in the database        
        String searchQuery = "SELECT * from flightDB WHERE flightNo= \"" + flightNo +"\"";
        
        ResultSet rs = statement.executeQuery(searchQuery); // execute the query, and get a java resultset
        return rs.absolute(1);
    }
    
    public void deleteFlight(String flightNo)throws SQLException{ 
        //returns true if this flightNo exists in the database
        String deleteQuery = "DELETE FROM flightDB WHERE flightNo= \"" + flightNo +"\"";
        
        statement.executeUpdate(deleteQuery); // execute the query, and get a java resultset
        
    }
    
    public void insertCustomer(Customer cust) throws SQLException{
        String username = cust.ID;
        String password = cust.getPassword();
        String firstName = cust.custFirstName;
        String lastName = cust.custLastName;
        int login = cust.getLogin()? 1:0;
        String sql;
        sql = String.format("INSERT INTO custDB VALUES (\""+username+"\", \""+password+"\", \""+firstName+"\", \""+lastName+"\", "+login+")");
        statement.executeUpdate(sql);
    }
    
    public void insertFlight(Flight flight) throws SQLException{
        String flightNo = flight.getFlightNo();
        String source = flight.getOrigin();
        String dest = flight.getDest();
        int numSeats = flight.getSeats();
        int numAvail = flight.getAvail();
        int price = flight.getPrice();
        String sql;
        sql = String.format("INSERT INTO flightDB VALUES (\""+flightNo+"\", \""+source+"\", \""+dest+"\", "+numSeats+", "+numAvail+", "+price+")");
        statement.executeUpdate(sql);
    }
       
    public void insertTransaction(Transaction trans) throws SQLException{
        String transID = trans.getTransID();
        String custID  = trans.getCustID();
        String flightNo = trans.getFlightNo();
        int cost = trans.getCost();
        int numSeats = trans.getSeats();
        String type = trans.getType();
        
        String sql;
        sql = String.format("INSERT INTO transDB (ID, custID, flightNo, type, numSeats, cost) VALUES (\""+transID+"\", \""+custID+"\", \""+flightNo+"\", \""+type+"\", "+numSeats+", "+cost+")");
        statement.executeUpdate(sql);
    }
    
    public void updateFlight(Flight flight) throws SQLException{
        String flightNo = flight.getFlightNo();
        String source = flight.getOrigin();
        String dest = flight.getDest();
        int numSeats = flight.getSeats();
        int numAvail = flight.getAvail();
        int price = flight.getPrice();
        
        String update = "UPDATE `airlineDB`.`flightDB` SET `source` = \'" + source +"\', `dest` = \'"+dest+"\', `numSeats` = \'"+numSeats+"\', `numAvail` = \'"+numAvail+"\', `price` = \'"+price+"\' WHERE `flightDB`.`flightNo` = \'"+flightNo+"\';";
        statement.executeUpdate(update);
        
    }
    
    public void updateCustomer(Customer cust) throws SQLException{
        String username = cust.ID;
        String password = cust.getPassword();
        String firstName = cust.custFirstName;
        String lastName = cust.custLastName;
        int login = cust.getLogin()? 1:0;
        
        String update = "UPDATE `airlineDB`.`custDB` SET `password` = \'" + password +"\', `firstName` = \'"+firstName+"\', `login` = \'"+login+"\' WHERE `custDB`.`username` = \'"+username+"\';";
        statement.executeUpdate(update);
    }
    
    public void logoutAll() throws SQLException{
        
        String update1 = "UPDATE `airlineDB`.`custDB` SET `login` = \'"+0+"\';";
        String update2 = "UPDATE `airlineDB`.`ARDB` SET `login` = \'"+0+"\';";
        statement.executeUpdate(update1);
        statement.executeUpdate(update2);
    }
    
    public void custLogin(String username) throws SQLException {
        int login = 1;
        String update = "UPDATE `airlineDB`.`custDB` SET `login` = \'"+login+"\' WHERE `custDB`.`username` = \'"+username+"\';";
        statement.executeUpdate(update);
    }
    
    public void loginAR(String ID) throws SQLException {
        int login = 1;
        String update = "UPDATE `airlineDB`.`ARDB` SET `login` = \'"+login+"\' WHERE `ARDB`.`username` = \'"+ID+"\';";
        statement.executeUpdate(update);
    }
    
    public void custLogout(String username, boolean isAR) throws SQLException {
        int login = 0;
        
        if (!isAR){
            String update = "UPDATE `airlineDB`.`custDB` SET `login` = \'"+login+"\' WHERE `custDB`.`username` = \'"+username+"\';";
            statement.executeUpdate(update);
        }
        else {
            String update = "UPDATE `airlineDB`.`ARDB` SET `login` = \'"+login+"\' WHERE `ARDB`.`username` = \'"+username+"\';";
            statement.executeUpdate(update);            
        }
        

    }
    
    public Flight retrieveFlight(String flightNo) throws SQLException{
        String searchQuery = "SELECT * from flightDB WHERE flightNo= \"" + flightNo +"\"";
        ResultSet rs = statement.executeQuery(searchQuery);
        
        if(rs.absolute(1)){
            String source = rs.getString("source");
            String dest = rs.getString("dest");
            int numSeats = rs.getInt("numSeats");
            int numAvail = rs.getInt("numAvail");
            int price = rs.getInt("price");
            Flight f = new Flight(flightNo, source, dest, numSeats, numAvail, price);
            return f;
        }
        else {
            Flight error = new Flight("Error", "Error", "Error", 0, 0);
            return error;
        }
    }

    public Transaction retrieveTransaction(String transID) throws SQLException{
        String searchQuery = "SELECT * from transDB WHERE ID= \"" + transID +"\"";
        ResultSet rs = statement.executeQuery(searchQuery);
        
        if(rs.absolute(1)){
            String custID = rs.getString("custID");
            String flightNo = rs.getString("flightNo");
            int numSeats = rs.getInt("numSeats");
            int cost = rs.getInt("cost");
            String type = rs.getString("type");
            Transaction t = new Transaction(transID, custID, flightNo, type, numSeats);
            t.setCost(cost);
            return t;
        }
        else {
            Transaction error = new Transaction("Error", "Error", "Error", 0);
            return error;
        }
    }
    
    public int custFlightSeats(String custID, String flightNo) throws SQLException{
        String searchQuery = "SELECT SUM(numSeats) AS answer from transDB WHERE custID= \"" + custID +"\" AND flightNo= \"" + flightNo + "\";";
        ResultSet seatsHistory = statement.executeQuery(searchQuery);
        int answer = 0;
        while(seatsHistory.next()){
            answer = seatsHistory.getInt(1);
        }
        
        return answer;
    }
    
    public DefaultTableModel custFlightHistory(String custID, String flightNo) throws SQLException{
        String searchQuery = "SELECT * from transDB WHERE custID= \"" + custID +"\" AND flightNo= \"" + flightNo + "\"";
        ResultSet history = statement.executeQuery(searchQuery);
        return resultSetToTableModel(history);
    }
    
    public DefaultTableModel viewHistory(String custID) throws SQLException{
        String searchQuery = "SELECT * from transDB WHERE custID= \"" + custID + "\"";
        
        ResultSet history = statement.executeQuery(searchQuery);
        return resultSetToTableModel(history);
    }
    
    public DefaultTableModel getFlights(String source, String dest) throws SQLException {
        String searchQuery = "SELECT * from flightDB WHERE source= \"" + source +"\" AND dest= \"" + dest + "\"";
        ResultSet flights = statement.executeQuery(searchQuery);
        return resultSetToTableModel(flights);
    }
    
    public DefaultTableModel resultSetToTableModel(ResultSet row) throws SQLException {
        
        ResultSetMetaData meta = row.getMetaData();
        DefaultTableModel model = new DefaultTableModel();
        
        String cols[] = new String[meta.getColumnCount()];
        for (int i = 0; i < cols.length; ++i) {
            cols[i] = meta.getColumnLabel(i + 1);
        }

        model.setColumnIdentifiers(cols);

        while (row.next()) {
            Object data[] = new Object[cols.length];
            for (int i = 0; i < data.length; ++i) {
                data[i] = row.getObject(i + 1);
            }
            model.addRow(data);
        }
        return model;
    }
    
}




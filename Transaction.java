/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package airlines;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author bhavs_000
 */




public class Transaction implements Serializable {
    private final String transID;
    private final String type;
    private final String custID;
    private final String flightNo;
    private final int numSeats;
    private int cost;
    
    //public enum transType { ADD, DELETE, BOOK, CANCEL, OBLITERATE};
    
    
    
    
    Transaction(String ID, String custID, String f, String type, int numSeats){
        this.flightNo = f;
        this.custID = custID;
        //using transtype
        /*//<editor-fold defaultstate="collapsed" desc="set Transaction type">
        switch(type){
        case "ADD":
        this.type = transType.ADD;
        break;
        case "DELETE":
        this.type = transType.DELETE;
        break;
        case "BOOK":
        this.type = transType.BOOK;
        break;
        case "CANCEL":
        this.type = transType.CANCEL;
        break;
        case "OBLITERATE":
        this.type = transType.OBLITERATE;
        break;
        default:
        this.type = transType.BOOK;
        break;
        }*/
//</editor-fold>
        this.type = type;
        this.numSeats = numSeats;
        this.transID = ID;
    }
    
    Transaction(String custID, String f, String type, int numSeats){
        this.flightNo = f;
        this.custID = custID;
        this.type = type;
        this.numSeats = numSeats;        
        
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-DD-YYYY-HH:mm:ssZ");
        this.transID = sdf.format(date);
    }
    
    public String getTransID() {
        return this.transID;
    }
    
    public String getFlightNo() {
        return this.flightNo;
    }
    
    public String getCustID() {
        return this.custID;
    }
    
    public int getCost(){
        return this.cost;
    }
    
    public void setCost(int cost){
        this.cost = cost;
    }
    
    public String getType(){
        return this.type;
    }
    
    public int getSeats() {
        return this.numSeats;
    }
    
    
    
    
}
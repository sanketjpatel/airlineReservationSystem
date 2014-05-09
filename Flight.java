/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package airlines;

import java.io.*;

/**
 *
 * @author bhavs_000
 */
public class Flight implements Serializable {
    
    private String flightNo;
    private int price;  //in dollars
    private int numSeats;   //total number of seats
    private int numAvail;   //total number of seats available
    private String origin;
    private String dest;    
    
    Flight(String flightNo, String origin, String dest, int numSeats, int price){
        this.flightNo = flightNo;
        this.origin = origin;
        this.dest = dest;        
        this.numSeats = numSeats;
        this.price = price;
        this.numAvail = this.numSeats;
        
    }
    
     Flight(String flightNo, String origin, String dest, int numSeats, int numAvail, int price){
        this.flightNo = flightNo;
        this.origin = origin;
        this.dest = dest;        
        this.numSeats = numSeats;
        this.price = price;
        this.numAvail = numAvail;
        
    }
    
    public String getFlightNo() {
        return this.flightNo;
    }
    
    public int getPrice() {
            return this.price;
    }
    
    public int getSeats() {
        return this.numSeats;
    }
    
    public int getAvail() {
        return this.numAvail;
    }
    
    public String getOrigin() {
        return this.origin;
    }
    
    public String getDest() {
        return this.dest;
    }
    
    public boolean bookSeat(int n) {      
        if (this.numAvail >= n) {
            this.numAvail -= n;
            return true;
        } else {
            //System.out.println("Only " + this.numAvail + " seats left in Flight " + flightNo);  // or send message over socket
            return false;
        }   
    }
    
    public void cancelSeat(int n) {
        this.numAvail -= n;        
    }
    
    
}   //class Flight

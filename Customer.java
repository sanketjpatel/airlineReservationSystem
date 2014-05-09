/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package airlines;

import java.io.*;
import java.util.HashMap;

/**
 *
 * @author bhavs_000
 */
public class Customer implements Serializable{
    public String ID;
    private String password;
    public String custLastName;
    public String custFirstName;
    private boolean login;
    public boolean newCust;
    public boolean isAR;
    
    
    Customer(String ID, String pass, String custFirstName, String custLastName)
    {
        this.ID = ID;
        this.password = pass;
        this.custFirstName = custFirstName;
        this.custLastName = custLastName;        
        this.login = false;
        this.newCust = false;
        this.isAR = false;
        
    }
    
    Customer(String ID, String pass){
        this.ID = ID;
        this.password = pass;
        this.newCust = false;
        this.isAR = false;
    }
    
    public void setLogin() {
        this.login = true;
    }

    public void setLogout(){
        this.login = false;
    }
    
    public boolean getLogin() {
        return this.login;
    }
    
    public String getPassword() {
        return this.password;        
    }
       
    public String getID() {        
        return this.ID;        
    }
    
    public void makeAR(){
        this.isAR = true;
    }
    
}

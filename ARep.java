/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package airlines;

import java.util.HashMap;

/**
 *
 * @author bhavs_000
 */
public class ARep {

    private String ARID;
    private String password;
    private String lastName;
    private String firstName;
    private boolean login;
    public HashMap<String,Transaction> transDB = new HashMap<>();
   
    ARep(String ARID, String pass, String lastName, String firstName)
    {
        this.ARID = ARID;
        this.password = pass;
        this.lastName = lastName;
        this.firstName = firstName;
        this.login = false;
        
    }
    
    public void setLogin(String ARID) {
        if(this.ARID.equals(ARID))
            this.login = true;
    }

    public void setLogout(String ARID){
        if(this.ARID.equals(ARID)){
            this.login = false;
        }
    }
    
    public boolean getLogin(String ARID) {
        if(this.ARID.equals(ARID))
            return this.login;
        else return false;
    }
   
    public Transaction getTransaction(String transID) {
        return this.transDB.get(transID);
    }
    
    
}
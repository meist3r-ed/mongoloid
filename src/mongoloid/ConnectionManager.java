/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongoloid;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Gustavo
 */
public class ConnectionManager {
    public String message_issueClose = ">[System]: ERROR: could not close connection";
    public String message_issueDriver = ">[System]: ERROR: check database driver";
    public String message_issueLogin = ">[System]: ERROR: wrong username or password";
    public String message_successClose = ">[System]: Successfully closed connection!";
    public String message_successLogin = ">[System]: Successfully connected to database!";
    
    Connection connection;
    
    /* Connection API */
    /* ----------------------------------------------------------------*/
    public int connect(String server, String user, String pass){
        try {
            String constring = "jdbc:oracle:thin:" + server;
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(
                    constring,
                    user,
                    pass);
            System.out.println(message_successLogin);
            return 1;
        } catch (ClassNotFoundException cnfe) {
            System.out.println(message_issueDriver);
            return -1;
        } catch(SQLException sqle){
            System.out.println(message_issueLogin);
            return -2;
        }
    }
    
    public boolean disconnect(){
        try{
            connection.close();
            System.out.println(message_successClose);
            return true;
        } catch(SQLException sqle){
            System.out.println(message_issueClose);
        }
        return false;
    }
    /* ----------------------------------------------------------------*/
    
    /* Get current connection */
    public Connection getConnection(){
        return connection;
    }
    
    public Statement makeStatement(Connection connection){
        try{
            return connection.createStatement();
        }catch(SQLException sqle){
            System.out.println(sqle.getMessage());
            return null;
        }
    }
    
    public ResultSet makeResultSet(Statement stmt, String query){
        try{
            return stmt.executeQuery(query);
        }catch(SQLException sqle){
            System.out.println(sqle.getMessage());
            return null;
        }
    }
    
    public ResultSetMetaData makeRSMD(ResultSet rs){
        try{
            return rs.getMetaData();
        }catch(SQLException sqle){
            System.out.println(sqle.getMessage());
            return null;
        }
    }
}

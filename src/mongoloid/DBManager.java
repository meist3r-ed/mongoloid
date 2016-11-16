/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongoloid;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

/**
 *
 * @author Gustavo
 */
public class DBManager {
    public String message_issueGeneric = ">[System]: ERROR: ";
    public String message_successOpenFile = ">[System]: File successfully opened";
    public String message_successCloseFile = ">[System]: File successfully closed";
    
    String server;
    String user;
    String pass;
        
    ConnectionManager connection;
    Statement stmt;
    ResultSet rs;
    ResultSetMetaData rsmd;
    
    public DBManager(){
        server = "";
        user = "";
        pass = "";
        connection = new ConnectionManager();
    }
    
    public boolean generateDatabase() throws IOException{
        Scanner scanner = new Scanner(System.in);
        String filename = "";
        FileWriter output = null;
        
        System.out.println(">[DATABASE GENERATOR aka DbGen]:");
        System.out.println(">[DbGen]: Please type the output filename");
        System.out.printf(">[DbGen]: ");
        filename = scanner.nextLine();
        
        try{
            output = new FileWriter(filename);
            System.out.println(message_successOpenFile);
            output.write("use " + this.user + "\n");
        }
        finally{
            if(output != null){
                output.close();
                System.out.println(message_successCloseFile);
            }
        }
        
        return true;
    }
    
    public void getConnection(){
        Scanner scanner = new Scanner(System.in);
        String tmp = "";
        int ok = 0;
        
        while(ok != 1){
            System.out.println(">[Please enter the connection values]");
            if(ok != -2){
                /* @hostname */
                System.out.printf(">[Hostname]: ");
                tmp = scanner.nextLine();
                server = "@" + tmp;
                /* :port */
                System.out.printf(">[Port]: ");
                tmp = scanner.nextLine();
                server = server + ":" + tmp;
                /* :SID */
                System.out.printf(">[SID]: ");
                tmp = scanner.nextLine();
                server = server + ":" + tmp;
            }
            /* Username */
            System.out.printf(">[Username]: ");
            tmp = scanner.nextLine();
            user = tmp;
            /* Password */
            System.out.printf(">[Password]: ");
            tmp = scanner.nextLine();
            pass = tmp;

            ok = connection.connect(server, user, pass);
        }
    }
    
    public void getOps(){
        Scanner scanner = new Scanner(System.in);
        int opt = 0;
        
        while(opt != 3){
            System.out.println(">[" + user + "]: " + server);
            System.out.println("\t[1]: Generate database from tables");
            System.out.println("\t[2]: Generate indexes from tables");
            System.out.println("\t[3]: Disconnect and close");
        
            opt = scanner.nextInt();
            switch(opt){
                case 1:
                    try{
                        generateDatabase();
                    }catch(IOException ioe){
                        System.out.println(message_issueGeneric + ioe.getMessage());
                    }
                    break;
                case 2:
                    break;
            }
        }
        connection.disconnect();
    }
}

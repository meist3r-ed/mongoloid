/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongoloid;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 *
 * @author Gustavo
 */
public class DBManager {
    public String message_issueGeneric = ">[System]: ERROR: ";
    public String message_issueTables = ">[System]: ERROR: Could not generate Statement or ResultSet";
    public String message_successOpenFile = ">[System]: File successfully opened";
    public String message_successCloseFile = ">[System]: File successfully closed";
    
    String server;
    String user;
    String pass;
        
    ConnectionManager conman;
    Connection connection;
    Statement stmt;
    ResultSet rs;
    ResultSetMetaData rsmd;
    
    public DBManager(){
        server = "";
        user = "";
        pass = "";
        conman = new ConnectionManager();
    }
    
    private void flushStatement(Statement stmt){
        try{
            stmt.close();
        } catch(SQLException sqle){
            System.out.println(sqle.getMessage());
        }
    }
    
    private void flushDB(Connection connection, Statement stmt, ResultSet rs){
        if(connection != null) connection = null;
        if(stmt != null) flushStatement(stmt);
        if(rs != null) connection = null;
    }
    
    private void generateDBTuples(FileWriter output, String curtab, int opt) throws IOException{
        try{
            Statement stmttup = connection.createStatement();
            ResultSet rstup = stmttup.executeQuery("SELECT * FROM " + curtab);
            rsmd = rstup.getMetaData();
            
            output.write("db." + curtab + ".insert([");
            
            rstup.next();
            /* Runs through tuples */
            while(true){
                output.write("{");
                int cnt = rsmd.getColumnCount();
                int i = 0;
                
                for(i = 0; i < cnt; i++){
                    output.write("\"" + rsmd.getColumnName(i + 1) + "\" : ");
                    output.write("\"" + rstup.getString(i + 1) + "\"");
                    if(i < cnt - 1)
                        output.write(", ");
                }
                output.write("}");
                if(rstup.next()){
                    output.write(", ");
                }
                else{
                    break;
                }
                    
            }
            
            output.write("])" + System.getProperty("line.separator"));
            flushStatement(stmttup);
        }catch(SQLException sqle){
            output.write("])" + System.getProperty("line.separator"));
            System.out.println(sqle.getMessage());
        }
    }
    
    private void generateDBTable(FileWriter output, String curtab) throws IOException{
        if(!curtab.substring(0, 4).equals("MLOG")){
            Scanner scanner = new Scanner(System.in);
            int opt = 0;
            
            System.out.println(">[DbGen]: CURRENT TABLE: " + curtab);
            System.out.println(">[DbGen]: Choose an operation [1. Simple, 2. Link, 3. Embedding, 4. NxN, 5. Skip]");
            System.out.printf(">[DbGen]: ");
            opt = scanner.nextInt();
            output.write("db.createCollection(\"" + curtab + "\")" + System.getProperty("line.separator"));
            generateDBTuples(output, curtab, opt);
        }
    }
    
    private boolean generateDBOutput(FileWriter output) throws IOException{
        connection = conman.getConnection();
        stmt = conman.makeStatement(connection);
        rs = conman.makeResultSet(stmt, "SELECT table_name FROM user_tables WHERE table_name NOT IN(SELECT table_name FROM user_snapshots)");
        
        if(connection != null && stmt != null && rs != null){
            /* Iteration in tables */
            try{
                while (rs.next()) {
                    generateDBTable(output, rs.getString("table_name"));
                }
            }catch(SQLException sqle){
                System.out.println(sqle.getMessage());
            }
            
            flushDB(connection, stmt, rs);
        }
        else{
            System.out.println(message_issueTables);
        }
        
        return true;
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
            output.write("use " + this.user + System.getProperty("line.separator"));
            generateDBOutput(output);
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

            ok = conman.connect(server, user, pass);
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
        conman.disconnect();
    }
}

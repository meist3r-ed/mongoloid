/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongoloid;

import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    
    ArrayList<String> pks;
    ArrayList<String> uniques;
    ArrayList<String> fks;
    ArrayList<String> fksRefTabs;
        
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
            System.out.println(message_issueGeneric + sqle.getMessage());
        }
    }
    
    private void flushDB(Connection connection, Statement stmt, ResultSet rs){
        if(connection != null) connection = null;
        if(stmt != null) flushStatement(stmt);
        if(rs != null) connection = null;
    }
    
    /* Get current table primary keys */
    private void getPks(String curtab){
        try{
            Statement stmt2 = connection.createStatement();
            
            String pkQuery = "SELECT column_name FROM all_cons_columns WHERE constraint_name = (" +
            "SELECT constraint_name FROM user_constraints " +
            "WHERE UPPER(table_name) = UPPER('" + curtab + "') AND CONSTRAINT_TYPE = 'P'" +
            ")";
            
            ResultSet rs2 = stmt2.executeQuery(pkQuery);
            
            if(pks == null){
                pks = new ArrayList<>();
            }
            if(!pks.isEmpty()){
                pks.clear();
            }

            while(rs2.next()){
                pks.add(rs2.getString(1));
            }

            stmt2.close();
        } catch(SQLException sqle){
            System.out.println(message_issueGeneric + sqle.getMessage());
        }
    }
    
    /* Get current table foreign keys + referenced tables */
    private void getFks(String curtab){
        try{
            Statement stmt2 = connection.createStatement();
            
            String fksQuery = "SELECT DISTINCT a.column_name, c_pk.table_name r_table_name FROM all_cons_columns a " +
            "JOIN all_constraints c ON a.owner = c.owner " +
            "AND a.constraint_name = c.constraint_name " +
            "JOIN all_constraints c_pk ON c.r_owner = c_pk.owner " +
            "AND c.r_constraint_name = c_pk.constraint_name " +
            "WHERE c.constraint_type = 'R' " +
            "AND a.table_name = '" + curtab + "'";
            
            ResultSet rs2 = stmt2.executeQuery(fksQuery);
            
            if(fks == null && fksRefTabs == null){
                fks = new ArrayList<>();
                fksRefTabs = new ArrayList<>();
            }
            if(!fks.isEmpty()){
                fks.clear();
            }
            if(!fksRefTabs.isEmpty()){
                fksRefTabs.clear();
            }

            while(rs2.next()){
                fks.add(rs2.getString(1));
                fksRefTabs.add(rs2.getString(2));
            }

            stmt2.close();
        } catch(SQLException sqle){
            System.out.println(message_issueGeneric + sqle.getMessage());
        }
    }
    
    /* Writes the column specified by col into the output file */
    private void writeColumn(FileWriter output, ResultSet rstup, int col) throws IOException{
        try{
            String curtype = rsmd.getColumnTypeName(col).toLowerCase();
            output.write("\"" + rsmd.getColumnName(col) + "\" : ");
            if(curtype.equals("number"))
                output.write(rstup.getString(col));
            else if(curtype.equals("date"))
                output.write("ISODate(\"" + rstup.getString(col) + "\")");
            else
                output.write("\"" + rstup.getString(col) + "\"");
        }catch(SQLException sqle){
            System.out.println(message_issueGeneric + sqle.getMessage());
        }
    }
    
    /* Generates mongo documents from current table + linking option */
    private void generateDBTuples(FileWriter output, String curtab, int opt) throws IOException{
        try{
            Statement stmttup = connection.createStatement();
            ResultSet rstup = stmttup.executeQuery("SELECT * FROM " + curtab);
            rsmd = rstup.getMetaData();
            
            getPks(curtab);
            if(opt != 1) getFks(curtab);
            
            int cnt = rsmd.getColumnCount();
            int pkcount = pks.size();
            String curtype = "";
            
            rstup.next();
            /* Runs through tuples */
            while(true){
                output.write("db." + curtab + ".insert(");
                output.write("{ ");
                int i = 0;
                
                /* Mongo's document _id */
                if(pkcount != 0){
                    output.write("\"_id\" : { ");
                    for(i = 0; i < pkcount; i++){
                        if(i != 0) output.write(", ");
                        writeColumn(output, rstup, rstup.findColumn(pks.get(i)));
                    }
                    output.write(" }");
                }
                
                /* Mongo's attributes */
                if(pkcount < cnt){
                    for(i = 0; i < cnt; i++){
                        if(!pks.contains(rsmd.getColumnName(i + 1)) && rstup.getString(i + 1) != null){
                            if(i != 0) output.write(", ");
                            writeColumn(output, rstup, i + 1);
                        }
                    }
                }
                
                output.write(" })");
                if(rstup.next())
                    output.write(System.getProperty("line.separator"));
                else
                    break;
            }
            output.write(System.getProperty("line.separator"));
            flushStatement(stmttup);
        }catch(SQLException sqle){
            output.write("})" + System.getProperty("line.separator"));
            System.out.println(message_issueGeneric + sqle.getMessage());
        }
    }
    
    /* Generates mongo collections from current table */
    private void generateDBTable(FileWriter output, String curtab) throws IOException{
        if(!curtab.substring(0, 4).equals("MLOG")){
            Scanner scanner = new Scanner(System.in);
            int opt = 0;
            
            System.out.println(">[DbGen]: CURRENT TABLE: " + curtab);
            System.out.println(">[DbGen]: Choose an operation [1. Simple, 2. Referencing, 3. Embedding, 4. NxN, 5. Skip]");
            System.out.printf(">[DbGen]: ");
            opt = scanner.nextInt();
            if(opt != 5){
                output.write("db.createCollection(\"" + curtab + "\")" + System.getProperty("line.separator"));
                generateDBTuples(output, curtab, opt);
            }
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
                System.out.println(message_issueGeneric + sqle.getMessage());
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

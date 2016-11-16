/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongoloid;

import java.util.Scanner;

/**
 *
 * @author Gustavo
 */
public class DBManager {
    String server;
    String user;
    String pass;
        
    ConnectionManager connection;
    
    public DBManager(){
        server = "";
        user = "";
        pass = "";
        connection = new ConnectionManager();
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
        
        System.out.println(">[" + user + "]: " + server + "\n");
        System.out.println("\t[1]: Generate database from tables");
        System.out.println("\t[2]: Generate indexes from ");
        System.out.println("\t[3]: Disconnect and close");
        while(opt != 3){
            opt = scanner.nextInt();
        }
        connection.disconnect();
    }
}

/*
 * Mongoloid is a SQL to MongoDB conversor featuring
 * the basic syntax for collection & document creation.
 * For further documentation, please check README.MD.
 */
package mongoloid;

/**
 *
 * @author meist3r-ed
 */
public class Main {
    
    private static void splash(){
        System.out.println("    __  ___                        __      _     __");
        System.out.println("   /  |/  /___  ____  ____ _____  / /___  (_)___/ /");
        System.out.println("  / /|_/ / __ \\/ __ \\/ __ `/ __ \\/ / __ \\/ / __  / ");
        System.out.println(" / /  / / /_/ / / / / /_/ / /_/ / / /_/ / / /_/ /  ");
        System.out.println("/_/  /_/\\____/_/ /_/\\__, /\\____/_/\\____/_/\\__,_/   ");
        System.out.println("                   /____/                      v0.0");
        System.out.println(">>>>>  SQL 2 MongoDB Conversor by meist3r-ed  <<<<<");
        System.out.println();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main.splash();
        DBManager mng = new DBManager();
        mng.getConnection();
        mng.getOps();
    }
    
}

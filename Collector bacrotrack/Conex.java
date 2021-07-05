import java.sql.*;

public class Conex{

    private static Connection conn;
    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String user = "bacros";
    private static final String pass = "bacros";
    private static final String url = "jdbc:mysql://192.168.100.30:3306/id12491651_bac?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";//?serverTimezone=UTC
    


    public Conex(){
        conn = null;
         try{
             Class.forName(driver);
             conn = DriverManager.getConnection(url,user,pass);
             if (conn != null) {
                 System.out.println("Conexion establecida al hosting");
             }
         }catch(ClassNotFoundException | SQLException e){
             System.out.println("Error al conectar: " + e);
         }
     }

    public Connection getConnection(){
        return conn;
    }
    
    public void desconectar(){
        conn = null;
        if (conn == null) {
            System.out.println("Conexion desconectada");
        }
    }

    /*public static void main (String [] args){
        Conex obj = new Conex();
        obj.getConnection();
    }*/
            
}



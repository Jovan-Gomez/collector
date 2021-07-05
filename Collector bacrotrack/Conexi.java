import java.sql.*;

public class Conexi{

    private static Connection conn;

    private static final String connectionUrl =
        "jdbc:sqlserver://192.168.100.205:49712;"
        //+ "database=xahamexpruebas;"//AMHAHMEX
        + "database=xahamexpruebas1;"//
        + "user=sa;"
        + "password=Limac00;"
        //+ "encrypt=true;"
        + "trustServerCertificate=false;"
        + "loginTimeout=30;";


    public Conexi(){
        conn = null;
         try{
             conn = DriverManager.getConnection(connectionUrl);
             if (conn != null) {
                 System.out.println("Conexion establecida servidor");
             }
         }catch(SQLException e){
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
            
}
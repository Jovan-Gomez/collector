import java.sql.*;

public class Conex{

    private static Connection conn;

    /*private static Statement s = null;
    private static PreparedStatement ps = null;
    private static ResultSet rs = null;*/

    private static final String connectionUrl =
        "jdbc:sqlserver://192.168.100.205:49712;"
        + "database=AMHAHMEX2021;"//AMHAHMEX AmhamexPruebaCollector
        + "user=sa;"
        + "password=Limac00;"
        //+ "encrypt=true;"
        + "trustServerCertificate=false;"
        + "loginTimeout=30;";

    public Conex() {
        conn = null;
        try{
            conn = DriverManager.getConnection(connectionUrl);
            if (conn != null) {
                System.out.println("Conexion establecida");
            }
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos");
            e.printStackTrace();
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



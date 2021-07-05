import java.util.Timer;
import java.util.TimerTask;

import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;

import java.sql.*;

public class Collector {
    public static void main(String[] args) {
        System.out.println("Welcome\nStarting Collector\n");

        // Calls the run method in the Check object every x milliseconds
        Timer timer = new Timer();
        // Checar cada 60,000 ms
        timer.schedule(new Check(), 0, 3000);
    }
}

class Check extends TimerTask{

    Timestamp ts, tsPast;

    int salesrepContactid = 0;

    Connection conM, conS;
    PreparedStatement psS, psM;
    Statement stM;
    ResultSet rsM, rsS;
    String querySql, querySql2, querySql3;
    String queryMysql,queryMysql2;

    int idProd;
    int iddoc;

    
    @Override
    public void run() {
        // Get the current Timestamp
        Date date = new Date();
        long time = date.getTime();
        ts = new Timestamp(time);

        try {
            // This code substracts 10 minutes to the timestamp
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.MINUTE, -10);  // number of minutes to add

            date = c.getTime();
            time = date.getTime();
            tsPast =  new Timestamp(time);  // The timestamp of 10 minutes ago
            //System.out.println(tsPast + " - " + ts);
            System.out.println("\n\n\nChecking for new info\t" + ts);
        } catch (Exception e) {
            System.out.println("Exception Error: ");
            e.printStackTrace();
        }

        try {
            Conexi conexSQL = new Conexi();
            conS = conexSQL.getConnection();

            Conex conexMYSQL = new Conex();
            conM = conexMYSQL.getConnection();
            queryMysql2 = "SELECT * FROM upleva";
            psM = conM.prepareStatement(queryMysql2);
            rsM = psM.executeQuery();
            while (rsM.next()) {
                //System.out.println(rsM.getInt("id"));
                querySql = "SELECT * FROM docDocument WHERE Custom2 = ?";
                psS = conS.prepareStatement(querySql);
                psS.setString(1, String.valueOf(rsM.getInt("id")));
                rsS = psS.executeQuery();
                if (Boolean.FALSE.equals(rsS.next())) {
                        //System.out.println("No existe en base");
                        salesrepContactid = 0;
                        querySql3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                        psS = conS.prepareStatement(querySql3);
                        psS.setString(1, rsM.getString("nomtec"));
                        rsS = psS.executeQuery();
                        if (rsS.next()) {
                        // If we found the name in the table, then we assign its corresponding ID
                            salesrepContactid = Integer.parseInt( rsS.getString(1) );
                        //System.out.println("Tecnico " + rs.getString(1));
                        }

                        querySql2 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                        + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                        + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);";
                        psS = conS.prepareStatement(querySql2);
                        psS.setInt(1, 15);
                        psS.setString(2, rsM.getString("concepto"));
                        psS.setInt(3, salesrepContactid);
                        psS.setString(4, String.valueOf(rsM.getInt("id")));
                        psS.setString(5, String.valueOf(rsM.getInt("id")));
                        psS.setString(6, rsM.getString("emergencia"));
                        int validar = psS.executeUpdate();
                        if (validar>0){
                            System.out.println("Insertado correctamente");
                        }       
                }
            }
        } catch (Exception e) {
            System.out.println("Exception Error: ");
            e.printStackTrace();
        } finally {
            try { rsM.close(); } catch (Exception e) { /* ignored */ }
            try { psM.close(); } catch (Exception e) { /* ignored */ }
            //try { conM.close(); } catch (Exception e) { /* ignored */ }
            try { rsS.close(); } catch (Exception e) { /* ignored */ }
            try { psS.close(); } catch (Exception e) { /* ignored */ }
            try { conS.close(); } catch (Exception e) { /* ignored */ }
        }  
        
    }
}


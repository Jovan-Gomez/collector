import java.util.Timer;
import java.util.TimerTask;

import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;

//import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

//import jdk.internal.jshell.tool.ConsoleIOContext.FixComputer;

import org.json.JSONArray;

import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.ResultSet;
//import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
//import java.sql.Time;

public class Collector {
    public static void main(String[] args) {
        System.out.println("Welcome\nStarting Collector\n");

        // Calls the run method in the Check object every x milliseconds
        Timer timer = new Timer();
        // Checar cada 60,000 ms
        timer.schedule(new Check(), 0, 30000);
    }

}

class Check extends TimerTask{

    Timestamp ts, tsPast;

    String url0 = "https://api.visitrack.com/api/Users/Authentication";// Authentication Token
    String url1 = "https://api.visitrack.com/api/Surveys/Activities";  // All activities in general (can be filtered between 2 timestamps)
    String url2 = "https://api.visitrack.com/api/Surveys/Activity";    // Detail of an Activity by its guid
    String url3 = "https://api.visitrack.com/api/Locations/Get";       // Detal of a Location by its guid
    String urlParameters;
    String response;          // Here we we will store the response from the requests

    String usr = "Limac01";
    String pswd = "limac01";
    // (BE CAREFUL, this token expires)
    String accessToken = "";//"1DD8F449-6A3F-4748-BB32-2B5967722538";      // Access Token for the API
    String from = null;   // From date
    String to = null;     // To date
    String guid;
    String listValues = "false";
    String centroCostos = "";
    int salesrepContactid = 0;
    float precioLista = 0;

    JSONArray myJsonArray;
    JSONArray ja_values;
    JSONObject myJsonObject;

    JSONArray ValidarcentroCostos;

    JSONArray prodJsonArray;

    String connectionUrl;
    Connection conn;
    PreparedStatement ps, ps2;
    ResultSet rs;
    String queryString, queryString2, queryString3, queryString4;

    int idProd;
    int iddoc;
    int idDepto;
    String prodKey;

    int tec = 0;int imagen1 = 0;int prod = 0;int dicTec = 0;int tman = 0;int conc = 0;int comm = 0;int srm = 0;int prio = 0;int prioridad = 0;int sucu = 0;

    PruebaImagenes fc;
    String carpeta;
    String folder;
    String urlImagen;
    String urlVideo;
    String caso;
    String nombreArchivo;
    int contador;
    String consideraciones;
    String proddd;

    @Override
    public void run() {
        // Get the current Timestamp
        Date date = new Date();
        long time = date.getTime();
        ts = new Timestamp(time);

        try {
            // Get the Access Toekn
            urlParameters = "Login="+usr +
                      "&Password="+pswd;
            //System.out.println(urlParameters);

            // New URL connection to get AccessToken
            HttpURLConnection http = new HttpURLConnection(url0, urlParameters);

            try {
                response = http.sendPost();
                accessToken = new JSONObject(response).getString("AccessToken");  // Saving the AccessToken from the response
                //System.out.println(response);
                //System.out.println("AT = " + accessToken);
            } catch (Exception e) {
                System.out.println("Exception Error: ");
                e.printStackTrace();
            }

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

        // Format the timestamps for the urlParameters to send to the API
        
        String time1Str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tsPast);
        String time2Str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);

        //String time1Str = "2021-01-18 10:40:00";
        //String time2Str = "2021-01-18 11:55:00";

        urlParameters = "AccessToken="+accessToken +
                           "&From="+time1Str +
                           "&To="+time2Str;
        System.out.println(urlParameters);

        // New URL connection to get all the activities in the last 10 minutes
        HttpURLConnection http = new HttpURLConnection(url1, urlParameters);

        try {
          response = http.sendPost();
          //System.out.println(response);
        } catch (Exception e) {
          System.out.println("Exception Error: ");
          e.printStackTrace();
        }

        try {
          // Array containing all the activities in the last 10 minutes
          myJsonArray = new JSONArray(response);

          // Iterate over each activity
          for (int i=0; i < myJsonArray.length(); i++) {
            System.out.println(myJsonArray.getJSONObject(i).getString("GUID"));

            queryString = "SELECT * FROM docDocument WHERE Custom1=?";
            Conex conx = new Conex();
            conn = conx.getConnection();
            ps = conn.prepareStatement(queryString);
            ps.setString(1, myJsonArray.getJSONObject(i).getString("GUID"));
            // Execute Query to Check if the activity has already been registered
            rs = ps.executeQuery();

            if (Boolean.FALSE.equals(rs.next())) {
              System.out.println("Nueva Entrada: " + myJsonArray.getJSONObject(i).getString("GUID"));
              urlParameters = "AccessToken="+accessToken +
                                 "&GUID="+myJsonArray.getJSONObject(i).getString("GUID") +
                                 "&ListValues="+listValues;
              System.out.println(urlParameters);

              // Prepare connection to the API to get the details of the activity
              http.setUrl(url2);
              http.setUrlParams(urlParameters);
              
              // Get the details of the activity
              response = http.sendPost();
              //System.out.println(response);
              
              // Create a JSONObject containing the details of the activity from the response
              myJsonObject = new JSONObject(response);

              
              if(!myJsonObject.getString("FormGUID").equals("")){

                ja_values = myJsonObject.getJSONArray("Values");

                switch(myJsonObject.getString("FormGUID")){


                  case "D4087D22-BD4F-423B-BCE3-99107E443B77": //LEVANTAMIENTO Y REQUISICION-CAEM-CONTRATO 50
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }

                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;

                  case "0F34638A-74DB-458D-8AA0-658B1F3819C0": //LEVANTAMIENTO Y REQUISICION-ISSEMYM-CONTRATO 47
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }

                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    
                    ValidarcentroCostos = new JSONObject(response).getJSONArray("Values");
                    if(ValidarcentroCostos.length() != 0){
                        centroCostos = ValidarcentroCostos.getJSONObject(0).getString("Value");
                    }
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;

                  case "9766F3B2-9945-4453-B705-1BC3179DCF77": //LEVANTAMIENTO Y REQUISICION-ISSEMYM-CONTRATO 54
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }

                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    
                    ValidarcentroCostos = new JSONObject(response).getJSONArray("Values");
                    if(ValidarcentroCostos.length() != 0){
                        centroCostos = ValidarcentroCostos.getJSONObject(0).getString("Value");
                    }
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;

                  //CONTRATOS 2020 *********************************************************************************************************************************
                  case "yykNOjVaHP": //LEVANTAMIENTO Y REQUISICION-IMSS-CONTRATO25
                
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }
                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Concepto")==0){
                        conc = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }

                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }

                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");

                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
    
                    //Collector imagenes
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
    
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+7; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "3" :
                            nombreArchivo = "factura_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "4" :
                            nombreArchivo = "factura_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          default:       
                        }
                      }
                    }
                    break;
                  case "pWlAtHZj4E": //LEVANTAMIENTO Y REQUISICION-ISEM-CONTRATO28
                
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }
                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }


                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }

                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
    
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "Orden de Servicio" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_2.pdf";
                              contador ++;
                            }
                            if(contador == 1){
                              nombreArchivo = "factura_3.pdf";
                              contador ++;
                            }
                            if(contador == 2){
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:     
                        }
                      }
                    }
                    break;
                  case "q2AZAyQNFJ": //LEVANTAMIENTO Y REQUISICION-ISEM-CONTRATO30
                
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }
                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
    
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "Orden de Servicio" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
    
                      }
                    }
                    break;
                  case "EC06601E-B154-41B9-96C6-1AF4E667B41E": //LEVANTAMIENTO Y REQUISICION-ISSEMYM-CONTRATO12
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }

                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;
                  //contrato39
                  case "BF562C25-21C9-4BB9-B474-7EFE785BF9E9": //LEVANTAMIENTO Y REQUISICION-ISSEMYM-CONTRATO39
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }

                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;
                    //aqui acaba 39

                    case "YjgCji85WT": //LEVANTAMIENTO Y REQUISICION-ISEM-CONTRATO32*******************************
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }
  
                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }
  
                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }
  
  
                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    //ValidarcentroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    ValidarcentroCostos = new JSONObject(response).getJSONArray("Values");
                    if(ValidarcentroCostos.length() != 0){
                        centroCostos = ValidarcentroCostos.getJSONObject(0).getString("Value");
                    }
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }
  
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Imagen Placa (Marca-Modelo-Serie)")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "Imagen Placa (Marca-Modelo-Serie)" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_2.pdf";
                              contador = 1;
                            }else{
                              if(contador == 1){
                                nombreArchivo = "factura_3.pdf";
                                contador = 2;
                              }else{
                                nombreArchivo = "factura_4.pdf";
                              }
                              
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;

                  case "814C604D-7BFB-4EEE-9E22-2AE1B839F546": //LEVANTAMIENTO Y REQUISICION-CAEM-CONTRATO 37
        
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }

                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }
                    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    centroCostos = new JSONObject(response).getJSONArray("Values").getJSONObject(0).getString("Value");
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, centroCostos);
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
                    
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
  
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+9; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "PDF" :
                            nombreArchivo = "factura_1.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "PDFac" :
                            nombreArchivo = "factura_2.pdf";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          case "Factura" :
                            if(contador == 0){
                              nombreArchivo = "factura_3.pdf";
                              contador = 1;
                            }else{
                              nombreArchivo = "factura_4.pdf";
                            }
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional
                          default:       
                        }
                      }
                    }
                    break;  
                    
                  case "43D7422A-A470-4C71-9F1A-797AE740F1BA": //LEVANTAMIENTO Y REQUISICION-SIN CONTRATO
                  
                    // This Part of the code gets the salesrepContactid(number) from the name of the Tecnico
                    for(int x = 0; x < ja_values.length(); x++){
                      if(ja_values.getJSONObject(x).getString("apiId").compareTo("Tecnicos")==0){
                        tec = x;
                        break;
                      }
                    }
                    salesrepContactid = 0;
                    queryString3 = "SELECT ContactID FROM vwLBSContactList WHERE ContactName=?";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(tec).getString("Value"));
                    rs = ps2.executeQuery();
                    if (rs.next()) {
                       // If we found the name in the table, then we assign its corresponding ID
                       salesrepContactid = Integer.parseInt( rs.getString(1) );
                       //System.out.println("Tecnico " + rs.getString(1));
                    }
                    //System.out.println("Found: " + ja_values.getJSONObject(3).getString("Value") + "\nID: "+ salesrepContactid);
                    // ---------------------------------------------------
                
                    // Falta Buscar en BusinessEntityID en tabla (por ahora se pone 2 por default)
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Consideraciones")==0){
                        comm = k;
                        break;
                      }
                    }
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("concep")==0){
                        conc = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("sucursal")==0){
                        sucu = k;
                        break;
                      }
                    }

                    switch (ja_values.getJSONObject(sucu).getString("Value")){
                      case "ZINACANTEPEC":
                        idDepto = 1;
                        break;
                      case "ECATEPEC":
                        idDepto = 15;
                        break;  
                    }


                    //Folio = Custom2
                    queryString3 = "INSERT INTO docDocument(ModuleID,documentTypeID,DocRecipientID,OwnedBusinessEntityID,BusinessEntityID,DepotID,DateDocument,Title,SalesRepContactID,LanguageID,CurrencyID,Rate,SubTotal,Total,TotalTax,"
                                       + "CreatedOn, CreatedBy,Custom1,Custom2,Comments)"
                                 + "VALUES (1214, 40, 1, 1, 2, ?, GETDATE(), ?, ?, 3, 3, 1, 0, 0, 0, GETDATE(), 1, ?, ?, ?);"
                                 + "SELECT SCOPE_IDENTITY();";
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setInt(1, idDepto);
                    ps2.setString(2, ja_values.getJSONObject(conc).getString("Value").toUpperCase());
                    ps2.setInt(3, salesrepContactid);
                    ps2.setString(4, myJsonArray.getJSONObject(i).getString("GUID"));
                    ps2.setString(5, myJsonArray.getJSONObject(i).getString("Consecutive").toUpperCase());
                    consideraciones = (comm > 0)?ja_values.getJSONObject(comm).getString("Value").toUpperCase():"";
                    ps2.setString(6, consideraciones);
                    rs = ps2.executeQuery();
                    iddoc = -2; // If this number stays like this, it indicates an error
                    while (rs.next()) {
                       iddoc = Integer.parseInt( rs.getString(1) );
                       //System.out.println(rs.getString(1));
                    }
                
                    // This portion on the code gets the details for the location of the current Activity
                    // Details like unidad medica and centro de costos
                    urlParameters = "tkn="+accessToken +
                                    "&LocationGUID="+myJsonObject.getString("LocationGUID");
                    //System.out.println("Sending Request to:\n"+url3+"?"+urlParameters);
                
                    http.setUrl(url3);
                    http.setUrlParams(urlParameters);
                
                    response = http.sendPost();
                    //System.out.println(centroCostos);
                    // -------------------------------------------------
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Tipo_de_Mantenimiento")==0){
                        tman = k;
                        break;
                      }
                    }
    
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("desfalla")==0){
                        dicTec = k;
                        break;
                      }
                    }

                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("srm")==0){
                        srm = k;
                        break;
                      }
                    }
                
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Prioridad")==0){
                        prio = k;
                        break;
                      }
                    }
                
                    if (ja_values.getJSONObject(tman).getString("Value").compareTo( "Emergencia" ) == 0) {
                      // The service IS an Emergency
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",1,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    } else {
                      queryString3 = "INSERT INTO docDocumentExt (IDExtra,Emergencia,NoTicket,Prioridad,CC,[Dictamen Tecnico])"
                                   + "SELECT "+iddoc+",0,?,?,?,?;"
                                   + "INSERT INTO docDocumentCFD (DocumentID,FinancialOperationID,Anexo20Ver)"
                                   + "SELECT "+iddoc+",0,'3.3'";
                    }
                    
                    if(!ja_values.getJSONObject(prio).getString("Value").equals("")){
                      switch(ja_values.getJSONObject(prio).getString("Value")){
                        case "BAJA":
                          prioridad = 178948;
                          break;
                        case "MEDIA":
                          prioridad = 178949;
                          break;
                        case "ALTA":
                          prioridad = 178950;
                          break;    
                      }
                    }
                    ps2 = conn.prepareStatement(queryString3);
                    ps2.setString(1, ja_values.getJSONObject(srm).getString("Value"));
                    ps2.setInt(2, prioridad);
                    ps2.setString(3, "SIN CONTRATO");
                    ps2.setString(4, ja_values.getJSONObject(dicTec).getString("Value").toUpperCase());
                    
                    ps2.execute();
    
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("Articulo")==0){
                        prod = k;
                        break;
                      }
                    }
                
                    if(prod != 0){
                      prodJsonArray = ja_values.getJSONObject(prod).getJSONArray("Value");
                
                      for (int j=0; j < prodJsonArray.length(); j++) {
                        proddd = (prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));

                        queryString4 = "SELECT productId, priceList, productkey FROM orgProduct WHERE productName = ?";
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setString(1, proddd.toUpperCase());
                        rs = ps2.executeQuery();
                        if (rs.next()) {
                          // Product is al ready in table
                          //System.out.println("Product Already In  Table: " + prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(0).getString("Value"));
                          idProd = Integer.parseInt( rs.getString(1) );
                          precioLista = Float.parseFloat( rs.getString(2) );
                          prodKey = rs.getString(3);
                          //System.out.println(idProd);
                        } else {
                          idProd = 0;
                          precioLista = 0;
                          prodKey = null;
                        }

                        queryString4 = "INSERT INTO docDocumentItem(DocumentID,Quantity,ProductID,Description,DiscountPerc,TaxTypeID,TaxPerc,UnitPrice,Total,MustBeDelivered,CostPrice,LineNumber,Unit,ProductID1,CantSolicitada,TipoPago,Productkey)"
                                    + "VALUES (?,?,?,?,0,5,0.16,0,0,1,?,?,?,?,?,?,?);";
                        //System.out.println(queryString4);
                        ps2 = conn.prepareStatement(queryString4);
                        ps2.setInt(1, iddoc);
                        ps2.setFloat(2, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setInt(3, idProd);
                        ps2.setString(4, proddd.toUpperCase());
                        ps2.setFloat(5, precioLista);
                        ps2.setInt(6, (j+1));
                        ps2.setString(7, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(5).getString("Value"));
                        ps2.setInt(8, idProd);
                        ps2.setFloat(9, Float.valueOf(prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(3).getString("Value")));
                        ps2.setString(10, prodJsonArray.getJSONObject(j).getJSONArray("Value").getJSONObject(4).getString("Value"));
                        ps2.setString(11, prodKey);
                        ps2.execute();
                      }
                    }
    
                    //Collector imagenes
                    imagen1 = 0;
                    for(int k = 0; k < ja_values.length(); k++){
                      if(ja_values.getJSONObject(k).getString("apiId").compareTo("1")==0){
                        imagen1 = k;
                        break;
                      }
                    }
    
    
                    fc = new PruebaImagenes();
                    carpeta = myJsonObject.getString("Consecutive");//myJsonArray.getJSONObject(i).getString("Consecutive")
                    folder = fc.DirectorioNuevo(carpeta);
                    contador = 0;
                    for(int x = imagen1; x < imagen1+7; x++){
                      urlImagen = ja_values.getJSONObject(x).getString("Value");
                      caso = ja_values.getJSONObject(x).getString("apiId");
                      if(!urlImagen.equals("") ){
                        switch(caso){
                          case "1" :
                            nombreArchivo = "imagen_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break;
                          case "2" :
                            nombreArchivo = "imagen_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Imagen");
                            ps2.execute();
                            break; // break es opcional
                          case "Video equipo" :
                            nombreArchivo = "video_1.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break;
                          case "Video falla" :
                            nombreArchivo = "video_2.mp4";
                            urlVideo = "https://and.visitrack.com/WebResource.aspx?e=VIDEO&id="+urlImagen;
                            fc.Imagenes(folder,nombreArchivo,urlVideo);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Video");
                            ps2.execute();
                            break; // break es opcional
                          case "Dictamen adicional" :
                            /*fc.Imagenes(folder,nombreImagen,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreImagen);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Audio");
                            ps2.execute();*/
                            break; // break es opcional    
                          case "3" :
                            nombreArchivo = "factura_1.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional    
                          case "4" :
                            nombreArchivo = "factura_2.png";
                            fc.Imagenes(folder,nombreArchivo,urlImagen);
                            queryString4 = "INSERT INTO engModuleFile(ModuleID,ID,FileName,FilePath,Description)VALUES (?,?,?,?,?);";
                            ps2 = conn.prepareStatement(queryString4);
                            ps2.setInt(1, 1214);
                            ps2.setInt(2, iddoc);
                            ps2.setString(3, nombreArchivo);
                            ps2.setString(4, folder);
                            ps2.setString(5, "Factura");
                            ps2.execute();
                            break; // break es opcional 
                          default:       
                        }
                      }
                    }
                    break;


                  case "0032B318-87CC-4AA5-8F71-A93178707B68": //ORDEN DE SERVICIO-IMSS-CONTRATO 25
                  
                    break;
                  case "1pnGRbxs3h": //ORDEN DE SERVICIO-ISEM-CONTRATO 28
    
                    break;
                  case "606393A4-9CBF-4B71-84C9-1918A91B8A62": //ORDEN DE SERVICIO-ISSEMYM-CONTRATO 12
                    
                    break;
                  case "9119AA9A-3D7A-4F5F-85FD-F58E930131D9": //ORDEN DE SERVICIO-SIN CONTRATO  
                
                    break;


                  case "0qqwGd56nu":  //REPORTE DE SERVICIO IMSS
              
                    break;
                  case "Kblo3Bs48D":  //REPORTE DE SERVICIO ISEM
              
                    break;
                  case "1ZY27hxh2L":  //REPORTE DE SERVICIO ISSEMYM
                
                    break;
                  case "Rqgxb7cadh":  //REPORTE DE SERVICIO-SIN CONTRATO
                
                    break;            
                }

              }
              
            }  

          }  

        } catch (SQLException ex) {
          System.out.println("Error connecting to the DB");
          ex.printStackTrace();
        } catch (Exception e) {
          System.out.println("Exception Error: ");
          e.printStackTrace();
        } finally {
          try { rs.close(); } catch (Exception e) { /* ignored */ }
          try { ps.close(); } catch (Exception e) { /* ignored */ }
          try { ps2.close(); } catch (Exception e) { /* ignored */ }
          try { conn.close(); } catch (Exception e) { /* ignored */ }
        } 
        
    }
}


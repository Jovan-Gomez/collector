import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class PruebaImagenes{

    /*public static void main(String[] args) {
        PruebaImagenes pp = new PruebaImagenes();
        String folder = pp.DirectorioNuevo();
        String url1 = "https://and.visitrack.com/WebResource.aspx?e=PICTURE&id=a511581a-7ecf-4394-845c-259fc07dfb9a";
        pp.Imagenes(folder,"uno.png",url1);
        pp.Imagenes(folder,"dos.png",url1);
    }*/

    public String DirectorioNuevo(String carpeta){
        String folder = "C:/Compac/documents/VT/"+carpeta+"/";
        //String folder = "C:/Users/DEMO/Desktop/New folder/"+carpeta+"/";
        String folderInsertar = "C:\\Compac\\documents\\VT\\"+carpeta+"\\";
        //String folderInsertar = "C:\\Users\\DEMO\\Desktop\\New folder\\"+carpeta+"\\";

		File directorio = new File(folder);
        if (!directorio.exists()) {
            if (directorio.mkdirs()) {
				System.out.println("Directorio creado");
            } else {
				System.out.println("Error al crear directorio");
            }
		}
		return folderInsertar;
    }
    
    public void Imagenes(String folder,String nombre,String urlext){
		String url = urlext; //dirección url del recurso a descargar
        String name = nombre; //nombre del archivo destino
        File file = new File(folder + name);

        try {

            URLConnection conn = new URL(url).openConnection();
            do{
                conn = new URL(url).openConnection();
            }while(conn.getContentLength() <= 0);
            conn.connect();
            System.out.println("\nempezando descarga: \n");
            System.out.println(">> URL: " + url);
            System.out.println(">> Nombre: " + name);
            System.out.println(">> tamaño: " + conn.getContentLength() + " bytes");


            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream(file);

            int b = 0;
            while (b != -1) {
            b = in.read();
            if (b != -1)
                out.write(b);
            }


            out.close();
            in.close();
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}    

    






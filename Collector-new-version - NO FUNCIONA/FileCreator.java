import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileCreator {
	//String carpeta;
	//String url1;

	/*public FileCreator(String carpeta,String url1){
		this.carpeta = carpeta;
		this.url1 = url1;
	}*/


	public String DirectorioNuevo(String carpeta){
		String ruta = "C:/Users/DEMO/Desktop/New folder/"+carpeta;
		File directorio = new File(ruta);
        if (!directorio.exists()) {
            if (directorio.mkdirs()) {
				System.out.println("Directorio creado");
            } else {
				System.out.println("Error al crear directorio");
            }
		}
		return ruta;
	}

	public void Imagenes(String url1, String ruta, String nombre){
		try {
            // Url con la foto
			//String uu = "https://and.visitrack.com/WebResource.aspx?e=PICTURE&id=a511581a-7ecf-4394-845c-259fc07dfb9a";
			URL url = new URL(url1);

			// establecemos conexion
			URLConnection urlCon = url.openConnection();

			// Sacamos por pantalla el tipo de fichero
			System.out.println(urlCon.getContentType());

			// Se obtiene el inputStream de la foto web y se abre el fichero
			// local.
			String rutaImg = ruta+"/"+nombre;
			InputStream is = urlCon.getInputStream();
			FileOutputStream fos = new FileOutputStream(rutaImg);

			// Lectura de la foto de la web y escritura en fichero local
			byte[] array = new byte[1000]; // buffer temporal de lectura.
			int leido = is.read(array);
			while (leido > 0) {
				fos.write(array, 0, leido);
				leido = is.read(array);
			}

			// cierre de conexion y fichero.
			is.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

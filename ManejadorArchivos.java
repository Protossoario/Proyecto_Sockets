/**
 * Clase que ejecuta los metodos para listar archivos en el directorio actual, borrar archivos, y copiar archivos de manera local. Se debe implementar tanto en la maquina del cliente como la del servidor.
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
public class ManejadorArchivos {
	// Metodo que regresa los nombres de los archivos en el directorio actual
	public static String[] listarArchivos() {
		File directorioActual = new File(".");
		File[] archivos = directorioActual.listFiles();
		String[] nombres = new String[archivos.length];
		for (int i = 0; i < archivos.length; i++) {
			nombres[i] = archivos[i].getName();
		}
		return nombres;
	}
	// Metodo que intenta borrar el archivo que recibe como parametro, y regresa true si lo logra, false si no
	public static boolean borrarArchivo(String archivo) {
		Path archivoPath = Paths.get(archivo);
		try {
			Files.delete(archivoPath);
			return true;
		}
		catch (NoSuchFileException ex) {
			System.err.println(archivo + ": no existe tal archivo o directorio");
		}
		catch (DirectoryNotEmptyException ex) {
			System.err.println(archivo + " no esta vacio");
		}
		catch (IOException ex) {
			System.err.println(ex);
		}
		return false;
	}
	// Metodo que intenta copiar un archivo, recibiendo como parametro el nombre original del archivo, y el nombre del archivo al que se quiere copiar
	public static boolean copiarArchivo(String archivoOriginal, String archivoNuevo) {
		Path archivoOriginalPath = Paths.get(archivoOriginal);
		Path archivoNuevoPath = Paths.get(archivoNuevo);
		try {
			Files.copy(archivoOriginalPath, archivoNuevoPath);
			return true;
		}
		catch (FileAlreadyExistsException ex) {
			System.err.println(archivoNuevo + " ya existe");
		}
		catch (DirectoryNotEmptyException ex) {
			System.err.println(archivoNuevo + " no esta vacio");
		}
		catch (IOException ex) {
			System.err.println(ex);
		}
		return false;
	}
}

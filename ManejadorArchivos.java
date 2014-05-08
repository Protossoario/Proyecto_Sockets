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
		File directorioActual = new File("."); // guarda el path del directorio actual como un File
		File[] archivos = directorioActual.listFiles(); // guarda la lista de archivos en un arreglo de File
		String[] nombres = new String[archivos.length]; // guarda el nombre de los archivos en un arreglo String
		for (int i = 0; i < archivos.length; i++) {
			nombres[i] = archivos[i].getName();
		}
		return nombres;
	}
	// Metodo que intenta borrar el archivo que recibe como parametro, y regresa true si lo logra, false si no
	public static boolean borrarArchivo(String archivo) {
		Path archivoPath = Paths.get(archivo); // guarda el path del archivo
		try {
			Files.delete(archivoPath); // borra el path
			return true; // si logro borrar el archivo
		}
		catch (NoSuchFileException ex) {
			System.err.println(archivo + ": no existe tal archivo o directorio");
		}
		catch (DirectoryNotEmptyException ex) { // no puede borrar un directorio que tenga cosas adentro
			System.err.println(archivo + " no esta vacio");
		}
		catch (IOException ex) {
			System.err.println(ex);
		}
		return false; // no logro borrar el archivo
	}
	// Metodo que intenta copiar un archivo, recibiendo como parametro el nombre original del archivo, y el nombre del archivo al que se quiere copiar
	public static boolean copiarArchivo(String archivoOriginal, String archivoNuevo) {
		Path archivoOriginalPath = Paths.get(archivoOriginal); // guarda el path del archivo original
		Path archivoNuevoPath = Paths.get(archivoNuevo); // guarda el path del nuevo archivo
		try {
			Files.copy(archivoOriginalPath, archivoNuevoPath); // copia el path1 en el path2
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

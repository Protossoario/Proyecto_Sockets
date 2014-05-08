import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente2 implements Operaciones {
	public static void main(String[] args) {
		/** Inicio de conexion del socket (codigo de ejemplo javax) */
		Socket yo = null; // socket del cliente
		PrintWriter alServidor = null; // mandar cosas en un string al servidor
		Scanner delTeclado; // lee input del teclado
		BufferedReader delServidor = null; // lee strings que el servidor le manda al cliente
		String tecleado; // guardar lo que recibe del teclado

		try {
			yo = new Socket(args[0],5000);
		}
		catch (UnknownHostException e){
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}
		System.out.println("Conectado:" + yo.toString());
		/* Fin de la conexion del socket */

		// objeto para leer input del usuario desde el teclado
		delTeclado = new Scanner(System.in);
		try {
			// objeto para enviar lineas de texto al servidor
			alServidor = new PrintWriter(yo.getOutputStream(),true);
			// objeto para recibir lineas de texto desde el servidor
			delServidor = new BufferedReader(new InputStreamReader(yo.getInputStream()));
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// Interfaz de comandos
		String respuesta;
		int operacion;
		do {
			System.out.print("> ");
			tecleado = delTeclado.nextLine(); // guardar lo que se teclea
			alServidor.println(tecleado); // Enviar comando al servidor
			try {
				operacion = Integer.parseInt(delServidor.readLine()); // Espera la indicacion del servidor
				while (operacion != NONE && operacion != END) { // cualquier operacion que no termine ejecucion ni conexion
					switch (operacion) {
					// El cliente lee otra linea del teclado y se la envia al servidor
					case READ_LINE:
						tecleado = delTeclado.nextLine(); // input del teclado del cliente
						alServidor.println(tecleado); // enviar al sevidor
						break;
					// El cliente lee una linea del servidor y la imprime como una nueva linea en la terminal
					case PRINT_LINE:
						respuesta = delServidor.readLine(); // lo que manda el servidor
						System.out.println(respuesta); // imprimir lo que mando el servidor en una nueva linea
						break;
					// EL cliente lee una linea del servidor y la imprime sin salto de linea
					case PRINT:
						respuesta = delServidor.readLine(); // lo que manda el servidor
						System.out.print(respuesta); // imprimir en la misma linea lo que mando el servidor
						break;
					// El cliente recibe del servidor el nombre de un archivo como String y envia el archivo con ese nombre al servidor a traves del socket
					case TRANSFER_FILE:
						String archivoNom = delServidor.readLine(); // servidor manda el nombre del archivo
						File archivo = new File(archivoNom); 
						byte[] datos = new byte[(int) archivo.length()]; // guarda el archivo en arreglo de bytes
						alServidor.println(datos.length); // enviar al servidor el arreglo de bytes del archivo
						BufferedInputStream lectorArchivo = new BufferedInputStream(new FileInputStream(archivo)); // cliente lee el archivo que tiene en su directorio
						lectorArchivo.read(datos, 0, datos.length); // lee el archivo y lo guarda en el arreglo de bytes
						OutputStream os = yo.getOutputStream(); // objeto que conecta el cliente con el servidor para enviar los bytes
						System.out.println("Transfiriendo archivo: " + archivoNom);
						os.write(datos, 0, datos.length); // envia al servidor el arreglo de bytes
						os.flush(); // asegurar que si se manden los bytes (y no se queden solo guardados en la variable)
						lectorArchivo.close();
						System.out.println("Archivo enviado con exito");
						break;
                    // El cliente imprime una lista del nombre de los archivos en el directorio actual de si mismo
                    case LISTAR:
                        String s[];
                        s = ManejadorArchivos.listarArchivos(); // llama a listarArchivos() de la clase ManejadorArchivos
                        for(int i=0;i<s.length;i++) {
                            System.out.println(s[i]); // imprime cada nombre de archivo en una nueva linea
                        }
                        break;
                    // El cliente recibe del servidor el nombre de un archivo como String y borra el archivo de la carpeta
                    case BORRAR:
                        String archivoNom = delServidor.readLine(); // servidor manda el nombre del archivo
                        // llama a borrarArchivo enviando como parametro el nombre el archivo
                        boolean resultadoB=ManejadorArchivos.borrarArchivo(archivoNom); // true si se pudo borrar, false no se pudo
                        if(resultadoB)
                            System.out.println("Archivo borrado.");
                        else
                            System.out.println("No se pudo borrar.");
                        break;
                    // El cliente recibe del servidor dos nombres de archivo: el que se desea copiar y el nombre del nuevo archivo que se creara 
                    case COPIAR:
                        String nombreArchivo1 = delServidor.readLine(); // servidor manda el nombre del archivo a copiar
                        String nombreArchivo2 = delServidor.readLine(); // servidor manda el nombre del archivo nuevo
                        // llama a copiarArchivo enviando como parametros los dos archivos
                        boolean resultado=ManejadorArchivos.copiarArchivo(nombreArchivo1,nombreArchivo2); // true si se pudo copiar, false no se pudo
                        if(resultado)
                            System.out.println("Archivo copiado.");
                        else
                            System.out.println("No se pudo copiar.");
                        break;
					}
					operacion = Integer.parseInt(delServidor.readLine()); // lee una nueva operacion del cliente
				}
			}
			catch (IOException ex) {
				System.err.println(ex);
				ex.printStackTrace();
				operacion = END;
			}
		} while (operacion != END); // el cliente deja de recibir comandos y se cierra cuando el servidor le envia la operacion de finalizacion
		System.out.println("Cerrando el programa");
		try {
			delServidor.close();
			delTeclado.close();
			alServidor.close();
			yo.close();
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}

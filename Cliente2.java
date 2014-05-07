import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente2 implements Operaciones {
	public static void main(String[] args) {
		Socket yo = null;
		PrintWriter alServidor = null;
		Scanner delTeclado;
		BufferedReader delServidor = null;
		String tecleado;

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
	
		delTeclado = new Scanner(System.in);
		try {
			alServidor  = new PrintWriter(yo.getOutputStream(),true);
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
			tecleado = delTeclado.nextLine();
			alServidor.println(tecleado); // Enviar comando al servidor
			try {
				operacion = Integer.parseInt(delServidor.readLine()); // Espera la indicacion del servidor
				while (operacion != NONE && operacion != END) {
					switch (operacion) {
					// Le indica al cliente que debe leer otra linea del teclado y enviarla al servidor
					case READ_LINE:
						tecleado = delTeclado.nextLine();
						alServidor.println(tecleado);
						break;
					// Le indica al cliente que debe leer una linea del servidor e imprimirla como una nueva linea en la terminal
					case PRINT_LINE:
						respuesta = delServidor.readLine();
						System.out.println(respuesta);
						break;
					// Le indica al cliente que debe leer una linea del servidor e imprimirla sin salto de linea
					case PRINT:
						respuesta = delServidor.readLine();
						System.out.print(respuesta);
						break;
					}
					operacion = Integer.parseInt(delServidor.readLine());
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

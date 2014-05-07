import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente2{
	public static void main(String[] args){
		Socket yo = null;
		PrintWriter alServidor = null;
		Scanner delTeclado;
		DataInputStream delServidor = null;
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
			delServidor = new DataInputStream(yo.getInputStream());
		}
		catch (IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}		

		// Validar inicio de sesion
		int intentos = 0;
		boolean aceptado = false;
		do {
			System.out.print("Introduzca su nombre de usuario: ");
			tecleado = delTeclado.nextLine();
			alServidor.println(tecleado);
			System.out.print("Introduzca su contraseña: ");
			tecleado = delTeclado.nextLine();
			alServidor.println(tecleado);
			try {
				aceptado = delServidor.readBoolean();
			}
			catch (IOException e){
				System.err.println(e.getMessage());
				System.exit(1);
			}
			intentos++;
			if (!aceptado) {
				System.out.println("Credenciales invalidas (" + (3 - intentos) + " intentos restantes)");
			}
		} while (intentos < 3 && !aceptado);	

		// Si el usuario no fue aceptado, se tuvo que pasar del limite de intentos
		if (!aceptado) {
			System.out.println("Limite de intentos excedido");
		}
		// Si el usuario y contraseña fueron aceptados, continuar la ejecucion del programa
		else {
			System.out.println("Bienvenido");
		}

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

import java.io.*;
import java.net.*;

public class ServidorM2{
	public static void main(String[] args){
		ServerSocket servidor = null;
		Socket cliente = null;
		boolean escuchando = true;
		final int PUERTO = 5000; // Puerto del servidor

		// Abrir el socket del servidor
		try {
			servidor = new ServerSocket(PUERTO);
			System.out.println("Socket escuchando en puerto "+ PUERTO);
		}
		catch (IOException e) {
			System.err.println("Error al abrir ServerSocket: "+e.getMessage());
			System.err.println("Terminando ejecucin del servidor.......");
			System.exit(1);
		}

		// Servidor escuchando por peticiones de conexin
		// Ciclo principal, creando un hilo para manejar cada conexin pedida
		while (escuchando) {
			try {
				cliente = servidor.accept();
			}
			catch (IOException e) {
				System.err.println("Error al llegar una peticin de conexin: " + e.getMessage());
				cliente = null;
			}
			if (cliente != null) {
				new AtiendeM2(cliente).start();
			}
		}
		try {
			servidor.close();
		}
		catch (IOException e) {}
	}
}

class DatosSocket {
	InetAddress dirIPLocal  = null;
	InetAddress dirIPRemota = null;
	int puertoLocal         = 0;
	int puertoRemoto        = 0;

	public DatosSocket(Socket socket) {
		dirIPRemota = socket.getInetAddress();
		puertoRemoto = socket.getPort();
		dirIPLocal = socket.getLocalAddress();
		puertoLocal = socket.getLocalPort();
	}

	public String toString() {
		String s = "Remoto: "+ dirIPRemota.getHostAddress() + "/" + puertoRemoto;
		s += "  Local: "+ dirIPLocal.getHostAddress() + "/" + puertoLocal;
		return s;
	}

	public String getDireccionRemota() {
		return dirIPRemota.getHostAddress();
	}

	public String getDireccionLocal() {
		return dirIPLocal.getHostAddress();
	}
}

class AtiendeM2 extends Thread {
	private BufferedReader entrada;
	private DataOutputStream salida;
	private String llego;
	private Socket cliente = null;
	DatosSocket dSocket = null;

	public AtiendeM2(Socket cliente){
		this.cliente = cliente;
		dSocket = new DatosSocket(cliente);
		System.out.println("Ya se conecto --> " + dSocket.toString());
	}

	public void run() {
		try {
        		entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			salida = new DataOutputStream(cliente.getOutputStream());
			// Validar por el lado del servidor el usuario y contraseña un maximo de 3 veces
			int intentos = 0;
			boolean aceptado = false;
			String usuario, contrasena;
			do {
				usuario = entrada.readLine();
				contrasena = entrada.readLine();
				System.out.println("("+dSocket.toString()+") Intento de conexion:");
				System.out.println("Usuario: " + usuario);
				System.out.println("Contraseña: " + contrasena);
				aceptado = validarUsuario(usuario, contrasena);
				if (aceptado) {
					salida.writeBoolean(true);
				}
				else {
					salida.writeBoolean(false);
				}
				intentos++;
			} while (intentos < 3 && !aceptado);

			// Si el usuario fue aceptado, continuar la ejecucion
			if (aceptado) {
				// TODO: guardar el log de inicio de sesion
				System.out.println("("+dSocket.toString()+") inicio sesion con el nombre de usuario: " + usuario);
			}
		}
		// Para que el ctrl-C no haga "tronar" al servidor
		catch (SocketException se) {
			System.err.println("Error al recibir datos, cerrando conexin.....");
		}
		catch (IOException e) {
			System.err.println("Error al recibir datos: " + e.getMessage());
			e.printStackTrace();
		}
		try {
			entrada.close();
			salida.close();
			cliente.close();
		}
		catch(IOException e){}
		System.out.println("Ya se desconecto --> "+ dSocket.toString());
	}

	// Metodo para validar las credenciales de un usuario intentando conectarse, leyendo del archivo usuarios.txt los nombres y contrasenas validos
	private boolean validarUsuario(String usuario, String contra) {
		try {
			BufferedReader delArchivo = new BufferedReader(new FileReader("usuarios.txt"));
			String linea = delArchivo.readLine();
			while (linea != null) {
				// Separar las lineas del archivo usando el caracter ':' como delimitador
				String[] info = linea.split(":");
				if (info.length != 2) {
					System.err.println("Error de formato en el archivo de usuarios.txt: " + linea);
				}
				// Validar las credenciales del usuario con la informacion de la linea leida del archivo usuarios.txt
				else if (info[0].compareTo(usuario) == 0 && info[1].compareTo(contra) == 0) {
					return true;
				}
				linea = delArchivo.readLine();
			}
			delArchivo.close();
		}
		catch (IOException ex) {
			System.err.println("Error leyendo el archivo de usuarios: " + ex.getMessage());
			ex.printStackTrace();
		}
		return false;
	}
}

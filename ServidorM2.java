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

class AtiendeM2 extends Thread implements Operaciones {
	private BufferedReader entrada;
	private PrintWriter salida;
	private Socket cliente = null;
	private final int MAX_INTENTOS = 3;
	DatosSocket dSocket = null;

	public AtiendeM2(Socket cliente){
		this.cliente = cliente;
		dSocket = new DatosSocket(cliente);
		System.out.println("Ya se conecto --> " + dSocket.toString());
	}

	public void run() {
		try {
        		entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			salida = new PrintWriter(cliente.getOutputStream(), true);
			String leido, usuario;
			String[] comandos;
			int intentos = 0; // numero de intentos de inicio de sesion
			boolean aceptado = false; // indica si el usuario ya inicio sesion con exito
			do {
				leido = entrada.readLine();
				comandos = leido.split(" ");
				if (comandos.length == 0) {
					salida.println(NONE);
				}
				else if (comandos[0].equals("exit")) {
					salida.println(END);
				}
				else if (comandos[0].equals("log") && !aceptado) {
					intentos++;
					usuario = comandos[1];
					salida.println(PRINT);
					salida.println("Contraseña: ");
					salida.println(READ_LINE);
					String contrasena = entrada.readLine();
					salida.println(PRINT_LINE);
					aceptado = validarUsuario(usuario, contrasena);
					if (aceptado) {
						salida.println("Bienvenido");
						// TODO: agregar el usuario al log
					}
					else {
						salida.println("Credenciales rechazadas (" + (MAX_INTENTOS - intentos) + " intentos restantes)");
					}
					salida.println(NONE);
				}
				else if (!aceptado) {
					salida.println(PRINT_LINE);
					salida.println("Comando rechazado, inicia sesion con el comando \"log <nombre de usuario>\"");
					salida.println(NONE);
				}
				else {
					salida.println(NONE);
				}
			} while (!comandos[0].equals("exit")); // se termina la sesion cuando el usuario envia el comando de exit
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

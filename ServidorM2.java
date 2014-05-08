import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.util.Date;

public class ServidorM2{
	public static void main(String[] args){
		ServerSocket servidor = null;  // Inicializa el servidor
		Socket cliente = null; // Aceptar conexiones de clientes
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

		// Servidor escuchando por peticiones de conexion
		// Ciclo principal, creando un hilo para manejar cada conexion pedida
		while (escuchando) {
			try {
				// espera hasta que llegue una peticion de conexion y la guarda en variable cliente
				cliente = servidor.accept(); // socket del cliente
			}
			catch (IOException e) {
				System.err.println("Error al llegar una peticion de conexion: " + e.getMessage());
				cliente = null;
			}
			if (cliente != null) {
				new AtiendeM2(cliente).start(); // manda como parametro el socket del cliente
				/* cada que un nuevo cliente se conecta, se inicializa un nuevo thread AtiendeM2
				que va a estar corriendo el codigo para atender al cliente */
			}
		}
		try {
			servidor.close();
		}
		catch (IOException e) {}
	}
}

/* cuando se hace la llamada de AtiendeM2, se crea una instancia de DatosSocket */
class DatosSocket {
	InetAddress dirIPLocal  = null; // Direccion IP de Servidor
	InetAddress dirIPRemota = null; // Direccion IP de Cliente
	int puertoLocal         = 0; // Puerto con el que esta escuchando Servidor 5000
	int puertoRemoto        = 0; // Puerto del cliente

	public DatosSocket(Socket socket) { // Recibe como parametro el socket del cliente que se conecto
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

/* el hilo para cada cliente */
class AtiendeM2 extends Thread implements Operaciones {
	private BufferedReader entrada; // lo que se lee del cliente
	private PrintWriter salida; // por donde se manda algo al cliente
	private PrintWriter bitacoraSesion;
	private PrintWriter bitacoraUsuario;
	private DateFormat df;
	private Socket cliente = null; // variable de conexion que recibe el servidor
	private final int MAX_INTENTOS = 3; // intentos de log in del usuario
	private final int MAX_SIZE = 1000000; // tamano maximo en bytes del buffer para la transferencia de archivos del cliente al servidor
	DatosSocket dSocket = null;

	public AtiendeM2(Socket cliente){ // recibe el socket del cliente
		this.cliente = cliente;
		dSocket = new DatosSocket(cliente); // guarda los datos del socket
		System.out.println("Ya se conecto --> " + dSocket.toString());
	}

	// comunicacion del servidor con el cliente
	public void run() {
		String usuario = "";
		boolean aceptado = false; // indica si el usuario ya inicio sesion con exito
		try {
        	entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
			salida = new PrintWriter(cliente.getOutputStream(), true);
			bitacoraSesion = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true)));
			df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			String leido; // guarda lo que lee de la entrada del cliente
			String[] comandos;
			int intentos = 0; // numero de intentos de log in
			boolean comando = false; // checa si el usuario introdujo un comando valido para registrar en el log
			do {
				leido = entrada.readLine(); // entrada del cliente
				comandos = leido.split(" "); // parte el comando en un arreglo de strings

				if (comandos.length == 0) { // no mando nada el cliente
					salida.println(NONE); // manda la constante none (finalizar la ejecucion del comando; next comando)
				}
				else if (comandos[0].equals("exit")) {
					salida.println(END); // cerrar la conexion con el cliente
					comando = true;
				}
				else if (comandos[0].equals("log") && !aceptado) { //log in de un usuario que NO ha iniciado sesion
					intentos++; // aumenta contador de intentos de log in
					usuario = comandos[1]; // cuenta del usuario
					salida.println(PRINT); // servidor le manda un string al cliente para que lo imprima
					salida.println("Contraseña: "); // pide contraseña del usuario
					salida.println(READ_LINE); // el cliente lee el string y se lo manda al servidor
					String contrasena = entrada.readLine(); // servidor en espera hasta que llegue el string del cliente
					salida.println(PRINT_LINE);
					aceptado = validarUsuario(usuario, contrasena); // revisar archivo de texto para validar usuario
					if (aceptado) {
						salida.println("Bienvenido");
						bitacoraSesion.println("Inicio sesion: " + usuario + "; " + df.format(new Date()));
						bitacoraUsuario = new PrintWriter(new BufferedWriter(new FileWriter(usuario + ".txt", true)));
					}
					else {
						salida.println("Credenciales rechazadas (" + (MAX_INTENTOS - intentos) + " intentos restantes)");
					}
					salida.println(NONE); // finaliza ejecucion del comando de log in
					comando = true;
				}
				else if (!aceptado) { // no permite ejecutar nada, sin haber iniciado sesion
					salida.println(PRINT_LINE);
					salida.println("Comando rechazado, inicia sesion con el comando \"log <nombre de usuario>\"");
					salida.println(NONE);
				}
				/*METODOS CLIENTE*/
				else if (comandos[0].equals("ipinfoL")) {
					salida.println(PRINT_LINE);
					salida.println("IP local: " + dSocket.getDireccionRemota());
					salida.println(NONE);
					comando = true;
				}

				else if(comandos[0].equals("dirL"))
                {//mostrar todos los archivos del directorio actual
                    if(comandos.length==1)
                    {//llamada al metodo listar archivos
                        salida.println(LISTAR);
                        salida.println(NONE);
                    }
                    else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"dirL\"");
						salida.println(NONE);
					}
		    comando = true;

                }
				else if(comandos[0].equals("rmL"))
                {//comando para borrar archivos
                    if(comandos.length==2)
                    {
                        salida.println(BORRAR);
                        salida.println(comandos[1]);
                        salida.println(NONE);

                    }
                    else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"rmL\" <archivo>");
						salida.println(NONE);
					}

		    comando = true;
                }
                else if(comandos[0].equals("cpL"))
                {//copiar archivo
                    if(comandos.length==3)
                    {
                        salida.println(COPIAR);
                        salida.println(comandos[1]);
                        salida.println(comandos[2]);
                        salida.println(NONE);
                    }
                    else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"cpL\" <archivo1> <archivo2>");
						salida.println(NONE);
					}
		    comando = true;
                }


				/*METODOS SERVIDOR*/
				else if (comandos[0].equals("ipinfoR")) {
					salida.println(PRINT_LINE);
					salida.println("IP remota: " + dSocket.getDireccionLocal());
					salida.println(NONE);
					comando = true;
				}
				else if(comandos[0].equals("dirR"))
                {//mostrar todos los archivos del directorio actual
                    if(comandos.length==1)
                    {//llamada al metodo listar archivos
                        String s[];
                        s=ManejadorArchivos.listarArchivos();
                        for(int i=0;i<s.length;i++)
                        {
                            salida.println(PRINT_LINE);
                            salida.println(s[i]);
                        }
                        salida.println(NONE);
                    }
                    else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"dirR\"");
						salida.println(NONE);
					}

		    comando = true;
                }
				else if(comandos[0].equals("rmR"))
                {//comando para borrar archivos
                    if(comandos.length==2)
                    {//llamada al metodo borrarArchivo

                        salida.println(PRINT_LINE);
                        boolean s=ManejadorArchivos.borrarArchivo(comandos[1]);
                        if(s)
                        {
                            salida.println("Archivo borrado");
                        }
                        else
                        {
                            salida.println("No se pudo borrar");
                        }

                        salida.println(NONE);

                    }
                    else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"rmR\" <archivo>");
						salida.println(NONE);
					}

		    comando = true;
                }
                else if(comandos[0].equals("cpR"))
                {//copiar archivo
                    if(comandos.length==3)
                    {//llamada al metodo copiarArchivo
                        salida.println(PRINT_LINE);
                        boolean s=ManejadorArchivos.copiarArchivo(comandos[1],comandos[2]);
                        if(s)
                        {
                            salida.println("Archivo copiado");
                        }
                        else
                        {
                            salida.println("No se pudo copiar");
                        }

                        salida.println(NONE);
                    }
                    else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"cpR\" <archivo1> <archivo2>");
						salida.println(NONE);
					}
		    comando = true;
                }
                else if(comandos[0].equals("?"))
                {
                    salida.println(PRINT_LINE);
                    salida.println("Menu de comandos");
                    salida.println(PRINT_LINE);
                    salida.println("dirR");
                    salida.println(PRINT_LINE);
                    salida.println("rmR");
                    salida.println(PRINT_LINE);
                    salida.println("cpR");
                    salida.println(PRINT_LINE);
                    salida.println("dirL");
                    salida.println(PRINT_LINE);
                    salida.println("rmL");
                    salida.println(PRINT_LINE);
                    salida.println("cpL");
                    salida.println(PRINT_LINE);
                    salida.println("cpL2R");
                    salida.println(PRINT_LINE);
                    salida.println("ipinfoL");
                    salida.println(PRINT_LINE);
                    salida.println("ipinfoR");
                    salida.println(NONE);
		    comando = true;
                }

				else if (comandos[0].equals("cpL2R")) { // transferir un archivo desde el cliente hasta el servidor
					if (comandos.length == 3) {
						salida.println(TRANSFER_FILE);
						salida.println(comandos[1]); // enviar el nombre del archivo al cliente
						int bytesPorLeer = Integer.parseInt(entrada.readLine());
						byte[] buffer = new byte[bytesPorLeer];
						InputStream delCliente = cliente.getInputStream();
						BufferedOutputStream escritorArchivo = new BufferedOutputStream(new FileOutputStream(comandos[2]));
						delCliente.read(buffer, 0, buffer.length);
						escritorArchivo.write(buffer, 0, buffer.length);
						escritorArchivo.flush();
						escritorArchivo.close();
						salida.println(NONE);
					}
					else {
						salida.println(PRINT_LINE);
						salida.println("Error: utilice el comando \"cpL2R <archivo original> <archivo copiado>\"");
						salida.println(NONE);
					}
					comando = true;
				}
				else { // si es un comando que no reconoce, no hace nada y lo finaliza
					salida.println(NONE);
				}
				if (comando && aceptado) {
					bitacoraUsuario.println(comandos[0] + " " + df.format(new Date()));
				}
			} while (intentos < MAX_INTENTOS && (!comando || !comandos[0].equals("exit"))); /* va a seguir recibiendo comandos, mientras no reciba exit
													se termina la sesion cuando el usuario envia el comando de exit */
		}
		catch (SocketException se) { // Para que el ctrl-C no haga "tronar" al servidor
			System.err.println("Error al recibir datos, cerrando conexion.....");
		}
		catch (IOException e) {
			System.err.println("Error al recibir datos: " + e.getMessage());
			e.printStackTrace();
		}
		if (aceptado) {
			bitacoraSesion.println("Cerro sesion: " + usuario + "; " + df.format(new Date()));
		}

		// conexion se cierra: por el comando exit o por error
		try {
			salida.println(END);
			entrada.close();
			salida.close();
			cliente.close();
			if (aceptado) {
				bitacoraSesion.close();
				bitacoraUsuario.close();
			}
		}
		catch(IOException e){}
		System.out.println("Ya se desconecto --> "+ dSocket.toString());
	}

	/* Metodo para validar las credenciales de un usuario intentando conectarse,
	   lee del archivo usuarios.txt los nombres y contrasenas validos */
	private boolean validarUsuario(String usuario, String contra) {
		try {
			BufferedReader delArchivo = new BufferedReader(new FileReader("usuarios.txt")); // abre archivo usuarios.txt
			String linea = delArchivo.readLine(); // lee primera linea del archivo
			while (linea != null) {
				// Separar las lineas del archivo usando el caracter ':' como delimitador
				String[] info = linea.split(":");
				if (info.length != 2) {
					System.err.println("Error de formato en el archivo de usuarios.txt: " + linea);
				}
				// Validar las credenciales del usuario con la informacion de la linea leida del archivo usuarios.txt
				else if (info[0].equals(usuario) && info[1].equals(contra)) {
					return true; // usuario y contrasenas del usuario coinciden con un usuario en el archivo
				}
				linea = delArchivo.readLine(); // lee la siguiente linea
			}
			delArchivo.close();
		}
		catch (IOException ex) {
			System.err.println("Error leyendo el archivo de usuarios: " + ex.getMessage());
			ex.printStackTrace();
		}
		return false; // no encontro usuario y contrasena validos
	}
}

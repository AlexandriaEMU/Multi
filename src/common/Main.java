package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import communication.ComServer;


import realm.RealmServer;

public class Main {
	private static final String CONFIG = "config.txt";
	public static int REALM_PORT = -1;
	public static int REALM_COM_PORT = -1;
	public static String REALM_DB_HOST = null;
	public static String REALM_DB_USER = null;
	public static String REALM_DB_PASSWORD = null;
	public static String REALM_DB_NAME = null;
	public static int REALM_DB_COMMIT = 30*1000;
	public static boolean REALM_DEBUG = false;
	public static boolean REALM_IGNORE_VERSION = false;
	public static String CLIENT_VERSION = "1.29.1";
	public static boolean isInit = false;
	public static boolean isRunning = false;
	public static boolean USE_SUBSCRIBE = false;

	/* LOGS */
	public static BufferedWriter Log_Realm;
	public static BufferedWriter Log_Com;
	public static BufferedWriter Log_Errors;

	/* THEARDS */
	public static RealmServer realmServer;
	public static ComServer comServer;
	
	
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(Main::cerrarservidores));
		
		System.out.println("==============================================================");
		System.out.println("AlexandriaEMU - MULTI");
		System.out.println("Por Player-xD, basado en AncestraR v54 - Gracias DIABU");
		System.out.println("==============================================================\n");
		System.out.print("Cargando el archivo de configuracion: ");
		cargarconfiguracion();
		System.out.println("OK");
		Main.isInit = true;
		System.out.print("Conectando a la base de datos: ");
		if(SQLManager.setUpConnexion()) {
			System.out.println("OK");
		} else {
			System.out.println("Conexion invalida");
			Main.agregaralogdeerrores("SQL: Fallo la conexion");
			System.exit(0);
		}
		Realm.cargandomulti();
		System.out.print("\n\nCreacion de REALM en el puerto "+ Main.REALM_PORT);
		realmServer = new RealmServer();
		System.out.println(": OK");
		System.out.print("Creacion de COM en el puerto "+ Main.REALM_COM_PORT);
		comServer = new ComServer();
		System.out.println(": OK");
		
		System.out.println("\nAtento a nuevas conexiones\n");
		agregaralogdemulti("REALM iniciado: Atento a nuevas conexiones");
		agregaralogdecom("COM iniciado: Atento a nuevas conexiones");
	}
	
	private static void cargarconfiguracion() {
		try {
			BufferedReader config = new BufferedReader(new FileReader(CONFIG));
			String line = "";
			while((line = config.readLine()) != null) {
				if (line.split("=").length == 1)continue;
				String param = line.split("=")[0].trim();
				String value = line.split("=")[1].trim();
				
			if(param.equalsIgnoreCase("REALM_DB_COMMIT")) {
				Main.REALM_DB_COMMIT = Integer.parseInt(value);
			}else 
			if (param.equalsIgnoreCase("CLIENT_VERSION")) {
				Main.CLIENT_VERSION = value;
			}else
			if(param.equalsIgnoreCase("REALM_PORT")) {
				try{
					Main.REALM_PORT = Integer.parseInt(value);
				}catch(Exception e) {
					System.out.println("REALM_PORT doit etre un entier!"); System.exit(1);
				}
			}else
			if(param.equalsIgnoreCase("REALM_DB_HOST")) {
				Main.REALM_DB_HOST = value;
			}else
			if(param.equalsIgnoreCase("REALM_IGNORE_VERSION")) {
				Main.REALM_IGNORE_VERSION = (value.equalsIgnoreCase("true") ? true : false);
			}else
			if(param.equalsIgnoreCase("REALM_DB_USER")) {
				Main.REALM_DB_USER = value;
			}else
			if(param.equalsIgnoreCase("REALM_DB_PASSWORD")) {
                Main.REALM_DB_PASSWORD = Objects.requireNonNullElse(value, "");
			}else
			if(param.equalsIgnoreCase("REALM_DB_NAME")) {
				Main.REALM_DB_NAME = value;
			}else
			if(param.equalsIgnoreCase("REALM_DEBUG")) {
				Main.REALM_DEBUG = (value.equalsIgnoreCase("true") ? true : false);
			}else
			if(param.equalsIgnoreCase("REALM_COM_PORT")) {
				Main.REALM_COM_PORT = Integer.parseInt(value);
			}else
			if(param.equalsIgnoreCase("USE_SUBSCRIBE")) {
				Main.USE_SUBSCRIBE = (value.equalsIgnoreCase("true") ? true : false);
			}
			}
			if (REALM_DB_NAME == null || REALM_DB_HOST == null || REALM_DB_PASSWORD == null || REALM_DB_USER == null || REALM_PORT == -1 || REALM_COM_PORT == -1) {
				throw new Exception();
			}
		}catch(Exception e) {
            System.out.println(e.getMessage());
			System.out.println("Fichier de configuration non existant ou illisible !");
			System.out.println("Fermeture du serveur de connexion.");
			System.exit(1);
		}
		
		try {
			String date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(Calendar.getInstance().get(Calendar.MONTH) + 1)+"-"+Calendar.getInstance().get(Calendar.YEAR);
			if(!(new File("Realm_logs")).exists()) {
				new File("Realm_logs").mkdir();
			}
			if(!(new File("Error_logs")).exists()) {
				new File("Error_logs").mkdir();
			}
			if(!(new File("Com_logs")).exists()) {
				new File("Com_logs").mkdir();
			}
			Log_Realm = new BufferedWriter(new FileWriter("Realm_logs/"+date+".txt", true));
			Log_Com = new BufferedWriter(new FileWriter("Com_logs/"+date+".txt", true));
			Log_Errors = new BufferedWriter(new FileWriter("Error_logs/"+date+".txt", true));
		}catch(IOException e) {
			System.out.println("No se pudieron crear los logs");
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}
	
	public synchronized static void agregaralogdeerrores(String str) {
			try {
				String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
				Main.Log_Errors.write(date+": "+str);
				Main.Log_Errors.newLine();
				Main.Log_Errors.flush();
			}catch(Exception e) {
				System.out.println("No se pudieron escribir los logs");
			}
	}
	
	public synchronized static void agregaralogdemulti(String str) {
		try {
			String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
			Main.Log_Realm.write(date+": "+str);
			Main.Log_Realm.newLine();
			Main.Log_Realm.flush();
		}catch(Exception e) {
			System.out.println("No se pudieron escribir los logs");
		}
	}
	
	public synchronized static void agregaralogdecom(String str) {
		try {
			String date = Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND;
			Main.Log_Com.write(date+": "+str);
			Main.Log_Com.newLine();
			Main.Log_Com.flush();
		}catch(Exception e) {
			System.out.println("No se pudieron escribir los logs");
		}
	}
	
	public static void cerrarservidores() {
		if (isRunning) {
			agregaralogdemulti("REALM cerrado: Eliminando conexiones");
			isRunning = false;
			try {
				if(realmServer != null) realmServer.kickAll();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
			agregaralogdecom("COM cerrado: Eliminando conexiones");
			try {
				if(comServer != null) comServer.kickAll();
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
		isRunning = false;
		System.out.println("Cerrando el multi: OK");
	}
}
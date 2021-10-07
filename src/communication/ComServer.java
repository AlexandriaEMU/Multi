package communication;

import java.io.IOException;
import java.net.ServerSocket;

import common.Main;

public class ComServer implements Runnable {
	
	private ServerSocket _SS;
	private Thread _t;
	
	public ComServer() 
	{
		try
		{
			_SS = new ServerSocket(Main.REALM_COM_PORT);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}catch(IOException e)
		{
			System.out.println("COMServer: "+e.getMessage());
			Main.agregaralogdecom("COMServer: "+e.getMessage());
			Main.agregaralogdeerrores("COMServer: "+e.getMessage());
			Main.cerrarservidores();
		}
	}
	
	public void run() 
	{
		while(Main.isRunning)
		{
			try
			{
				new ComThread(_SS.accept());
			}catch(IOException e)
			{
				try {
					if(!_SS.isClosed()) _SS.close();
					} catch (IOException e1) {}
				System.out.println("COMServer Ejecutando: "+e.getMessage());
				Main.agregaralogdecom("COMServer Ejecutando: "+e.getMessage());
				Main.agregaralogdeerrores("COMServer Ejecutando: "+e.getMessage());
			}
		}
	}
	
	public void kickAll() {
		try {
			_SS.close();
		}catch(Exception e) {
			System.out.println("ComServerKickAll : "+e.getMessage());
			Main.agregaralogdecom("ComServerKickAll : "+e.getMessage());
			Main.agregaralogdeerrores("ComServerKickAll : "+e.getMessage());
		}
	}
	public Thread getThread() 
	{
		return _t;
	}
}

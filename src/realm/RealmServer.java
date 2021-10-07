package realm;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import common.Main;

public class RealmServer implements Runnable {

	private ServerSocket _SS;
	private Thread _t;
	private final ArrayList<RealmThread> _clients = new ArrayList<>();

	public RealmServer()
	{
		try
		{
			_SS = new ServerSocket(Main.REALM_PORT);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
		}catch(IOException e)
		{
			System.out.println("RealmServer : "+e.getMessage());
			Main.agregaralogdemulti("RealmServer : "+e.getMessage());
			Main.agregaralogdeerrores("RealmServer : "+e.getMessage());
			Main.cerrarservidores();
		}
	}

	public void run()
	{
		while(Main.isRunning)
		{
			try
			{
				_clients.add(new RealmThread(_SS.accept()));
			}catch(IOException e)
			{
				try {
					if(!_SS.isClosed()) _SS.close();
					} catch (IOException e1) {}
				System.out.println("RealmServerRun : "+e.getMessage());
				Main.agregaralogdemulti("RealmServerRun : "+e.getMessage());
				Main.agregaralogdeerrores("RealmServerRun : "+e.getMessage());
			}
		}
	}
	
	public void kickAll()
	{
		try
		{
			_SS.close();
		}catch(Exception e)
		{
			System.out.println("RealmServerKickAll : "+e.getMessage());
			Main.agregaralogdemulti("RealmServerKickAll : "+e.getMessage());
			Main.agregaralogdeerrores("RealmServerKickAll : "+e.getMessage());
		}
		ArrayList<RealmThread> c = new ArrayList<>(_clients);
		 for(RealmThread RT : c)
		 {
		 	try
		 	{
		 		RT.closeSocket();
		 	}catch(Exception e){};
	 	}
	}
	
	public void delClient(RealmThread RT)
	{
		_clients.remove(RT);
	}

	public Thread getThread()
	{
		return _t;
	}
}

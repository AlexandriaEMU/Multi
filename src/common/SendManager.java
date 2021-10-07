package common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;


public class SendManager {

	private static final Map<Integer, Map<Long, Map<PrintWriter, String>>> PacketBuffer	= new TreeMap<>();//<hachID, <PacketID, <PrintWriter,String>>>
	static long packetid = 1;
	private static String BufferRemove = "";
	
	public static String get_BufferRemove()
	{
		return BufferRemove;
	}
	
	public static void set_BufferRemove(String str)
	{
		BufferRemove += str;
	}
	
	public static void del_BufferRemove()
	{
		BufferRemove = "";
	}
	
	public static Map<Integer, Map<Long, Map<PrintWriter, String>>> getPacketBuffer()
	{
		return PacketBuffer;
	}
	
	public static void FlushTimer() {
	    ActionListener action = event -> {
			for(Entry<Integer, Map<Long, Map<PrintWriter, String>>> data : SendManager.getPacketBuffer().entrySet()) {
				if(SendManager.getPacketBuffer().get(data.getKey()).isEmpty()) continue;
				StringBuilder Totaldata = new StringBuilder();
				PrintWriter pw = null;
				for(Entry<Long, Map<PrintWriter, String>> s : SendManager.getPacketBuffer().get(data.getKey()).entrySet()) {
					for(Entry<PrintWriter, String> s2 : SendManager.getPacketBuffer().get(data.getKey()).get(s.getKey()).entrySet()) {
						Totaldata.append((s2.getValue())).append((char)0x00);
						if(pw != null && (pw.hashCode() == s2.getKey().hashCode())) continue;
						pw = s2.getKey();
					}
					SendManager.set_BufferRemove(s.getKey()+",");
				}
				if(Totaldata.toString().isEmpty()) continue;
				for(String id : SendManager.get_BufferRemove().split(",")) {
					data.getValue().remove(Long.parseLong(id));
				}

				SendManager.del_BufferRemove();
				if(pw != null) {
					pw.print(Totaldata.toString());
					pw.flush();
				}

				Main.agregaralogdemulti("REALM: Envia>>"+Totaldata.toString());
				if(Main.REALM_DEBUG) System.out.println("REALM: Envia>>"+Totaldata.toString());
			}
		};
	    return;
	}
	
	public static void send(PrintWriter out, String packet) {
		if(!getPacketBuffer().containsKey(out.hashCode())) {
			Map<PrintWriter, String> firstData = new TreeMap<>();
			firstData.put(out, packet);
			Map<Long, Map<PrintWriter, String>> secondData = new TreeMap<>();
			secondData.put(packetid++, firstData);
			PacketBuffer.put(out.hashCode(), secondData);
		}else {
			Map<PrintWriter, String> data = new TreeMap<>();
			data.put(out, packet);
			PacketBuffer.get(out.hashCode()).put(packetid++, data);
		}
	}
}
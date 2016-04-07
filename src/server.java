import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class server extends fcntcp{
	byte[] rcvBuffer = new byte[1460];
	DatagramPacket rcvPacket;
	DatagramSocket socket;
	byte[] tempAppData = new byte[3*1000000];
	
	server() throws IOException{
		init();
	}
	
	public void init() throws IOException{
		socket = new DatagramSocket(super.port);
		rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
		
		int dataSize = rcvPacketData();
		byte[] appData = new byte[dataSize];
		System.arraycopy(tempAppData, 0, appData, 0, dataSize);
		
		print.debug("Read data form client: " + dataSize);
		print.info("MD5 hash of the data read: " + super.md5hash(appData));
	}
	
	public int rcvPacketData() throws IOException{
		
		int appDataIndex = 0;
		int rcvIndex = 0;
		int rcvLen = 0;
		
		for(int i=0; i<4; i++){
			socket.receive(rcvPacket);
			rcvBuffer = rcvPacket.getData();
			rcvLen = rcvPacket.getLength();
			
			System.arraycopy(rcvBuffer, 20, tempAppData, appDataIndex, rcvLen-20);
			appDataIndex += rcvLen - 20;
		}
		
		return appDataIndex;
	}
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;


public class server extends fcntcp{
	byte[] rcvBuffer = new byte[1460];
	DatagramPacket rcvPacket;
	DatagramSocket socket;
	byte[] tempAppData = new byte[3*1000000];
	int windowMax = 1428;
	byte[] temp;
	int fileSize = 0;
	int ackNum = 0;
	
	server() throws IOException{
		init();
	}
	
	public void init() throws IOException{
		socket = new DatagramSocket(super.port);
		rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
		
		//Receive packet to determine file size to be received.
		socket.receive(rcvPacket);
		rcvBuffer = rcvPacket.getData();
		ackNum += 4;
		sendAck();
		
		temp = new byte[4];
		System.arraycopy(rcvBuffer, 20, temp, 0, 4);
		fileSize = ByteBuffer.wrap(temp).getInt();
		
		rcvAndAssembleData();
//		int dataSize = rcvPacketData();
//		byte[] appData = new byte[dataSize];
//		System.arraycopy(tempAppData, 0, appData, 0, dataSize);
		
//		print.debug("Read data form client: " + dataSize);
//		print.info("MD5 hash of the data read: " + super.md5hash(appData));
	}
	
	public int rcvAndAssembleData() throws IOException{
		
		int appDataIndex = 0;
		int rcvIndex = 0;
		int rcvLen = 0;
		int windowBase = 0;
		int windowIndex = 0;
		
		while(appDataIndex != fileSize){
			
			socket.receive(rcvPacket);
			rcvBuffer = rcvPacket.getData();
			rcvLen = rcvPacket.getLength();
			
			System.arraycopy(rcvBuffer, 20, tempAppData, appDataIndex, rcvLen-20);
			appDataIndex += rcvLen - 20;
			//windowBase
		}
		
		return appDataIndex;
	}
	
	public void sendAck(){
		//TODO
		//Set ACK bit and send class variable ack number in header.
	}
}

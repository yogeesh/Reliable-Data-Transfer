import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	int appDataIndex = 0;
	InetAddress clientAdd;
	int sendPort;
	
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
		
		//Setting client information
		clientAdd = rcvPacket.getAddress();
		sendPort = rcvPacket.getPort();
		
		sendAck(ackNum);
		
		temp = new byte[4];
		System.arraycopy(rcvBuffer, 20, temp, 0, 4);
		fileSize = ByteBuffer.wrap(temp).getInt();
		
		print.debug("Expected File Size: " + fileSize);
		
		rcvAndAssembleData();

		byte[] appData = new byte[appDataIndex];
		System.arraycopy(tempAppData, 0, appData, 0, appDataIndex);
		
		print.debug("Read data form client: " + appDataIndex);
		print.info("MD5 hash of the data read: " + super.md5hash(appData));
	}
	
	public int rcvAndAssembleData() throws IOException{
		
		int rcvIndex = 0;
		int rcvLen = 0;
		int windowBase = 0;
		int windowIndex = 0;
		int seqNum;
		int expSeqNum = ackNum;
		
		while(appDataIndex != fileSize){
			
			socket.receive(rcvPacket);
			rcvBuffer = rcvPacket.getData();
			rcvLen = rcvPacket.getLength();
			
			seqNum = super.getSeqNumber(rcvBuffer);
			//print.debug("Seq Num recived : " + seqNum);
			
			if(seqNum != expSeqNum){
				sendAck(ackNum);
				continue;
			}
			else{
				ackNum += rcvLen - 20;
				expSeqNum = ackNum;
				sendAck(ackNum);
			}
			
			System.arraycopy(rcvBuffer, 20, tempAppData, appDataIndex, rcvLen-20);
			appDataIndex += rcvLen - 20;
			
		}
		
		return appDataIndex;
	}
	
	public void sendAck(int num) throws IOException{
		byte[] header = new byte[20];
		
		temp = new byte[4];
		temp = ByteBuffer.allocate(4).putInt(num).array();
		System.arraycopy(temp, 0, header, 4, 4);
		
		print.debug("Sending Ack Packet: Ack num = " + num);
		DatagramPacket sendPacket = new DatagramPacket(header, header.length, clientAdd, sendPort);
	    socket.send(sendPacket);
	}
}

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;


public class server extends fcntcp{
	byte[] rcvBuffer = new byte[windowSize];
	DatagramPacket rcvPacket;
	DatagramSocket socket;
	byte[] tempAppData = new byte[3*1000000];
	int windowMax = 64000;
	byte[] temp;
	int fileSize = 0;
	int ackNum = 0;
	int appDataIndex = 0;
	InetAddress clientAdd;
	int sendPort;
	int expSeqNum= 0;
	
	
	server() throws IOException{
		init();
	}
	
	public void init() throws IOException{
		socket = new DatagramSocket(super.port);
		rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);
		
		socket.receive(rcvPacket);
		
		//Setting client information
		clientAdd = rcvPacket.getAddress();
		sendPort = rcvPacket.getPort();
				
		//Receive packet to determine file size to be received.
		do{
			rcvBuffer = rcvPacket.getData();
			if(getSeqNumber(rcvBuffer) == expSeqNum){
				if(!super.checkPacketCorruption(rcvBuffer))
					break;
				sendAck(ackNum);
			}
			socket.receive(rcvPacket);
		}while(true);
		
		ackNum += 4;
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
		byte[] tempRcvData;
		
		expSeqNum = ackNum;
		
		
		while(appDataIndex != fileSize){
			socket.setSoTimeout(super.timeout);
			
			try{
				socket.receive(rcvPacket);
				
				rcvBuffer = rcvPacket.getData();
				rcvLen = rcvPacket.getLength();
				
				tempRcvData = new byte[rcvBuffer.length];
				System.arraycopy(rcvBuffer, 0, tempRcvData, 0, rcvLen);
				
				seqNum = super.getSeqNumber(tempRcvData);		
				print.debug("Seq Num recived : " + seqNum + " size = " + (rcvLen - 20) + " checksum = " + super.calChecksum(tempRcvData));
			
			}catch(SocketTimeoutException oops){
				sendAck(ackNum);
				continue;
			}
			

			//Check for corruption
			if(checkPacketCorruption(tempRcvData)){
				sendAck(ackNum);
				continue;
			}
						
			if(seqNum != expSeqNum){
				// unexpected sequence number: loss or corruption detected.
				print.debug("oops! expected seq sum = " + expSeqNum);
				sendAck(ackNum);
				continue;
			}
			else{
				// receiving in order.
				ackNum += rcvLen - 20;
				expSeqNum = ackNum;
				sendAck(ackNum);
				System.arraycopy(tempRcvData, 20, tempAppData, appDataIndex, rcvLen-20);
				appDataIndex += rcvLen - 20;
			}
		}
		
		return appDataIndex;
	}
	
	public void sendAck(int num) throws IOException{
		byte[] header = new byte[20];
		int checksum;
		
		temp = new byte[4];
		temp = ByteBuffer.allocate(4).putInt(num).array();
		System.arraycopy(temp, 0, header, 4, 4);
		
		header = super.addChecksum(header);
		
//		checksum = super.calChecksum(header);
//		temp = ByteBuffer.allocate(4).putInt(checksum).array();
//		System.arraycopy(temp, 2, header, 12, 2);
		
		print.debug("Sending Ack Packet: Ack num = " + num);
		DatagramPacket sendPacket = new DatagramPacket(header, header.length, clientAdd, sendPort);
	    socket.send(sendPacket);
	}
}

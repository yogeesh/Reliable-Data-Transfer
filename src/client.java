import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class client extends fcntcp{
	byte[] data;
	DatagramSocket server;
	InetAddress serverAddress;
	int fileSize;
	int MSS = 1440;
	byte[] temp;
	int seqNum = 0;
	
	//fast retransmit varibales
	int count = 0;
	int prevAck = 0;
	
	// skip test variable
	int skip = 0;
			
			
	client() throws IOException{
		init();
	}
	
	public void init() throws IOException{
		/**
		 * Read file into Byes array.
		 */
		try{
			InputStream file = new FileInputStream(super.fileName);
			fileSize = (int)new File(super.fileName).length();
			data = new byte[fileSize];
			file.read(data);
			file.close();
			
			print.debug("File read of size: " + fileSize);
			print.info("MD5 hash of the of the file: " + super.md5hash(data));
			
			server = new DatagramSocket();
			serverAddress = InetAddress.getByName(super.serverAdrress);
			
			//Letting server know of the file Size to be sent
			print.debug("Telling Server the file Size to be sent.");
			temp = new byte[4];
			temp = ByteBuffer.allocate(4).putInt(data.length).array();
			
			do{
				seqNum = 0;
				temp = ByteBuffer.allocate(4).putInt(data.length).array();
				createPacketSend(temp);
			}while(rcvCheckAck(4) != 4);
			seqNum = 4;
		}
		catch(IOException msg){
			print.debug("File could not be read. Check if file exists or check file permisssions");
			System.exit(1);
		}
		
		windowHandlePushData(data);
		//createSendPacket(data);
		
		print.info("File transmitted!");
		
		server.close();
	}
	
	public void windowHandlePushData(byte[] data) throws IOException{
		int dataIndex = 0;
		int windowBase = 0;
		int windowIndex = 0;
		int tempSeqNum = seqNum;
		int sendDataLen = 0;
		int ackFailSafeCheck;
				
		do{
			do{
				
				if (dataIndex+MSS > fileSize)
					sendDataLen = fileSize - dataIndex;
				else
					sendDataLen = MSS;
				temp = new byte[sendDataLen];
				System.arraycopy(data, dataIndex, temp, 0, sendDataLen);

				createPacketSend(temp);
				dataIndex += sendDataLen;
			}while((dataIndex-windowBase <= windowSize) && dataIndex < fileSize);
			// to handle wrong ack numbers
			ackFailSafeCheck = rcvCheckAck(dataIndex);
			if ((ackFailSafeCheck-tempSeqNum) <= dataIndex && (ackFailSafeCheck-tempSeqNum) >= windowBase)
				seqNum = ackFailSafeCheck;
			windowBase = seqNum - tempSeqNum;
			dataIndex = windowBase;
		}while(dataIndex < fileSize);
		//print.debug("" + dataIndex + " " + seqNum + " " + tempSeqNum);
	}
	
	public void createPacketSend(byte[] data) throws IOException{
		byte[] header = addSeqNum();
		byte[] packetData = new byte[data.length + 20];
		
		byte[] tempData = new byte[data.length];
		System.arraycopy(data, 0, tempData, 0, data.length);
		
		System.arraycopy(header, 0, packetData, 0, 20);
		System.arraycopy(tempData, 0, packetData, 20, tempData.length);
		
		packetData = addChecksum(packetData);
		
		print.debug("[Send]Sending Packet: seq num = " + seqNum + " data size: " + tempData.length);
		
		DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, serverAddress, super.port);
		
		//cheap skip test
		if (skip%5 == 0)
			print.debug("skipped packet");
		else
			server.send(sendPacket);
		skip++;
		
		seqNum += tempData.length;
		//print.debug("seq num changed to " + seqNum);
	}
	
	public byte[] addSeqNum(){
		byte[] header = new byte[20];
		
		temp = new byte[4];
		temp = ByteBuffer.allocate(4).putInt(seqNum).array();
		System.arraycopy(temp, 0, header, 0, 4);
		
		return header;
	}
	
	public int rcvCheckAck(int expEndAckNum) throws IOException{
		server.setSoTimeout(super.timeout); 
		int maxAckNum = -1;
		int ackNum = 0;
		count = 0;
		
		byte[] rcvBuffer = new byte[20];
		DatagramPacket rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);;
		
		while(ackNum < expEndAckNum){
			try{
				server.receive(rcvPacket);
				rcvBuffer = rcvPacket.getData();
				
				//checking for packet corruption
				if(super.checkPacketCorruption(rcvBuffer))
					continue;
					
				ackNum = getAckNumber(rcvBuffer);
				print.debug("[Rcv]Received Ack Packet: Ack Num = " + ackNum);
				
				if (ackNum > maxAckNum)  
					maxAckNum = ackNum;
				
				//fast retransmit implementation
				if(prevAck == maxAckNum){
					count++;
					if (count >= 3)
						return maxAckNum;
				}
				prevAck = maxAckNum;
				
			}catch(SocketTimeoutException oops){
				return maxAckNum;
			}
		}
		return maxAckNum;
	}
}

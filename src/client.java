import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class client extends fcntcp{
	byte[] data;
	DatagramSocket server;
	InetAddress serverAddress;
	int fileSize;
	int MSS = 536;
	byte[] temp;
	int windowSize = 4128;
	
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
			
		}
		catch(IOException msg){
			print.info("File could not be read. Check if file exists or check file permisssions");
		}
		
		//Send data
		server = new DatagramSocket();
		serverAddress = InetAddress.getByName(super.serverAdrress);
		
		windowHandlePushData(data);
		//createSendPacket(data);
		
		server.close();
	}
	
	public void windowHandlePushData(byte[] data) throws IOException{
		int dataIndex = 0;
		int windowBase = 0;
		int windowIndex = 0;
		
		while(dataIndex < fileSize){
			dataIndex = windowBase;
			do{
				temp = new byte[MSS];
				System.arraycopy(data, dataIndex, temp, 0, MSS);
				createPacketSend(temp);
				dataIndex += MSS;
			}while((dataIndex-windowBase <= 1428) || dataIndex < fileSize);
			windowBase = rcvCheckAck(dataIndex);
		}
	}
	
	public void createPacketSend(byte[] data) throws IOException{
		byte[] header = getHeader();
		byte[] packetData = new byte[data.length + 20];
			
		System.arraycopy(header, 0, packetData, 0, 20);
		System.arraycopy(data, 0, packetData, 20, data.length);
		
		DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length, serverAddress, super.port);
		server.send(sendPacket);
	}
	
	public byte[] getHeader(){
		byte[] header = new byte[20];
		//TODO
		return header;
	}
	
	public int rcvCheckAck(int expEndAckNum) throws IOException{
		server.setSoTimeout(super.timeout); 
		int maxAckNum = 0;
		int ackNum = 0;
		
		byte[] rcvBuffer = new byte[20];
		DatagramPacket rcvPacket = new DatagramPacket(rcvBuffer, rcvBuffer.length);;
		
		while(ackNum < expEndAckNum)
			try{
				server.receive(rcvPacket);
				ackNum = getAckNumber(rcvPacket.getData());
				if (ackNum > maxAckNum)  
					maxAckNum = ackNum;
			}catch(SocketTimeoutException oops){
				return maxAckNum;
			}
				
		return maxAckNum;
	}
}

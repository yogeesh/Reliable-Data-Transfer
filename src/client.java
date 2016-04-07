import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class client extends fcntcp{
	byte[] data;
	DatagramSocket server;
	InetAddress serverAddress;
	int fileSize;
	int MSS = 536;
	byte[] temp;
	
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
			
			print.debug("File read of size: " + fileSize);
			print.info("MD5 hash of the of the file: " + super.md5hash(data));
			
		}
		catch(IOException msg){
			print.info("File could not be read. Check if file exists or check file permisssions");
		}
		
		//Send data
		server = new DatagramSocket();
		serverAddress = InetAddress.getByName(super.serverAdrress);
		
		createSendPacket(data);
	}
	
	public void send(byte[] message) throws IOException{
		DatagramPacket sendPacket = new DatagramPacket(message, message.length, serverAddress, super.port);
		server.send(sendPacket);
	}
	
	public void createSendPacket(byte[] data) throws IOException{
		byte[] header = getHeader();
		byte[] packetData = new byte[MSS + 20];
		int tempAppData = 0;
		int dataIndex = 0;
		
		while(dataIndex<fileSize){

			if (dataIndex+MSS >= fileSize)
				tempAppData = fileSize-dataIndex;
			else
				tempAppData = MSS;

			packetData = new byte[tempAppData+20];
			System.arraycopy(header, 0, packetData, 0, 20);
			System.arraycopy(data, dataIndex, packetData, 20, tempAppData);
			dataIndex += tempAppData;
			
			send(packetData);
			print.debug("" + dataIndex);
		}
	}
	
	public byte[] getHeader(){
		byte[] header = new byte[20];
		//TODO
		return header;
	}
}

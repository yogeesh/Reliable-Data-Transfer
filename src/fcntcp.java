import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.net.SocketTimeoutException;

/**
 * 
 * @author Yogeesh Seralathan
 * 
 * Foundations of COmputer Network Project - 2
 *
 * Building Reliable transfer protocol over unreliable transfer protocol
 * 
 */
public class fcntcp {
	static String serverAdrress;
	static int port;
	static String fileName;
	static boolean quiteMode;
	static int timeout = 1000;
	static print print = new print();
	byte[] temp;
	int windowSize = 64000;
		
	public static void main(String[] args) throws IOException{
		/**
		 * Parsing all the input populating class variables
		 * for further processing.
		 */
		int argIndex = 0; //ingnoring java and tsapp
		
		for(int i = argIndex; i<args.length; i++){
			switch(args[i]){
				case "-s":
				case "--server":
				case "-c":
				case "--client":
					break;
				case "-f":
				case "--file":
					fileName = args[i+1];
					i++;
					break;
				case "-t":
				case "--timeout":
					timeout = Integer.parseInt(args[i+1]);
					i++;
					break;
				case "--quiet":
				case "-q":
					quiteMode = true;
					break;
				default:
					if(args[0].equals("-c")){
						serverAdrress = args[i];
						i++;
					}
					port = Integer.parseInt(args[i]);
					i++;
			}
		}
	
	/**
	 * Initiate Server or Client according to the argument provided
	 */
	if(args[0].equals("-s"))
		new server();
	else
		new client();
	}
	
	
	public String md5hash(byte[] plaintext){
		/**
		 * Generates Md5 hash of the provided plain text
		 */
		MessageDigest m;
		String hash = "Null hash";
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(plaintext);
			byte[] hashByte = m.digest();
			hash = new BigInteger(1, hashByte).toString(16);
			
		} catch (NoSuchAlgorithmException e) {
			print.debug("Provided Algorithm does not exist!");
		}
		return hash;
	}
	
	public int getAckNumber(byte[] data){
		/**
		 * extracts acknowledgement number form the packet
		 */
		temp = new byte[4];
		System.arraycopy(data, 4, temp, 0, 4);
		return (ByteBuffer.wrap(temp).getInt());
	}
	
	public int getSeqNumber(byte[] data){
		/**
		 * extracts acknowledgement number form the packet
		 */
		temp = new byte[4];
		System.arraycopy(data, 0, temp, 0, 4);
		return (ByteBuffer.wrap(temp).getInt());
	}
	
	
	public boolean checkPacketCorruption(byte[] data){
		int packetChecksum;
		int checkPacketChecksum;
		
		byte[] tempData = new byte[data.length];
		System.arraycopy(data, 0, tempData, 0, data.length);
		
		//extracting checksum value
		temp = new byte[4];
		System.arraycopy(tempData, 12, temp, 2, 2);
		packetChecksum = ByteBuffer.wrap(temp).getInt();
		
		//updating checksum to null byte for checksum calculation
		temp = new byte[2];
		System.arraycopy(temp, 0, tempData, 12, 2);
		checkPacketChecksum = calChecksum(tempData);
		
		//print.debug("rcv checksum = " + packetChecksum + " cal checksum = " + checkPacketChecksum);
		
		if (packetChecksum == checkPacketChecksum)
			return false;
		return true;
	}
	
	/**
	 * Calculates the checksum and returns checksum value.
	 * @param data -> the packet data
	 * @return checksum value 
	 */
	public int calChecksum(byte[] data){
		
		byte[] tempData = new byte[data.length];
		System.arraycopy(data, 0, tempData, 0, data.length);
		
		int lenPacket = tempData.length;
		int tempSum = 0;
		int sum = 0;
		
		if ((lenPacket)%2 != 0){
			// padding 0x00, for compatible 16 bit addition
			temp = new byte[lenPacket+1];
			System.arraycopy(tempData, 0, temp, 0, lenPacket);
			lenPacket += 1;
			tempData = temp;
		}
		
		// 16 bit addition
		int dataIndex = 0;
		while( dataIndex < lenPacket ){
			tempSum = (tempData[dataIndex] << 8) + (tempData[dataIndex+1]);
			sum += tempSum;
			if( (sum & 0xFFFF0000) >= 1){
				sum += 1;
				sum = sum & 0x0000FFFF;
			}
			dataIndex += 2;
		}
		
		if ((sum & 0xFFFF0000) >= 1){
			sum += 1;
			sum = sum & 0x0000FFFF;
		}
		
		return (~sum)&0xFFFF;
	}
	
	public byte[] addChecksum(byte[] data){
		int checksum = calChecksum(data);
		
		byte[] tempData = new byte[data.length];
		System.arraycopy(data, 0, tempData, 0, data.length);
		
		temp = new byte[4];
		temp = ByteBuffer.allocate(4).putInt(checksum).array();
		System.arraycopy(temp, 2, tempData, 12, 2);
		
		//print.debug("Added checksum = " + checksum);
		checkPacketCorruption(tempData);
		return tempData;
	}
}

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}

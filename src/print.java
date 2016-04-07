
public class print extends fcntcp{
	
	public void debug(String message){
		if(!super.quiteMode)
			System.out.println("DEBUG: " + message);
	}
	
	public void info(String message){
		System.out.println("INFO: " + message);
	}
}


public class test {

	public static void change(byte[] temp){
		temp[1] = 0x00;
	}
	
	public static void main(String[] args){
		byte[] temp = {0x00, 0x01};
		change(temp);
		System.out.println("" + temp[0] + temp[1]);
	}
}

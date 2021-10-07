package common;

public class CryptManager {

	public static String CryptPassword(String Key, String Password) {
        char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
                't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
                'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
      
        StringBuilder _Crypted = new StringBuilder("#1");

        for (int i = 0; i < Password.length(); i++) {
            char PPass = Password.charAt(i);
            char PKey = Key.charAt(i);

            int APass = (int)PPass / 16;

            int AKey = (int)PPass % 16;

            int ANB = (APass + (int)PKey) % HASH.length;
            int ANB2 = (AKey + (int)PKey) % HASH.length;

            _Crypted.append(HASH[ANB]);
            _Crypted.append(HASH[ANB2]);
        }
        return _Crypted.toString();
	}

	public static String decryptpass(String pass,String key) {
		int l1, l2, l3, l4, l5;
        StringBuilder l7 = new StringBuilder();
        String Chaine = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
        for(l1 = 0; l1<= (pass.length()-1);l1+=2) {
        	l3 = (int)key.charAt((l1/2));
            l2 = Chaine.indexOf(pass.charAt(l1));
            l4 = (64 + l2) - l3;
            int l11 = l1+1;
            l2 = Chaine.indexOf(pass.charAt(l11));
            l5 = (64 + l2) - l3;
            if(l5 < 0)l5 = 64 + l5;
            
            l7.append((char) (16 * l4 + l5));
        }
        return l7.toString();
	}
	
	public static String CryptIP(String IP) {
		String[] Splitted = IP.split("\\.");
		String Encrypted = "";
        int Count = 0;
        for (int i = 0; i < 50; i++) {
            for (int o = 0; o < 50; o++) {
                if (((i & 15) << 4 | o & 15) == Integer.parseInt(Splitted[Count])) {
                    Character A = (char)(i+48);
                    Character B = (char)(o + 48);
                    Encrypted += A.toString() + B.toString();
                    i = 0;
                    o = 0;
                    Count++;
                    if (Count == 4)
                        return Encrypted;
                }
            }
        }
        return "DD";
    }
	
	public static String CryptPort(int config_game_port) {
		char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
		int P = config_game_port;
		StringBuilder nbr64 = new StringBuilder();
		for(int a = 2;a>=0;a--) {
			nbr64.append(HASH[(int) (P / (Math.pow(64, a)))]);
			P = (int)(P%(int)(Math.pow(64,a)));
		}
		return nbr64.toString();
	}

	public static int getIntByHashedValue(char c) {
		char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};
		for(int a = 0;a<HASH.length; a++) {
			if(HASH[a] == c) {
				return a;
			}
		}	
		return -1;
	}
	
	public static char getHashedValueByInt(int c) {
		char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	            't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
	            'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};	
		return HASH[c];
	}
	
	public static String toUtf(String _in) {
		String _out = "";

		try {
			_out = new String(_in.getBytes("UTF8"));
			
		}catch(Exception e) {
			System.out.println ("CryptManager : Conversion en UTF-8 echoue : "+e.getMessage());
			Main.agregaralogdeerrores("CryptManager : Conversion en UTF-8 echoue : "+e.getMessage());
			_out = _in;
		}
		
		return _out;
	}
}
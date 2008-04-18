package commerce.util;

import java.io.File;

public class Common {
	public static String getCurrentDir()
	{
		File dir1 = new File (".");
		String strCurrentDir = "";
		try {
			strCurrentDir = dir1.getCanonicalPath();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return strCurrentDir;
	}
}

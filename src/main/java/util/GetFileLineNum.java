package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;

public class GetFileLineNum {

	public static Integer getLineNum(String totalPath)  {
		File file = new File(totalPath);
		int linenumber = 0;
		if (file.exists()) {
			try {
				FileReader fr = new FileReader(file);
				LineNumberReader lnr = new LineNumberReader(fr);
				while (lnr.readLine() != null) {
					linenumber++;
				}
				lnr.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return linenumber;
	}
	

}

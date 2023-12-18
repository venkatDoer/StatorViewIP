package doer.print;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import doer.sv.Configuration;

public class PrintBarcode {
	private void PrintBarCode () {
		
	}
 
	// function to print bar/qr code
	public void print(String code, String line1, String line2, String line3, String line4) throws Exception {
			String appDir = Configuration.APP_DIR;
			
			// 1. create final file to be printed from template
			Path tempFile = Paths.get(appDir + "\\print\\template.prn");
			String finalFilePath = appDir + "\\print\\final.prn";
			Path finalFile = Paths.get(finalFilePath);
			
			Charset charset = StandardCharsets.UTF_8;

			String content = new String(Files.readAllBytes(tempFile), charset);
			content = content.replaceAll("9999999999", code);
			content = content.replaceAll("LINE1", line1);
			content = content.replaceAll("LINE2", line2);
			content = content.replaceAll("LINE3", limitChar(line3));
			content = content.replaceAll("LINE4", limitChar(line4));

			Files.write(finalFile, content.getBytes(charset));
					
			// 2. print the label
			String cmd = "cmd.exe /c copy /B \"" + finalFilePath + "\" \"" + Configuration.QR_CODE_PRINTER_PATH + "\"";
			Runtime.getRuntime().exec(cmd);
	}
	
	// strip lengthy strings
	private String limitChar(String source) {
		source = (source.length() > 12 ? source.substring(0, 12) : source);
		return source;
	}
}

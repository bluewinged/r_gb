package ch.gb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RessourceLoader {
	private static RessourceLoader instance = new RessourceLoader();

	private RessourceLoader() {

	}

	public static InputStream load(String path) throws FileNotFoundException {
		// TODO: also implemented android thingy for portability
		File f = new File(path);// "Roms/"
		FileInputStream fis = null;

		fis = new FileInputStream(f);

		return fis;
	}

	public static OutputStream write(String path) throws FileNotFoundException {
		File f = new File(path);
		System.out.println(f.getAbsolutePath());
		FileOutputStream fos = null;

		fos = new FileOutputStream(f);

		return fos;
	}
}

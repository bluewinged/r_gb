/*******************************************************************************
 *     <A simple gameboy emulator>
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/
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

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
import java.io.UnsupportedEncodingException;

public class Utils {

	public static boolean compare(int[] dump1, int[] dump2) {
		return false;
	}

	public static void dumpMem(byte[] memory) {
		dumpMem(memory, 0);
	}

	public static void dumpMem(byte[] memory, int offsetreduce) {
		System.out.println();
		System.out.println("MEMORY DUMP OF:" + memory);
		for (int i = 0; i < memory.length / 16; i++) {
			int offset = 0x10 * i;
			Utils.printStr(Utils.dumpHex(offset - offsetreduce)
					+ " "
					+ Utils.dumpHex(memory[offset], memory[offset + 1], memory[offset + 2], memory[offset + 3],
							memory[offset + 4], memory[offset + 5], memory[offset + 6], memory[offset + 7],
							memory[offset + 8], memory[offset + 9], memory[offset + 10], memory[offset + 11],
							memory[offset + 12], memory[offset + 13], memory[offset + 14], memory[offset + 15]));
		}
	}

	public static void dumpMem(String[] array, byte[] memory) {
		if (array.length < memory.length / 16) {
			throw new RuntimeException("array size not sufficient");
		}
		for (int i = 0; i < memory.length / 16; i++) {
			int offset = 0x10 * i;
			array[i] = (Utils.dumpHex(offset) + " " + Utils.dumpHex(memory[offset], memory[offset + 1],
					memory[offset + 2], memory[offset + 3], memory[offset + 4], memory[offset + 5], memory[offset + 6],
					memory[offset + 7], memory[offset + 8], memory[offset + 9], memory[offset + 10],
					memory[offset + 11], memory[offset + 12], memory[offset + 13], memory[offset + 14],
					memory[offset + 15]));
		}
	}

	public static String dumpHex(byte hex) {
		return String.format("%02X", hex & 0xff);
	}

	public static String dumpHex(int word) {
		return String.format("%04X", word);
	}

	public static String dumpHex(byte... hex) {
		String format = "";
		Object[] tmp = new Object[hex.length];
		for (int i = 0; i < hex.length; i++) {
			format += "%02X ";
			tmp[i] = Integer.valueOf(hex[i] & 0xff);
		}
		return String.format(format, tmp);
	}

	public static String decASCII(byte[] ascii) {
		try {
			return new String(ascii, 0, ascii.length, "ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void printStr(String str) {
		System.out.println(str);
	}

	public static String removeExtension(String in) {
		int p = in.lastIndexOf(".");
		if (p < 0)
			return in;

		int d = in.lastIndexOf(File.separator);

		if (d < 0 && p == 0)
			return in;

		if (d >= 0 && d > p)
			return in;

		return in.substring(0, p);
	}

	public static String[] addArrays(String[] a, String[] b) {
		String[] c = new String[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);

		return c;
	}
}

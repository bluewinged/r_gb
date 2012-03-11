package ch.gb.mem;

import java.io.IOException;
import java.io.InputStream;

import ch.gb.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Rom {
	private final byte[] header;
	private byte[][] rom;

	private String title;
	private int cgbflag;
	private int sgbflag;
	private int cartridgetype;
	private int romsize;
	private int ramsize;
	private int destination;
	private int maskRomVersionNumber;
	private int headerchecksum;
	private int globalchecksum;
	private int banks16kB;

	private String cartridgeAsString;
	private final String nl;

	public Rom(String path) {
		header = new byte[0x50 + 0x100];
		nl = System.getProperty("line.separator");
		load(path);
	}

	private void load(String path) {
		FileHandle file = Gdx.files.internal(path);
		InputStream is = file.read();
		byte[] tmp = new byte[0x10];

		// get header
		file.readBytes(header, 0x0, 0x150);
		System.arraycopy(header, 0x134, tmp, 0, 0x10);

		title = Utils.decASCII(tmp);
		// 013F-0142 -Manufacturere Code
		cgbflag = header[0x143];
		// 0144-0145 - New Licensee Code
		sgbflag = header[0x146];
		cartridgetype = header[0x147];
		romsize = 0x8000 << (header[0x148] & 0xff);
		banks16kB = romsize / 0x8000 * 2;
		ramsize = header[0x149] * 0x800 * (int) Math.pow(2, header[0x149] - 1);
		destination = header[0x14A];
		// 014B - Old Licensee Code
		maskRomVersionNumber = header[0x14C];
		headerchecksum = header[0x14D];
		globalchecksum = (header[0x14E] & 0xff) << 8 | (header[0x14F] & 0xff);

		cartridgeAsString = "Title:" + title + nl + "CGB flag:" + cgbflag + nl + "Cartridgetype:"
				+ getCartridgeType(cartridgetype) + nl + "Rom size:" + romsize + " in 16kB:" + banks16kB + nl
				+ "Ram size:" + ramsize + nl + "Destination:" + destination + nl + "Mask rom version num:"
				+ maskRomVersionNumber + nl + "Header checksum:" + Utils.dumpHex(headerchecksum&0xff) + nl
				+ "Global checksum:" + Utils.dumpHex(globalchecksum);

		// load in 16kB chunks
		try {
			rom = new byte[banks16kB][0x8000];
			for (int i = 0; i < banks16kB; i++) {
				for (int x = 0; x < 0x8000; x++) {
					rom[i][x] = (byte) is.read();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getInformation() {
		return cartridgeAsString;
	}

	public int getType() {
		return cartridgetype;
	}
	public byte[] get16kRomBank(int bank){
		return rom[bank];
	}
	public byte[] get2kRamBank(int bank){
		return null;
	}

	private String getCartridgeType(int type) {
		//@formatter:off
		String t = "UNKNOWN";
		switch (type){
		 case 0x00 : t="ROM ONLY                " ;break;
		 case 0x01 : t="MBC1                    " ;break;
		 case 0x02 : t="MBC1+RAM                " ;break;
		 case 0x03 : t="MBC1+RAM+BATTERY        " ;break;
		 case 0x05 : t="MBC2                    " ;break;
		 case 0x06 : t="MBC2+BATTERY            " ;break;
		 case 0x08 : t="ROM+RAM                 " ;break;
		 case 0x09 : t="ROM+RAM+BATTERY         " ;break;
		 case 0x0B : t="MMM01                   " ;break;
		 case 0x0C : t="MMM01+RAM               " ;break;
		 case 0x0D : t="MMM01+RAM+BATTERY       " ;break;
		 case 0x0F : t="MBC3+TIMER+BATTERY      " ;break;
		 case 0x10 : t="MBC3+TIMER+RAM+BATTERY  " ;break;
		 case 0x11 : t="MBC3                    " ;break;
		 case 0x12 : t="MBC3+RAM                " ;break;
		 case 0x13 : t="MBC3+RAM+BATTERY        " ;break;      
		 case 0x15 : t="MBC4                    " ;break;      
		 case 0x16 : t="MBC4+RAM                " ;break;      
		 case 0x17 : t="MBC4+RAM+BATTERY        " ;break;      
		 case 0x19 : t="MBC5                    " ;break;      
		 case 0x1A : t="MBC5+RAM                " ;break;      
		 case 0x1B : t="MBC5+RAM+BATTERY        " ;break;      
		 case 0x1C : t="MBC5+RUMBLE             " ;break;      
		 case 0x1D : t="MBC5+RUMBLE+RAM         " ;break;      
		 case 0x1E : t="MBC5+RUMBLE+RAM+BATTERY " ;break;      
		 case 0xFC : t="POCKET CAMERA           " ;break;      
		 case 0xFD : t="BANDAI TAMA5            " ;break;
		 case 0xFE : t="HuC3                    " ;break;
		 case 0xFF : t="HuC1+RAM+BATTERY        " ;break;
		}  
		return t;
	}
	
}

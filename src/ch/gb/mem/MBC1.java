package ch.gb.mem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import ch.gb.Settings;
import ch.gb.utils.RessourceLoader;
import ch.gb.utils.Utils;

public class MBC1 extends Mapper {
	private int rombank = 1;
	private int romRamMode = 0;
	private boolean ramEnabled = false;

	private int numRamBanks = 0;
	private int ramsize;
	private byte ram[][] = null;

	private final boolean hasRam;
	private final boolean saveable;

	public MBC1(boolean hasRam, boolean saveable) {// either by SRAM or Battery
		this.hasRam = hasRam;
		this.saveable = saveable;
	}

	@Override
	public void init() {
		switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
		switch16kRom(ROM_0x4000, rom.get16kRomBank(1));
		if (hasRam) {
			ramsize = rom.getRamSize();
			if (ramsize > 0x2000) {// split in banks
				numRamBanks = ramsize / 0x2000;
				ram = new byte[numRamBanks][0x2000];
			} else {
				numRamBanks = 1;
				ram = new byte[1][0x2000];// auch fuer 2kB ram, dann halt
											// einfach zu "viel" uebrig
			}
			switch8kRam(ram[0]);
		}
		loadRam();
	}

	@Override
	public void write(int add, byte b) {
		// System.out.println("DETECTED MBC1 WRITE ABORT:" + Utils.dumpHex(add)
		//+ "->" + Utils.dumpHex(b));
		if (add < 0x2000) {
			// RAM Enable
			if ((b & 0xA) == 0xA) {
				ramEnabled = true;//doesnt really matter since no acces control
			} else {
				ramEnabled = false;
			}
		} else if (add < 0x4000) {

			// select ROM bank number (lower 5 bits)
			rombank &= 0x7f;// mask bit 0-6;
			rombank = b & 0x1F;// set lower 5;
			if (rombank == 0x00 || rombank == 0x20 || rombank == 0x40 || rombank == 0x60)
				rombank++;
			switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank%rom.get16kRomNum()));//wrapping fix

		} else if (add < 0x6000) {
			b &= 3;// 2bit reg
			// select RAM bank number or Upper bits of ROM bank
			if (romRamMode == 0) {// ROM mode
				rombank |= (b << 5);
				switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank));
			} else { // RAM mode
				switch8kRam(ram[b % numRamBanks]);
			}
		} else if (add < 0x8000) {
			// ROM/RAM mode select
			romRamMode = b & 1;
		}
		
	}


	@Override
	public void loadRam() {
		if (!saveable)
			return;
		String filename = rom.getLoadPath();
		String[] splits = filename.split(Pattern.quote(File.separator));
		filename = Utils.removeExtension(splits[splits.length - 1]);
		filename += ".sav";
		
		//FileHandle filehandle = Gdx.files.external(Settings.root+filename);
		//if (!filehandle.exists())
			//return;

		//InputStream is = filehandle.read();
		InputStream is = null;
		try {
			is = RessourceLoader.load(Settings.root+File.separatorChar+filename);
		} catch (FileNotFoundException e1) {
			System.out.println("No savegame found");
			return;
		}

		try {
			for (int i = 0; i < numRamBanks; i++) {
				for (int x = 0; x < 0x2000; x++) {
					ram[i][x] = (byte) is.read();
				}
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Found existing savegame and loaded it");
	}
	@Override
	public void saveRam() {
		if (rom != null ) {
			if (getNumRamBanks() == 0 ||!hasSramOrBattery()) {
				return;
			}
			String filename = rom.getLoadPath();
			String[] splits = filename.split(Pattern.quote(File.separator));
			filename = Utils.removeExtension(splits[splits.length - 1]);
			filename += ".sav";
			System.out.println("Filename:"+filename);
			//FileHandle filehandle = Gdx.files.external(Settings.root + filename);


			//OutputStream os = filehandle.write(false);
			OutputStream os = null;
			try {
				os = RessourceLoader.write(Settings.root+File.separatorChar+filename);
			} catch (FileNotFoundException e1) {
				System.out.println("Couldnt save Ram");
				return;
			}
			try {
				for (int i = 0; i < getNumRamBanks(); i++) {
					os.write(getRam()[i]);
				}
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("Saved Ram in " + filename);
			//System.out.println("Path:" + filehandle.path() + " -> relative to user/<username>/");
		}
	}

	@Override
	public int getNumRamBanks() {
		return numRamBanks;
	}

	@Override
	public boolean hasSramOrBattery() {
		return saveable;
	}

	@Override
	public byte[][] getRam() {
		return hasRam ? ram : null;
	}





}

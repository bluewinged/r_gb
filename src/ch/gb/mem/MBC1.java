package ch.gb.mem;


public class MBC1 extends Mapper {
	private int rombank = 1;
	private int romRamMode = 0;
	private boolean ramEnabled = false;

	@Override
	public void init() {
		switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
		switch16kRom(ROM_0x4000, rom.get16kRomBank(1));
	}

	@Override
	public void write(int add, byte b) {
		//System.out.println("DETECTED MBC1 WRITE ABORT:" + Utils.dumpHex(add) + "->" + Utils.dumpHex(b));
		if (add < 0x2000) {
			// RAM Enable
			if ((b & 0xA) == 0xA) {
				ramEnabled = true;
			} else {
				ramEnabled = false;
			}
		} else if (add < 0x4000) {
			
			// select ROM bank number (lower 5 bits)
			rombank&=0x7f;//mask bit 0-6;
			rombank = b & 0x1F;//set lower 5;
			if (rombank == 0x00 || rombank == 0x20 || rombank == 0x40 || rombank == 0x60)
				rombank++;
			switch16kRom(ROM_0x4000,rom.get16kRomBank(rombank));

		} else if (add < 0x6000) {
			b&=3;//2bit reg
			// select RAM bank number or Upper bits of ROM bank
			if (romRamMode == 0) {// ROM mode
				rombank|= (b<<5);
				switch16kRom(ROM_0x4000,rom.get16kRomBank(rombank));
			} else { //RAM mode
				
			}
		} else if (add < 0x8000) {
			// ROM/RAM mode select
			romRamMode = b & 1;
		}

	}

}

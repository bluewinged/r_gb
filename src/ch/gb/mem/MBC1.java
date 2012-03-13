package ch.gb.mem;

import ch.gb.utils.Utils;

public class MBC1 extends Mapper {
	private int rombank = 1;
	private int romRamMode = 0;

	@Override
	public void init() {
		switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
		switch16kRom(ROM_0x4000, rom.get16kRomBank(1));
	}

	@Override
	public void write(int add, byte b) {
		System.out.println("DETECTED MBC1 WRITE ABORT:"+Utils.dumpHex(add)+"->"+Utils.dumpHex(b));
		if (add < 0x2000) {
			// RAM Enable

		} else if (add < 0x4000) {
			// select ROM bank number (lower 5 bits)
			rombank = b & 0x1F;
			if (rombank == 0x00 || rombank == 0x20 || rombank == 0x40
					|| rombank == 0x60) {
				rombank++;
			}

		} else if (add < 0x6000) {
			// select RAM bank number or Upper bits of ROM bank
			if(romRamMode==0){//ROM mode
				
			}else{
				
			}
		} else if (add < 0x8000) {
			// ROM/RAM mode select
			romRamMode = b&1; 
		}

	}

}

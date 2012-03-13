package ch.gb.mem;

public class RomOnly extends Mapper {
	public RomOnly() {
	}

	@Override
	public void init() {
		switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
		switch16kRom(ROM_0x4000, rom.get16kRomBank(1));
	}

	@Override
	public void write(int add, byte b) {
		// nothing happens
	}

}

package ch.gb.mem;

public class MBC1 extends Mapper{

	@Override
	public void init() {
	switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
		
	}

	@Override
	public void write(int add, byte b) {
		
		
	}
	
}

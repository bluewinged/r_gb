package ch.gb.mem;

/**
 * Memory bank controller handles bankswitching to extend addressable memory
 * 
 * @author bluew 
 * 
 */
public abstract class Mapper {
	public static final int ROM_0x0000 = 0;
	public static final int ROM_0x4000 = 1;
	protected MemoryManager mem;
	protected Rom rom;

	public Mapper() {
	}

	public static Mapper createMBC(MemoryManager mem, Rom rom) {
		assert mem != null && rom != null;
		Mapper mbc = null;
		switch (rom.getType()) {
		case 0x00:
			mbc = new RomOnly();
			break;
		case 0x01:
			mbc = new MBC1();
			break;
		}
		mbc.setMem(mem);
		mbc.setRom(rom);
		mbc.init();//load initial banks
		return mbc;
	}
	public abstract void init();

	public abstract void write(int add, byte b);

	protected void switch16kRom(int bankaddr, byte[] newbank) {
		mem.rombanks[bankaddr] = newbank;
	}

	public void setMem(MemoryManager mem) {
		this.mem = mem;
	}

	public void setRom(Rom rom) {
		this.rom = rom;
	}
}

package ch.gb.io;

import ch.gb.mem.MemoryManager;

public class SpriteDma implements IOport {
	private final MemoryManager mem;
	public static final int OAM_DMA = 0xFF46;

	public SpriteDma(MemoryManager mem) {
		this.mem = mem;
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(int add, byte b) {
		// for testing assume instant DMA transfer
		//b =(byte)(b%0xF1);
		int address = (b & 0xff) << 8; // source address is data * 100
		for (int i = 0; i < 0xA0; i++) {
			mem.writeByte(0xFE00 + i, mem.readByte(address + i));
		}
	}

	@Override
	public byte read(int add) {
		return 0;
	}

	public void tick() {
		// Later used for timed DMA transfer
	}


}

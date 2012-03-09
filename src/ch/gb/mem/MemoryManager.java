package ch.gb.mem;

import ch.gb.Component;
import ch.gb.GBComponents;

/**
 * Contains CPU memory and manages all writes to it
 * 
 * @author bluew 
 * 
 */
public class MemoryManager implements Component {
	private final byte[] cpumem;

	public MemoryManager() {
		cpumem = new byte[0x10000];
	}

	public void writeByte(int add, byte b) {
		cpumem[add] = b;
	}

	public void write2Byte(int add, int s) {
		writeByte(add, (byte)(s&0xff));
		writeByte(add+1,(byte)(s>>8&0xff));
	}

	public byte readByte(int add) {
		return cpumem[add];
	}

	public int read2Byte(int add){
		return ((readByte(add) & 0xff) | (readByte(add+ 1) & 0xff) << 8);
	}

	@Override
	public void link(GBComponents comps) {

	}

	@Override
	public void reset() {

	}
}

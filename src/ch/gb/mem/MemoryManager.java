package ch.gb.mem;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.utils.Utils;

/**
 * Contains CPU memory and manages all writes to it
 * 
 * @author bluew 
 * 
 */
public class MemoryManager implements Component {
	public static final int JOYP = 0xFF00;
	public static final int SB = 0xFF01;
	public static final int SC = 0xFF02;

	private byte serialtransferdata;
	private byte serialtransfercontrol;
	
	private final byte[] tmp = new byte[1];
	
	private final byte[] cpumem;
	public byte[][] rombanks;
	private final byte[] internalRam;

	private Mapper mbc;

	public MemoryManager() {
		cpumem = new byte[0x10000];
		rombanks = new byte[2][0x4000];
		internalRam = new byte[0x2000];
	}

	public void writeByte(int add, byte b) {
		if (add < 0x4000) {
			// 16kB Rom bank #0
			mbc.write(add, b);
		} else if (add < 0x8000) {
			// 16kB Rom switchable
			mbc.write(add, b);
		} else if (add < 0xA000) {
			// 8kB Video Ram
		} else if (add < 0xC000) {
			// 8kB Ram switchable
		} else if (add < 0xE000) {
			// 8kB Internal
			internalRam[add - 0xC000] = b;
		} else if (add < 0xFE00) {
			// Mirror of 8kB internal Ram
			internalRam[add & 0x2000] = b;
		} else if (add < 0xFEA0) {
			// OAM
		} else if (add < 0xFF00) {
			// empty and unusable
		} else if (add < 0xFF4C) {
			// I/O ports
			if (add == JOYP) {
				
			} else if (add == SB) {
				tmp[0]=serialtransferdata = b;
			} else if (add == SC) {
				if(b==0x81){
					System.out.print(Utils.decASCII(tmp));
					serialtransfercontrol = 1; //transfer "finished"
				}
			}
		} else if (add < 0xFF80) {
			// empty and unusuable
		} else if (add < 0xFFFF) {
			// internal RAM
		} else {
			// interrupt enable register
		}

		cpumem[add] = b;
	}

	public byte readByte(int add) {
		if (add < 0x4000) {
			// 16kB Rom bank #0
			return rombanks[0][add];
		} else if (add < 0x8000) {
			// 16kB Rom switchable
			return rombanks[1][add - 0x4000];
		} else if (add < 0xA000) {
			// 8kB Video Ram
		} else if (add < 0xC000) {
			// 8kB Ram switchable
		} else if (add < 0xE000) {
			// 8kB Internal
			return internalRam[add - 0xC000];
		} else if (add < 0xFE00) {
			// Mirror of 8kB internal Ram
			return internalRam[add & 0x2000];
		} else if (add < 0xFEA0) {
			// OAM
		} else if (add < 0xFF00) {
			// empty and unusable
		} else if (add < 0xFF4C) {
			// I/O ports
			if (add == JOYP) {
				
			} else if (add == SB) {
				return serialtransferdata;
			} else if (add == SC) {
				return serialtransfercontrol;
			}
		} else if (add < 0xFF80) {
			// empty and unusuable
		} else if (add < 0xFFFF) {
			// internal RAM
		} else {
			// interrupt enable register
		}
		return cpumem[add];
	}

	public void write2Byte(int add, int s) {
		writeByte(add, (byte) (s & 0xff));
		writeByte(add + 1, (byte) (s >> 8 & 0xff));
	}

	public int read2Byte(int add) {
		return ((readByte(add) & 0xff) | (readByte(add + 1) & 0xff) << 8);
	}

	public void loadRom(String path) {
		Rom rom = new Rom(path);
		System.out.println(rom.getInformation());

		mbc = Mapper.createMBC(this, rom);
	}

	@Override
	public void link(GBComponents comps) {

	}

	@Override
	public void reset() {

	}
}

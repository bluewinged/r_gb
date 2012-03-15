package ch.gb.mem;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.cpu.CPU;
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

	public byte[][] rombanks; // 2 x 16kB
	public byte[] vram; // 1 x 8kB , switchable in GBC
	public byte[][] exram;// 8kB external Ram, splitted int 4 x 2kB
	private final byte[] wram0; // 4kB
	public byte[] wram1; // 4kB, switchable in GBC
	private final byte[] oam;// 0xA0 bytes OAM
	private final byte[] hram;// 0x80 bytes high ram
	private byte interruptEnableReg;
	private byte irqReg;

	private Mapper mbc;

	public MemoryManager() {
		rombanks = new byte[2][0x4000];
		vram = new byte[0x2000];
		exram = new byte[4][0x800];
		wram0 = new byte[0x2000];
		wram1 = new byte[0x2000];
		oam = new byte[0xA0];
		hram = new byte[0x80];
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
			vram[add-0x8000]=b;
		} else if (add < 0xC000) {
			//8kB exram
			 exram[(add-0xA000)/4][add%0x800]=b;
		} else if (add <0xD000){
			//4kB WRAM 0
			 wram0[add-0xC000]=b;
		}else if (add < 0xE000) {
			//4kB WRAM 1, switchable
			wram1[add-0xD000]=b;
		} else if (add < 0xFE00) {
			// Partiall Mirror of WRAM 0
			 wram0[add-0xE000]=b;
			
		} else if (add < 0xFEA0) {
			// OAM
			oam[add-0xFE00]=b;
		} else if (add < 0xFF00) {
			// empty and unusable
		} else if (add < 0xFF4C) {
			// I/O ports
			if (add == JOYP) {

			} else if (add == SB) {
				tmp[0] = serialtransferdata = b;
				System.out.print(Utils.decASCII(tmp)); // HOLY SHIT
			} else if (add == SC) {
				if (b == 0x81) {
					System.out.print(Utils.decASCII(tmp));
					serialtransfercontrol = 1; // transfer "finished"
				}
			}else if (add == CPU.IF_REG){
				irqReg = b;
			}
		} else if (add < 0xFF80) {
			// empty and unusuable
		} else if (add < 0xFFFF) {
			// HRAM
			hram[add-0xFF80]=b;
		} else {
			// interrupt enable register
			interruptEnableReg = b;
		}
		//System.out.println("Couldnt write to:"+Utils.dumpHex(add)+", out of range");
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
			return vram[add-0x8000];
		} else if (add < 0xC000) {
			// 8kB EXRAM
			return exram[(add-0xA000)/4][add%0x800];
		} else if (add < 0xD000) {
			// 4kB WRAM 0
			return wram0[add-0xC000];
		} else if (add < 0xE000) {
			// 4kB WRAM 1 switchable
			return wram1[add-0xD000];
		} else if (add < 0xFE00) {
			// Partial mirror of WRAM 0
			return wram0[add-0xE000];
		} else if (add < 0xFEA0) {
			// OAM
			return oam[add-0xFE00];
		} else if (add < 0xFF00) {
			// empty and unusable
		} else if (add < 0xFF4C) {
			// I/O ports
			if (add == JOYP) {
				return 0;
			} else if (add == SB) {
				return serialtransferdata;
			} else if (add == SC) {
				return serialtransfercontrol;
			}else if( add ==CPU.IF_REG){
				return irqReg;
			}
			return 0;
		} else if (add < 0xFF80) {
			// empty and unusuable
		} else if (add < 0xFFFF) {
			// HRAM
			return hram[add-0xFF80];
		} else {
			// interrupt enable register
			return interruptEnableReg;
		}
		throw new RuntimeException("Couldnt decode Address:"+Utils.dumpHex(add));
	}

	public void write2Byte(int add, int s) {
		writeByte(add, (byte) (s & 0xff));
		writeByte(add + 1, (byte) (s >> 8 & 0xff));
		// writeByte(add, (byte) (s >> 8 & 0xff));
		// writeByte(add + 1, (byte) (s & 0xff));

	}

	public int read2Byte(int add) {
		return ((readByte(add) & 0xff) | (readByte(add + 1) & 0xff) << 8);
		// return ((readByte(add ) & 0xff) << 8) | (readByte(add+1) & 0xff);
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

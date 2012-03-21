package ch.gb.mem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.Settings;
import ch.gb.cpu.CPU;
import ch.gb.gpu.GPU;
import ch.gb.io.IOport;
import ch.gb.io.Joypad;
import ch.gb.io.Serial;
import ch.gb.io.SpriteDma;
import ch.gb.io.Timer;
import ch.gb.utils.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
	public static final int KEY1 = 0xFF4D;

	public byte[][] rombanks; // 2 x 16kB
	public byte[] vram; // 1 x 8kB , switchable in GBC
	public byte[] exram;// 8kB external Ram
	private byte[] wram0; // 4kB
	public byte[] wram1; // 4kB, switchable in GBC
	private byte[] oam;// 0xA0 bytes OAM
	private byte[] hram;// 0x80 bytes high ram
	private byte interruptEnableReg;
	private byte irqReg;

	private int speedmode;

	private Mapper mbc;
	private CPU cpu;
	private GPU gpu;

	private String romInfo = "";
	// more IO
	private Timer timer;
	private Serial serial;
	private Joypad joy;
	private SpriteDma sprdma;
	private HashMap<Integer, IOport> io;

	private Rom rom;

	public MemoryManager() {
		reset();
	}

	@Override
	public void reset() {
		rombanks = new byte[2][0x4000];
		vram = new byte[0x2000];
		exram = new byte[0x2000];
		wram0 = new byte[0x2000];
		wram1 = new byte[0x2000];
		oam = new byte[0xA0];
		hram = new byte[0x80];

		timer = new Timer(this);
		joy = new Joypad(this);
		serial = new Serial();
		sprdma = new SpriteDma(this);

		Gdx.input.setInputProcessor(joy);

		io = new HashMap<Integer, IOport>();

		// set mapping
		io.put(Timer.DIV, timer);
		io.put(Timer.TAC, timer);
		io.put(Timer.TIMA, timer);
		io.put(Timer.TMA, timer);

		io.put(Joypad.P1, joy);

		io.put(Serial.SB, serial);
		io.put(Serial.SC, serial);
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
			// System.out.println("finally:"+Utils.dumpHex(b));
			vram[add - 0x8000] = b;
		} else if (add < 0xC000) {
			// 8kB exram
			exram[(add - 0xA000)] = b;
		} else if (add < 0xD000) {
			// 4kB WRAM 0
			wram0[add - 0xC000] = b;
		} else if (add < 0xE000) {
			// 4kB WRAM 1, switchable
			wram1[add - 0xD000] = b;
		} else if (add < 0xFE00) {
			// Partiall Mirror of WRAM 0
			wram0[add - 0xE000] = b;

		} else if (add < 0xFEA0) {
			// OAM
			oam[add - 0xFE00] = b;
		} else if (add < 0xFF00) {
			// empty and unusable
			// System.out.println("MemManager-> couldnt map write@empty" +
			// Utils.dumpHex(add)+"->"+Utils.dumpHex(b));
		} else if (add < 0xFF80) {
			// I/O ports
			if (add == CPU.IF_REG) {
				irqReg = b;
			} else if (add == KEY1) {// speed switch
				// just surpress since no gbc support yet
				speedmode = (b &= 1);

			} else if (add == SpriteDma.OAM_DMA) {
				sprdma.write(add, b);
				// System.out.println("LAUNCHING DMA");
			} else if (add >= 0xFF10 && add <= 0xFF3F) {
				// Sound

			} else if (add >= 0xFF40 && add <= 0xFF4B) {
				// LCD
				gpu.write(add, b);
			} else if (add >= 0xFF00 && add <= 0xFF07) {
				// io port map
				io.get(add).write(add, b);
			} else {
				// System.out.println("MemManager-> couldnt map write@ioports" +
				// Utils.dumpHex(add));
			}
		} else if (add < 0xFFFF) {
			// HRAM
			hram[add - 0xFF80] = b;
		} else {
			// interrupt enable register
			interruptEnableReg = b;
		}
		// System.out.println("Couldnt write to:"+Utils.dumpHex(add)+", out of range");
	}

	public byte readByte(int add) {
		add &= 0xFFFF;
		if (add < 0x4000) {
			// 16kB Rom bank #0
			return rombanks[0][add];
		} else if (add < 0x8000) {
			// 16kB Rom switchable
			return rombanks[1][add - 0x4000];
		} else if (add < 0xA000) {
			// 8kB Video Ram
			return vram[add - 0x8000];
		} else if (add < 0xC000) {
			// 8kB EXRAM
			return exram[(add - 0xA000)];
		} else if (add < 0xD000) {
			// 4kB WRAM 0
			return wram0[add - 0xC000];
		} else if (add < 0xE000) {
			// 4kB WRAM 1 switchable
			return wram1[add - 0xD000];
		} else if (add < 0xFE00) {
			// Partial mirror of WRAM 0
			return wram0[add - 0xE000];
		} else if (add < 0xFEA0) {
			// OAM
			return oam[add - 0xFE00];
		} else if (add < 0xFF00) {
			// empty and unusable
			System.out.println("MemManager-> couldnt map read@ empty" + Utils.dumpHex(add));
			return 0;
		} else if (add < 0xFF80) {
			// I/O ports
			if (add == CPU.IF_REG) {
				return irqReg;
			} else if (add == KEY1) {
				// just surpress
				return 0;
			} else if (add >= 0xFF10 && add <= 0xFF3F) {
				// Sound
				return 0;
			} else if (add >= 0xFF40 && add <= 0xFF4B) {
				// LCD
				return gpu.read(add);
			} else if (add >= 0xFF00 && add <= 0xFF07) {
				// io port map
				return io.get(add).read(add);
			} else {
				System.out.println("MemManager-> couldnt map read@ioports" + Utils.dumpHex(add));
				return 0;
			}
			// return 0;
		} else if (add < 0xFFFF) {
			// HRAM
			return hram[add - 0xFF80];
		} else {
			// interrupt enable register
			return interruptEnableReg;
		}
		// throw new RuntimeException("Couldnt decode Address:" +
		// Utils.dumpHex(add));
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

	public void clock(int cpucycles) {
		timer.clock(cpucycles);
		// Utils.dumpMem(vram);
	}

	public void requestInterrupt(int i) {
		byte irq = readByte(CPU.IF_REG);
		irq = (byte) (irq | (1 << i));
		// System.out.println("IRQ?"+Utils.dumpHex(irq));
		writeByte(CPU.IF_REG, irq);
	}

	public void loadRom(String path) {
		rom = new Rom(path);
		System.out.println(rom.getInformation());
		romInfo = rom.getInformation();

		mbc = Mapper.createMBC(this, rom);
	}

	public void saveRam() {
		if (rom != null && mbc != null) {
			if (mbc.getNumRamBanks() == 0 || !mbc.hasSramOrBattery()) {
				return;
			}
			String filename = rom.getLoadPath();
			String[] splits = filename.split("/");
			filename = Utils.removeExtension(splits[splits.length - 1]);
			filename += ".sav";

			FileHandle filehandle = Gdx.files.external(Settings.root + filename);

			// Gdx.files.external
			OutputStream os = filehandle.write(false);
			try {
				for (int i = 0; i < mbc.getNumRamBanks(); i++) {
					os.write(mbc.getRam()[i]);
				}
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Saved Ram in " + filename);
			System.out.println("Path:" + filehandle.path() + " -> relative to user/<username>/");
		}
	}

	public String getRomInfo() {
		return romInfo;
	}

	@Override
	public void link(GBComponents comps) {
		cpu = comps.cpu;
		gpu = comps.gpu;

	}

}

package ch.gb.gpu;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;
import ch.gb.utils.Utils;

public class GPU implements Component {
	public static final int LCD_C = 0xFF40;
	public static final int STAT = 0xFF41;

	public static final int SCY = 0xFF42;
	public static final int SCX = 0xFF43;

	public static final int LY = 0xFF44;
	public static final int LYC = 0xFF45;

	public static final int WY = 0xFF4A;
	public static final int WX = 0xFF4B;

	public static final int BGP = 0xFF47;
	public static final int OBP0 = 0xFF48;
	public static final int OBP1 = 0xFF49;

	private byte lcdc;
	private boolean lcdEnabled;
	private int windowTilemap;
	private boolean windowEnable;
	private int bgWiTiledata;
	private int bgTilemap;
	private boolean spr8x16;
	private boolean sprEnable;
	private boolean bgEnable;

	private byte stat;
	private int mode;
	private int coincidence;

	private int scy;
	private int scx;

	private int ly;// scanlinecounter
	private int lyc;

	private int wx;
	private int wy;

	private byte bgpraw;
	private byte obp0raw;
	private byte obp1raw;
	private final byte[] bgp = { 0, 0, 0, 0 };
	private final byte[] obp0 = { 0, 0, 0, 0 };
	private final byte[] obp1 = { 0, 0, 0, 0 };
	// FORMAT: RGBA 10er hex
	// dark green: 015;056;015 -> 0xF , 0x38, 0xF
	// green : 048;098;048 -> 0x30, 0x62, 0x30
	// bright grn: 139;172;015 -> 0x8B, 0xAC, 0xF
	// brgter grn: 155;188;015 -> 0x9B, 0xBC, 0xF
	private final int[] palette = { 0x9BBC0FFF, 0x8BAC0FFF, 0x306230FF, 0x0F380FFF };

	private int scanlinecyc = 456;

	private MemoryManager mem;
	public int[][] videobuffer;

	public GPU() {
		videobuffer = new int[160][144];
	}

	public void write(int add, byte b) {
		if (add == LCD_C) {
			lcdc = b;
			lcdEnabled = (b & 0x80) == 0x80;
			windowTilemap = (b & 0x40) == 0x40 ? 0x9C00 : 0x9800;// nametable
			windowEnable = (b & 0x20) == 0x20;

			bgWiTiledata = (b & 0x10) == 0x10 ? 0x8000 : 0x9000;// patterns

			bgTilemap = (b & 8) == 8 ? 0x9C000 : 0x9800;// nametable
			spr8x16 = (b & 4) == 4;
			sprEnable = (b & 2) == 2;
			bgEnable = (b & 1) == 1;
		} else if (add == STAT) {
			b &= 0x78; // clear lower 3 bits and 7
			stat &= 87;// clear 6-3
			stat |= b;

		} else if (add == SCX) {
			scx = b & 0xff;
		} else if (add == SCY) {
			scy = b & 0xff;
		} else if (add == LY) {
			ly = 0;
		} else if (add == LYC) {
			lyc = b & 0xff;
		} else if (add == WY) {
			wy = b & 0xff;
		} else if (add == WX) {
			wx = (b & 0xff) - 7;
		} else if (add == BGP) {
			bgpraw = b;
			bgp[0] = (byte) (b & 3);
			bgp[1] = (byte) (b >> 2 & 3);
			bgp[2] = (byte) (b >> 4 & 3);
			bgp[3] = (byte) (b >> 6 & 3);
		} else if (add == OBP0) {
			obp0raw = b;
			obp0[0] = (byte) (b & 3);
			obp0[1] = (byte) (b >> 2 & 3);
			obp0[2] = (byte) (b >> 4 & 3);
			obp0[3] = (byte) (b >> 6 & 3);
		} else if (add == OBP1) {
			obp1raw = b;
			obp1[0] = (byte) (b & 3);
			obp1[1] = (byte) (b >> 2 & 3);
			obp1[2] = (byte) (b >> 4 & 3);
			obp1[3] = (byte) (b >> 6 & 3);
		}

	}

	public byte read(int add) {
		if (add == LCD_C) {
			return lcdc;
		} else if (add == STAT) {
			return (byte) (stat | (coincidence << 2 & 4) | mode & 3);
		} else if (add == SCX) {
			return (byte) scx;
		} else if (add == SCY) {
			return (byte) scy;
		} else if (add == LY) {
			return (byte) ly;
		} else if (add == LYC) {
			return (byte) lyc;
		} else if (add == WY) {
			return (byte) wy;
		} else if (add == WX) {
			return (byte) wx;
		} else if (add == BGP) {
			return bgpraw;
		} else if (add == OBP0) {
			return obp0raw;
		} else if (add == OBP1) {
			return obp1raw;
		} else {
			throw new RuntimeException("GPU->couldnt decode address:" + Utils.dumpHex(add) + " (Read)");
		}
	}
	/**
	 * 160x144 pixels to draw
	 * 
	 * @param cpucycles
	 */
	public void tick(int cpucycles) {
		
		if (!lcdEnabled) {
			scanlinecyc = 456;
			ly = 0;
			mode = 1;
		} else {
			int oldMode = mode;
			// mode 0: 204 cycles
			// mode 1:4560 cycles
			// mode 2: 80 cycles
			// mode 3: 172 cycles

			if (ly >= 144) {
				mode = 1;
			} else if (scanlinecyc >= 456 - 80) { // counting downwards!
				mode = 2;
			} else if (scanlinecyc >= 456 - 80 - 172) {
				mode = 3;
			} else {
				mode = 0;
			}
			// request interrupt if entered a new mode and if interrupt flag is
			// set
			// (mode 3 has no interrupt)
			if (mode != 3 && oldMode != mode && (((stat >> (3 + mode)) & 1) == 1))
				mem.requestInterrupt(CPU.LCD_IR);

			// coincidence flag
			coincidence = 0;
			if (ly == lyc && (stat & 0x40) == 0x40) {
				mem.requestInterrupt(CPU.LCD_IR);// TODO: this gets spammed
				coincidence = 1;
			}
		}
		if (!lcdEnabled)
			return;

		
		scanlinecyc -= cpucycles;
		if (scanlinecyc <= 0) {
			scanlinecyc = 456 + scanlinecyc; // adjust if taken too many
			ly++;

			// VBlank?
			if (ly == 144)
				mem.requestInterrupt(CPU.VBLANK_IR);

			if (ly > 153)
				ly = 0;

			// draw renderscanline
			if (ly < 144) {
				if (bgEnable)
					drawBg();
				if(sprEnable)
					drawSpr();
			}
		}
	}

	public void drawBg() { // bg scanline
		int y = ly + scy;// which scanline in the tilemap
		y = y % 256;// wrap around bg map

		int intiley = y % 8 * 2;

		boolean signed = bgWiTiledata == 0x9000;
		int bgEntry = bgTilemap + y / 8 * 32;// 20 tiles per scanline

		for (int x = 0; x < 160; x++) {
			int tx = (x + scx) % 256;
			// fetch namtable byte
			byte tileid = mem.readByte(bgEntry + tx / 8);

			int tileloc = bgWiTiledata + (signed ? (int) tileid : tileid & 0xff) * 16;

			// fetch tile pattern
			byte lo = mem.readByte(tileloc + intiley);
			byte hi = mem.readByte(tileloc + intiley + 1);

			int intilex = tx % 8;

			int color = palette[bgp[(lo >> (7 - intilex) & 1) | ((hi >> (7 - intilex) & 1) << 1)]];
			videobuffer[x][ly] = color;
		}
		

	}
	public void drawSpr() {

	}

	/**
	 * Debug method, gets 8 pixel from the pattern table
	 */
	public int[] get8bg(int line, byte tile, int table) {
		line = line % 8;
		// int target = table == 0 ? 0x8000 : 0x9000;
		int target =bgWiTiledata;
		boolean signed = target == 0x9000;
		int realtile = (signed ? (int) tile : tile & 0xff);
		int patternentry = target + realtile * 16 + line * 2;
		byte lo = mem.readByte(patternentry);
		byte hi = mem.readByte(patternentry + 1);
		int[] ib = new int[8];
		for (int i = 0; i < 8; i++) {
			ib[i] = palette[bgp[(lo >> (7 - i)) & 1 | ((hi >> (7 - i)) & 1) << 1]];
		}
		return ib;
	}



	@Override
	public void link(GBComponents comps) {
		this.mem = comps.mem;

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}

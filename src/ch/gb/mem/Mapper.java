/*******************************************************************************
 *     <A simple gameboy emulator>
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/
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
			mbc = new RomOnly();// ROM
			break;
		case 0x01:
			mbc = new MBC1(false, false);// MBC1
			break;
		case 0x02:// MBC1 + RAM
			mbc = new MBC1(true, false);
			break;
		case 0x03:// MBC1 + RAM + BATTERY
			mbc = new MBC1(true, true);
			break;
		}

		mbc.setMem(mem);
		mbc.setRom(rom);
		mbc.init();// load initial banks
		return mbc;
	}

	public abstract void init();

	public abstract void write(int add, byte b);

	protected void switch16kRom(int bankaddr, byte[] newbank) {
		mem.rombanks[bankaddr] = newbank;
	}

	protected void switch8kRam(byte[] newbank) {
		mem.exram = newbank;
	}

	public abstract int getNumRamBanks();

	public abstract byte[][] getRam();

	public abstract boolean hasSramOrBattery();
	public abstract void loadRam();
	public abstract void saveRam();

	public void setMem(MemoryManager mem) {
		this.mem = mem;
	}

	public void setRom(Rom rom) {
		this.rom = rom;
	}
}

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

public class MBC3 extends Mapper {
	private int rombank = 1;
	private int rtcRamMode = 0;
	private boolean ramAndRtcEnabled = false;

	private int numRamBanks = 0;
	private int ramsize;
	private byte ram[][] = null;

	private final boolean hasRam;
	private final boolean saveable;

	public MBC3(boolean hasRam, boolean saveable) {
		this.hasRam = hasRam;
		this.saveable = saveable;
	}

	@Override
	public void init() {
		switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
		switch16kRom(ROM_0x4000, rom.get16kRomBank(1));

		if (hasRam) {
			ramsize = rom.getRamSize();
			if (ramsize > 0x2000) {// split in banks
				numRamBanks = ramsize / 0x2000;
				ram = new byte[numRamBanks][0x2000];
			} else {
				numRamBanks = 1;
				ram = new byte[1][0x2000];// auch fuer 2kB ram, dann halt
											// einfach zu "viel" uebrig
			}
			switch8kRam(ram[0]);
		}
	}

	@Override
	public void write(int add, byte b) {
		if (add < 0x2000) {
			// RAM Enable
			if ((b & 0xff) == 0xA) {
				ramAndRtcEnabled = true;
			} else if (b == 0) {
				ramAndRtcEnabled = false;
			}
		} else if (add < 0x4000) {

			// select ROM bank number (all 7 bits)
			rombank = b & 0x7f;
			if (rombank == 0x00)
				rombank++;
			switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank % rom.get16kRomNum()));// wrapping
																						// fix

		} else if (add < 0x6000) {

		} else if (add < 0x8000) {
			// ROM/RAM mode select
			rtcRamMode = b & 1;
		}
	}

	@Override
	public int getNumRamBanks() {
		return numRamBanks;
	}

	@Override
	public boolean hasSramOrBattery() {
		return saveable;
	}

	@Override
	public byte[][] getRam() {
		return hasRam ? ram : null;
	}

	@Override
	public void loadRam() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveRam() {
		// TODO Auto-generated method stub

	}
}

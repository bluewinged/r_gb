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

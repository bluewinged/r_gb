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
package ch.gb;

import ch.gb.apu.APU;
import ch.gb.cpu.CPU;
import ch.gb.gpu.GPU;
import ch.gb.mem.MemoryManager;

public class GBComponents {
	public CPU cpu;
	public MemoryManager mem;
	public GPU gpu;
	public APU apu;
	public void link(){
		cpu.link(this);
		mem.link(this);
		gpu.link(this);
		apu.link(this);
	}
}

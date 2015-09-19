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
import ch.gb.io.Joypad;
import ch.gb.io.Serial;
import ch.gb.io.SpriteDma;
import ch.gb.io.Timer;
import ch.gb.mem.Memory;

public class GBComponents {

    public CPU cpu;
    public Memory mem;
    public GPU gpu;
    public APU apu;
    public Timer timer;
    public Joypad joypad;
    public Serial serial;
    public SpriteDma spriteDma;

    public void connect() {
        cpu.connect(this);
        mem.connect(this);
        gpu.connect(this);
        apu.connect(this);
        timer.connect(this);
        joypad.connect(this);
        serial.connect(this);
        spriteDma.connect(this);
    }
}

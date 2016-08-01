/* 
 * Copyright (C) 2017 bluew
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.gb.io;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.Config;
import ch.gb.cpu.CPU;
import ch.gb.mem.Memory;

public class Joypad implements IOport, IJoypad, Component {

    public final static int P1 = 0xff00;

    private int joy8 = 0xff;// all buttons mapped as 4 and 4 bits
    private byte selection = 0x3F;

    private Memory mem;

    public Joypad() {

    }

    @Override
    public void reset() {
        joy8 = 0xff;
        selection = 0x3F;
    }

    public void connect(GBComponents comps) {
        mem = comps.mem;
    }

    @Override
    public void write(int add, byte b) {
        //System.out.println("happens");
        // select buttons or direction pad
        selection &= 0xCF;
        selection |= (b & 0x30);
    }

    @Override
    public byte read(int add) {
        //System.out.println("hthattooappens");
        if ((selection & 0x20) == 0) {
            // select buttons
            // System.out.println(Integer.toBinaryString(selection &
            // (0x30|((joy8 >> 4) & 0xf))));
            return (byte) (selection & (0x30 | ((joy8 >> 4) & 0xf)));

        } else if ((selection & 0x10) == 0) {
            // select direction pad
            // System.out.println("faggy");
            return (byte) (selection & (0x30 | (joy8 & 0xf)));
        }
        return (selection);
    }

    public void tick() {

    }

    // 0 = pressed
    // 1 = not pressed
    @Override
    public boolean keyDown(int keycode) {
        if (Config.joymap.containsKey(keycode)) {
            mem.requestInterrupt(CPU.JOYPAD_IR);
            // clear corresponding bit
            int key = Config.joymap.get(keycode);
            joy8 &= ~(1 << key);

            if (key > 3 && (selection & 0x20) == 0) { // = buttons
                mem.requestInterrupt(CPU.JOYPAD_IR);
            } else if (!(key > 3) && (selection & 0x10) == 0) {
                mem.requestInterrupt(CPU.JOYPAD_IR);
            }
            // System.out.println("whatever");
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (Config.joymap.containsKey(keycode)) {
            // set corresponding bit
            joy8 |= (1 << Config.joymap.get(keycode));

            // System.out.println(Utils.dumpHex(joy8));
        }
        return false;
    }

}

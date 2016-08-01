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
import ch.gb.mem.Memory;
import ch.gb.utils.Utils;

public class SpriteDma implements IOport, Component {

    private Memory mem;
    public static final int OAM_DMA = 0xFF46;
    private int count;
    private int dmaSrc;
    private int dmaDest;
    private int accum;

    public SpriteDma() {

    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    public void connect(GBComponents comps) {
        this.mem = comps.mem;
    }

    @Override
    public void write(int add, byte b) {
        count = 161 * 4;
        count -= 4;
        //  for testing assume instant DMA transfer
//        int address = (b & 0xff) << 8; // source address is data * 100
//        for (int i = 0; i < 0xA0; i++) {
//            mem.writeByte(0xFE00 + i, mem.readByte(address + i));
//        }
        dmaSrc = (b & 0xff) << 8;
        dmaDest = 0xFE00;
        accum =0;
        //System.out.println("DMA START");
    }

    @Override
    public byte read(int add) {
        return 0;
    }

    public void clock(int cycles) {

        if (count > 0) {
            accum += cycles;
            while (accum >= 4 && count >0) {

                mem.writeByte(dmaDest, mem.readByte(dmaSrc));
                //System.out.println("from:" + Utils.dumpHex(dmaSrc) + "  to:" + Utils.dumpHex(dmaDest));
                accum -= 4;
                count -= 4;
                dmaDest++;
                dmaSrc++;
            }
            if (count == 0) {
                accum = 0;
                //System.out.println("DMA END");
                //System.out.println();
            }
        }
    }

}

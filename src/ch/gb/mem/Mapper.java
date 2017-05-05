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
package ch.gb.mem;

import java.util.Arrays;

/**
 * Memory bank controller handles bankswitching to extend addressable memory
 *
 * @author bluew
 *
 */
public abstract class Mapper {

    public static final int ROM_0x0000 = 0;
    public static final int ROM_0x4000 = 1;
    protected Memory mem;
    protected Cartridge rom;

    public byte[][] rombanks; // 2 x 16kB
    public byte[] exram;// 8kB external Ram

    public Mapper() {
        rombanks = new byte[2][0x4000];
        exram = new byte[0x2000];
    }

    public static Mapper createMBC(Memory mem, Cartridge rom) {
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
            case 0x0F:
                mbc = new MBC3(true, false, true);
                break;
            case 0x10:
                mbc = new MBC3(true, true, true);
                break;
            case 0x11:
                mbc = new MBC3(false, false, false);
                break;
            case 0x12:
                mbc = new MBC3(false, true, false);
                break;
            case 0x13:
                mbc = new MBC3(false, true, true);
                break;
        }
        if (mbc == null) {
            throw new RuntimeException("This particular MBC is not supported");
        }
        mbc.setMem(mem);
        mbc.setRom(rom);
        mbc.init();// load initial banks
        return mbc;
    }

    public abstract void init();

    public abstract void write(int add, byte b);

    public abstract byte read(int add);

    public void reset() {
        Arrays.fill(rombanks[0], (byte) 0);
        Arrays.fill(rombanks[1], (byte) 0);
        Arrays.fill(exram, (byte) 0);
    }

    protected void switch16kRom(int bankaddr, byte[] newbank) {
        rombanks[bankaddr] = newbank;
    }

    protected void switch8kRam(byte[] newbank) {
        exram = newbank;
    }

    public abstract int getNumRamBanks();

    public abstract byte[][] getRam();

    public abstract boolean hasSramOrBattery();

    public abstract void loadRam();

    public abstract void saveRam();

    public void setMem(Memory mem) {
        this.mem = mem;
    }

    public void setRom(Cartridge rom) {
        this.rom = rom;
    }
}

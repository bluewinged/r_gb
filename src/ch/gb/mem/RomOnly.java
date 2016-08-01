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

public class RomOnly extends Mapper {

    public RomOnly() {
        super();
    }

    @Override
    public void init() {
        switch16kRom(ROM_0x0000, rom.get16kRomBank(0));
        switch16kRom(ROM_0x4000, rom.get16kRomBank(1));
    }

    @Override
    public void write(int add, byte b) {
        // nothing happens
    }

    @Override
    public byte read(int add) {
        if (add < 0x4000) {
            // 16kB Rom bank #0
            return rombanks[0][add];
        } else if (add < 0x8000) {
            // 16kB Rom switchable
            return rombanks[1][add - 0x4000];
        } else if ( 0xA000<= add && add <0xC000){
            // 8kB exram (optionally wired)
            return exram[add - 0xA000];
        } else {
            throw new RuntimeException("Memory access violation in RomOnly");
        }
    }

    @Override
    public int getNumRamBanks() {
        return 0;

    }

    @Override
    public byte[][] getRam() {

        return null;
    }

    @Override
    public boolean hasSramOrBattery() {
        return false;
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

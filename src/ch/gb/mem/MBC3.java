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

import java.util.Calendar;

public class MBC3 extends Mapper {

    private int rombank = 1;
    private int rtcRamMode = 0;
    private boolean ramBankingEnabled = false;
    private boolean rtcEnabled = false;

    private int numRamBanks = 0;
    private int ramsize;
    private byte ram[][] = null;

    private final boolean hasRam;
    private final boolean saveable;
    private byte rtcLatch1 = (byte) 0xff;
    private byte rtcLatch2 = (byte) 0xff;
    private byte[] rtcReg;
    private byte[] latchReg;
    private byte bankBits;
    private boolean hasTimer;

    public MBC3(boolean hasTimer, boolean hasRam, boolean saveable) {
        this.hasTimer = hasTimer;
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
                ram = new byte[1][0x2000];
            }
            switch8kRam(ram[0]);
        }
        rtcReg = new byte[8];
        latchReg = new byte[8];
    }

    @Override
    public void write(int add, byte b) {
        if (add < 0x2000) {
            // RAM Enable
            if ((b & 0xF) == 0xA) {
                if (hasRam) {
                    ramBankingEnabled = true;
                }
                if (hasTimer) {
                    rtcEnabled = true;
                }
            } else {
                ramBankingEnabled = rtcEnabled = false;
            }
        } else if (add < 0x4000) {
            // select ROM bank number (all 7 bits)
            rombank = b & 0x7f;
            if (rombank == 0x00) {
                rombank++;
            }
            switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank % rom.get16kRomNum()));

        } else if (add < 0x6000) {
            bankBits = b;
            if (b <= 3) {
                switch8kRam(ram[b & 3]);
            } else {
                //nothing else to do
            }

        } else if (add < 0x8000) {
            if (rtcEnabled) {
                if (rtcLatch1 == 0xff && b == 0) {
                    rtcLatch1 = 0;
                } else if (rtcLatch2 == 0xff & b == 1) {
                    latchRTC();
                    //reset
                    rtcLatch1 = rtcLatch2 = (byte) 0xff;
                }
            }
        } else if (0xA000 <= add && add < 0xC000) {
            // 8kB exram           
            if (ramBankingEnabled && (bankBits <= 3)) {
                exram[(add - 0xA000) % exram.length] = b;
            } else if (rtcEnabled && (bankBits >= 0x8) && (bankBits <= 0xC)) {
                rtcReg[bankBits - 8] = b;
            }
        } else {
            throw new RuntimeException("Memory access violation in MBC3");
        }
    }

    @Override
    public byte read(int add) {
        if (add < 0x4000) {
            // 16kB Rom bank #0
            return rombanks[0][add];
        } else if (add < 0x8000) {
            // 16kB Rom switchable
            return rombanks[1][add - 0x4000];
        } else if (0xA000 <= add && add < 0xC000) {
            // 8kB exram 
            if (ramBankingEnabled && bankBits <= 3) {
                return exram[(add - 0xA000) % exram.length];
            } else if (rtcEnabled && (0x8 <= bankBits) && (bankBits <= 0xC)) {
                return latchReg[bankBits - 8];
            } else {
                return 0x00;
            }
        } else {
            throw new RuntimeException("Memory access violation in MBC3");
        }
    }

    //this is obviously totally wrong. if a new value gets written to the regsit doesnt count from there
    private void latchRTC() {
        long time = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        // Calendar.getInstance().getTime().
        rtcReg[0] = (byte) calendar.get(Calendar.SECOND);
        if (rtcReg[0] > 59) {
            rtcReg[0] = 59; //because of leap seconds
        }
        rtcReg[1] = (byte) calendar.get(Calendar.MINUTE);
        rtcReg[2] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_YEAR); //TODO: starts at 1 for day 0
        rtcReg[3] = (byte) (day & 0xff);
        rtcReg[4] = (byte) (rtcReg[4] & (~1)); //clear lowest bit
        rtcReg[4] |= ((day >>> 8) & 1);
        //TODO: day counter carry bit and halt not implemented yet
        for (int i = 0; i < 5; i++) {
            latchReg[i] = rtcReg[i];
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

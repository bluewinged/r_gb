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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import ch.gb.Config;
import ch.gb.utils.RessourceLoader;
import ch.gb.utils.Utils;

public class MBC1 extends Mapper {

    private int rombank = 1;
    private int romRamMode = 0;
    private boolean ramBankingEnabled = false;

    private int numRamBanks = 0;
    private int ramsize;
    private byte ram[][] = null;

    private final boolean hasRam;
    private final boolean saveable;

    public MBC1(boolean hasRam, boolean saveable) {// either by SRAM or Battery
        //super();
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
            loadRam();
        }
    }

    @Override
    public void write(int add, byte b) {
        // System.out.println("DETECTED MBC1 WRITE ABORT:" + Utils.dumpHex(add)
        //+ "->" + Utils.dumpHex(b));
        if (add < 0x2000) {
            // RAM Enable
            ramBankingEnabled = (b & 0xF) == 0xA;
        } else if (add < 0x4000) {
            // select ROM bank number (only lower 5 bits are used!)
            rombank &= 0x60;// clear lower 5 and uppermost bit
            rombank |= (b & 0x1F);// set lower 5;
            if ((rombank & 0x1f) == 0) { //0x00, 0x20, 0x40, 0x60 are translated to 0x01, 0x21, 0x41 etc
                rombank++;
            }
            switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank % rom.get16kRomNum()));//wrapping fix

        } else if (add < 0x6000) {
            b &= 3;// 2bit reg
            // select RAM bank number or Upper bits of ROM bank
            if (romRamMode == 0) {// ROM mode
                rombank &= (~0x60); //clear bit 5 and 6
                rombank |= (b << 5);
                switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank % rom.get16kRomNum()));
                switch8kRam(ram[0]);
            } else { // RAM mode
                switch16kRom(ROM_0x4000, rom.get16kRomBank(rombank & 0x1f));
                switch8kRam(ram[b]);
            }
        } else if (add < 0x8000) {
            // ROM/RAM mode select
            romRamMode = b & 1;
        } else if (0xA000 <= add && add < 0xC000) {
            // 8kB exram 
            if (ramBankingEnabled) {
                exram[(add - 0xA000) % exram.length] = b;
            }
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
            return ramBankingEnabled ? exram[(add - 0xA000) % exram.length]:0x00;
        } else {
            throw new RuntimeException("Memory access violation in MBC1");
        }
    }

    @Override
    public void loadRam() {
        if (!saveable) {
            return;
        }
        String filename = rom.getLoadPath();
        String[] splits = filename.split(Pattern.quote(File.separator));
        filename = Utils.removeExtension(splits[splits.length - 1]);
        filename += ".sav";

        //FileHandle filehandle = Gdx.files.external(Settings.root+filename);
        //if (!filehandle.exists())
        //return;
        //InputStream is = filehandle.read();
        InputStream is = null;
        try {
            is = RessourceLoader.load(Config.root + File.separatorChar + filename);
        } catch (FileNotFoundException e1) {
            System.out.println("No savegame found");
            return;
        }

        try {
            for (int i = 0; i < numRamBanks; i++) {
                for (int x = 0; x < 0x2000; x++) {
                    ram[i][x] = (byte) is.read();
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Found existing savegame and loaded it");
    }

    @Override
    public void saveRam() {
        if (rom != null) {
            if (getNumRamBanks() == 0 || !hasSramOrBattery()) {
                return;
            }
            String filename = rom.getLoadPath();
            String[] splits = filename.split(Pattern.quote(File.separator));
            filename = Utils.removeExtension(splits[splits.length - 1]);
            filename += ".sav";
            System.out.println("Filename:" + filename);
			//FileHandle filehandle = Gdx.files.external(Settings.root + filename);

            //OutputStream os = filehandle.write(false);
            OutputStream os = null;
            try {
                os = RessourceLoader.write(Config.root + File.separatorChar + filename);
            } catch (FileNotFoundException e1) {
                System.err.println("Couldnt save Ram");
                return;
            }
            try {
                for (int i = 0; i < getNumRamBanks(); i++) {
                    os.write(getRam()[i]);
                }
                os.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //System.out.println("Saved Ram in " + filename);
            //System.out.println("Path:" + filehandle.path() + " -> relative to user/<username>/");
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

}

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
package ch.gb.apu;

public class Noise extends Channel {

    private final int freq = 0;
    //private final int period = 2048 * 4;

    private int envvol;
    private int envadd;
    private int envperiod;
    private int envcounter = 0;
    private boolean envtriggered = true;

    private final int lengthmask = 0x40;
    private int lc;
    private boolean enabled; // also channel enabled flag (?)

    private int clockshift;
    private int widthmode;
    private int divisorcode;
    private int lfsr;
    private int sample;

    private final int[] noiseperiod = {8, 16, 32, 48, 64, 80, 96, 112};

    public Noise() {

    }

    @Override
    void reset() {

    }
    //TODO: tetris uses one of the timers to time the noise channel
    //and it seems to be a factor of 2 slower
    public void powerOn() {

    }

    private int reloadEnv() {
        int period = nr2 & 7;
        envcounter = (period != 0 ? period : 8);
        return period;
    }

    private boolean dacEnabled() {
        return (nr2 & 0xF8) != 0;
    }

    @Override
    void write(int add, byte b) {
        if (ignoreWrite) {
            return;
        }
        if (add == APU.NR40) {
            nr0 = b;
        } else if (add == APU.NR41) {
            nr1 = b;
            lc = 64 - (b & 0x3f); //load length counter
        } else if (add == APU.NR42) {
            nr2 = b;
            envvol = b >> 4 & 0xf;
            envadd = (b & 8) == 8 ? 1 : -1;
            envperiod = b & 7;
        } else if (add == APU.NR43) {
            nr3 = b;
            divisorcode = b & 7;
            widthmode = b >> 3 & 1;
            clockshift = b >> 4 & 0xf;
        } else if (add == APU.NR44) {
            nr4 = b;
            if ((b & triggermask) == triggermask) {// trigger
                nr4 &= 0x7F;// clear trigger flag

                envtriggered = true;
                enabled = true;
                if (lc == 0) {
                    lc = 64;
                }
                divider = noiseperiod[divisorcode];// TODO:low 2 bits are not modified
                reloadEnv();
                envvol = (nr2 >> 4) & 0xf;// reload volume

                lfsr = 0x7fff; //set all 15 bits to one
                if (!dacEnabled()) {
                    enabled = false;
                }

            }
        }
    }

    @Override
    byte read(int add) {
        if (add == APU.NR40) {
            return (byte) (nr0 | 0xff);
        } else if (add == APU.NR41) {
            return (byte) (nr1 | 0xff);
        } else if (add == APU.NR42) {
            return (byte) (nr2 | 0x00);
        } else if (add == APU.NR43) {
            return (byte) (nr3 | 0x00);
        } else if (add == APU.NR44) {
            return (byte) (nr4 | 0xBF);
        }
        throw new RuntimeException("shouldn't happen");
    }

    void clock(int cycles) {
        divider -= cycles;
        while (divider <= 0) {
            divider += ((noiseperiod[divisorcode])<<clockshift);
            int top = (lfsr & 1) ^ ((lfsr >> 1) & 1); //xor bit 0 and bit 1
            lfsr = lfsr >>> 1;
            lfsr = lfsr | (top << 14);
            if (widthmode == 1) {
                lfsr = ((lfsr & (~0x40)) | (top << 6));//put top into bit 6 (starting from bit 0)
            }
        }
    }

    void clocklen() {
        if ((nr4 & lengthmask) != 0 && lc != 0) {// length enabled
            if (--lc <= 0) {
                enabled = false;
            }
        }
    }

    void clockenv() {
        if (--envcounter <= 0) {
            envcounter = envperiod == 0 ? 8 : envperiod;// period 0 is treated
            // as 8
            if (envperiod != 0 && envtriggered) {
                envvol += envadd;
            }
            if (envvol == 16 || envvol == -1) {
                envtriggered = false;
            }
            if (envvol == 16) {
                envvol = 15;
            }
            if (envvol == -1) {
                envvol = 0;
            }
        }
    }

    public boolean status() {
        return enabled;
    }

    public int poll() { 
        return enabled ? (envvol* (~(lfsr) & 1)) : 0;
    }

    public void setIgnoreWrite(boolean val) {
        ignoreWrite = val;
    }
}

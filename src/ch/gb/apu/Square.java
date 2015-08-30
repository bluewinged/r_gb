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

public class Square extends Channel {

    private int duty;
    private int freq = 0;
    private int period = 2048 * 4;
    private final int waveform = 0x7EE18180;

    private int envvol;
    private int envadd;
    private int envperiod;
    private int envcounter = 0;
    private boolean envtriggered = true;

    private int sweepcounter;
    private boolean sweepenabled;
    private int sweepfreq;
    private int sweepneg;
    private final int sweepperiodmask = 0x70;
    private final int sweepshiftmask = 0x7;

    private final int lengthenabled = 0x40;
    private int lc;
    private boolean enabled; // also channel enabled flag (?)

    private int sequencer;
    private int sqsample;

    private final int off;
    private final boolean hasSweep;

    Square(boolean is2nd) {
        off = is2nd ? 5 : 0;
        hasSweep = !is2nd;
    }

    @Override
    public void reset() {
        freq = 0;
        duty = 0;
        period = 2048 * 4;
        envvol = 0;
        envadd = 0;
        envcounter = 0;
        envtriggered = true;
        sweepcounter = 0;
        sweepenabled = false;
        sweepfreq = 0;
        sweepneg = 0;
        lc = 0;
        enabled = false;
        sequencer = 0;
        sqsample = 0;
    }

    public void powerOn() {
        sequencer = 0;
    }

    private boolean dacEnabled() {
        return (nr2 & 0xF8) != 0;
    }

    private int reloadEnv() {
        int envperiod = nr2 & 7;
        envcounter = (envperiod != 0 ? envperiod : 8);
        return envperiod;
    }

    private int getFrequency() {
        return (((nr4 & 7) << 8) | (nr3 & 0xff));
    }

    private int getPeriod() {
        return (2048 - getFrequency()) * 4;
    }

    private void reloadSweep() {
        sweepcounter = (nr0 >> 4) & 7;
        if (sweepcounter == 0) {
            sweepcounter = 8;
        }
    }

    @Override
    void write(int add, byte b) {
        if (add == 0xFF10 + off) {
            nr0 = b;
        } else if (add == 0xFF11 + off) {
            nr1 = b;
            duty = (b >> 6) & 3;
            lc = 64 - (b & 0x3f);
        } else if (add == 0xFF12 + off) {
            nr2 = b;
            envvol = (b >> 4) & 0xf;
            envadd = (b & 8) == 8 ? 1 : -1;
            envperiod = b & 7;
            if (!dacEnabled()) {
                enabled = false;
            }
        } else if (add == 0xFF13 + off) {
            nr3 = b;
            freq &= 0x700;
            freq |= b;
            period = (2048 - freq) * 4;

        } else if (add == 0xFF14 + off) {
            // System.out.println(Utils.dumpHex(b));
            nr4 = b;
            freq &= 0xff;
            freq |= ((b & 7) << 8);
            period = (2048 - freq) * 4;

            // enabled = (b & 0x40) == 0x40;//TODO: fixes tetris oo? that isnt
            // correct i think
            if ((b & triggermask) == triggermask) {// trigger
                nr4 &= 0x7F;// clear trigger flag

                envtriggered = true;
                enabled = true;
                if (lc == 0) {
                    lc = 64;
                }
                divider = (divider & 0x3) | (period & (~0x3));// TODO:low 2 bits are not modified
                reloadEnv();
                envvol = (nr2 >> 4) & 0xf;// reload volume

                if (hasSweep) {// only first sq channel
                    sweepfreq = getFrequency();
                    reloadSweep();
                    sweepenabled = false;
                    if ((nr0 & sweepperiodmask) != 0 || (nr0 & sweepshiftmask) != 0) {
                        sweepenabled = true;
                    }
                    if ((nr0 & sweepshiftmask) != 0) {
                        // frequency calculation and overflow check
                        calculateSweep(false);
                    }

                }

            }
            if (!dacEnabled()) {
                enabled = false;
            }
        }
    }

    @Override
    byte read(int add) {
        //System.out.println("anything?" + Utils.dumpHex(add));
        if (add == 0xFF10 + off) { //square 1 has sweep
            return !hasSweep ? (byte) (nr0 | 0xff) : (byte) (nr0 | 0x80);
        } else if (add == 0xFF11 + off) {
            return (byte) (nr1 | 0x3f);
        } else if (add == 0xFF12 + off) {
            return (byte) (nr2 | 0x00);
        } else if (add == 0xFF13 + off) {
            return (byte) (nr3 | 0xff);
        } else if (add == 0xFF14 + off) {
            return (byte) (nr4 | 0xBF ); //set 0x40 to 0x0 and it passes the #2 of the registers testrom 
        } else {    //internal enable != length enable
            throw new RuntimeException("hurrdurr");
        }
    }

    void clock(int cycles) {
        divider -= cycles;
        while (divider <= 0) {
            divider += period;
            sqsample = (waveform >> (sequencer + duty * 8)) & 1;
            sequencer = (sequencer + 1) & 7;
        }
    }

    void clocklen() {
        if ((nr4 & lengthenabled) != 0 && lc != 0) {// length enabled
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

    void clocksweep() {
        if (--sweepcounter <= 0) {
            reloadSweep();
            if (sweepenabled && (nr0 & sweepperiodmask) != 0) {
                // calc frequency and overflow check
                calculateSweep(true);
                calculateSweep(false);
            }
        }
    }

    private void calculateSweep(boolean wantUpdate) {
        int tmpfreq = sweepfreq;
        int shift = (nr0 & sweepshiftmask);
        int offset = tmpfreq >> shift;
        sweepneg = nr0 & 8;
        if (sweepneg != 0) {
            offset = -offset;
        }
        tmpfreq += offset;
        if (tmpfreq > 2047) {
            enabled = false;
        } else if (shift != 0 && wantUpdate) {
            sweepfreq = tmpfreq;
            //update frequency and period
            nr3 = (byte) (tmpfreq & 0xff);
            nr4 = (byte) ((nr4 & 0xF8) | ((tmpfreq >> 8) & 7));
            freq = getFrequency();
            period = getPeriod();
        }
    }

    public boolean status() {
        return enabled;
    }

    public int poll() {
        return enabled ? sqsample * envvol : 0;
    }

}

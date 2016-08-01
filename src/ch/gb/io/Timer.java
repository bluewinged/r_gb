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
import ch.gb.cpu.CPU;
import ch.gb.mem.Memory;
import ch.gb.utils.Utils;

public class Timer implements IOport, Component {

    public static final int DIVAddr = 0xff04;
    public static final int TIMAAddr = 0xff05;
    public static final int TMAAddr = 0xff06;
    public static final int TACAddr = 0xff07;

    private byte div;
    private byte tima;// int for overflow control
    private byte tma;
    private byte tac;

    private final int period4096hz = 1024;
    private final int period16384hz = 256;// period in clockcycles
    // private final int period32768hz = 128;
    private final int period65536hz = 64;
    private final int period262144hz = 16; //changed to from 16
    private int divcounter = period16384hz;
    private int timercounter = period4096hz;
    private int timerperiod;

    private Memory mem;

    public Timer() {

    }

    @Override
    public void reset() {
        div = 0;
        tima = 0;
        tma = 0;
        tac = 0;
        divcounter = period16384hz;
        timercounter = period4096hz;

    }

    public void connect(GBComponents comps) {
        mem = comps.mem;
    }

    public void clock(int ticks) {
        // divider reg
        divcounter -= ticks;
        while (divcounter <= 0) {
            divcounter += period16384hz;// in case already far < 0
            div++;
        }

        // timer reg
        if ((tac & 4) == 4) {// timer enabled
            timercounter -= ticks;

            while (timercounter <= 0) {
                timercounter += timerperiod;
                tima++;
                if (tima == 0) {
                    mem.requestInterrupt(CPU.TIMER_IR);
                    tima = tma;
                }
            }
        }
    }

    @Override
    public void write(int add, byte b) {
        if (add == DIVAddr) {
            div = 0;
        } else if (add == TIMAAddr) {
            tima = b;
        } else if (add == TMAAddr) {
            tma = b;
        } else if (add == TACAddr) {
            tac = (byte) (b & 7);
            switch (tac & 3) {
                case 0:
                    timerperiod = period4096hz;
                    break;
                case 1:
                    timerperiod = period262144hz;
                    break;
                case 2:
                    timerperiod = period65536hz;
                    break;
                case 3:
                    timerperiod = period16384hz;
                    break;
            }
            timercounter = timerperiod;
        }
    }

    @Override
    public byte read(int add) {
        if (add == DIVAddr) {
            return div;
        } else if (add == TIMAAddr) {
            // System.out.println("forrealcall:"+tima);
            return (byte) tima;
        } else if (add == TMAAddr) {
            return tma;
        } else if (add == TACAddr) {
            return tac;
        } else {
            throw new RuntimeException("TIMER-> Shouldnt reacht this statement");
        }

    }

}

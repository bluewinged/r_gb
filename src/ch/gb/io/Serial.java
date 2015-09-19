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
package ch.gb.io;

import ch.gb.Component;
import ch.gb.GBComponents;

public class Serial implements IOport, Component {

    public static final int SB = 0xFF01;
    public static final int SC = 0xFF02;
    private byte serialtransferdata = (byte) 0xff;
    private byte serialtransfercontrol;

    private final byte tmp[] = new byte[1];

    @Override
    public void reset() {
        tmp[0] = (byte) 0;
        serialtransferdata = (byte) 0xff;
        serialtransfercontrol = (byte) 0;

    }

    public void connect(GBComponents comps) {

    }

    @Override
    public void write(int add, byte b) {
        if (add == SB) {
            tmp[0] = serialtransferdata = b;
            //System.out.print(Utils.decASCII(tmp)); // HOLY SHIT
        } else if (add == SC) {
            if (b == 0x81) {
                //System.out.print(Utils.decASCII(tmp));
                serialtransfercontrol = 1; // transfer "finished"
            }
        } else {
            throw new RuntimeException("Serial-> couldnt map write");
        }
    }

    @Override
    public byte read(int add) {
        if (add == SB) {
            return serialtransferdata;
        } else if (add == SC) {
            return serialtransfercontrol;
        }
        throw new RuntimeException("Serial-> coulnt map read");
    }

}

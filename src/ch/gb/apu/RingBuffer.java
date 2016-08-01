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
package ch.gb.apu;

import java.util.Arrays;

/**
 *
 * @author bluew 
 */
public class RingBuffer {

    private int size;
    private byte[] buffer;
    private int readptr = 0;
    private int writeptr = 0;
    private int diff = 0;

    public RingBuffer(int size) {
        this.size = size;
        buffer = new byte[size];
    }

    public void put(byte data) {
        buffer[writeptr] = data;
        writeptr = (writeptr + 1) % size;
        diff++;
        diff = Math.min(diff,size);//clamping
    }

    public byte[] get(int desired) {
        int possible = Math.min(desired, diff);//# of elements
        if(possible==0){
            return null;
        }
        diff = diff - possible;
        byte[] result;
        if (readptr + possible <= size) { //one piece
           result = Arrays.copyOfRange(buffer, readptr, readptr+possible);//definitely not the tryhard solution                  
        } else { //two pieces
           result = new byte[possible];
           System.arraycopy(buffer, readptr, result, 0, size-readptr);
           System.arraycopy(buffer, 0, result, size-readptr, possible - (size-readptr));
        }

        readptr = (readptr+possible)%size;
        return result;
    }
    public int buffered(){
        return diff;
    }
    @Override
    public String toString(){
        return Arrays.toString(buffer);
    }
}

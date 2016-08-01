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

public class RTC {
	private int latchcounter;
	private final byte[] regs;
	private byte lastLatch=(byte)0xff;
	public RTC() {
		regs = new byte[5];
	}

	public void write(int add, byte b) {
		if (add >= 0x400 && add < 0x6000) {

		} else if (add < 0x800) {
			if(lastLatch==0 && b==1)
				latch();
			lastLatch=b;
		}
	}
	
	private void set(boolean enabled,byte b){
		b &=0xf;
		b-=8;
	}
	
	private void latch() {
		long unixTime = System.currentTimeMillis()/1000L;
	}
}

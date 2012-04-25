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
package ch.gb.utils;

public class FreqMeter {
	private static long startPoint;
	private static long calls;
	private static boolean flag = true;

	private FreqMeter() {

	}

	public static void forceAccurateMeasuring() {
		new Thread() {
			{
				setDaemon(true);
				start();
			}

			public void run() {
				while (true) {
					try {
						Thread.sleep(Long.MAX_VALUE);
					} catch (Throwable t) {
					}
				}
			}
		};
	}

	public static void measure() {
		calls++;
		if (flag) {
			startPoint = System.currentTimeMillis();
			flag = false;
		}
		if (System.currentTimeMillis() - startPoint >= 1000) {
			System.out.println("frequency:" + (calls) + " f");
			calls = 0;
			startPoint = System.currentTimeMillis();
			flag = true;
		}
	}

	public static void reset() {

	}
}

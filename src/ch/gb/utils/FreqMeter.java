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

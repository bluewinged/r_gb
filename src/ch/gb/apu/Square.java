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

	private int lc;
	private boolean lengthEnable; // also channel enabled flag (?)

	private int sequencer;
	private int sqsample;

	private final int off;

	Square(boolean is2nd) {
		off = is2nd ? 5 : 0;
	}

	public void powerOn() {
		sequencer = 0;
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
			envperiod = b & 3;
			if ((b & 0xF8) == 0) { // check DAC
				lengthEnable = false;
			}
		} else if (add == 0xFF13 + off) {
			nr3 = b;
			freq &= 0x700;
			freq |= b;
			period = (2048 - freq) * 4;

		} else if (add == 0xFF14 + off) {
			nr4 = b;
			freq &= 0xff;
			freq |= ((b & 7) << 8);
			period = (2048 - freq) * 4;

			lengthEnable = (b & 0x40) == 0x40;

			if ((b & 0x80) == 0x80) {// trigger
				envtriggered = true;
				lengthEnable = true;
				if (lc == 0)
					lc = 64;

				divider = period;// TODO:low 2 bits are not modified
				envperiod = nr2 & 3;// reload env period
				envvol = (nr2 >> 4) & 0xf;// reload volume
				// TODO: sweep does some stuff

				if ((b & 0xF8) == 0) {// check DAC
					lengthEnable = false;
					lc = 0;
				}
			}
		}
	}

	@Override
	byte read(int add) {
		// System.out.println("anything?"+Utils.dumpHex(add));
		if (add == 0xFF10 + off) {
			return off == 5 ? (byte) (nr0 | 0xff) : (byte) (nr0 | 0x80);
		} else if (add == 0xFF11 + off) {
			return (byte) (nr1 | 0x3f);
		} else if (add == 0xFF12 + off) {
			return (byte) (nr2 | 0x00);
		} else if (add == 0xFF13 + off) {
			return (byte) (nr3 | 0xff);
		} else if (add == 0xFF14 + off) {
			// System.out.println("pololo");
			return (byte) (nr4 | 0xBF | (lengthEnable ? 0x40 : 0));
		} else {
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
		if (lengthEnable && lc > 0)
			lc--;
		if (lc == 0) {
			lengthEnable = false;
		}
	}

	void clockenv() {
		if (--envcounter <= 0) {
			envcounter = envperiod == 0 ? 8 : envperiod;// period 0 is treated
														// as 8
			if (envperiod != 0 && envtriggered)
				envvol += envadd;
			if (envvol == 16 || envvol == -1) {
				envtriggered = false;
			}
			if (envvol == 16)
				envvol = 15;
			if (envvol == -1)
				envvol = 0;
		}
	}

	public boolean status() {
		return lengthEnable;
	}

	int poll() {
		return lengthEnable ? sqsample * envvol : 0;
	}

}

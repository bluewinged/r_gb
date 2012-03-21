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
	private boolean lcEnabled; // also channel enabled flag

	private int sequencer;
	private int sqsample;

	private final int off;

	Square(boolean is2nd) {
		off = is2nd ? 5 : 0;
	}

	@Override
	void write(int add, byte b) {
		if (add == 0xFF10 + off) {
			nr0 = b;
		} else if (add == 0xFF11 + off) {
			nr1 = b;
			duty = (b >> 6) & 3;
			lc = b & 0x3f;
		} else if (add == 0xFF12 + off) {
			nr2 = b;
			envvol = (b >> 4) & 0xf;
			envadd = (b & 8) == 8 ? 1 : -1;
			envperiod = b & 3;
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
			lcEnabled = (b & 0x40) == 0x40;
			if ((b & 0x80) == 0x80) {// trigger event
				envtriggered = true;
				lcEnabled = true;
				nr4 |= 0x80;
				if (lc == 0)
					lc = 64;
				divider = period;
				envperiod = nr2 & 3;// reload env period
				envvol = (nr2 >> 4) & 0xf;// reload volume
				// TODO: sweep does some stuff
			}
		}
	}

	@Override
	byte read(int add) {
		if (add == 0xFF10 + off) {
			return off == 5 ? (byte) (nr0 | 0xff) : (byte) (nr0 | 0x80);
		} else if (add == 0xFF11 + off) {
			return (byte) (nr1 | 0x3f);
		} else if (add == 0xFF12 + off) {
			return (byte) (nr2 | 0x00);
		} else if (add == 0xFF13 + off) {
			return (byte) (nr3 | 0xff);
		} else if (add == 0xFF14 + off) {
			return (byte) (nr4 | 0xBF);
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
		if (lcEnabled && lc > 0)
			lc--;
		if (lc == 0) {
			lcEnabled = false;
			nr4 &= 0xBF; // disable channel
		}
	}

	void clockenv() {
		if (--envcounter <= 0) {
			envcounter = envperiod;
			if (envperiod != 0 && envvol != 15 && envvol != 0 && envtriggered)
				envvol += envadd;
			if (envvol == 15 || envvol == 0)
				envtriggered = false;
		}
	}
	int poll(){
		return lcEnabled? sqsample*envvol:0;
	}


}

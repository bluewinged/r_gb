package ch.gb.apu;

import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.cpu.CPU;

public class APU implements Component {

	// Tone & sweep
	public static final int NR10 = 0xFF10;
	public static final int NR11 = 0xFF11;
	public static final int NR12 = 0xFF12;
	public static final int NR13 = 0xFF13;
	public static final int NR14 = 0xFF14;

	// Tone
	public static final int NR20 = 0xFF15;
	public static final int NR21 = 0xFF16;
	public static final int NR22 = 0xFF17;
	public static final int NR23 = 0xFF18;
	public static final int NR24 = 0xFF19;

	// Wave Output
	public static final int NR30 = 0xFF1A;
	public static final int NR31 = 0xFF1B;
	public static final int NR32 = 0xFF1C;
	public static final int NR33 = 0xFF1D;
	public static final int NR34 = 0xFF1E;
	// FF30-FF3F Wave Pattern RAM

	// Noise
	public static final int NR40 = 0xFF20;
	public static final int NR41 = 0xFF20;
	public static final int NR42 = 0xFF21;
	public static final int NR43 = 0xFF22;
	public static final int NR44 = 0xFF23;

	// Soundcontrol
	public static final int NR51 = 0xFF25;
	public static final int NR52 = 0xFF26;

	private SourceDataLine line; // audio line

	private final int samplerate = 44100;
	private final int numChannels = 1;
	private final int bitspersample = 16;
	private int resamplerate; // 95 for 44100hz yeeeaahh
	private int resamplecounter;
	private int seqstep;
	private int seqcounter;

	private final HashMap<Integer, Channel> iochannel;

	private final Square quadrangle1;
	private final Square quadrangle2;
	private final Noise noise;
	private final Wave wave;
	private final PowerControl powercontrol;
	private final Wavetables wt;

	public APU() {
		quadrangle1 = new Square(false);
		quadrangle2 = new Square(true);
		noise = new Noise();
		wave = new Wave();
		powercontrol = new PowerControl();
		wt = new Wavetables();

		iochannel = new HashMap<Integer, Channel>();
		// channel 1
		iochannel.put(NR10, quadrangle1);
		iochannel.put(NR11, quadrangle1);
		iochannel.put(NR12, quadrangle1);
		iochannel.put(NR13, quadrangle1);
		iochannel.put(NR14, quadrangle1);
		// channel 2
		iochannel.put(NR20, quadrangle2);
		iochannel.put(NR21, quadrangle2);
		iochannel.put(NR22, quadrangle2);
		iochannel.put(NR23, quadrangle2);
		iochannel.put(NR24, quadrangle2);
		// channel 3
		iochannel.put(NR30, wave);
		iochannel.put(NR31, wave);
		iochannel.put(NR32, wave);
		iochannel.put(NR33, wave);
		iochannel.put(NR34, wave);
		// channel 4
		iochannel.put(NR40, noise);
		iochannel.put(NR41, noise);
		iochannel.put(NR42, noise);
		iochannel.put(NR43, noise);
		iochannel.put(NR44, noise);
		// power control
		iochannel.put(0xFF24, powercontrol);
		iochannel.put(0xFF25, powercontrol);
		iochannel.put(0xFF26, powercontrol);
		// FF27 ... FF2F not used
		// FF30 ....FF3F wave tables
		iochannel.put(0xFF30, wt);
		iochannel.put(0xFF31, wt);
		iochannel.put(0xFF32, wt);
		iochannel.put(0xFF33, wt);
		iochannel.put(0xFF34, wt);
		iochannel.put(0xFF35, wt);
		iochannel.put(0xFF36, wt);
		iochannel.put(0xFF37, wt);
		iochannel.put(0xFF38, wt);
		iochannel.put(0xFF39, wt);
		iochannel.put(0xFF3A, wt);
		iochannel.put(0xFF3B, wt);
		iochannel.put(0xFF3C, wt);
		iochannel.put(0xFF3D, wt);
		iochannel.put(0xFF3E, wt);
		iochannel.put(0xFF3F, wt);

	}

	public void write(int add, byte b) {
		iochannel.get(add).write(add, b);
	}

	public byte read(int add) {
		return iochannel.get(add).read(add);
	}

	public void start() {
		if (line != null) {
			return;
		}
		AudioFormat format = new AudioFormat(samplerate, bitspersample, numChannels, true, false);
		// downsampling: cpu freq -> 44'100hz
		resamplecounter = resamplerate = (CPU.CLOCK / samplerate);

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format, samplerate);
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (line != null) {
			line.drain();
			line.close();
		}
	}

	public void tick(int cpucycles) {
		// frame sequencer, 512 hz (4194304/512 =8192)
		seqcounter -= cpucycles;
		if (seqcounter <= 8192) {
			seqcounter += 8192;
			seqstep = (seqstep + 1) & 7;
			switch (seqstep) {
			case 0:
				break;// clock
			case 1:
				break;
			case 2:
				break;// clock clock
			case 3:
				break;
			case 4:
				break;// clock
			case 5:
				break;
			case 6:
				break;// clock clock
			case 7:
				break;// clock
			}
		}
		resamplecounter--;
		if (resamplecounter <= 0) {
			resamplecounter += resamplerate;

		}
	}

	@Override
	public void link(GBComponents comps) {

	}

	@Override
	public void reset() {

	}

	// actually not a channel, just abusing oop
	private class PowerControl extends Channel {

		@Override
		void write(int add, byte b) {
			// TODO Auto-generated method stub

		}

		@Override
		byte read(int add) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	private class Wavetables extends Channel {
		private final byte[] table;

		Wavetables() {
			table = new byte[0xF];
		}

		@Override
		void write(int add, byte b) {
			table[add - 0xFF30] = b;

		}

		@Override
		byte read(int add) {
			return table[add - 0xFF30];
		}

	}

}

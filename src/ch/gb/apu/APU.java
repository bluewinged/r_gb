package ch.gb.apu;

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
	private int resamplerate;

	private int seqstep;
	private int seqcounter;

	public APU() {

	}

	public void start() {
		if (line != null) {
			return;
		}
		AudioFormat format = new AudioFormat(samplerate, bitspersample, numChannels, true, false);
		// downsampling: cpu freq -> 44'100hz
		resamplerate = (CPU.CLOCK / samplerate);

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
			seqcounter -= 8192;
			seqstep = (seqstep) & 7;
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
	}

	@Override
	public void link(GBComponents comps) {

	}

	@Override
	public void reset() {

	}

	private abstract class Channel {
		protected int divider=1;
		abstract void write(int add, byte b);
	}

	private class Square extends Channel {
		private int duty;
		private int freq = 0;
		private int period = 2048 * 4;
		private final int waveform=0x7EE18180;
		private int sequencer;
		private int sqsample;
		private final int off;

		Square(boolean is2nd) {
			off = is2nd ? 5 : 0;
		}

		@Override
		void write(int add, byte b) {
			if (add == 0xFF10 + off) {

			} else if (add == 0xFF11 + off) {
				duty=(b>>6)&3;
			} else if (add == 0xFF12 + off) {

			} else if (add == 0xFF13 + off) {
				freq &= 0x700;
				freq |= b;
				period = (2048 - freq) * 4;
			} else if (add == 0xFF14 + off) {
				freq &= 0xff;
				freq |= ((b & 7) << 8);
				period = (2048 - freq) * 4;
			}
		}
		void clock(int cycles){
			divider-=cycles;
			while(divider<=0){
				divider+=period;
				sqsample =(  waveform>>(sequencer+duty*8))&1;
				sequencer = (sequencer+1)&7;
			}
		}
		void clocklen(){
			
		}
		void clockenv(){
			
		}
	}

}

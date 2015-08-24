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

package ch.gb.apu;

import java.util.Arrays;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.Settings;
import ch.gb.cpu.CPU;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

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

<<<<<<< HEAD
	// Noise
	public static final int NR40 = 0xFF1F;
	public static final int NR41 = 0xFF20;
	public static final int NR42 = 0xFF21;
	public static final int NR43 = 0xFF22;
	public static final int NR44 = 0xFF23;

	// Soundcontrol
	public static final int NR50 = 0xFF24;
	public static final int NR51 = 0xFF25;
	public static final int NR52 = 0xFF26;

	public static final int MONO = 1;
	public static final int STEREO = 2;

	private final int samplingrate = 44100;
	private final int numChannels = 1;
	private final int bitspersample = 16;
	private int resamplerate; // 95 for 44100hz yeeeaahh
	private int resamplecounter;
	private final int bufferSize = 2048;
	public byte[] samplebuffer8;
	public short[] samplebuffer16;
	private int sampleoffset;

	private int seqstep;
	private int seqcounter = 8192;

	private final HashMap<Integer, Channel> iochannel;

	private final Square quadrangle1;
	private final Square quadrangle2;
	private final Noise noise;
	private final Wave wave;
	private final PowerControl powercontrol;

	private AudioPlaybackJava audio;
	private final BandpassFilter bpfilter;

	public APU() {
		samplebuffer8 = new byte[bufferSize];
		samplebuffer16 = new short[bufferSize / 2];

		audio = new AudioPlaybackJava();
		bpfilter =new BandpassFilter(512, 0.49f,0.003f);//cutoff frequency is x * samplingrate

		quadrangle1 = new Square(false);
		quadrangle2 = new Square(true);
		noise = new Noise();
		wave = new Wave();
		powercontrol = new PowerControl(this);

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
		iochannel.put(0xFF30, wave);
		iochannel.put(0xFF31, wave);
		iochannel.put(0xFF32, wave);
		iochannel.put(0xFF33, wave);
		iochannel.put(0xFF34, wave);
		iochannel.put(0xFF35, wave);
		iochannel.put(0xFF36, wave);
		iochannel.put(0xFF37, wave);
		iochannel.put(0xFF38, wave);
		iochannel.put(0xFF39, wave);
		iochannel.put(0xFF3A, wave);
		iochannel.put(0xFF3B, wave);
		iochannel.put(0xFF3C, wave);
		iochannel.put(0xFF3D, wave);
		iochannel.put(0xFF3E, wave);
		iochannel.put(0xFF3F, wave);

	}

	@Override
	public void reset() {
		seqstep = 0;
		seqcounter = 8192;
		audio.discardSamples();
		audio.stopPlayback();
		audio = new AudioPlaybackJava();
		audio.startPlayback();
		samplebuffer8 = new byte[bufferSize];

		quadrangle1.reset();
		quadrangle2.reset();
		wave.reset();
		noise.reset();

		accumsq1 = 0;
		accumsq2 = 0;
		accumwave = 0;
	}

	public void write(int add, byte b) {
		if (add >= NR10 && add <= NR51 && !powercontrol.powerstatus)
			return;
		if (add >= 0xFF27 && add <= 0xFF2F)
			return;
		// System.out.println(Utils.dumpHex(add));
		iochannel.get(add).write(add, b);
	}

	public byte read(int add) {
		if (add >= 0xFF27 && add <= 0xFF2F) {
			return 0;
		}
		return iochannel.get(add).read(add);
	}

	/**
	 * Audio playback with the java audio api
	 * 
	 * @author bluew
	 * 
	 */
	private class AudioPlaybackJava extends Thread implements AudioPlayback {
		final Object waitlock = new Object();
		private SourceDataLine line; // audio line
		private final boolean isRunning;
		private boolean requestFlush;

		AudioPlaybackJava() {
			setDaemon(true);
			isRunning = true;
			start();
		}

		@Override
		public void run() {
			while (isRunning) {
				synchronized (waitlock) {
					try {
						waitlock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (line != null) {
					if (!requestFlush) {
						//System.out.println(line.available());
						line.write(samplebuffer8, 0, (sampleoffset));
					} else {
						requestFlush = !requestFlush;
						line.flush();
					}
				} else {
					System.out.println("line is null, shutting down audio Thread");
					return;
				}
				sampleoffset = 0;
			}
			stopPlayback();
		}

		@Override
		public void startPlayback() {
			if (line != null) {
				return;
			}
			// AudioFormat format = new AudioFormat(samplerate, bitspersample,
			// numChannels, true, false);
			AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, samplingrate, bitspersample, numChannels, 2,
					samplingrate, false);
			// downsampling: cpu freq -> 44'100hz
			resamplecounter = resamplerate = (CPU.CLOCK / samplingrate);

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format, samplingrate);
			try {
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(format);// TODO: use fixed size Audio buffers
				line.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void stopPlayback() {
			if (line != null) {
				line.flush();
				line.drain();
				line.close();
			}
			line = null;
			// isRunning = false;
		}

		@Override
		public void discardSamples() {
			requestFlush = true;
		}

		@Override
		public void flush() {
			synchronized (waitlock) {
				waitlock.notify();
			}
		}
	}

	private class AudioPlaybackOpenAL extends Thread implements AudioPlayback {
		private AudioDevice device;
		private final boolean isRunning;
		final Object waitlock = new Object();

		public AudioPlaybackOpenAL() {
			setDaemon(true);
			isRunning = true;
			start();
		}

		@Override
		public void run() {
			while (isRunning) {
				synchronized (waitlock) {
					try {
						waitlock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//device.writeSamples(samples, offset, numSamples)
				device.writeSamples(samplebuffer16, 0, (sampleoffset/2));
				
			}
		}

		@Override
		public void startPlayback() {
			if (device != null) {
				return;
			}
			device = Gdx.audio.newAudioDevice(samplingrate, numChannels == 1);
			//device.writeSamples(samples, offset, numSamples)
			// device.writeSamples(samples, offset, numSamples)
		}

		@Override
		public void stopPlayback() {
			device.dispose();
			device = null;
		}

		@Override
		public void flush() {
			synchronized (waitlock) {
				waitlock.notify();
			}
		}

		@Override
		public void discardSamples() {
			// TODO Auto-generated method stub
		}

	}

	public void flush() {
		audio.flush();
	}

	public void start() {
		audio.startPlayback();
	}

	public void stop() {
		audio.stopPlayback();
	}

	public void discard() {
		audio.discardSamples();
	}

	private int accumsq1;
	private int accumsq2;
	private int accumwave;
	private int accumcycles;

	public void tick(int cpucycles) {
		// frame sequencer, 512 hz (4194304/512 =8192)
		seqcounter -= cpucycles;
		if (seqcounter <= 0) {
			seqcounter += 8192;
			seqstep = (seqstep + 1) & 7;
			switch (seqstep) {
			case 0:
				quadrangle1.clocklen();
				quadrangle2.clocklen();
				wave.clocklen();
				break;// clock len
			case 1:
				break;
			case 2:
				quadrangle1.clocklen();
				quadrangle2.clocklen();
				quadrangle1.clocksweep();
				wave.clocklen();
				break;// clock len clock sweep
			case 3:
				break;
			case 4:
				quadrangle1.clocklen();
				quadrangle2.clocklen();
				wave.clocklen();
				break;// clock len
			case 5:
				break;
			case 6:
				quadrangle1.clocklen();
				quadrangle2.clocklen();
				quadrangle1.clocksweep();
				wave.clocklen();
				break;// clock len clock sweep
			case 7:
				quadrangle1.clockenv();
				quadrangle2.clockenv();
				break;// clock env
			}

		}
		if (powercontrol.powerstatus) {
			quadrangle1.clock(cpucycles);
			quadrangle2.clock(cpucycles);
			wave.clock(cpucycles);
		}
		accumsq1 += quadrangle1.poll() * cpucycles;
		accumsq2 += quadrangle2.poll() * cpucycles;
		accumwave += wave.poll() * cpucycles;
		accumcycles += cpucycles;
		//@formatter:off
		
		
		int le = (powercontrol.nr51 >> 4) & 0xf;// left and right enables
		int re = powercontrol.nr51 & 0xf;
		//le =0xf;//TODO: remove audio-on hack
		//re = 0xf;
		float q1 = accumsq1 / ((float) (accumcycles * 15));
		float q2 = accumsq2 / ((float) (accumcycles * 15));// 15 is max
		float w = accumwave /((float)(accumcycles *15));
		//System.out.println(""+(le>>1&1)+" yoo:"+ (re>>1&1));
		q1*=Settings.ch1enable;
		q2*=Settings.ch2enable;
		w*= Settings.ch3enable;
		float chanL = ((q1 * (le & 1) + q2 * (le >> 1 & 1)+w*(le>>2&1)) / 3) * (powercontrol.leftvol + 1) / 8;
		float chanR = ((q1 * (re & 1) + q2 * (re >> 1 & 1)+w*(re>>2&1)) / 3) * (powercontrol.rightvol + 1) / 8;
		float mixed = (chanL + chanR) / 2 * Settings.mastervolume;//0-1
		accumcycles = 0;
		accumsq1 = 0;
		accumsq2 = 0;
		accumwave=0;
		
		bpfilter.store(mixed);
		
		resamplecounter -= cpucycles;//44100hz resample?
		if (resamplecounter <= 0) {
			resamplecounter += resamplerate;
			//float usample = (bpfilter.convolveStep()*60000);
			float usample=0;
			if(mixed!=0)
			usample = ((mixed) * 0xffff -0x8000 );
			else
			usample = 0x8000;	
			//usample = Math.max(Math.min(32768, usample),-32768);//clamp
			//usample -=32768;
			int sample = (int) usample;
			//System.out.println(sample);
			//sample += 32768;
			// ONLY MONO supported for now
			//samplebuffer16[sampleoffset/2]=(short)(sample&0xffff);
			samplebuffer8[sampleoffset++] = (byte) (sample & 0xff);
			samplebuffer8[sampleoffset++] = (byte) ((sample >> 8) & 0xff);
			// overflowcontrol
			if (sampleoffset == samplebuffer8.length) {
				//System.out.println("Audio BufferOVERFLOW");
				Arrays.fill(samplebuffer8,(byte)0);
				//Arrays.fill(samplebuffer16, (short)0);
				audio.discardSamples();// too speed up resync?//TODO: SPEED MODUS disturbs later sound output
				sampleoffset = 0;
			}
			//@formatter:on
		}
	}

	public int getSampleoffset() {
		return sampleoffset;
	}

	@Override
	public void link(GBComponents comps) {

	}

	public void powerOn() {
		seqstep = 0;
		seqcounter = 8192;
		quadrangle1.powerOn();
		quadrangle2.powerOn();
		wave.powerOn();
	}

	public void powerOff() {
		byte i = 0;
		quadrangle1.write(NR10, i);
		quadrangle1.write(NR11, i);
		quadrangle1.write(NR12, i);
		quadrangle1.write(NR13, i);
		quadrangle1.write(NR14, i);
		quadrangle2.write(NR20, i);
		quadrangle2.write(NR21, i);
		quadrangle2.write(NR22, i);
		quadrangle2.write(NR23, i);
		quadrangle2.write(NR24, i);
		wave.write(NR30, i);
		wave.write(NR31, i);
		wave.write(NR32, i);
		wave.write(NR33, i);
		wave.write(NR34, i);
	}

	public byte channelstates() {
		// TODO: other channels
		int q1 = quadrangle1.status() ? 1 : 0;
		int q2 = quadrangle2.status() ? 2 : 0;
		int w = wave.status() ? 4 : 0;
		return (byte) (w | q2 | q1);
	}

	// actually not a channel, just abusing oop
	private class PowerControl extends Channel {
		private byte nr50;
		private byte nr51;
		private byte nr52;

		boolean vinLenable;
		boolean vinRenable;
		int leftvol;
		int rightvol;
		boolean powerstatus = false;

		private final APU apu;

		PowerControl(APU apu) {
			this.apu = apu;
		}

		@Override
		void write(int add, byte b) {
			if (add == NR50) {
				nr50 = b;
				vinLenable = (b & 0x80) == 0x80;
				vinRenable = (b & 8) == 8;
				leftvol = (b >> 4) & 7;
				rightvol = b & 7;
			} else if (add == NR51) {
				nr51 = b;
			} else if (add == NR52) {
				nr52 = b;
				// System.out.println("NR52:" + Utils.dumpHex(b));
				boolean oldstatus = powerstatus;
				powerstatus = (b & 0x80) == 0x80;
				if (oldstatus != powerstatus) {
					if (powerstatus) {
						apu.powerOn();
					} else {
						apu.powerOff();
					}
				}
			}
		}

		@Override
		byte read(int add) {
			if (add == NR50) {
				return (byte) (nr50 | 0x00);
			} else if (add == NR51) {
				return (byte) (nr51 | 0x00);
			} else if (add == NR52) {
				// System.out.println("want read");
				return (byte) (channelstates() | nr52 | 0x70);
			}
			throw new RuntimeException("trololol not possibru");
		}

		@Override
		void reset() {

		}
	}
=======
    // Noise
    public static final int NR40 = 0xFF1F;
    public static final int NR41 = 0xFF20;
    public static final int NR42 = 0xFF21;
    public static final int NR43 = 0xFF22;
    public static final int NR44 = 0xFF23;

    // Soundcontrol
    public static final int NR50 = 0xFF24;
    public static final int NR51 = 0xFF25;
    public static final int NR52 = 0xFF26;

    public static final int MONO = 1;
    public static final int STEREO = 2;

    private final int samplingrate = 44100;
    private final int numChannels = 1;
    private final int bitspersample = 16;
    private float resamplerate; // 95 for 44100hz yeeeaahh
    private int resamplecounter;
    private final int bufferSize = 2048;
    private double[] inp = new double[4];
    private double[] oup = new double[4];

    public byte[] samplebuffer8;
    public short[] samplebuffer16;
    private int sampleoffset;

    private int seqstep;
    private int seqcounter = 8192;

    private final HashMap<Integer, Channel> iochannel;

    private final Square quadrangle1;
    private final Square quadrangle2;
    private final Noise noise;
    private final Wave wave;
    private final PowerControl powercontrol;

    private AudioPlayback audio;
    
    private final BandpassFilter bpfilter;

    public APU() {
        samplebuffer8 = new byte[bufferSize];
        samplebuffer16 = new short[bufferSize / 2];

        audio = new AudioPlaybackJava();
       // audio = new AudioPlaybackOpenAL();
        bpfilter = new BandpassFilter(512, 0.49f, 0.003f);//cutoff frequency is x * samplingrate

        quadrangle1 = new Square(false);
        quadrangle2 = new Square(true);
        noise = new Noise();
        wave = new Wave();
        powercontrol = new PowerControl(this);

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
        iochannel.put(0xFF30, wave);
        iochannel.put(0xFF31, wave);
        iochannel.put(0xFF32, wave);
        iochannel.put(0xFF33, wave);
        iochannel.put(0xFF34, wave);
        iochannel.put(0xFF35, wave);
        iochannel.put(0xFF36, wave);
        iochannel.put(0xFF37, wave);
        iochannel.put(0xFF38, wave);
        iochannel.put(0xFF39, wave);
        iochannel.put(0xFF3A, wave);
        iochannel.put(0xFF3B, wave);
        iochannel.put(0xFF3C, wave);
        iochannel.put(0xFF3D, wave);
        iochannel.put(0xFF3E, wave);
        iochannel.put(0xFF3F, wave);

    }

    @Override
    public void reset() {
        seqstep = 0;
        seqcounter = 8192;
        audio.discardSamples();
        audio.stopPlayback();
        audio = new AudioPlaybackJava();
       // audio = new AudioPlaybackOpenAL();
        audio.startPlayback();
        samplebuffer8 = new byte[bufferSize];

        quadrangle1.reset();
        quadrangle2.reset();
        wave.reset();
        noise.reset();

        accumsq1 = 0;
        accumsq2 = 0;
        accumwave = 0;
    }

    public void write(int add, byte b) {
        if (add >= NR10 && add <= NR51 && !powercontrol.powerstatus) {
            return;
        }
        if (add >= 0xFF27 && add <= 0xFF2F) {
            return;
        }
        // System.out.println(Utils.dumpHex(add));
        iochannel.get(add).write(add, b);
    }

    public byte read(int add) {
        if (add >= 0xFF27 && add <= 0xFF2F) {
            return 0;
        }
        return iochannel.get(add).read(add);
    }

    /**
     * Audio playback with the java audio api
     *
     * @author bluew
     *
     */
    private class AudioPlaybackJava extends Thread implements AudioPlayback {

        final Object waitlock = new Object();
        private SourceDataLine line; // audio line
        private final boolean isRunning;
        private boolean requestFlush;

        AudioPlaybackJava() {
            setDaemon(true);
            isRunning = true;
            start();
        }

        @Override
        public void run() {
            while (isRunning) {
                synchronized (waitlock) {
                    try {
                        waitlock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (line != null) {
                    if (!requestFlush) {
                        //System.out.println(line.available());
                        line.write(samplebuffer8, 0, (sampleoffset));
                    } else {
                        requestFlush = !requestFlush;
                        line.flush();
                    }
                } else {
                    System.out.println("line is null, shutting down audio Thread");
                    return;
                }
                sampleoffset = 0;
            }
            stopPlayback();
        }

        @Override
        public void startPlayback() {
            if (line != null) {
                return;
            }
            // AudioFormat format = new AudioFormat(samplerate, bitspersample,
            // numChannels, true, false);
            AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, samplingrate, bitspersample, numChannels, 2,
                    samplingrate, false);
            // downsampling: cpu freq -> 44'100hz
            resamplerate = ((float)CPU.CLOCK / samplingrate);
            resamplecounter = (int)resamplerate;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format, samplingrate);
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);// TODO: use fixed size Audio buffers
                line.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stopPlayback() {
            if (line != null) {
                line.flush();
                line.drain();
                line.close();
            }
            line = null;
            // isRunning = false;
        }

        @Override
        public void discardSamples() {
            requestFlush = true;
        }

        @Override
        public void flush() {
            synchronized (waitlock) {
                waitlock.notify();
            }
        }
    }

    private class AudioPlaybackOpenAL extends Thread implements AudioPlayback {

        private AudioDevice device;
        private final boolean isRunning;
        final Object waitlock = new Object();

        public AudioPlaybackOpenAL() {
            setDaemon(true);
            isRunning = true;
            start();
        }

        @Override
        public void run() {
            while (isRunning) {
                synchronized (waitlock) {
                    try {
                        waitlock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //device.writeSamples(samples, offset, numSamples)
                device.writeSamples(samplebuffer16, 0, (sampleoffset / 2));
            }
        }

        @Override
        public void startPlayback() {
            if (device != null) {
                return;
            }
            device = Gdx.audio.newAudioDevice(samplingrate, numChannels == 1);
            //device.writeSamples(samples, offset, numSamples)
            // device.writeSamples(samples, offset, numSamples)
        }

        @Override
        public void stopPlayback() {
            device.dispose();
            device = null;
        }

        @Override
        public void flush() {
            synchronized (waitlock) {
                waitlock.notify();
            }
        }

        @Override
        public void discardSamples() {
            // TODO Auto-generated method stub
        }

    }

    public void flush() {
        audio.flush();
    }

    public void start() {
        audio.startPlayback();
    }

    public void stop() {
        audio.stopPlayback();
    }

    public void discard() {
        audio.discardSamples();
    }

    private int accumsq1;
    private int accumsq2;
    private int accumwave;
    private int accumcycles;

    public void tick(int cpucycles) {
        // frame sequencer, 512 hz (4194304/512 =8192)
        seqcounter -= cpucycles;
        if (seqcounter <= 0) {
            seqcounter += 8192;
            seqstep = (seqstep + 1) & 7;
            switch (seqstep) {
                case 0:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    wave.clocklen();
                    break;// clock len
                case 1:
                    break;
                case 2:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    quadrangle1.clocksweep();
                    wave.clocklen();
                    break;// clock len clock sweep
                case 3:
                    break;
                case 4:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    wave.clocklen();
                    break;// clock len
                case 5:
                    break;
                case 6:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    quadrangle1.clocksweep();
                    wave.clocklen();
                    break;// clock len clock sweep
                case 7:
                    quadrangle1.clockenv();
                    quadrangle2.clockenv();
                    break;// clock env
            }

        }
        if (powercontrol.powerstatus) {
            quadrangle1.clock(cpucycles);
            quadrangle2.clock(cpucycles);
            wave.clock(cpucycles);
        }
        accumsq1 += quadrangle1.poll() * cpucycles;
        accumsq2 += quadrangle2.poll() * cpucycles;
        accumwave += wave.poll() * cpucycles;
        accumcycles += cpucycles;
        //@formatter:off

        int le = (powercontrol.nr51 >> 4) & 0xf;// left and right enables
        int re = powercontrol.nr51 & 0xf;
        //le =0xf;//TODO: remove audio-on hack
        //re = 0xf;
        float q1 = accumsq1 / ((float) (accumcycles * 15));
        float q2 = accumsq2 / ((float) (accumcycles * 15));// 15 is max
        float w = accumwave / ((float) (accumcycles * 15));
        //System.out.println(""+(le>>1&1)+" yoo:"+ (re>>1&1));
        q1 *= Settings.ch1enable;
        q2 *= Settings.ch2enable;
        w *= Settings.ch3enable;
        //each channel can output between 0 and 15
        float chanL = ((q1 * (le & 1) + q2 * (le >> 1 & 1) + w * (le >> 2 & 1)) / 3) / 15 * (powercontrol.leftvol + 1) / 8;
        float chanR = ((q1 * (re & 1) + q2 * (re >> 1 & 1) + w * (re >> 2 & 1)) / 3) / 15 * (powercontrol.rightvol + 1) / 8;
        float mixed = (chanL + chanR) / 2 * Settings.mastervolume;//0-1
        //System.out.println(mixed);
        accumcycles = 0;
        accumsq1 = 0;
        accumsq2 = 0;
        accumwave = 0;

        //assume that the output is constant for #cpucycles
        //y[n]= 1/a0 * (b0 * x[n] + b1 * x[n-1] +... + bp * x[n-p]
        //-a1 * y[n-1] - a2 * y[n-2] - ... - aq * y[n-q]
        double a0 = 0.0002665215059663596;
        double a1 = 0.0005330430119327192;
        double a2 = 0.0002665215059663596;
        double b1 = -1.9532941526051335;
        double b2 = 0.9543602386289991;

        double out = 0;
        for (int i = 0; i < cpucycles; i++) {
            inp[3] = inp[2];
            inp[2] = inp[1];
            inp[1] = inp[0];
            inp[0] = mixed;
            out = a0 * inp[0] + a1 * inp[1] + a2 * inp[2] - b1 * oup[1] - b2 * oup[2];
            oup[3] = oup[2];
            oup[2] = oup[1];
            oup[1] = out;

        }
        mixed = (float) out;
        //TODO: better filtering?
        //first we can decimate withouth filtering by taking every 8th sample
        //since the highest frequency produced is (4194304/8)/2 Hz by the noise channel
        //then we apply a iir filter with cutoff 22050 hz and finally we decimate by taking every
        // ~12th sample
        // or even better, do cubic interpolation between samples and pick not necessarily integer samples
        resamplecounter -= cpucycles;//44100hz resample?
        if (resamplecounter <= 0) {
            resamplecounter = (int) ((float)resamplecounter + resamplerate);
            //float usample = (bpfilter.convolveStep()*60000);
           float usample = 0;
//            if (mixed != 0) {
//                usample = ((mixed) * 0xffff - 0x8000);
//            } else {
//                usample = 0x8000;
//            }
             usample = mixed*0x7ffff+0xffff; //0xffff to center the unsigne pcm signal
             //and since mixed is normalized and only positive we can max multiply by 0xffff/2
            //usample = Math.max(Math.min(32768, usample),-32768);//clamp
            //usample -=32768;
            int sample = (int) usample;
            //System.out.println(sample);
            //sample += 32768;
            // ONLY MONO supported for now
            samplebuffer16[sampleoffset/2]=(short)(sample&0xffff);
            samplebuffer8[sampleoffset++] = (byte) (sample & 0xff);
            samplebuffer8[sampleoffset++] = (byte) ((sample >> 8) & 0xff);
            // overflowcontrol
            if (sampleoffset == samplebuffer8.length) {
                //System.out.println("Audio BufferOVERFLOW");
                Arrays.fill(samplebuffer8, (byte) 0);
                //Arrays.fill(samplebuffer16, (short)0);
                audio.discardSamples();// too speed up resync?//TODO: SPEED MODUS disturbs later sound output
                sampleoffset = 0;
            }
            //@formatter:on
        }
    }

    public int getSampleoffset() {
        return sampleoffset;
    }

    @Override
    public void link(GBComponents comps) {

    }

    public void powerOn() {
        seqstep = 0;
        seqcounter = 8192;
        quadrangle1.powerOn();
        quadrangle2.powerOn();
        wave.powerOn();
    }

    public void powerOff() {
        byte i = 0;
        quadrangle1.write(NR10, i);
        quadrangle1.write(NR11, i);
        quadrangle1.write(NR12, i);
        quadrangle1.write(NR13, i);
        quadrangle1.write(NR14, i);
        quadrangle2.write(NR20, i);
        quadrangle2.write(NR21, i);
        quadrangle2.write(NR22, i);
        quadrangle2.write(NR23, i);
        quadrangle2.write(NR24, i);
        wave.write(NR30, i);
        wave.write(NR31, i);
        wave.write(NR32, i);
        wave.write(NR33, i);
        wave.write(NR34, i);
    }

    public byte channelstates() {
        // TODO: other channels
        int q1 = quadrangle1.status() ? 1 : 0;
        int q2 = quadrangle2.status() ? 2 : 0;
        int w = wave.status() ? 4 : 0;
        return (byte) (w | q2 | q1);
    }

    // actually not a channel, just abusing oop
    private class PowerControl extends Channel {

        private byte nr50;
        private byte nr51;
        private byte nr52;

        boolean vinLenable;
        boolean vinRenable;
        int leftvol;
        int rightvol;
        boolean powerstatus = false;

        private final APU apu;

        PowerControl(APU apu) {
            this.apu = apu;
        }

        @Override
        void write(int add, byte b) {
            if (add == NR50) {
                nr50 = b;
                vinLenable = (b & 0x80) == 0x80;
                vinRenable = (b & 8) == 8;
                leftvol = (b >> 4) & 7;
                rightvol = b & 7;
            } else if (add == NR51) {
                nr51 = b;
            } else if (add == NR52) {
                nr52 = b;
                // System.out.println("NR52:" + Utils.dumpHex(b));
                boolean oldstatus = powerstatus;
                powerstatus = (b & 0x80) == 0x80;
                if (oldstatus != powerstatus) {
                    if (powerstatus) {
                        apu.powerOn();
                    } else {
                        apu.powerOff();
                    }
                }
            }
        }

        @Override
        byte read(int add) {
            if (add == NR50) {
                return (byte) (nr50 | 0x00);
            } else if (add == NR51) {
                return (byte) (nr51 | 0x00);
            } else if (add == NR52) {
                // System.out.println("want read");
                return (byte) (channelstates() | nr52 | 0x70);
            }
            throw new RuntimeException("trololol not possibru");
        }

        @Override
        void reset() {

        }
    }
>>>>>>> bd23cad... fixed broken stuff

}

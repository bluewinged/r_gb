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

import ch.audio.AudioPlaybackJava;
import java.util.Arrays;
import java.util.HashMap;


import ch.gb.Component;
import ch.gb.GBComponents;
import ch.gb.Config;
import static java.lang.Math.*;

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
    private double resamplecounter;
    private final int bufferSize = 2200;
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

    public APU() {
        samplebuffer8 = new byte[bufferSize];
        samplebuffer16 = new short[bufferSize / 2];

        audio = new AudioPlaybackJava(samplingrate, bitspersample, numChannels);
        // audio = new AudioPlaybackOpenAL();

        quadrangle1 = new Square(this, false);
        quadrangle2 = new Square(this, true);
        noise = new Noise(this);
        wave = new Wave(this);
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
        audio.stop();
        audio = new AudioPlaybackJava(samplingrate, bitspersample, numChannels);
        audio.start();
        samplebuffer8 = new byte[bufferSize];

        quadrangle1.reset();
        quadrangle2.reset();
        wave.reset();
        noise.reset();

        accumsq1 = 0;
        accumsq2 = 0;
        accumwave = 0;
        accumnoise = 0;
    }

    @Override
    public void connect(GBComponents comps) {

    }

    public void write(int add, byte b) {
        if (add >= NR10 && add <= NR51 && !powercontrol.powerstatus) {
            //can only write to the length counter
            //TODO
            return;
        }
        if (add >= 0xFF27 && add <= 0xFF2F) {
            return;
        }
        //System.out.println("WRTE: " + Utils.dumpHex(add) + "  " + Utils.dumpHex(b));
        iochannel.get(add).write(add, b);
    }

    public byte read(int add) {
        if (add >= 0xFF27 && add <= 0xFF2F) {
            return (byte) 0xFF;
        }
        byte var = iochannel.get(add).read(add);
        //System.out.println("READ: " + Utils.dumpHex(add) + "  " + Utils.dumpHex(var));
        return var;
    }



    public void flush() {
        audio.flush();
    }

    public void start() {
        audio.start();
    }

    public void stop() {
        audio.stop();
    }

    public void close() {
        audio.close();
    }

    public void discard() {
        audio.discardSamples();
    }

    private int accumsq1;
    private int accumsq2;
    private int accumwave;
    private int accumnoise;
    private int accumcycles;

    public void clock(int cpucycles) {
        for (int i = 0; i < cpucycles; i++) {
            float sample = apucycle();
            //float filtered = filter(sample); //drops initial fps from 60 to 40
            resample(sample);
        }
    }

    public float apucycle() {
        // frame sequencer, 512 hz (4194304/512 =8192)
        int cpucycles = 1;
        if (powercontrol.powerstatus) {
            quadrangle1.clock(cpucycles);
            quadrangle2.clock(cpucycles);
            wave.clock(cpucycles);
            noise.clock(cpucycles);
        }

        seqcounter -= cpucycles;
        if (seqcounter <= 0) {
            seqcounter += 8192;
            switch (seqstep) {
                case 0:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    wave.clocklen();
                    noise.clocklen();
                    break;// clock len
                case 1:
                    break;
                case 2:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    quadrangle1.clocksweep();
                    wave.clocklen();
                    noise.clocklen();
                    break;// clock len clock sweep
                case 3:
                    break;
                case 4:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    wave.clocklen();
                    noise.clocklen();
                    break;// clock len
                case 5:
                    break;
                case 6:
                    quadrangle1.clocklen();
                    quadrangle2.clocklen();
                    quadrangle1.clocksweep();
                    wave.clocklen();
                    noise.clocklen();
                    break;// clock len clock sweep
                case 7:
                    quadrangle1.clockenv();
                    quadrangle2.clockenv();
                    noise.clockenv();
                    break;// clock env
            }
            seqstep = (seqstep + 1) & 7;

        }

        accumsq1 += quadrangle1.poll() * cpucycles;
        accumsq2 += quadrangle2.poll() * cpucycles;
        accumwave += wave.poll() * cpucycles;
        accumnoise += noise.poll() * cpucycles;
        accumcycles += cpucycles;
        //@formatter:off

        int le = (powercontrol.nr51 >> 4) & 0xf;// left and right enables
        int re = powercontrol.nr51 & 0xf;
        //le =0xf;//TODO: remove audio-on hack
        //re = 0xf;
        float q1 = accumsq1 / ((float) (accumcycles * 15));
        float q2 = accumsq2 / ((float) (accumcycles * 15));// 15 is max
        float w = accumwave / ((float) (accumcycles * 15));
        float n = accumnoise / ((float) (accumcycles * 15));
        if (n < 0) {
            System.out.println("weird");
        }
        //System.out.println(""+(le>>1&1)+" yoo:"+ (re>>1&1));
        q1 *= Config.ch1enable;
        q2 *= Config.ch2enable;
        w *= Config.ch3enable;
        n *= Config.ch4enable;
        //each channel can output between 0 and 15
        float chanL = (q1 * (le & 1) + q2 * (le >> 1 & 1) + w * (le >> 2 & 1) + n * (le >> 3 & 1)) / 4 * (powercontrol.leftvol + 1) / 8;
        float chanR = (q1 * (re & 1) + q2 * (re >> 1 & 1) + w * (re >> 2 & 1) + n * (re >> 3 & 1)) / 4 * (powercontrol.rightvol + 1) / 8;
        float mixed = (chanL + chanR) / 2 * Config.mastervolume;//TODO: remove master volume, its a terrible idea

        //System.out.println(mixed);
        accumcycles = 0;
        accumsq1 = 0;
        accumsq2 = 0;
        accumwave = 0;
        accumnoise = 0;
        return mixed;
    }

    public int seqstep() {
        return seqstep;
    }

    public float filter(float n) {
        //N=4, IIR butterworth filter
        //assume that the output is constant for #cpucycles
        //y[n]= 1/a0 * (b0 * x[n] + b1 * x[n-1] +... + bp * x[n-p]
        //-a1 * y[n-1] - a2 * y[n-2] - ... - aq * y[n-q]
        double a0 = 0.0002665215059663596;
        double a1 = 0.0005330430119327192;
        double a2 = 0.0002665215059663596;
        double b1 = -1.9532941526051335;
        double b2 = 0.9543602386289991;

        double out = 0;
        inp[3] = inp[2];
        inp[2] = inp[1];
        inp[1] = inp[0];
        inp[0] = n;
        out = a0 * inp[0] + a1 * inp[1] + a2 * inp[2] - b1 * oup[1] - b2 * oup[2];
        oup[3] = oup[2];
        oup[2] = oup[1];
        oup[1] = out;

        return (float) out;
        //TODO: better filtering?
        //first we can decimate withouth filtering by taking every 8th sample
        //since the highest frequency produced is (4194304/8)/2 Hz by the noise channel
        //then we apply a iir filter with cutoff 22050 hz and finally we decimate by taking every
        // ~12th sample
        // or even better, do cubic interpolation between samples and pick not necessarily integer samples
    }

    //lets do the resample step outside the APU in the GB render loop
    //accumulate samples in here and feed them in the apu loop
    //we feed too many samples in too short a time causing the ringbuffer to overflow
//    double[] fifo =
    //we use a lanczos window with a=3 for sample interpolation (instead of truncated sinc)
    //Maybe do as blargg said and make a table for a few fractional positions and 
    //then interpolate linearly between entries to reduce computation time
    //http://forums.nesdev.com/viewtopic.php?f=5&t=10580&sid=59c187fb5b775f3b42154b31606b60d2&start=15
    private double lanczos(double x) {
        double a = 3;
        if (x == 0) {
            return 1;
        } else if (0 < abs(x) && abs(x) < a) {
            return (a * sin(PI * x) * sin(PI * x / a)) / (PI * PI * x * x);
        } else {
            return 0;
        }
    }
    long debugclk = 0;
    long old = 0;
    long counter;
    long acc = 0;

    int clk8 = 8;
    double boxcar = 0;
    double[] lanczosbuffer = new double[6];

    public void resample(float in) {

        clk8--;
        if (clk8 == 0) { //picking every eight sample // we can do this because I think the highest frequency appearing is 1/16 of the max cpu frequency
            clk8 += 8;   // but obviousely the square wave pulse shape creates even higher harmonics but hopefully they dont interfer too much
            resamplecounter--;
            boxcar += in;
            //for a windowed sinc interpolation we need 5 (total 6) samples buffered
            for (int k = 6; k > 1; k--) {
                lanczosbuffer[k - 1] = lanczosbuffer[k - 2]; //the dumbfounded direct approach for prototyping
            }
            lanczosbuffer[0] = in;

            if (resamplecounter < 0) { //fractional position is between [-1,0)
                double frac = resamplecounter;
                final double samplespace = (double) 524288 / 44100;
                resamplecounter = ((double) resamplecounter + samplespace);
                //now we do the convolution of the windowed sinc with the samples
                //we sum from -3 to 2 and 
                in = 0;
                for (int i = -3; i < 3; i++) {
                    in += lanczosbuffer[i + 3] * lanczos(frac - i);
                }

                boxcar = boxcar / samplespace;
                float signedsample = in * 0x7fff + 0xffff; //0xffff to center the unsigne pcm signal
                //and since mixed is normalized and only positive we can max multiply by 0xffff/2
                int sample = (int) signedsample;
                byte[] data = new byte[2];//TODO: change this new bullshit
                data[0] = (byte) (sample & 0xff);
                data[1] = (byte) ((sample >> 8) & 0xff);
                audio.outputSamples(data, 0, 2);
                boxcar = 0;

                samplebuffer8[sampleoffset++] = (byte) (sample & 0xff);
                samplebuffer8[sampleoffset++] = (byte) ((sample >> 8) & 0xff);

                if (sampleoffset == samplebuffer8.length) {
                    Arrays.fill(samplebuffer8, (byte) 0);
                    sampleoffset = 0;
                }
                debugclk = System.currentTimeMillis();
                counter++;
                acc = acc + debugclk - old;
                old = debugclk;
                if (acc > 1000) {
                    acc = 0;
                    // System.out.println("events per second:" + counter);
                    // System.out.println("frac:" + frac);
                    counter = 0;
                }

            }
        }

//                int cpucycles = 1;
//        resamplecounter -= cpucycles;//44100hz resample?
//        if (resamplecounter <= 0) {
//            resamplecounter = (int) ((float) resamplecounter + resamplerate);
//            float signedsample = in * 0x7fff + 0xffff; //0xffff to center the unsigne pcm signal
//            //and since mixed is normalized and only positive we can max multiply by 0xffff/2
//            int sample = (int) signedsample;
//            //System.out.println(sample);
//            //sample += 32768;
//            // ONLY MONO supported for now
//            //samplebuffer16[sampleoffset/2]=(short)(sample&0xffff);
//            samplebuffer8[sampleoffset++] = (byte) (sample & 0xff);
//            samplebuffer8[sampleoffset++] = (byte) ((sample >> 8) & 0xff);
//            debugclk = System.currentTimeMillis();
//
//            counter++;
//            acc = acc + debugclk - old;
//            old = debugclk;
//            if (acc > 1000) {
//                acc = 0;
//                System.out.println("events per second:" + counter);
//                counter = 0;
//            }
//
//            byte[] data = new byte[2];
//            data[0] = (byte) (sample & 0xff);
//            data[1] = (byte) ((sample >> 8) & 0xff);
//            audio.outputSamples(data, 0, 2);
//
//            if (sampleoffset == samplebuffer8.length) {
//                //  audio.outputSamples(samplebuffer8, 0, samplebuffer8.length);
//                Arrays.fill(samplebuffer8, (byte) 0);
//                sampleoffset = 0;
//            }
//        }
    }

    public int getSampleoffset() {
        return sampleoffset;
    }

    public void powerOn() {
        seqstep = 0;
        seqcounter = 8192;
        quadrangle1.powerOn();
        quadrangle2.powerOn();
        wave.powerOn();
        noise.powerOn();
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
        noise.write(NR40, i);
        noise.write(NR41, i);
        noise.write(NR42, i);
        noise.write(NR43, i);
        noise.write(NR44, i);
        noise.write(NR44, i);
        powercontrol.write(NR50, i); //need direct writes because calls 
        powercontrol.write(NR51, i);//to write() only are already filtered since power is off

        //disallow writes to register
    }

    public byte channelstates() {
        // TODO: other channels
        int q1 = quadrangle1.status() ? 1 : 0;
        int q2 = quadrangle2.status() ? 2 : 0;
        int w = wave.status() ? 4 : 0;
        int n = noise.status() ? 8 : 0;
        return (byte) (w | q2 | q1 | n);
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
        boolean powerstatus = true;

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

                boolean oldstatus = powerstatus;
                powerstatus = (b & 0x80) == 0x80;
                if (oldstatus != powerstatus) {
                    if (powerstatus) {
                        //System.out.println("powering on");
                        apu.powerOn();
                    } else {
                        //System.out.println("powering off");
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
                return (byte) (channelstates() | 0x70 | (powerstatus ? 0x80 : 0));
            }
            throw new RuntimeException("not possible");
        }

        @Override
        void reset() {

        }
    }

}

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
package ch.audio;

import ch.gb.apu.APU;
import ch.gb.apu.AudioPlayback;
import ch.gb.cpu.CPU;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author bluew
 */

    public class AudioPlaybackJava implements AudioPlayback {

        private SourceDataLine line; // audio line
        private byte[] buffer;
        private int buffptr;
        
        private int samplingrate;
        private int bitspersample;
        private int numChannels;
        
         private float resamplerate; // 95 for 44100hz
         
        public AudioPlaybackJava(int samplingrate, int bitspersample, int numChannels) {
            this.samplingrate = samplingrate;
            this.bitspersample = bitspersample;
            this.numChannels = numChannels;
        }

        @Override
        public void start() {
            if (line != null) {
                line.start();
                return;
            }
            buffer = new byte[2 * 8192];
            // AudioFormat format = new AudioFormat(samplerate, bitspersample,
            // numChannels, true, false);
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    samplingrate, bitspersample, numChannels, 2,
                    samplingrate, false);
            // downsampling: cpu freq -> 44'100hz
            resamplerate = ((float) CPU.CLOCK / samplingrate); //524288
            //resamplecounter = resamplerate;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                    format, samplingrate);
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, 2 * 8820);//4410 is 50ms @ 44100hz
                //standard buffer size is 44100 bytes which is too big
            } catch (LineUnavailableException ex) {
                Logger.getLogger(APU.class.getName()).log(Level.SEVERE, null, ex);
            }
            line.start();
        }

        @Override
        public void stop() {
            close();
        }

        @Override
        public void discardSamples() {
        }

        @Override
        public void flush() {
            int avail = line.available();
            //if (avail > 0) {
//            byte[] data = rb.get(avail);
//            if (data != null) {
//                line.write(data, 0, data.length);
//            }
            // }
            if (avail >= buffptr) {
                line.write(buffer, 0, buffptr);

            }
            buffptr = 0;
        }

        public void close() {
            if (line != null) {
                line.flush();
                line.drain();
                line.close();
            }
            line = null;
        }

        public void outputSamples(byte[] data, int off, int len) {
            for (int i = off; i < len; i++) {
                buffer[buffptr++] = data[i];
            }
        }

        public int available() {
            return line.available();
        }
    }


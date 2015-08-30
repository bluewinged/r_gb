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
package ch.gb;

import ch.gb.apu.APU;

import ch.gb.cpu.CPU;
import ch.gb.gpu.GPU;
import ch.gb.gpu.OpenglDisplay;
import ch.gb.mem.MemoryManager;
import ch.gb.utils.Complex;
import ch.gb.utils.FFT;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import static java.lang.Math.*;

public class GB implements ApplicationListener {

    private GBComponents comps;
    private CPU cpu;
    private GPU gpu;
    private APU apu;
    private MemoryManager mem;

    private final float framerate = 60f;// 60hz
    private float hz60accu;
    private final float hz60tick = 1f / framerate;
    private double cpuacc;
    private final double cyclesperframe = (CPU.CLOCK / framerate);
    private int clock;
    // graphics
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont fadeoutFont;
    private OpenglDisplay screen;
    private OpenglDisplay map;
    private OpenglDisplay sprshow;
    private OpenglDisplay waveform;
    private OpenglDisplay fftdisp;
    private OpenglDisplay krnldisplay;

    private GUI gui;

    public static InputMultiplexer multiplexer = new InputMultiplexer();
    ;

	private float fontalpha = 1.0f;
    private boolean paused;
    private boolean hasRom = false;
    private boolean showfps = true;
    private boolean romInfo = true;

    @Override
    public void create() {
        //missing data from here http://libgdx.googlecode.com/svn/tags/0.9.3/tests/gdx-tests-android/assets/data/
		/*
         try {
         System.setOut(new PrintStream(new File("stdout.txt")));//not portable!
         System.setErr(new PrintStream(new File("errout.txt")));
         } catch (Exception e) {
         e.printStackTrace();
         }
         */
        gui = new GUI(this);
        gui.build();

        Gdx.input.setInputProcessor(multiplexer);

        multiplexer.addProcessor(gui.getInputProcessor1());
        multiplexer.addProcessor(gui.getInputProcessor2());

        batch = new SpriteBatch();
        font = new BitmapFont();
        fadeoutFont = new BitmapFont();
        screen = new OpenglDisplay(160, 144, 256, 16);
        map = new OpenglDisplay(256, 256, 256, 16);
        sprshow = new OpenglDisplay(64, 80, 128, 16);
        waveform = new OpenglDisplay(256, 65, 256, 16);
        fftdisp = new OpenglDisplay(256, 65, 256, 16);
        krnldisplay = new OpenglDisplay(1024, 768, 1024, 16);

        comps = new GBComponents();

        cpu = new CPU();
        gpu = new GPU(this);
        apu = new APU();
        mem = new MemoryManager();

        // @formatter:off
        // GAMES
        //mem.loadRom("Roms/Tetris.gb");//bg bugged, sound bugged, wave doesnt silence
        // mem.loadRom("Roms/Asteroids.gb"); //works, sound too fast
        //mem.loadRom("Roms/Boulder Dash (U) [!].gb");//works
        //mem.loadRom("Roms/Missile Command (U) [M][!].gb");//works, bullshit game
        // mem.loadRom("Roms/Motocross Maniacs (E) [!].gb");//blank screen, doesnt start
        //mem.loadRom("Roms/Amida (J).gb");//works but crappy game
        //mem.loadRom("Roms/Castelian (E) [!].gb");//halt is bugging and flickers like madfx
        //mem.loadRom("Roms/Boxxle (U) (V1.1) [!].gb");//works
        //mem.loadRom("Roms/Super Mario Land (V1.1) (JUA) [!].gb");//works
        //mem.loadRom("Roms/Super Mario Land 2 - 6 Golden Coins (UE) (V1.2) [!].gb");//
        //mem.loadRom("Roms/Super Mario Land 3 - Warioland (JUE) [!].gb");//coin -> wrong sweep?, tube glitches, 
        //mem.loadRom("Roms/Tetris 2 (UE) [S][!].gb");//notes too short and aliasing?
        //mem.loadRom("Roms/Legend of Zelda, The - Link's Awakening.gb");//fixed 8x16 glitch, sound glitches after speedmode
        //mem.loadRom("Roms/Pokemon Red (U) [S][!].gb");//y dude its MBC3 
        //mem.loadRom("Roms/Metroid II - Return of Samus (UE) [!].gb");
        //mem.loadRom("Roms/Kirby's Dream Land 2 (U) [S][!].gb");//short tones?
        //mem.loadRom("Roms/Yoshi (U) [!].gb");//hella bugged
        //mem.loadRom("Roms/Batman - Return of the Joker.gb");
        //mem.loadRom("Roms/Final Fantasy Adventure (U) [!].gb");//MBC 2
        //mem.loadRom("Roms/Donkey Kong (V1.1) (JU) [S][!].gb"); //graphic glitches
        //mem.loadRom("Roms/Mega Man 4 (U) [!].gb");
        //mem.loadRom("Roms/Kid Dracula (U) [!].gb");
        // CPU INSTRUCTION TESTS - ALL PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/01-special.gb");//PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/02-interrupts.gb");
        // //FAILED #5 Halt sucks
        // mem.loadRom("Testroms/cpu_instrs/individual/03-op sp,hl.gb");//
        // PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/04-op r,imm.gb");//PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/06-ld r,r.gb");//PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");//PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/08-misc instrs.gb");//PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/09-op r,r.gb");// PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/10-bit ops.gb");// PASSED
        // mem.loadRom("Testroms/cpu_instrs/individual/11-op a,(hl).gb");//
        // PASSED
        // mem.loadRom("Testroms/cpu_instrs/cpu_instrs.gb");// passed except #5
        // CPU TIMING TESTS - ALL UNTESTED
        //mem.loadRom("Testroms/instr_timing/instr_timing.gb");
        // CPU MEM TIMING
        // mem.loadRom("Testroms/mem_timing/individual/01-read_timing.gb");
        // mem.loadRom("Testroms/mem_timing/individual/02-write_timing.gb");
        // mem.loadRom("Testroms/mem_timing/individual/03-modify_timing.gb");
        // @formatter:on
        // GRAPHICS
        // mem.loadRom("Testroms/graphicskev/gbtest.gb");
        // SOUND
        // mem.loadRom("Testroms/sound/dmg_sound.gb");
        // mem.loadRom("Testroms/sound/01-registers.gb");
        // mem.loadRom("Testroms/sound/02-len ctr.gb");
        // general SYSTEST
        // mem.loadRom("Testroms/systest/test.gb");//not supported
        // testgb
        // mem.loadRom("Testroms/testgb/PUZZLE.GB");
        // mem.loadRom("Testroms/testgb/RPN.GB");
        // mem.loadRom("Testroms/testgb/SOUND.GB");
        // mem.loadRom("Testroms/testgb/SPACE.GB");
        // mem.loadRom("Testroms/testgb/SPRITE.GB");//works
        // mem.loadRom("Testroms/testgb/TEST.GB");
        // IRQ
        // mem.loadRom("Testroms/irq/IRQ Demo (PD).gb");
        // JOYPAD
        // mem.loadRom("Testroms/joypad/Joypad Test V0.1 (PD).gb");//PASSED
        // mem.loadRom("Testroms/joypad/You Pressed Demo (PD).gb");//graphic
        // bugged, input passed
        // Scrolling
        // mem.loadRom("Testroms/scroll/Scroll Test Dungeon (PD) [C].gbc");//not
        // supported
        // Demos
        // mem.loadRom("Testroms/demos/99 Demo (PD) [C].gbc");// MBC 5 goddamnit
        // mem.loadRom("Testroms/demos/Filltest Demo (PD).gb"); //Works
        // mem.loadRom("Testroms/demos/Paint Demo (PD).gb");
        // mem.loadRom("Testroms/demos/Big Scroller Demo (PD).gb");//works
        comps.cpu = cpu;
        comps.mem = mem;
        comps.gpu = gpu;
        comps.apu = apu;
        comps.link();

        reset();
        // cpu.DEBUG_ENABLED = true;
        paused = true;
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    private final int[][] bg = new int[256][256];
    private final int[][] spr = new int[64][80];
    private final int[][] wave = new int[256][65];
    private final int[][] krnl = new int[1024][768];
    private int wavecounter = 0;
    //private final int waveshift = 0;

    private void doDebugVram() {
        for (int y = 0; y < 256 / 8; y++) {
            for (int x = 0; x < 32; x++) {
                int mapentry = 0x9800 + y * 32 + x;
                byte tileid = mem.readByte(mapentry);
                for (int i = 0; i < 8; i++) {
                    int[] data = gpu.get8bg(i, tileid, 0);
                    for (int w = 0; w < 8; w++) {
                        bg[x * 8 + w][y * 8 + i] = data[w];
                    }
                }
            }
        }
        map.refresh(bg);
    }

    private void doDebugSpr() {
        for (int i = 0; i < 40; i++) { // 8x4
            int spry = (mem.readByte(i * 4 + 0xFE00) & 0xff);
            int sprx = (mem.readByte(i * 4 + 1 + 0xFE00) & 0xff);
            byte sprid = mem.readByte(i * 4 + 2 + 0xFE00);
            byte attr = mem.readByte(i * 4 + 3 + 0xFE00);
            boolean hidden = false;
            if (sprx <= 0 || sprx >= 168 || spry <= 0 || spry >= 144) {
                hidden = true;
            }
            for (int z = 0; z < 8; z++) {
                int[] data = gpu.get8spr(z, sprid, attr);

                for (int w = 0; w < 8; w++) {
                    spr[i % 8 * 8 + w][i / 8 * 16 + z] = data[w];
                }
                if (hidden) {
                    spr[i % 8 * 8 + z][i / 8 * 16 + z] = 0xFF0000FF;
                }
            }
            // test hidden

        }
        sprshow.refresh(spr);
    }
    double[] tmp = new double[256];
    Complex[] sig = new Complex[256];
    private final int[][] fftdata = new int[256][65];

    private void doDebugWaveforms() {

        // System.out.println(apu.getSampleoffset());
        // can draw max 256 samples from the buffer...
        // int limit = Math.min(256, apu.getSampleoffset() / 2);// always dividable
        int limit = 256;
        // shift buffer left by limit
        for (int y = 0; y < 65; y++) {
            for (int x = 0; x < 256; x++) {
                // wave[x - limit][y] = wave[x][y];
                wave[x][y] = 0xFFFFFFFF;
                fftdata[x][y] = 0xFFFFFFFF;
            }
        }

        wavecounter = 0;
        for (int i = 256 - limit; i < 256; i++) {
            short sample = (short) ((apu.samplebuffer8[wavecounter * 2] & 0xff) | ((apu.samplebuffer8[wavecounter * 2 + 1] & 0xff) << 8));
            tmp[wavecounter] = (double) sample / 32768;
            int y = (sample / 1024);// equals /32768 *32//range from +32 to -32
            y = 64 - (y + 32);// transform space cooridantes
            wave[i][y] = 0xFF0000FF;
            wavecounter++;
        }
        for (int j = 0; j < 256; j++) {
            sig[j] = new Complex(tmp[j], (double) 0);
        }
        Complex[] fftd = FFT.fft(sig);
        for (int i = 0; i < 256; i++) {
            int y = (int) (fftd[i / 2].abs() * 2); //we only need one half of the spectrum because of symmetry
            y = 64 - (y);
            for (int j = 64; j > y; j--) {
                fftdata[i][min(max(j, 0), 64)] = 0xFF0000FF; //clamping because I dont know why my norming fails
            }
        }

        waveform.refresh(wave);
        fftdisp.refresh(fftdata);
    }

//    private void doKernelDisplay() {
//        //FILTER KERNEL
//        int krnltlen = krnltest.getKernel().length;
//        int yoffset = 500;
//        int xoffset = 250;
//        for (int i = 0; i < krnltlen; i++) {
//            int x = i;
//            int y = (int) ((krnltest.getKernel()[i]) * krnltlen);
//            krnl[(int) clamp(0, 1023, x + xoffset)][(int) clamp(0, 767, 767 - y - yoffset)] = 0xff0000ff;
//        }
//
//        //DFT OF FILTER KERNEL
//        if (signal[0] == null) {
//            for (int i = 0; i < signal.length; i++) {
//                signal[i] = new BandpassFilter.Complex();
//            }
//        }
//
//        for (int i = 0; i < signal.length; i++) {
//            signal[i].img = 0;
//            signal[i].real = krnltest.getKernel()[i];
//        }
//
//        Complex[] result = BandpassFilter.complexfourier(signal);
//        int height = 300;
//        int width = 300;
//        int arbscale = 50;
//
//        for (int i = 0; i < result.length / 2; i++) {
//            float real = result[i].real;
//            float img = result[i].img;
//            //find magnitude of each complex number
//            double mag = Math.sqrt(real * real + img * img);
//            int x = i;
//            int y = (int) ((float) mag * arbscale);
//            //zero line
//            krnl[(int) clamp(0, 1023, x + width)][(int) clamp(0, 767, 767 - 0 - height)] = 0xffff00ff;
//            krnl[(int) clamp(0, 1023, x + width)][(int) clamp(0, 767, 767 - y - height)] = 0xff0000ff;
//
//        }
//
//        //IMPULSE TEST (FILTER CONVOLVED WITH UNIT IMPULSE)
//        int offx = 300;
//        int offy = 100;
//        //reset buffer
//        for (int i = 0; i < krnltest.getKernel().length; i++) {
//            krnltest.store(0f);
//        }
//        float stepresponse = 0;
//        for (int i = 0; i < krnltest.getKernel().length; i++) {
//            if (i == 0) {
//                krnltest.store(1f);
//            } else {
//                krnltest.store(0f);
//            }
//            int x = i;
//            float rawconv = krnltest.convolveStep();
//            int y = (int) (rawconv * krnltlen);
//            //if(i==krnltlen/2)System.out.println(krnltest.getKernel()[i]);
//            stepresponse += rawconv;
//            krnl[(int) clamp(0, 1023, x + offx)][(int) clamp(0, 767, 767 - y - offy)] = 0xffff00ff;
//        }
//        //STEP RESPONSE
//        float krnlstep = 0;
//        for (int i = 0; i < krnltlen; i++) {
//            krnlstep += krnltest.getKernel()[i];
//        }
//        //System.out.println("Kernel sum:"+krnlstep+" vs stepresponse:"+stepresponse);
//        krnldisplay.refresh2(krnl);
//    }
    private float clamp(float min, float max, float c) {
        return Math.min(Math.max(min, c), max);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        int cycles = 0;
        if (!paused && hasRom) {
            hz60accu += Gdx.graphics.getDeltaTime();
            if (hz60accu >= hz60tick) {
                hz60accu -= hz60tick;

                double rate = Gdx.input.isKeyPressed(Keys.SPACE) ? cyclesperframe * Settings.speedup : cyclesperframe;

                while (cpuacc < rate) {

                    cycles = cpu.tick();
                    mem.clock(cycles);
                    gpu.tick(cycles);
                    apu.tick(cycles);
                    cpuacc += cycles;
                }

                cpuacc -= rate;

                apu.flush();
                doDebugWaveforms();

                clock++;
                fontalpha -= 0.003f;

            }
        }
        //doDebugVram();
        //doDebugSpr();
        //doKernelDisplay();
        //map.refresh(bg);

        int sprzoom = 2;
        int h = Gdx.graphics.getHeight();
        int w = Gdx.graphics.getWidth();
        float fontoffset = font.getCapHeight() - font.getDescent();

        batch.begin();

        screen.drawStraight(batch, 0, 0, 0, 0, 160, 144, Settings.zoom, Settings.zoom, 0, 0, 0, 160, 144);
        //map.drawStraight(batch, 160*2, 80, 0, 0, 256, 256, 1, 1, 0, 0, 0, 256, 256);
        //sprshow.drawStraight(batch, 160*2 ,0, 0, 0, 64, 80, 1, 1, 0, 0, 0, 64, 80);
        waveform.drawStraight(batch, 320 + 10, 0, 0, 0, 256, 64, 1, 1, 0, 0, 0, 256, 64);
        fftdisp.drawStraight(batch, 320 + 10, 90, 0, 0, 256, 64 * 2, 1, 2, 0, 0, 0, 256, 64);
        //krnldisplay.drawStraight(batch, 0,0, 0, 0, 1024, 768, 1, 1, 0, 0, 0, 1024, 768);
        if (showfps) {
            font.setColor(1f, 1f, 0, 0.5f);
            font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 10, h - 10);
        }
        if (fontalpha > 0f && romInfo) {
            fadeoutFont.drawMultiLine(batch, mem.getRomInfo(), 50, h - 30);
            fadeoutFont.setColor(1f, 1f, 0f, (fontalpha > 0f ? fontalpha : 0f));
        }
        //font.draw(batch, "Background map", w - 300, h - 300 + 256 + fontoffset);
        //font.draw(batch, "Gameboy screen: 160x144, 2x zoom", 50, 50 + 144 * 2 + fontoffset);
        //font.draw(batch, "Sprites", w - 300, h - 500 + 80 * sprzoom + fontoffset);

        batch.end();
        gui.draw();
    }

    private void timedDebug(float trigger) {
        if (clock / 60f >= trigger) {
            CPU.DEBUG_ENABLED = true;
        }
    }

    public void flushScreen() {
        screen.refresh2(gpu.videobuffer);
    }

    public void runGameboy() {
        paused = false;
    }

    public void stopGameboy() {
        paused = true;
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        font.dispose();
        fadeoutFont.dispose();
        screen.dispose();
        map.dispose();
        sprshow.dispose();
        batch.dispose();

        apu.stop();
        apu.close();
        // write savegames
        mem.saveRam();
    }

    public void reset() {
        mem.reset();
        apu.reset();
        gpu.reset();
        cpu.reset();
    }

    public void loadRom(String path) {
        fontalpha = 1.0f;
        mem.loadRom(path);
        hasRom = true;
    }

    public String currentRomPath() {
        return mem.romLoadPath();
    }

    public void setShowFps(boolean set) {
        this.showfps = set;
    }

    public void setShowRomInfo(boolean set) {
        this.romInfo = set;
    }

    public String getRomInfo() {
        return mem.getRomInfo();
    }

}

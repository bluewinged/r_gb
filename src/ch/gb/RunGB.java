package ch.gb;

import ch.gb.apu.APU;
import ch.gb.cpu.CPU;
import ch.gb.gpu.GPU;
import ch.gb.gpu.OpenglDisplay;
import ch.gb.mem.MemoryManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RunGB implements ApplicationListener {
	private GBComponents comps;
	private CPU cpu;
	private GPU gpu;
	private APU apu;
	private MemoryManager mem;

	private final float framerate = 60f;// 60hz
	private float hz60accu;
	private final float hz60tick = 1f / framerate;
	private int cpuacc;
	private final int cyclesperframe = (int) (CPU.CLOCK / framerate);
	private int clock;
	// graphics
	private SpriteBatch batch;
	private BitmapFont font;
	private BitmapFont fadeoutFont;
	private OpenglDisplay screen;
	private OpenglDisplay map;
	private OpenglDisplay sprshow;
	private OpenglDisplay waveforms;

	private float fontalpha = 1.0f;

	private final int speedupf = 10;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		fadeoutFont = new BitmapFont();
		screen = new OpenglDisplay(160, 144, 256, 16);
		map = new OpenglDisplay(256, 256, 256, 16);
		sprshow = new OpenglDisplay(64, 80, 128, 16);
		waveforms = new OpenglDisplay(256, 65, 256, 16);

		comps = new GBComponents();

		cpu = new CPU();
		gpu = new GPU(this);
		apu = new APU();
		mem = new MemoryManager();

		apu.start();
		// @formatter:off
		// GAMES
		//mem.loadRom("Roms/Tetris.gb");//bg bugged, sound bugged
		// mem.loadRom("Roms/Asteroids.gb"); //works
		//mem.loadRom("Roms/Boulder Dash (U) [!].gb");//works
		// mem.loadRom("Roms/Missile Command (U) [M][!].gb");//works
		// mem.loadRom("Roms/Motocross Maniacs (E) [!].gb");//blank screen, doesnt start
		//mem.loadRom("Roms/Amida (J).gb");//works but crappy game
		 //mem.loadRom("Roms/Castelian (E) [!].gb");//halt is bugging and flickers like mad
		//mem.loadRom("Roms/Boxxle (U) (V1.1) [!].gb");//works, 8x16 mode glitch
		//mem.loadRom("Roms/Super Mario Land (V1.1) (JUA) [!].gb");//works
		mem.loadRom("Roms/Super Mario Land 2 - 6 Golden Coins (UE) (V1.2) [!].gb");//
		//mem.loadRom("Roms/Super Mario Land 3 - Warioland (JUE) [!].gb");
		//mem.loadRom("Roms/Tetris 2 (UE) [S][!].gb");
		//mem.loadRom("Roms/Legend of Zelda, The - Link's Awakening.gb");
		//mem.loadRom("Roms/Pokemon Red (U) [S][!].gb");//y dude its MBC3 
		//mem.loadRom("Roms/Metroid II - Return of Samus (UE) [!].gb");
		//mem.loadRom("Roms/Kirby's Dream Land 2 (U) [S][!].gb");
		//mem.loadRom("Roms/Yoshi (U) [!].gb");//hella bugged
		//mem.loadRom("Roms/Batman - Return of the Joker.gb");
		
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

		cpu.reset();
		// cpu.DEBUG_ENABLED = true;
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	private final int[][] bg = new int[256][256];
	private final int[][] spr = new int[64][80];
	private final int[][] wave = new int[256][65];
	private int wavecounter = 0;
	private final int waveshift = 0;

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

	private void doDebugWaveforms() {

		// System.out.println(apu.getSampleoffset());
		// can draw max 256 samples from the buffer...
		int limit = Math.min(256, apu.getSampleoffset() / 2);// always dividable

		// shift buffer left by limit
		for (int y = 0; y < 65; y++) {
			for (int x = 0; x < 256; x++) {
				// wave[x - limit][y] = wave[x][y];
				wave[x][y] = 0xFFFFFFFF;
			}
		}
		wavecounter = 0;
		for (int i = 256 - limit; i < 256; i++) {
			short sample = (short) ((apu.samplebuffer[wavecounter * 2] & 0xff) | ((apu.samplebuffer[wavecounter * 2 + 1] & 0xff) << 8));
			int y = (sample / 1024);// equals /32768 *32//range from +32 to -32
			y = 64 - (y + 32);// transform space cooridantes
			wave[i][y] = 0xFF0000FF;
			wavecounter++;
		}

		waveforms.refresh(wave);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		hz60accu += Gdx.graphics.getDeltaTime();
		if (hz60accu >= hz60tick) {
			hz60accu -= hz60tick;

			int framerate = Gdx.input.isKeyPressed(Keys.SPACE) ? cyclesperframe * speedupf : cyclesperframe;

			while (cpuacc < framerate) {
				int cycles = cpu.tick();
				mem.clock(cycles);
				gpu.tick(cycles);
				apu.tick(cycles);
				cpuacc += cycles;
				//if (apu.getSampleoffset() > 2000)
				
			}
			//System.out.println(apu.getSampleoffset());
			apu.flush();
			doDebugWaveforms();

			cpuacc -= framerate;
			clock++;
			fontalpha -= 0.003f;

		}

		doDebugVram();
		doDebugSpr();

		map.refresh(bg);

		int sprzoom = 2;
		int h = Gdx.graphics.getHeight();
		int w = Gdx.graphics.getWidth();
		float fontoffset = font.getCapHeight() - font.getDescent();

		batch.begin();

		screen.drawStraight(batch, 50, 50, 0, 0, 160, 144, 2, 2, 0, 0, 0, 160, 144);
		map.drawStraight(batch, w - 300, h - 300, 0, 0, 256, 256, 1, 1, 0, 0, 0, 256, 256);
		sprshow.drawStraight(batch, w - 300, h - 500, 0, 0, 64, 80, sprzoom, sprzoom, 0, 0, 0, 64, 80);
		waveforms.drawStraight(batch, 50, h - 150, 0, 0, 256, 64, 1, 1, 0, 0, 0, 256, 64);

		font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 10, h - 10);
		font.draw(batch, "bluew, 2012", 100, h - 10);
		if (fontalpha > 0f) {
			fadeoutFont.drawMultiLine(batch, mem.getRomInfo(), 50, h - 30);
			fadeoutFont.setColor(1f, 1f, 1f, (fontalpha > 0f ? fontalpha : 0f));
		}
		font.draw(batch, "Background map", w - 300, h - 300 + 256 + fontoffset);
		font.draw(batch, "Gameboy screen: 160x144, 2x zoom", 50, 50 + 144 * 2 + fontoffset);
		font.draw(batch, "Sprites", w - 300, h - 500 + 80 * sprzoom + fontoffset);

		batch.end();

		// timedDebug(15);
	}

	private void timedDebug(float trigger) {
		if (clock / 60f >= trigger) {
			CPU.DEBUG_ENABLED = true;
		}
	}

	public void flushScreen() {
		screen.refresh(gpu.videobuffer);
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
		// write savegames
		mem.saveRam();
	}

}

package ch.gb;

import ch.gb.cpu.CPU;
import ch.gb.gpu.GPU;
import ch.gb.gpu.OpenglDisplay;
import ch.gb.mem.MemoryManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RunGB implements ApplicationListener {
	private GBComponents comps;
	private CPU cpu;
	private GPU gpu;
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
	private float fontalpha = 1.0f;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();
		fadeoutFont = new BitmapFont();
		screen = new OpenglDisplay(160, 144, 256, 16);
		map = new OpenglDisplay(256, 256, 256, 16);
		comps = new GBComponents();

		cpu = new CPU();
		gpu = new GPU();
		mem = new MemoryManager();

		//@formatter:off
		//GAMES
		//mem.loadRom("Roms/Tetris.gb");
		mem.loadRom("Roms/Asteroids.gb");
		//mem.loadRom("Roms/Boulder Dash (U) [!].gb");
		//mem.loadRom("Roms/Missile Command (U) [M][!].gb");
		//mem.loadRom("Roms/Motocross Maniacs (E) [!].gb");
		//mem.loadRom("Roms/Amida (J).gb");
		//mem.loadRom("Roms/Castelian (E) [!].gb");//halt is bugging
		//mem.loadRom("Roms/Boxxle (U) (V1.1) [!].gb");
		
		//mem.loadRom("Roms/Super Mario Land (V1.1) (JUA) [!].gb");
		
		//CPU INSTRUCTION TESTS - ALL PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/01-special.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/02-interrupts.gb"); //FAILED #5 Halt sucks
		//mem.loadRom("Testroms/cpu_instrs/individual/03-op sp,hl.gb");// PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/04-op r,imm.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/06-ld r,r.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/08-misc instrs.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/09-op r,r.gb");// PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/10-bit ops.gb");// PASSED FUCK YEAH
		//mem.loadRom("Testroms/cpu_instrs/individual/11-op a,(hl).gb");// PASSED
		//mem.loadRom("Testroms/cpu_instrs/cpu_instrs.gb");// passed except #5
		
		//CPU TIMING TESTS - ALL UNTESTED
		//mem.loadRom("Testroms/instr_timing/instr_timing.gb");
		
		//CPU MEM TIMING
		//mem.loadRom("Testroms/mem_timing/individual/01-read_timing.gb");
		//mem.loadRom("Testroms/mem_timing/individual/02-write_timing.gb");
		//mem.loadRom("Testroms/mem_timing/individual/03-modify_timing.gb");
		//@formatter:on

		// GRAPHICS
		// mem.loadRom("Testroms/graphicskev/gbtest.gb");

		// general SYSTEST
		// mem.loadRom("Testroms/systest/test.gb");//not supported

		// testgb
		// mem.loadRom("Testroms/testgb/PUZZLE.GB");
		// mem.loadRom("Testroms/testgb/RPN.GB");
		// mem.loadRom("Testroms/testgb/SOUND.GB");
		// mem.loadRom("Testroms/testgb/SPACE.GB");
		// mem.loadRom("Testroms/testgb/SPRITE.GB");
		// mem.loadRom("Testroms/testgb/TEST.GB");

		// IRQ
		// mem.loadRom("Testroms/irq/IRQ Demo (PD).gb");

		// JOYPAD
		// mem.loadRom("Testroms/joypad/Joypad Test V0.1 (PD).gb");
		// mem.loadRom("Testroms/joypad/You Pressed Demo (PD).gb");

		// Scrolling
		// mem.loadRom("Testroms/scroll/Scroll Test Dungeon (PD) [C].gbc");//not
		// supported

		// Demos
		// mem.loadRom("Testroms/demos/99 Demo (PD) [C].gbc");// MBC 5 goddamnit
		// mem.loadRom("Testroms/demos/Filltest Demo (PD).gb"); //Works
		// mem.loadRom("Testroms/demos/Paint Demo (PD).gb");

		comps.cpu = cpu;
		comps.mem = mem;
		comps.gpu = gpu;
		comps.link();

		cpu.reset();
		// cpu.DEBUG_ENABLED=true;
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	private final int[][] bg = new int[256][256];

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

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		hz60accu += Gdx.graphics.getDeltaTime();
		if (hz60accu >= hz60tick) {
			hz60accu -= hz60tick;

			while (cpuacc < cyclesperframe) {
				int cycles = cpu.tick();
				mem.clock(cycles);
				gpu.tick(cycles);
				cpuacc += cycles;
			}
			cpuacc -= cyclesperframe;
			clock++;
			fontalpha -= 0.003f;
		}

		doDebugVram();
		screen.refresh(gpu.videobuffer);
		map.refresh(bg);

		int h = Gdx.graphics.getHeight();
		int w = Gdx.graphics.getWidth();
		float fontoffset = font.getCapHeight() - font.getDescent();

		batch.begin();

		font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 10, h - 10);
		font.draw(batch, "bluew, 2012", 100, h - 10);
		if (fontalpha > 0f) {
			fadeoutFont.drawMultiLine(batch, mem.getRomInfo(), 50, h - 50);
			fadeoutFont.setColor(1f, 1f, 1f, (fontalpha > 0f ? fontalpha : 0f));
		}
		font.draw(batch, "Background map", w - 300, h - 300 + 256 + fontoffset);
		font.draw(batch, "Gameboy screen: 160x144, 2x zoom", 50, 50 + 144 * 2 + fontoffset);

		screen.drawStraight(batch, 50, 50, 0, 0, 160, 144, 2, 2, 0, 0, 0, 160, 144);
		map.drawStraight(batch, w - 300, h - 300, 0, 0, 256, 256, 1, 1, 0, 0, 0, 256, 256);
		batch.end();

		// timedDebug(15);
	}

	private void timedDebug(float trigger) {
		if (clock / 60f >= trigger) {
			CPU.DEBUG_ENABLED = true;
		}
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
		batch.dispose();
		fadeoutFont.dispose();
		screen.dispose();
		map.dispose();
	}

}

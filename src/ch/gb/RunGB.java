package ch.gb;

import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RunGB implements ApplicationListener {
	private GBComponents comps;
	private CPU cpu;
	private MemoryManager mem;

	private final float framerate = 60f;// 60hz
	private float hz60accu;
	private final float hz60tick = 1f / framerate;
	private int cpuacc;
	private final int cyclesperframe = (int) (CPU.CLOCK / framerate);

	// graphics
	private SpriteBatch batch;
	private BitmapFont font;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();

		comps = new GBComponents();

		cpu = new CPU();
		mem = new MemoryManager();
		//@formatter:off
		//mem.loadRom("Roms/Tetris.gb");
		// mem.loadRom("Roms/Asteroids.gb");
		
		//CPU INSTRUCTION TESTS - ALL PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/01-special.gb");//PASSED
		mem.loadRom("Testroms/cpu_instrs/individual/02-interrupts.gb"); //FAILED #4 Timer doesnt work
		//mem.loadRom("Testroms/cpu_instrs/individual/03-op sp,hl.gb");// PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/04-op r,imm.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/06-ld r,r.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/08-misc instrs.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/09-op r,r.gb");// PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/10-bit ops.gb");// PASSED FUCK YEAH
		//mem.loadRom("Testroms/cpu_instrs/individual/11-op a,(hl).gb");// PASSED
		
		//CPU TIMING TESTS - ALL UNTESTED
		
		
		//@formatter:on

		// Utils.dumpMem(mem.rombanks[0]);

		comps.cpu = cpu;
		comps.mem = mem;
		comps.link();

		cpu.reset();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		hz60accu += Gdx.graphics.getDeltaTime();
		if (hz60accu >= hz60tick) {
			hz60accu -= hz60tick;

			while (cpuacc < cyclesperframe) {
				int cycles = cpu.tick();
				cpuacc += cycles;
			}
			cpuacc -= cyclesperframe;
		}

		// FPS
		batch.begin();
		font.draw(batch, "FPS:" + Gdx.graphics.getFramesPerSecond(), 50, 50);
		batch.end();
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
	}

}

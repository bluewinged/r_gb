package ch.gb;

import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public class RunGB implements ApplicationListener {
	private GBComponents comps;
	private CPU cpu;
	private MemoryManager mem;
	private final float framerate = 60f;// 60hz
	private float hz60accu;
	private final float hz60tick = 1f / framerate;
	private int cpuaccu;
	private final int cpucycles = (int) (CPU.CLOCK / framerate);

	@Override
	public void create() {
		comps = new GBComponents();

		cpu = new CPU();
		mem = new MemoryManager();
		//@formatter:off
		//mem.loadRom("Roms/Tetris.gb");
		// mem.loadRom("Roms/Asteroids.gb");
		//mem.loadRom("Testroms/cpu_instrs/individual/01-special.gb");//POP AF FAILED #5
		//mem.loadRom("Testroms/cpu_instrs/individual/02-interrupts.gb"); //FAILED #2
		//mem.loadRom("Testroms/cpu_instrs/individual/03-op sp,hl.gb");// E8 E8 F8 F8 FAILED
		//mem.loadRom("Testroms/cpu_instrs/individual/04-op r,imm.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/06-ld r,r.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/08-misc instrs.gb");//F1 (POP AF)  FAILED
		//mem.loadRom("Testroms/cpu_instrs/individual/09-op r,r.gb");// PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/10-bit ops.gb");// PASSED FUCK YEAH
		//mem.loadRom("Testroms/cpu_instrs/individual/11-op a,(hl).gb");// 27 (DAA) FAILED
		//@formatter:on

		//Utils.dumpMem(mem.rombanks[0]);
		
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
		hz60accu += Gdx.graphics.getDeltaTime();
		if (hz60accu >= hz60tick) {
			hz60accu -= hz60tick;

			while (cpuaccu <= cpucycles+1000) {
				//FreqMeter.measure();
				cpu.tick();
				cpuaccu++;//baws speedmode
			}
			cpuaccu -= cpucycles;
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

	}

}

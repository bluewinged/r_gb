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
		mem.loadRom("Testroms/cpu_instrs/individual/01-special.gb");//POP AF FAILED #5
		//mem.loadRom("Testroms/cpu_instrs/individual/02-interrupts.gb"); //FAILED #2
		//mem.loadRom("Testroms/cpu_instrs/individual/03-op sp,hl.gb");//39 E8 E8 F8 F8 FAILED
		//mem.loadRom("Testroms/cpu_instrs/individual/04-op r,imm.gb");//FE C6 CE D6 -> crash
		//mem.loadRom("Testroms/cpu_instrs/individual/06-ld r,r.gb");//PASSED
		//mem.loadRom("Testroms/cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");// FE C6 CE D6 -> crash
		//mem.loadRom("Testroms/cpu_instrs/individual/08-misc instrs.gb");//F2 E2 F1  FAILED
		//mem.loadRom("Testroms/cpu_instrs/individual/09-op r,r.gb");//B8 B9 BA BB BC BD 80 81 82 83 84 85 87 88 89 8A 8B 8C 8D 8F 90 91 92 93 94 95 98 99 9A 9B 9C 9D 9F 07 17 0F 1F FAILED
		//mem.loadRom("Testroms/cpu_instrs/individual/10-bit ops.gb");// PASSED FUCK YEAH
		//mem.loadRom("Testroms/cpu_instrs/individual/11-op a,(hl).gb");//BE 86 8E 96 9E CB 46 CB 4E CB 56 CB 5E CB 66 CB 6E CB 76 CB 7E CB 86 CB 8E CB 96 CB 9E CB A6 CB AE CB B6 CB BE CB C6 CB CE CB D6 CB DE CB E6 CB EE CB F6 CB FE 27 FAILED 
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

			while (cpuaccu <= cpucycles) {
				//FreqMeter.measure();
				cpuaccu+=cpu.tick();
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

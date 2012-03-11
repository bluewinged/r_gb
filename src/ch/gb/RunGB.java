package ch.gb;

import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

import com.badlogic.gdx.ApplicationListener;

public class RunGB implements ApplicationListener {
	private GBComponents comps;
	private CPU cpu;
	private MemoryManager mem;

	@Override
	public void create() {
		comps = new GBComponents();

		cpu = new CPU();
		mem = new MemoryManager();
		//mem.loadRom("Roms/Tetris.gb");
		//mem.loadRom("Roms/Asteroids.gb");
		mem.loadRom("Testroms/cpu_instrs/individual/01-special.gb");
		//mem.loadRom("Testroms/cpu_instrs/individual/06-ld r,r.gb");
		
		comps.cpu = cpu;
		comps.mem = mem;
		comps.link();

		cpu.reset();
		cpu.tick();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub

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

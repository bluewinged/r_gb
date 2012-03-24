package ch.gb;

import ch.gb.apu.APU;
import ch.gb.cpu.CPU;
import ch.gb.gpu.GPU;
import ch.gb.mem.MemoryManager;

public class GBComponents {
	public CPU cpu;
	public MemoryManager mem;
	public GPU gpu;
	public APU apu;
	public void link(){
		cpu.link(this);
		mem.link(this);
		gpu.link(this);
		apu.link(this);
	}
}

package ch.gb;

import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

public class GBComponents {
	public CPU cpu;
	public MemoryManager mem;
	public void link(){
		cpu.link(this);
		mem.link(this);
	}
}

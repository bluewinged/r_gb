package ch.gb.io;

import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

public class Timer implements IOport {
	public static final int DIV = 0xff04;
	public static final int TIMA = 0xff05;
	public static final int TMA = 0xff06;
	public static final int TAC = 0xff07;

	private byte div;
	private int tima;// int for overflow control
	private byte tma;
	private byte tac;

	private final int period4096hz = 1024;
	private final int period16384hz = 256;// period in clockcycles
	private final int period32768hz = 128;
	private final int period65536hz = 64;
	private final int period262144hz = 16;
	private int divcounter = period65536hz;
	private int timercounter = period4096hz;
	
	private static final int timaClock[] = { 10, 4, 6, 8 };
	
	private final MemoryManager mem;

	public Timer(MemoryManager mem) {
		this.mem = mem;
	}
	@Override
	public void reset() {
		div = 0;
		tima = 0;
		tma = 0;
		tac = 0;
		divcounter = 64;
		timercounter = 1024;
	}
	public void clock(int cpucycles) {
		// divider reg
		divcounter -= cpucycles;
		while (divcounter <= 0) {
			divcounter = period65536hz + divcounter;// in case already far < 0
			div++;
		}

		// timer reg
		if ((tac & 4) == 4) {// timer enabled
			timercounter -= cpucycles;

			while (timercounter <= 0) {
				timercounter = getFreq() + timercounter;
				tima++;
				if (tima > 0xFF) {
					tima = tma & 0xff;
					mem.requestInterrupt(CPU.TIMER_IR);
				}

			}
		}
	}

	private int getFreq() {
		switch (tac & 3) {
		case 0:
			return period4096hz;
		case 1:
			return period262144hz;
		case 2:
			return period65536hz;
		case 3:
			return period16384hz;
		}
		throw new RuntimeException("Reloading Timer failed" + (tac & 3));// cant
																			// happen
	}

	@Override
	public void write(int add, byte b) {
		if (add == DIV) {
			div = 0;
		} else if (add == TIMA) {
			tima = b & 0xff;
		} else if (add == TMA) {
			tma = b;
		} else if (add == TAC) {
			int freqold = tac & 3;
			tac = (byte)(b&7);
			int freqnew = tac & 3;
			if (freqold != freqnew) {
				timercounter = getFreq();
			}
		}
	}

	@Override
	public byte read(int add) {
		if (add == DIV) {
			return div;
		} else if (add == TIMA) {
			//System.out.println("forrealcall:"+tima);
			return (byte) tima;
		} else if (add == TMA) {
			return tma;
		} else if (add == TAC) {
			return tac;
		} else {
			throw new RuntimeException("TIMER-> Shouldnt reacht this statement");
		}

	}


}

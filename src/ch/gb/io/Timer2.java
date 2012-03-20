package ch.gb.io;

import ch.gb.cpu.CPU;
import ch.gb.mem.MemoryManager;

public class Timer2 implements IOport {
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


	private final MemoryManager mem;

	public Timer2(MemoryManager mem) {
		this.mem = mem;
	}

	public void clock(int cpucycles) {
		divcounter +=cpucycles;
		if(divcounter>=256){
			div++;
			divcounter -=256;
		}
		
		if((tac&4)>0){
			timercounter+=cpucycles;
			int selectclock = tac&03;
			if(selectclock==0 && timercounter >=1024){
				tima+=1;
				timercounter -=1024;
			}else if(selectclock==1 && timercounter >=16){
				tima +=1;
				timercounter -=16;
			}else if(selectclock==2&& timercounter >=64){
				tima+=1;
				timercounter -=64;
			}else if(selectclock==3&&timercounter >=256){
				tima+=1;
				timercounter-=256;
			}
			
			if(tima>=0x100){
				tima = tma&0xff;
				mem.requestInterrupt(CPU.TIMER_IR);
			}
		}
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
			tac =(byte)(b&7);
		}
	}

	@Override
	public byte read(int add) {
		if (add == DIV) {
			return div;
		} else if (add == TIMA) {
			return (byte) tima;
		} else if (add == TMA) {
			return tma;
		} else if (add == TAC) {
			return tac;
		} else {
			throw new RuntimeException("TIMER-> Shouldnt reacht this statement");
		}

	}

	public void reset() {
		div = 0;
		tima = 0;
		tma = 0;
		tac = 0;
		divcounter = 64;
		timercounter = 1024;
	}
}

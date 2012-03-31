package ch.gb.mem;

public class RTC {
	private int latchcounter;
	private final byte[] regs;
	private byte lastLatch=(byte)0xff;
	public RTC() {
		regs = new byte[5];
	}

	public void write(int add, byte b) {
		if (add >= 0x400 && add < 0x6000) {

		} else if (add < 0x800) {
			if(lastLatch==0 && b==1)
				latch();
			lastLatch=b;
		}
	}
	
	private void set(boolean enabled,byte b){
		b &=0xf;
		b-=8;
	}
	
	private void latch() {
		long unixTime = System.currentTimeMillis()/1000L;
	}
}

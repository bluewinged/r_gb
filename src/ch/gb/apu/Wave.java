package ch.gb.apu;



public class Wave extends Channel {
	private boolean enabled;
	private boolean dacEnabled;

	private int volumecode;

	private int lc;
	private final int lengthmask = 0xff;
	
	private int samplebuffer;
	private int position;
	private final byte[] wavetable;

	private static int[] shift ={4,0,1,2};
	
	public Wave() {
		wavetable = new byte[0x10];
		//TODO: init vals 
	}
	@Override
	public void reset(){
		enabled=false;
		dacEnabled=false;
		volumecode=0;
		lc=0;
		samplebuffer=0;
		position=0;
	}

	public void powerOn() {
		//System.out.println("poweron@wave");
		samplebuffer=0; 
	}

	private int getFrequency() {
		return (((nr4 & 7) << 8) | (nr3 & 0xff));
	}

	private int getPeriod() {
		return (2048 - getFrequency()) * 2;
	}

	@Override
	void write(int add, byte b) {
		if (add == APU.NR30) {
			nr0 = b;
			dacEnabled = (b & 0x80) == 0x80;
		} else if (add == APU.NR31) {
			nr1 = b;
			lc = (256 - (b & 0xff));
		} else if (add == APU.NR32) {
			nr2 = b;
			volumecode = b >> 5 & 3;
		
		} else if (add == APU.NR33) {
			nr3 = b;
		} else if (add == APU.NR34) {
			nr4 = b;
			//System.out.println(Utils.dumpHex(b));
			if ((b & triggermask) == triggermask) {// trigger
				enabled = true;
				if(lc==0)
					lc = 256;
				divider = getPeriod();
				volumecode = nr2 >> 5 & 3;
				position =0;
				if(!dacEnabled){
					enabled = false;
				}
			}
		}else if(add >=0xff30 && add<=0xff3f){
			wavetable[add-0xff30]=b;
		}

	}

	@Override
	byte read(int add) {
		//System.out.println("reads detected"+Utils.dumpHex(add));
		if (add == APU.NR30) {
			return (byte) (nr0 | 0x7F);
		} else if (add == APU.NR31) {
			return (byte) (nr1 | 0xFF);
		} else if (add == APU.NR32) {
			return (byte) (nr2 | 0x9F);
		} else if (add == APU.NR33) {
			return (byte) (nr3 | 0xFF);
		} else if (add == APU.NR34) {
			return (byte) (nr4 | 0xBF);
		}else if(add >=0xff30 && add<=0xff3f){
			return wavetable[add-0xff30];
		}
		throw new RuntimeException("never happens");
	}

	void clock(int cycles) {
		divider -= cycles;
		while (divider <= 0) {
			divider += getPeriod();
			position=(position+1)&0x1f;//32 step
			samplebuffer = (wavetable[position/2]>>((position%2)*4)) &0xf;
			//System.out.println(position+"->"+samplebuffer);
		}
	}

	void clocklen() {
		if ((nr4 & lengthmask) != 0 && lc != 0) {// length enabled
			if (--lc <= 0)
				enabled = false;
		}
	}
	public int poll(){
		//System.out.println(samplebuffer>>>volumecode);
		return enabled?(samplebuffer>>>shift[volumecode]):0;
	}
	public boolean status(){
		return enabled;
	}
}

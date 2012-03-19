package ch.gb.io;

public class SoundPorts implements IOport {
	// Tone & sweep
	public static final int NR10 = 0xFF10;
	public static final int NR11 = 0xFF11;
	public static final int NR12 = 0xFF12;
	public static final int NR13 = 0xFF13;
	public static final int NR14 = 0xFF14;

	// Tone
	public static final int NR21 = 0xFF16;
	public static final int NR22 = 0xFF17;
	public static final int NR23 = 0xFF18;
	public static final int NR24 = 0xFF19;

	// Wave Output
	public static final int NR30 = 0xFF1A;
	public static final int NR31 = 0xFF1B;
	public static final int NR32 = 0xFF1C;
	public static final int NR33 = 0xFF1D;
	public static final int NR34 = 0xFF1E;
	//FF30-FF3F Wave Pattern RAM
	
	//Noise
	public static final int NR41 = 0xFF20;
	public static final int NR42 = 0xFF21;
	public static final int NR43 = 0xFF22;
	public static final int NR44 = 0xFF23;
	
	//Soundcontrol
	public static final int NR51 = 0xFF25;
	public static final int NR52 = 0xFF26;

	@Override
	public void write(int add, byte b) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte read(int add) {
		// TODO Auto-generated method stub
		return 0;
	}

}

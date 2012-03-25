package ch.gb.apu;

public abstract class Channel {
	protected byte nr0;
	protected byte nr1;
	protected byte nr2;
	protected byte nr3;
	protected byte nr4;
	protected int divider = 1;
	protected int triggermask = 0x80;

	abstract void write(int add, byte b);

	abstract byte read(int add);

	abstract void reset();
}

package ch.gb.io;

public interface IOport {
	public void write(int add, byte b);

	public byte read(int add);
	public void reset();
}

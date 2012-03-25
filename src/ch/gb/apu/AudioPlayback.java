package ch.gb.apu;

public interface AudioPlayback {

	public void startPlayback();

	public void stopPlayback();

	public void flush();

	public void discardSamples();

}

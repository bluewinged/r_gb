package ch.gb.utils;

public class Audio {
	private static float lastAmp=0;
	private static float lastOut=0;

	//Beannich
	public static float blockDC(float sample) {
		float output = sample - lastAmp + 0.999f * lastOut;
		lastAmp = sample;
		lastOut = output;

		return output;
	}
	public static float runLowpass(){
		return 0f;
	}
}

package ch.gb.apu;
public class BandpassFilter { 
	//windowed sinc filter (blackman)
	//Decimating FIR filter
	//				sin(2*pi*f*(i-M/2))		[				2*pi*i				4*pi*i		]
	//	h[i] = K* ---------------------- * 	[0.42 - 0.5cos(----------) + 0.08cos(-------)	]
	//				i-M/2					[				  M						M		]
	private final float[] ringbuffer;
	private float[] kernel;
	//private final  float[] kerneltmp;
	private int rindex=-1;
	private final int rsize;
	
	public BandpassFilter(int taps,float cutofflo,float cutoffhi){//aiming for 128 taps?
		ringbuffer = new float[taps+1];
		kernel = new float[taps+1];
		//kerneltmp = new float[taps+1];
		rsize = taps+1;


		//LOWPASS
		float[] tmp1 = generateLowpass(taps,cutofflo);
		//HIPASS
		float[] tmp2 = generateHipass(taps,cutofflo);//exchange cutoffs

		
		//BANDREJECT
		for(int i=0; i< tmp1.length;i++){
			kernel[i] = tmp1[i] + tmp2[i];
		}
		//BANDPASS
		for(int i=0; i<kernel.length;i++){
			kernel[i] = -kernel[i];
		}
		kernel[(kernel.length-1) /2] +=1;
		kernel = tmp1;
	//	for(int i=0; i< (kernel.length-1) /2;i++){
	//		kernel[i] =0f;
	//	}
	}//
	public float[] generateLowpass(int taps,float cutofflo){
		//LOWPASSFILTER
		float[] res = new float[taps+1];
		int m = taps+1; //must be even, m+1 total points (=uneven)
		float k =1;//unity gain
		for(int i=0; i< taps; i++){
			float numeratorSinc = (float)( k*Math.sin(2*Math.PI*cutofflo*(i-m/2)));
			float denominatorSinc= (i-m/2)*(float)Math.PI;
			float window = (float)(0.42d -0.5d*Math.cos(2*Math.PI*i/m) + 0.08d*Math.cos(4*Math.PI*i /m));
			if(i == m/2){//prevent divide by zero
				res[i] = (2*cutofflo*k);
				continue;
			}
			res[i] = numeratorSinc/denominatorSinc * window;
		}
		return res;
	}
	public float[] generateHipass(int taps, float cutoffhi){
	   float[] res =generateLowpass(taps,cutoffhi);
		for(int i=0; i<res.length;i++){
			res[i] =-res[i];
		}
		res[(res.length-1)/2]+=1;
		return res;
	}
/**
 * Store all samples in the delay line
 * @param sample
 */
	public void store(float sample ){
		rindex = (rindex+1)%rsize;//advance ringbuffer (and wrap around if needed)
		ringbuffer[rindex] = sample;
		
		
	}
	/**
	 * Only compute the samples you need. There is no feedback, so calculation doesnt depend on earlier results
	 * @return
	 */
	public float convolveStep(){

		int downwards = rsize-1;
		float accumsample=0;
		int upwards = 0;
		//perform convolution
		for(int i=0; i< rsize;i++){
			accumsample = accumsample + ringbuffer[(rindex +i) %rsize] * kernel[downwards];
			downwards--;
			upwards++;
		}
		return accumsample;
	}
	
	public int wrapneg(int i, int wrap){
		int r = i% wrap;
		if( r<0){
			r+=wrap;
		}
		return r;
	}
	public float[] getKernel(){
		return kernel;
	}
	
	public int getbuffindx(){
		return rindex;
	}
	
	
	public static class Complex{
		public float real;
		public float img;
	}
	
	public static Complex[] complexfourier(Complex[] x){
		int size = x.length;
		Complex[] result = new Complex[size];
		for(int i =0; i< size;i++){
			result[i] = new Complex();
		}
		for(int k=0; k< size;k++){
			for(int i=0; i< size;i++){
				//complex sinusoid
				double sr = Math.cos(2*Math.PI*k*i/ size);
				double si = -Math.sin(2*Math.PI*k*i/size);
				result[k].real = (float) (result[k].real + x[i].real*sr - x[i].img * si);
				result[k].img  = (float) (result[k].img  + x[i].real*si + x[i].img * sr);
			}
		}
		return result;
	}
}


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.gb.utils;
import ch.gb.utils.Complex;
import static java.lang.Math.*;
/**
 *
 * @author bluew
 */
public class FFT {
    public static Complex[] fft(Complex[] x){
        int N = x.length; //power of two please
        if(N==1){
           return new Complex[]{x[0]};
        }else{
            Complex[] even = new Complex[N/2];
            for(int i =0; i< N/2 ;i++){
                even[i] = x[2*i];
            }
            Complex[] odd = new Complex[N/2];
            for(int i =0; i< N/2;i++){
                odd[i] = x[2*i+1];
            }
            Complex[] g = fft (even );
            Complex[] u = fft(odd);
            
            Complex[] result = new Complex[N];
            for(int k=0; k< N/2;k++){
                Complex e = (new Complex((double)0,-2*PI*k/N)).exp();
                result[k] = Complex.add(g[k], u[k].mul(e));
                result[k+N/2] = Complex.sub(g[k], u[k].mul(e));
            }
            return result;
        }
        
    }
}

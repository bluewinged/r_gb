/*******************************************************************************
 *     <A simple gameboy emulator>
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/
package ch.gb.utils;
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

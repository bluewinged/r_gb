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
public class Complex {

    public double real;
    public double img;

    public Complex(double real, double img) {
        this.real = real;
        this.img = img;
    }

    public double abs() {
        return sqrt(real * real + img * img);
    }

    public Complex exp() {
        return new Complex(Math.exp(real) * cos(img), Math.exp(real) * sin(img));
    }

    public static Complex add(Complex a, Complex b) {
        return new Complex(a.real + b.real, a.img + b.img);
    }

    public static Complex sub(Complex a, Complex b) {
        return new Complex(a.real - b.real, a.img - b.img);
    }

    public Complex mul(Complex b) {

        return new Complex(this.real * b.real - this.img * b.img,
                this.real * b.img + this.img * b.real);
    }

}

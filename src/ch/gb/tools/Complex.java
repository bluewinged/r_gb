/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.gb.tools;

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

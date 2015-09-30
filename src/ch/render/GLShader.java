/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 *
 * @author bluew
 */
public class GLShader {

    private String code = null;
    private int ref = 0;

    // private int shaderType = -1;

    public GLShader(String code, int shaderType) {
        this.code = code;
        ref = glCreateShader(shaderType);
        if (ref == 0) {
            throw new RuntimeException("glCreateShader failed");
        }

        glShaderSource(ref, code);
        glCompileShader(ref);
        if (glGetShaderi(ref, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Couldn't compile shader");
        }
    }
    public int object(){
        return ref;
    }
    public void release(){
        glDeleteShader(ref);
    }
}

/* 
 * Copyright (C) 2017 bluew
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.render;

import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 *
 * @author bluew
 */
public class GLProgram {

    private int ref = 0;

    public GLProgram(ArrayList<GLShader> shaders) {
        if (shaders == null || shaders.size() <= 0) {
            throw new RuntimeException("No Shaders were provided to create the program");
        }
        ref = glCreateProgram();
        if (ref == 0) {
            throw new RuntimeException("glCreateProgram failed");
        }
        for (int i = 0; i < shaders.size(); i++) {
            glAttachShader(ref, shaders.get(i).object());
        }

        glLinkProgram(ref);
        //glValidateProgram(ref);
        //after linking one can modify the shaders however one wants
        for (int i = 0; i < shaders.size(); i++) {
            glDetachShader(ref, shaders.get(i).object());
        }

        //check if linking succesful
        if (glGetProgrami(ref, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Linking the program failed");
        }
    }

    public int attrib(String name) {
        if (name == null) {
            throw new RuntimeException("Attribute name is null");
        }
        int attrib = glGetAttribLocation(ref, name);
        if (attrib == -1) {
            throw new RuntimeException("Program attribute not found: " + name);
        }
        return attrib;
    }

    public int uniform(String name) {
        if (name == null) {
            throw new RuntimeException("Uniform name is null");
        }
        int uniform = glGetUniformLocation(ref, name);
        if (uniform == -1) {
            throw new RuntimeException("Program uniform not found: " + name);
        }
        return uniform;
    }

    public boolean isInUse() {

        int tmp = glGetInteger(GL_CURRENT_PROGRAM);
        return (tmp == ref);
    }

    public int object() {
        return ref;
    }

    public void release() {
        glDeleteProgram(ref);
    }
}

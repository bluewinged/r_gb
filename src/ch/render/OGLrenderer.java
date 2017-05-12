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

import ch.gb.GB;
import ch.gb.cpu.CPU;
import ch.input.GLFWInput;
import ch.render.Bitmap.Format;
import static ch.render.GLUtils.fileToString;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 *
 * @author bluew
 */
public class OGLrenderer {

    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback keyCallback;

    private long window;
    private int vbo;
    private int vao;

    GLProgram prog;
    private static GLTexture glTexture;

    private Bitmap bmp;

    private GB gb;
    private final float framerate = 60f;// 60hz
    private float hz60accu;
    private final float hz60tick = 1f / framerate;
    private double cpuacc;
    private final double cyclesperframe = (CPU.CLOCK / framerate);
    private double lastTime = 0;
    private int zoom = 1;

    public OGLrenderer(GB gb, int zoom) {
        this.gb = gb;
        this.zoom = zoom;
    }

    private void loadShaders() {
        GLShader vert = new GLShader(fileToString("resources/vert02.txt"), GL_VERTEX_SHADER);
        GLShader frag = new GLShader(fileToString("resources/frag02.txt"), GL_FRAGMENT_SHADER);
        ArrayList<GLShader> shaders = new ArrayList();
        shaders.add(vert);
        shaders.add(frag);
        prog = new GLProgram(shaders);
        vert.release(); //after linking we don't need the shaders anymore
        frag.release();
    }

    private void loadRectangle() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        float vertexData[] = {
            //  X     Y     Z       U     V
            -1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 1.0f, 0.0f, 0.0f
        };
        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(vertexData.length);
        vertBuffer.put(vertexData).flip();
        glBufferData(GL_ARRAY_BUFFER, vertBuffer, GL_STATIC_DRAW);

        //connect the xyz to the vert attribute of the vertex shader
        glEnableVertexAttribArray(prog.attrib("vert"));
        glVertexAttribPointer(prog.attrib("vert"), 3, GL_FLOAT, false,
                5 * Float.BYTES, NULL);

        //connect the uv coords to the vertTexCoord attribute of the vertex shader
        glEnableVertexAttribArray(prog.attrib("vertTexCoord"));
        glVertexAttribPointer(prog.attrib("vertTexCoord"), 2, GL_FLOAT, true,
                5 * Float.BYTES, 3 * Float.BYTES);

        //unbind the VAO
        glBindVertexArray(0);

    }

    private void loadTexture() {
        // bmp = Bitmap.bitmapFromFileAsRGBA8("resources/wooden-crate.jpg");
        bmp = new Bitmap(160, 144, Format.RGBA); //TODO: non power of two tex
        //bmp.flipVertically(); 
        glTexture = new GLTexture(bmp, GL_LINEAR, GL_CLAMP_TO_EDGE);
    }

    public void run() {
        init();
        loop();

        //terminate window and callbacks
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // terminate glfw
        glfwTerminate();
        glfwSetErrorCallback(null).free();

        prog.release();
        glTexture.release();
        gb.release();
    }

    private void init() {

        //init GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable initialize GLFW");
        }

        //open a window with GLFW
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
        window = glfwCreateWindow(160 * zoom, 144 * zoom, "R_GB Gameboy Color Emulator", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("glfwCreateWindow failed. OpenGL 3.2 supported?");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWInput().register(gb.comps.joypad)); //hook this

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(2);
        glfwGetFramebufferSize(window, w, h);
        //System.out.println(w.get());
        //System.out.println(h.get());
        //GLFW settings
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        //init GLEW
        //lwjgl has no GLEW
        //TODO: output graphics card capabilities
        //openGL settings
        //glEnable(GL_BLEND);
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glDisable(GL_LIGHTING);
        loadShaders();
        loadTexture();
        loadRectangle();

    }

    public void loop() {

        while (!glfwWindowShouldClose(window)) {
            //process events
            glfwPollEvents();

            //after 1/60 th of a second we render one Gameboy frame
            int cycles = 0;
            double thisTime = glfwGetTime();
            hz60accu += (thisTime - lastTime);
            lastTime = thisTime;
            if (hz60accu >= hz60tick) {
                hz60accu -= hz60tick;

                double rate = cyclesperframe;
                while (cpuacc < rate) {
                    cycles = gb.clock();
                    cpuacc += cycles;
                }
                cpuacc -= rate;
                gb.flushAudio();
            }

            //draw the current lcd contents of the gameboy
            render();
        }

    }

    private void render() {

        glTexture.upload(gb.lcdContent());
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        //bind the programm
        glUseProgram(prog.object());

        //bind the texture and set the "tex" uniform in the fragment shader
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, glTexture.object());
        glUniform1i(prog.uniform("tex"), 0); //set to 0 because texture is bound to GL_TEXTURE0

        //bind the VAO (the triangle)
        glBindVertexArray(vao);

        //draw the VAO 
        glDrawArrays(GL_TRIANGLES, 0, 2 * 3);

        //unbind the VAO, the program and the texture
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        assert (prog.isInUse());
        glUseProgram(0);

        //swap the display buffers
        glfwSwapBuffers(window);
    }
}

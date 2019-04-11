package lwjglwindow;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.InputStream;
import java.nio.*;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class LWJGLWindow 
{
	public FontRenderer fontRenderer;

	// The window handle
	protected long window;
	public int absoluteWidth;
	public int absoluteHeight;
	public double absoluteMouseX;
	public double absoluteMouseY;
	
	public ArrayList<Integer> pressedKeys = new ArrayList<Integer>();
	public ArrayList<Integer> validPressedKeys = new ArrayList<Integer>();

	public ArrayList<Integer> pressedButtons = new ArrayList<Integer>();
	public ArrayList<Integer> validPressedButtons = new ArrayList<Integer>();
	
	public boolean validScrollUp;
	public boolean validScrollDown;
	
	public boolean vsync;

	protected ArrayList<Long> framesList = new ArrayList<Long>();
	protected ArrayList<Double> frameFrequencies = new ArrayList<Double>();
	protected long lastFrame = System.currentTimeMillis(); 
	public double frameFrequency = 1;
	
	protected HashMap<String, Integer> textures = new HashMap<String, Integer>();
	protected HashMap<String, Integer> textureSX = new HashMap<String, Integer>();
	protected HashMap<String, Integer> textureSY = new HashMap<String, Integer>();

	public String name;
	
	public Drawer drawer;
	public Updater updater;
	
	public LWJGLWindow(String name, int x, int y, Updater u, Drawer d, boolean vsync)
	{
		this.name = name;
		this.absoluteWidth = x;
		this.absoluteHeight = y;
		this.updater = u;
		this.drawer = d;
	}
	
	public void run() 
	{
		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	protected void init() 
	{
		this.fontRenderer = new FontRenderer(this, "/font.png");
		
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are
		// already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden
		// after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be
		// resizable

		// Create the window
		window = glfwCreateWindow(this.absoluteWidth, this.absoluteHeight, this.name, NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed,
		// repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> 
		{
			if (action == GLFW_PRESS)
			{
				pressedKeys.add((Integer)key);
				validPressedKeys.add((Integer)key);
			}
			else if (action == GLFW_RELEASE)
			{
				pressedKeys.remove((Integer)key);
				validPressedKeys.remove((Integer)key);
			}
		});
		
		glfwSetScrollCallback(window, (window, xoffset, yoffset) -> 
		{
			if (yoffset > 0)
				this.validScrollUp = true;
			else if (yoffset < 0)
				this.validScrollDown = true;
		});

		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> 
		{
			if (action == GLFW_PRESS)
			{
				pressedButtons.add(button);
				validPressedButtons.add(button);
			}
			else if (action == GLFW_RELEASE)
			{
				pressedButtons.remove((Integer)button);
				validPressedButtons.remove((Integer)button);
			}
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) 
		{
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		
		if (vsync)
			GLFW.glfwSwapInterval(1);
		else
			GLFW.glfwSwapInterval(0);
		
		// Make the window visible
		glfwShowWindow(window);
	}

	protected void loop() 
	{
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// Run the rendering loop until the user has attempted to close
		// the window.
		
		while (!glfwWindowShouldClose(window)) 
		{
			long milliTime = System.currentTimeMillis();

			framesList.add(milliTime);

			ArrayList<Long> removeList = new ArrayList<Long>();

			for (int i = 0; i < framesList.size(); i++)
			{
				if (milliTime - framesList.get(i) > 1000)
					removeList.add(framesList.get(i));
			}

			for (int i = 0; i < removeList.size(); i++)
			{
				framesList.remove(removeList.get(i));
			}
			
			this.updater.update();

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			
			int[] w = new int[1];
			int[] h = new int[1];
			glfwGetWindowSize(window, w, h);
			absoluteWidth = w[0];
			absoluteHeight = h[0];
			
			double[] mx = new double[1];
			double[] my = new double[1];
			glfwGetCursorPos(window, mx, my);
			absoluteMouseX = mx[0];
			absoluteMouseY = my[0];
			
			if (System.getProperties().contains("windows"))
				glViewport(0, 0, absoluteWidth, absoluteHeight);
			
			glOrtho(0, absoluteWidth, absoluteHeight, 0, 0, 1);
			this.drawer.draw();

			glfwSwapBuffers(window);
			glfwPollEvents();
			

			long time = System.currentTimeMillis();
			long lastFrameTime = lastFrame;
			lastFrame = time;

			double freq =  (time - lastFrameTime) / 10.0;
			frameFrequencies.add(freq);

			if (frameFrequencies.size() > 5)
			{
				frameFrequencies.remove(0);
			}

			double totalFrequency = 0;
			for (int i = 0; i < frameFrequencies.size(); i++)
			{
				totalFrequency += frameFrequencies.get(i);
			}

			frameFrequency = totalFrequency / frameFrequencies.size();
		}
	}

	public void fillOval(double x, double y, double sX, double sY)
	{		
		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + 5);

		glBegin(GL_TRIANGLE_FAN);
		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
		}

		glEnd();
	}
	
	public void setColor(double r, double g, double b, double a)
	{
		glColor4d(r / 255, g / 255, b / 255, a / 255);
	}
	
	public void setColor(double r, double g, double b)
	{
		glColor3d(r / 255, g / 255, b / 255);
	}

	public void drawOval(double x, double y, double sX, double sY)
	{		
		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + 5);

		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glBegin(GL_LINES);
			glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
			glVertex2d(x + Math.cos(i + Math.PI * 2 / sides) * sX / 2, y + Math.sin(i + Math.PI * 2 / sides) * sY / 2);
			glEnd();
		}

	}

	public void fillRect(double x, double y, double sX, double sY)
	{
		glBegin(GL_TRIANGLE_FAN);

		glVertex2d(x, y);
		glVertex2d(x + sX, y);
		glVertex2d(x + sX, y + sY);
		glVertex2d(x, y + sY);

		glEnd();
	}
	
	public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		glBegin(GL_TRIANGLE_FAN);

		glVertex2d(x1, y1);
		glVertex2d(x2, y2);
		glVertex2d(x3, y3);
		glVertex2d(x4, y4);

		glEnd();
	}

	public void drawRect(double x, double y, double sX, double sY)
	{
		glBegin(GL_LINES);
		glVertex2d(x, y);
		glVertex2d(x + sX, y);
		glEnd();

		glBegin(GL_LINES);
		glVertex2d(x, y);
		glVertex2d(x, y + sY);
		glEnd();

		glBegin(GL_LINES);
		glVertex2d(x, y + sY);
		glVertex2d(x + sX, y + sY);
		glEnd();

		glBegin(GL_LINES);
		glVertex2d(x + sX, y);
		glVertex2d(x + sX, y + sY);
		glEnd();
	}

	public void drawImage(double x, double y, double sX, double sY, String image, boolean scaled)
	{
		drawImage(x, y, sX, sY, 0, 0, 1, 1, image, scaled);
	}
	
	protected void createImage(String image)
	{
		try
		{
			InputStream in;

			
			in = getClass().getResourceAsStream(image);
			
			if (in == null)
				in = getClass().getResourceAsStream("/missing.png");

			try 
			{
				PNGDecoder decoder = new PNGDecoder(in);

				ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
				decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
				buf.flip();

				glEnable(GL_TEXTURE_2D);
				glEnable(GL_BLEND);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				int id = glGenTextures();

				glBindTexture(GL_TEXTURE_2D, id);

				glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);		
				in.close();
				
				textures.put(image, id);
				textureSX.put(image, decoder.getWidth());
				textureSY.put(image, decoder.getHeight());

			}
			finally 
			{
				in.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
	{
		if (!textures.containsKey(image))
			createImage(image);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, absoluteWidth, absoluteHeight, 0, 0, 1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glEnable(GL_TEXTURE_2D); 
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glBindTexture(GL_TEXTURE_2D, textures.get(image));

		double width = sX * (u2 - u1);
		double height = sY * (v2 - v1);
		
		if (scaled)
		{
			width *= textureSX.get(image);
			height *= textureSY.get(image);
		}

		glBegin(GL_TRIANGLE_FAN);
		glTexCoord2d(u1, v1);
		glVertex2d(x, y);
		glTexCoord2d(u1, v2);
		glVertex2d(x, y + height);
		glTexCoord2d(u2, v2);
		glVertex2d(x + width, y + height);
		glTexCoord2d(u2, v1);
		glVertex2d(x + width, y);

		glEnd();
		
		glMatrixMode(GL_PROJECTION);
		glDisable(GL_TEXTURE_2D);
	}
}
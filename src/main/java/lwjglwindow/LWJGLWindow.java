package lwjglwindow;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLWindow 
{
	public FontRenderer fontRenderer;

	// The window handle
	protected long window;
	public double absoluteWidth;
	public double absoluteHeight;
	public double absoluteDepth;

	public double absoluteMouseX;
	public double absoluteMouseY;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorA;

	protected double yaw = 0;
	protected double pitch = 0;
	protected double roll = 0;

	protected double xOffset = 0;
	protected double yOffset = 0;
	protected double zOffset = 0;

	public ArrayList<Integer> pressedKeys = new ArrayList<Integer>();
	public ArrayList<Integer> validPressedKeys = new ArrayList<Integer>();

	public ArrayList<Integer> pressedButtons = new ArrayList<Integer>();
	public ArrayList<Integer> validPressedButtons = new ArrayList<Integer>();

	public boolean validScrollUp;
	public boolean validScrollDown;

	public boolean mac = false;

	public boolean vsync;
	public boolean showMouseOnLaunch;

	protected ArrayList<Long> framesList = new ArrayList<Long>();
	protected ArrayList<Double> frameFrequencies = new ArrayList<Double>();
	protected long lastFrame = System.currentTimeMillis(); 
	public double frameFrequency = 1;

	protected HashMap<String, Integer> textures = new HashMap<String, Integer>();
	protected HashMap<String, Integer> textureSX = new HashMap<String, Integer>();
	protected HashMap<String, Integer> textureSY = new HashMap<String, Integer>();

	public String name;

	public IDrawer drawer;
	public IUpdater updater;
	public IWindowHandler windowHandler;

	public SoundPlayer soundPlayer;

	public boolean angled = false;

	public LWJGLWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
	{
		this.name = name;
		this.absoluteWidth = x;
		this.absoluteHeight = y;
		this.absoluteDepth = z;
		this.updater = u;
		this.drawer = d;
		this.vsync = vsync;
		this.windowHandler = w;
		this.showMouseOnLaunch = showMouse;
		this.soundPlayer = new SoundPlayer();

		if (System.getProperties().toString().contains("Mac OS X"))
			mac = true;
	}

	public long getWindow()
	{
		return this.window;
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
		window = glfwCreateWindow((int)this.absoluteWidth, (int)this.absoluteHeight, this.name, NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed,
		// repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> 
		{
			if (action == GLFW_PRESS)
			{
				pressedKeys.add(key);
				validPressedKeys.add(key);
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
		
		this.setShowCursor(this.showMouseOnLaunch);

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

			if (!mac)
				glViewport(0, 0, (int)absoluteWidth, (int)absoluteHeight);

			setPerspective();
			
			//glOrtho(0, absoluteWidth, absoluteHeight, 0, 0, 1);

			glMatrixMode(GL_MODELVIEW);
			
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

			frameFrequency = Math.max(0, totalFrequency / frameFrequencies.size());
		}

		this.windowHandler.onWindowClose();
		this.soundPlayer.exit();
		System.exit(0);
	}
	
	public void setShowCursor(boolean show) 
	{
		int mouse = GLFW.GLFW_CURSOR_HIDDEN;
		if (show)
			mouse = GLFW.GLFW_CURSOR_NORMAL;
		
 		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, mouse);
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
	
	public void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest)
	{
		if (depthTest)
		{
			glEnable(GL_DEPTH_TEST);
			glDepthMask(false);
		}

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + 5);

		glBegin(GL_TRIANGLE_FAN);
		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glVertex3d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2, z);
		}

		glEnd();

		if (depthTest)
		{
			glDepthMask(true);
			glDisable(GL_DEPTH_TEST);
		}
	}

	public void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest)
	{
		if (depthTest)
		{
			glEnable(GL_DEPTH_TEST);
			glDepthMask(false);
		}

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + 5);

		glTranslated(x, y, z);
		this.addAngle(-this.yaw, -this.pitch, -this.roll);

		glBegin(GL_TRIANGLE_FAN);
		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glVertex3d(Math.cos(i) * sX / 2, Math.sin(i) * sY / 2, 0);
		}

		glEnd();

		setPerspective();

		if (depthTest)
		{
			glDepthMask(true);
			glDisable(GL_DEPTH_TEST);
		}
	}

	public void setColor(double r, double g, double b, double a)
	{
		glColor4d(r / 255, g / 255, b / 255, a / 255);
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = a / 255;
	}

	public void setColor(double r, double g, double b)
	{
		glColor3d(r / 255, g / 255, b / 255);
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = 1;
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
	
	public void drawOval(double x, double y, double z, double sX, double sY)
	{		
		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + 5);

		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glBegin(GL_LINES);
			glVertex3d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2, z);
			glVertex3d(x + Math.cos(i + Math.PI * 2 / sides) * sX / 2, y + Math.sin(i + Math.PI * 2 / sides) * sY / 2, z);
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

	public void fillBox(double x, double y, double z, double sX, double sY, double sZ)
	{
		fillBox(x, y, z, sX, sY, sZ, (byte) 0);
	}
	
	/**
	 * Options byte:
	 * 
	 * 0: default
	 * 
	 * +1 hide behind face
	 * +2 hide front face
	 * +4 hide bottom face
	 * +8 hide top face
	 * +16 hide left face
	 * +32 hide right face
	 * 
	 * +64 draw on top
	 * */
	public void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options)
	{
		glEnable(GL_DEPTH_TEST);

		if ((options >> 6) % 2 == 0)
			glDepthFunc(GL_LESS);
		else
			glDepthFunc(GL_ALWAYS);

		GL11.glBegin(GL11.GL_QUADS);

		if (options % 2 == 0)
		{
			GL11.glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);           
			GL11.glVertex3d(x + sX, y, z);
			GL11.glVertex3d(x, y, z);
			GL11.glVertex3d(x, y + sY, z);
			GL11.glVertex3d(x + sX, y + sY, z);
		}

		if ((options >> 2) % 2 == 0)
		{
			GL11.glColor4d(this.colorR * 0.8, this.colorG * 0.8, this.colorB * 0.8, this.colorA);           
			GL11.glVertex3d(x + sX, y + sY, z + sZ);        
			GL11.glVertex3d(x, y + sY, z + sZ);        
			GL11.glVertex3d(x, y + sY, z);
			GL11.glVertex3d(x + sX, y + sY, z);  
		}

		if ((options >> 3) % 2 == 0)
		{
			GL11.glColor4d(this.colorR * 0.8, this.colorG * 0.8, this.colorB * 0.8, this.colorA);           
			GL11.glVertex3d(x + sX, y , z + sZ);
			GL11.glVertex3d(x, y, z + sZ);
			GL11.glVertex3d(x, y, z);
			GL11.glVertex3d(x + sX, y, z);
		}

		if ((options >> 4) % 2 == 0)
		{
			GL11.glColor4d(this.colorR * 0.6, this.colorG * 0.6, this.colorB * 0.6, this.colorA);           
			GL11.glVertex3d(x, y + sY, z + sZ);
			GL11.glVertex3d(x, y + sY, z);
			GL11.glVertex3d(x, y, z);
			GL11.glVertex3d(x, y, z + sZ);
		}

		if ((options >> 5) % 2 == 0)
		{
			GL11.glColor4d(this.colorR * 0.6, this.colorG * 0.6, this.colorB * 0.6, this.colorA);           
			GL11.glVertex3d(x + sX, y + sY, z);
			GL11.glVertex3d(x + sX, y + sY, z + sZ);
			GL11.glVertex3d(x + sX, y, z + sZ);
			GL11.glVertex3d(x + sX, y, z);
		}

		if ((options >> 1) % 2 == 0)
		{
			GL11.glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);           
			GL11.glVertex3d(x + sX, y + sY, z + sZ);
			GL11.glVertex3d(x, y + sY, z + sZ);
			GL11.glVertex3d(x, y, z + sZ);
			GL11.glVertex3d(x + sX, y, z + sZ);
		}
		
		GL11.glEnd();
		glDisable(GL_DEPTH_TEST);
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
	
	public void fillQuadBox(double x1, double y1,
			double x2, double y2,
			double x3, double y3,
			double x4, double y4,
			double z, double sZ,
			byte options)
	{    
		glEnable(GL_DEPTH_TEST);
		
		if ((options >> 6) % 2 == 0)
			glDepthFunc(GL_LESS);  
		else
			glDepthFunc(GL_ALWAYS);  

		if (options % 2 == 0)
		{
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);    
			GL11.glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);           
			GL11.glVertex3d(x1, y1, z);
			GL11.glVertex3d(x2, y2, z);
			GL11.glVertex3d(x3, y3, z);
			GL11.glVertex3d(x4, y4, z);
			GL11.glEnd();    
		}

		if ((options >> 2) % 2 == 0)
		{
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);    
			GL11.glColor4d(this.colorR * 0.6, this.colorG * 0.6, this.colorB * 0.6, this.colorA);           
			GL11.glVertex3d(x1, y1, z + sZ);        
			GL11.glVertex3d(x2, y2, z + sZ);        
			GL11.glVertex3d(x2, y2, z);  
			GL11.glVertex3d(x1, y1, z);
			GL11.glEnd();    
		}

		if ((options >> 3) % 2 == 0)
		{
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);    
			GL11.glColor4d(this.colorR * 0.6, this.colorG * 0.6, this.colorB * 0.6, this.colorA);           
			GL11.glVertex3d(x3, y3, z + sZ);
			GL11.glVertex3d(x4, y4, z + sZ);
			GL11.glVertex3d(x4, y4, z);
			GL11.glVertex3d(x3, y3, z);
			GL11.glEnd();    
		}
		
		if ((options >> 4) % 2 == 0)
		{
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);    
			GL11.glColor4d(this.colorR * 0.8, this.colorG * 0.8, this.colorB * 0.8, this.colorA);           
			GL11.glVertex3d(x1, y1, z + sZ);
			GL11.glVertex3d(x4, y4, z + sZ);
			GL11.glVertex3d(x4, y4, z);
			GL11.glVertex3d(x1, y1, z);
			GL11.glEnd();    
		}
		
		if ((options >> 5) % 2 == 0)
		{
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);    
			GL11.glColor4d(this.colorR * 0.6, this.colorG * 0.6, this.colorB * 0.6, this.colorA);           
			GL11.glVertex3d(x3, y3, z + sZ);
			GL11.glVertex3d(x2, y2, z + sZ);
			GL11.glVertex3d(x2, y2, z);
			GL11.glVertex3d(x3, y3, z);
			GL11.glEnd();    
		}

		if ((options >> 1) % 2 == 0)
		{
			GL11.glBegin(GL11.GL_TRIANGLE_FAN);    
			GL11.glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);           
			GL11.glVertex3d(x1, y1, z + sZ);
			GL11.glVertex3d(x2, y2, z + sZ);
			GL11.glVertex3d(x3, y3, z + sZ);
			GL11.glVertex3d(x4, y4, z + sZ);
			GL11.glEnd();    
		}
		
		glDisable(GL_DEPTH_TEST);
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
	
	public void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled)
	{
		drawImage(x, y, z, sX, sY, 0, 0, 1, 1, image, scaled);
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

	public void setAngles(double y, double p, double r)
	{
		this.yaw = y;
		this.pitch = p;
		this.roll = r;

		this.angled = !(this.yaw == 0 && this.pitch == 0 && this.roll == 0);
		this.setPerspective();
	}

	public void setOffsets(double x, double y, double z)
	{
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
		this.setPerspective();
	}

	public void addAngle(double yaw, double pitch, double roll)
	{
		glMultMatrixd(new double[]{Math.cos(roll), -Math.sin(roll), 0, 0,  Math.sin(roll), Math.cos(roll), 0, 0,  0, 0, 1, 0,  0, 0, 0, 1});
		glMultMatrixd(new double[]{1, 0, 0, 0,  0, Math.cos(pitch), -Math.sin(pitch), 0,  0, Math.sin(pitch), Math.cos(pitch), 0,  0, 0, 0, 1});
		glMultMatrixd(new double[]{Math.cos(yaw), 0, -Math.sin(yaw), 0,  0, 1, 0, 0,  Math.sin(yaw), 0, Math.cos(yaw), 0,  0, 0, 0, 1});
	}

	public void setPerspective()
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glFrustum(-absoluteWidth / (absoluteDepth * 2.0), absoluteWidth / (absoluteDepth * 2.0), absoluteHeight / (absoluteDepth * 2.0), -absoluteHeight / (absoluteDepth * 2.0), 1, absoluteDepth * 2);

		glTranslated(0, 0, -absoluteDepth);

		glMultMatrixd(new double[]{Math.cos(this.roll), -Math.sin(this.roll), 0, 0,  Math.sin(this.roll), Math.cos(this.roll), 0, 0,  0, 0, 1, 0,  0, 0, 0, 1});
		glMultMatrixd(new double[]{1, 0, 0, 0,  0, Math.cos(this.pitch), -Math.sin(this.pitch), 0,  0, Math.sin(this.pitch), Math.cos(this.pitch), 0,  0, 0, 0, 1});
		glMultMatrixd(new double[]{Math.cos(this.yaw), 0, -Math.sin(this.yaw), 0,  0, 1, 0, 0,  Math.sin(this.yaw), 0, Math.cos(this.yaw), 0,  0, 0, 0, 1});

		glTranslated(absoluteWidth * (-0.5 + xOffset), absoluteHeight * (-0.5 + yOffset), absoluteDepth * zOffset);


		//glOrtho(0, absoluteWidth, absoluteHeight, 0, -1, 1);
	}

	public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
	{
		if (!textures.containsKey(image))
			createImage(image);

		setPerspective();
		
		glMatrixMode(GL_MODELVIEW);
		//glLoadIdentity();
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

	public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
	{
		this.drawImage(x, y, z, sX, sY, u1, v1, u2, v2, image, scaled, true);
	}

	public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest)
	{
		if (!textures.containsKey(image))
			createImage(image);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		if (depthtest)
			glEnable(GL_DEPTH_TEST);
		
		//glFrustum(-absoluteWidth / (absoluteDepth * 2.0), absoluteWidth / (absoluteDepth * 2.0), absoluteHeight / (absoluteDepth * 2.0), -absoluteHeight / (absoluteDepth * 2.0), 1, absoluteDepth * 2);
		//glTranslated(-absoluteWidth / 2, -absoluteHeight / 2, -absoluteDepth);
		setPerspective();

		glMatrixMode(GL_MODELVIEW);
		//glLoadIdentity();
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
		glVertex3d(x, y, z);
		glTexCoord2d(u1, v2);
		glVertex3d(x, y + height, z);
		glTexCoord2d(u2, v2);
		glVertex3d(x + width, y + height, z);
		glTexCoord2d(u2, v1);
		glVertex3d(x + width, y, z);

		glEnd();

		glMatrixMode(GL_PROJECTION);
		glDisable(GL_TEXTURE_2D);

		if (depthtest)
			glDisable(GL_DEPTH_TEST);
	}
}
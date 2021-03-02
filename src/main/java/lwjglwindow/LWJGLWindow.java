package lwjglwindow;

import basewindow.*;
import basewindow.transformation.Transformation;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL10.AL_PITCH;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_ONE_MINUS_CONSTANT_COLOR;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLWindow extends BaseWindow
{
	protected long window;
	protected GLFWVidMode vidmode;

	protected String audioDevice;

	public double colorR;
	public double colorG;
	public double colorB;
	public double colorA;
	public double glow;

	protected double[] mx = new double[1];
	protected double[] my = new double[1];

	protected int[] w = new int[1];
	protected int[] h = new int[1];

	protected int[] prevPosX = new int[1];
	protected int[] prevPosY = new int[1];

	protected int[] prevSizeX = new int[1];
	protected int[] prevSizeY = new int[1];

	protected HashMap<String, Integer> textures = new HashMap<String, Integer>();
	protected HashMap<String, Integer> textureSX = new HashMap<String, Integer>();
	protected HashMap<String, Integer> textureSY = new HashMap<String, Integer>();

	public boolean batchMode = false;

	protected boolean useShader = false;

	protected int textureFlag;
	protected int depthFlag;
	protected int glowFlag;

	public int lightFlag;
	public int glowLightFlag;
	public int shadowFlag;
	public int glowShadowFlag;

	public ShadowMap shadowMap;

	double bbx1 = 1;
	double bby1 = 0;
	double bbz1 = 0;
	double bbx2 = 0;
	double bby2 = 1;
	double bbz2 = 0;
	double bbx3 = 0;
	double bby3 = 0;
	double bbz3 = 1;

	public LWJGLWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
	{
		super(name, x, y, z, u, d, w, vsync, showMouse);

		this.audioDevice = ALC11.alcGetString(NULL, ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);

		try
		{
			this.soundPlayer = new SoundPlayer();
			this.soundsEnabled = true;
		}
		catch (Exception e)
		{
			this.soundsEnabled = false;
			System.out.println("Failed to enable sounds");
			e.printStackTrace();
		}

		if (System.getProperty("os.name").toLowerCase().contains("mac"))
			this.mac = true;

		this.os = System.getProperty("os.name").toLowerCase();

		this.antialiasingSupported = true;
	}

	public long getWindow()
	{
		return this.window;
	}

	public void run()
	{
		init();
		loop();

		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	protected void init()
	{
		this.fontRenderer = new FontRenderer(this, "/font.png");

		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		if (antialiasingEnabled)
			glfwWindowHint(GLFW_SAMPLES, 4);

		window = glfwCreateWindow((int) this.absoluteWidth, (int) this.absoluteHeight, this.name, NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
		{
			if (action == GLFW_PRESS)
			{
				pressedKeys.add(key);
				validPressedKeys.add(key);

				textPressedKeys.add(key);
				textValidPressedKeys.add(key);
			}
			else if (action == GLFW_RELEASE)
			{
				pressedKeys.remove((Integer) key);
				validPressedKeys.remove((Integer) key);

				textPressedKeys.remove((Integer) key);
				textValidPressedKeys.remove((Integer) key);
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
				pressedButtons.remove((Integer) button);
				validPressedButtons.remove((Integer) button);
			}
		});

		try (MemoryStack stack = stackPush())
		{
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			glfwGetWindowSize(window, pWidth, pHeight);
			vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		}

		glfwMakeContextCurrent(window);

		this.setShowCursor(this.showMouseOnLaunch);

		if (vsync)
			GLFW.glfwSwapInterval(1);
		else
			GLFW.glfwSwapInterval(0);

		glfwShowWindow(window);
	}

	protected int createShader(String filename, int shaderType) throws Exception
	{
		int shader = 0;
		try
		{
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if (shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		}
		catch (Exception exc)
		{
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}

	protected static String getLogInfo(int obj)
	{
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	protected String readFileAsString(String filename) throws Exception
	{
		StringBuilder source = new StringBuilder();

		InputStream in = getClass().getResourceAsStream(filename);

		Exception exception = null;

		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			Exception innerExc = null;
			try
			{
				String line;
				while ((line = reader.readLine()) != null)
					source.append(line).append('\n');
			}
			catch (Exception exc)
			{
				exception = exc;
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (Exception exc)
				{
					if (innerExc == null)
						innerExc = exc;
					else
						exc.printStackTrace();
				}
			}

			if (innerExc != null)
				throw innerExc;
		}
		catch (Exception exc)
		{
			exception = exc;
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception exc)
			{
				if (exception == null)
					exception = exc;
				else
					exc.printStackTrace();
			}

			if (exception != null)
				throw exception;
		}

		return source.toString();
	}


	protected void loop()
	{
		GL.createCapabilities();

		this.shadowMap = new ShadowMap(this);

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		while (!glfwWindowShouldClose(window))
		{
			this.startTiming();

			String audio = ALC11.alcGetString(NULL, ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);

			if (!(audio == null && this.audioDevice == null || (this.audioDevice != null && this.audioDevice.equals(audio))))
			{
				this.soundPlayer = new SoundPlayer();
			}

			this.audioDevice = audio;

			SoundPlayer soundPlayer = (SoundPlayer) this.soundPlayer;

			if (soundPlayer != null)
			{
				soundPlayer.musicPlaying = soundPlayer.currentMusic != -1 && AL10.alGetSourcef(soundPlayer.currentMusic, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;

				if (soundPlayer.prevMusic != -1 && soundPlayer.fadeEnd < System.currentTimeMillis())
				{
					AL10.alSourceStop(soundPlayer.prevMusic);
					soundPlayer.prevMusic = -1;

					if (soundPlayer.currentMusic != -1)
						AL10.alSourcef(soundPlayer.currentMusic, AL10.AL_GAIN, soundPlayer.currentVolume);
				}

				if (soundPlayer.prevMusic != -1 && soundPlayer.currentMusic != -1)
				{
					double frac = (System.currentTimeMillis() - soundPlayer.fadeBegin) * 1.0 / (soundPlayer.fadeEnd - soundPlayer.fadeBegin);

					AL10.alSourcef(soundPlayer.prevMusic, AL10.AL_GAIN, (float) (soundPlayer.prevVolume * (1 - frac)));
					AL10.alSourcef(soundPlayer.currentMusic, AL10.AL_GAIN, (float) (soundPlayer.currentVolume * frac));
				}

				if (soundPlayer.musicsToLoad)
				{
					synchronized (soundPlayer.finishedMusicBuffers)
					{
						for (String path : soundPlayer.finishedMusicBuffers.keySet())
						{
							soundPlayer.musicBuffers.put(path, soundPlayer.finishedMusicBuffers.get(path));
						}
					}
				}
			}

			this.updater.update();

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glfwGetWindowSize(window, w, h);
			absoluteWidth = w[0];
			absoluteHeight = h[0];

			glfwGetCursorPos(window, mx, my);
			absoluteMouseX = mx[0];
			absoluteMouseY = my[0];

			glfwGetFramebufferSize(window, w, h);

			if (useShader)
			{
				this.shadowMap.renderShadowMap();
				this.shadowMap.renderNormal();
			}
			else
			{
				if (!mac)
					glViewport(0, 0, (int) absoluteWidth, (int) absoluteHeight);

				loadPerspective();
				this.drawer.draw();
			}

			glfwSwapBuffers(window);
			glfwPollEvents();

			this.stopTiming();
		}

		this.windowHandler.onWindowClose();

		if (this.soundsEnabled)
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

	public void setCursorLocked(boolean locked)
	{
		int mouse = GLFW_CURSOR_DISABLED;
		if (!locked)
			mouse = GLFW_CURSOR_NORMAL;

		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, mouse);
	}

	public void setCursorPos(double x, double y)
	{
		GLFW.glfwSetCursorPos(window, x, y);
	}

	public void setFullscreen(boolean enabled)
	{
		this.fullscreen = enabled;

		this.pressedButtons.clear();
		this.validPressedButtons.clear();

		if (enabled)
		{
			glfwGetWindowSize(this.window, this.prevSizeX, this.prevSizeY);
			glfwGetWindowPos(this.window, this.prevPosX, this.prevPosY);
			glfwSetWindowMonitor(this.window, glfwGetPrimaryMonitor(), 0, 0, this.vidmode.width(), this.vidmode.height(), this.vidmode.refreshRate());
		}
		else
			glfwSetWindowMonitor(this.window, NULL, this.prevPosX[0], this.prevPosY[0], this.prevSizeX[0], this.prevSizeY[0], this.vidmode.refreshRate());

		this.setVsync(this.vsync);
	}

	public void fillOval(double x, double y, double sX, double sY)
	{
		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY) / 4 + 5;

		glBegin(GL_TRIANGLE_FAN);
		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
		}

		glEnd();
	}

	public void fillGlow(double x, double y, double sX, double sY, boolean shade)
	{
		this.fillGlow(x, y, sX, sY, shade, false);
	}

	public void fillGlow(double x, double y, double sX, double sY, boolean shade, boolean light)
	{
		if (this.drawingShadow)
			return;

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY) / 16 + 5;

		if (!shade)
			this.setGlowBlendFunc();

		if (light)
			this.setLightBlendFunc();

		glBegin(GL_TRIANGLES);
		double step = Math.PI * 2 / sides;

		double pX = x + Math.cos(0) * sX / 2;
		double pY = y + Math.sin(0) * sY / 2;
		double d = 0;
		for (int n = 0; n < sides; n++)
		{
			d += step;

			if (!shade)
				glColor3d(0, 0, 0);
			else
				glColor4d(this.colorR, this.colorG, this.colorB, 0);

			glVertex2d(pX, pY);
			pX = x + Math.cos(d) * sX / 2;
			pY = y + Math.sin(d) * sY / 2;
			glVertex2d(pX, pY);

			if (!shade)
				glColor3d(this.colorR * this.colorA, this.colorG * this.colorA, this.colorB * this.colorA);
			else
				glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

			glVertex2d(x, y);
		}

		glEnd();

		if (!shade)
			this.setTransparentBlendFunc();
	}

	@Override
	public void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest)
	{
		if (depthTest)
		{
			enableDepthtest();

			if (colorA < 1)
				glDepthMask(false);
		}

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + Math.max(z / 20, 0)) / 4 + 5;

		glBegin(GL_TRIANGLE_FAN);
		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			glVertex3d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2, z);
		}

		glEnd();

		if (depthTest)
		{
			glDepthMask(true);
			disableDepthtest();
		}
	}

	@Override
	public void fillPartialOval(double x, double y, double sX, double sY, double start, double end)
	{
		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY) / 4 + 5;

		glBegin(GL_TRIANGLES);
		for (double i = start; i < end; i += (end - start) / sides)
		{
			glVertex2d(x + Math.cos(i) * sX / 2, y + Math.sin(i) * sY / 2);
			glVertex2d(x + Math.cos(i + 1) * sX / 2, y + Math.sin(i + 1) * sY / 2);
			glVertex2d(x, y);
		}

		glEnd();
	}

	public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade)
	{
		this.fillGlow(x, y, z, sX, sY, depthTest, shade, false);
	}

	public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light)
	{
		if (this.drawingShadow)
			return;

		if (depthTest)
		{
			enableDepthtest();
			glDepthMask(false);
		}

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + Math.max(z / 20, 0)) / 16 + 5;

		if (!shade)
			this.setGlowBlendFunc();

		if (light)
			this.setLightBlendFunc();

		glBegin(GL_TRIANGLES);
		double step = Math.PI * 2 / sides;

		double pX = x + Math.cos(0) * sX / 2;
		double pY = y + Math.sin(0) * sY / 2;
		double d = 0;
		for (int n = 0; n < sides; n++)
		{
			d += step;

			if (!shade)
				glColor3d(0, 0, 0);
			else
				glColor4d(this.colorR, this.colorG, this.colorB, 0);

			glVertex3d(pX, pY, z);
			pX = x + Math.cos(d) * sX / 2;
			pY = y + Math.sin(d) * sY / 2;
			glVertex3d(pX, pY, z);

			if (!shade)
				glColor3d(this.colorR * this.colorA, this.colorG * this.colorA, this.colorB * this.colorA);
			else
				glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

			glVertex3d(x, y, z);
		}

		glEnd();

		if (!shade)
			this.setTransparentBlendFunc();

		if (depthTest)
		{
			glDepthMask(true);
			disableDepthtest();
		}
	}

	public void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest)
	{
		if (depthTest)
		{
			enableDepthtest();

			if (colorA < 1)
				glDepthMask(false);
		}

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + Math.max(z / 20, 0)) / 4 + 5;

		glBegin(GL_TRIANGLE_FAN);
		for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
		{
			double ox = Math.cos(i) * sX / 2;
			double oy = Math.sin(i) * sY / 2;
			glVertex3d(x + ox * bbx1 + oy * bbx2, y + ox * bby1 + oy * bby2, z + ox * bbz1 + oy * bbz2);
		}

		glEnd();

		if (depthTest)
		{
			glDepthMask(true);
			disableDepthtest();
		}
	}

	@Override
	public void fillGlow(double x, double y, double sX, double sY)
	{
		this.fillGlow(x, y, sX, sY, false);
	}

	@Override
	public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest)
	{
		this.fillGlow(x, y, z, sX, sY, false, false);
	}

	@Override
	public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest)
	{
		this.fillFacingGlow(x, y, z, sX, sY, depthTest,false);
	}

	public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade)
	{
		this.fillFacingGlow(x, y, z, sX, sY, depthTest, shade,false);
	}

	public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light)
	{
		if (this.drawingShadow)
			return;

		if (depthTest)
		{
			enableDepthtest();
			glDepthMask(false);
		}

		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY + Math.max(z / 20, 0)) / 16 + 5;

		if (!shade)
			this.setGlowBlendFunc();

		if (light)
			this.setLightBlendFunc();

		glBegin(GL_TRIANGLES);
		double step = Math.PI * 2 / sides;

		double ox = Math.cos(0) * sX / 2;
		double oy = Math.sin(0) * sY / 2;
		double d = 0;
		for (int n = 0; n < sides; n++)
		{
			d += step;

			if (!shade)
				glColor3d(0, 0, 0);
			else
				glColor4d(this.colorR, this.colorG, this.colorB, 0);

			glVertex3d(x + ox * bbx1 + oy * bbx2, y + ox * bby1 + oy * bby2, z + ox * bbz1 + oy * bbz2);
			ox = Math.cos(d) * sX / 2;
			oy = Math.sin(d) * sY / 2;
			glVertex3d(x + ox * bbx1 + oy * bbx2, y + ox * bby1 + oy * bby2, z + ox * bbz1 + oy * bbz2);

			if (!shade)
				glColor3d(this.colorR * this.colorA, this.colorG * this.colorA, this.colorB * this.colorA);
			else
				glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

			glVertex3d(x, y, z);
		}

		glEnd();

		if (!shade)
			this.setTransparentBlendFunc();

		if (depthTest)
		{
			glDepthMask(true);
			disableDepthtest();
		}
	}

	public void setColor(double r, double g, double b, double a, double glow)
	{
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = a / 255;
		this.glow = glow;

		glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

		if (useShader)
			GL20.glUniform1f(glowFlag, (float) glow);
	}

	public void setColor(double r, double g, double b, double a)
	{
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = a / 255;
		glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

		if (useShader)
			GL20.glUniform1f(glowFlag, 0);
	}

	public void setColor(double r, double g, double b)
	{
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = 1;
		glColor3d(this.colorR, this.colorG, this.colorB);

		if (useShader)
			GL20.glUniform1f(glowFlag, 0);
	}

	public void drawOval(double x, double y, double sX, double sY)
	{
		x += sX / 2;
		y += sY / 2;

		int sides = (int) (sX + sY) / 4 + 5;

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

		int sides = (int) (sX + sY + Math.max(z / 20, 0)) / 4 + 5;

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
		if (!batchMode)
		{
			enableDepthtest();

			if (colorA < 1)
				glDepthMask(false);

			if ((options >> 6) % 2 == 0)
				glDepthFunc(GL_LEQUAL);
			else
				glDepthFunc(GL_ALWAYS);

			GL11.glBegin(GL11.GL_QUADS);
		}

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

		if (!batchMode)
		{
			GL11.glEnd();
			disableDepthtest();
			glDepthMask(true);
		}
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
		enableDepthtest();

		if ((options >> 6) % 2 == 0)
			glDepthFunc(GL_LEQUAL);
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

		disableDepthtest();
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

				enableTexture();
				glEnable(GL_BLEND);
				this.setTransparentBlendFunc();
				int id = glGenTextures();

				glBindTexture(GL_TEXTURE_2D, id);

				glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

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

	public void setIcon(String icon)
	{
		InputStream in;

		in = getClass().getResourceAsStream(icon);

		if (in == null)
			in = getClass().getResourceAsStream("/missing.png");

		try
		{
			PNGDecoder decoder = new PNGDecoder(in);

			ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();

			GLFWImage image = GLFWImage.malloc();
			GLFWImage.Buffer imagebuf = GLFWImage.malloc(1);
			image.set(decoder.getWidth(), decoder.getHeight(), buf);
			imagebuf.put(0, image);
			glfwSetWindowIcon(window, imagebuf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected String loadResource(String fileName)
	{
		String result = null;
		try (InputStream in = getClass().getResourceAsStream(fileName);
			 Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name()))
		{
			result = scanner.useDelimiter("\\A").next();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public void setUpPerspective()
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		if (this.drawingShadow)
			glOrtho(0, absoluteWidth, absoluteHeight, 0, -absoluteDepth, absoluteDepth);
		else
			glFrustum(-absoluteWidth / (absoluteDepth * 2.0), absoluteWidth / (absoluteDepth * 2.0), absoluteHeight / (absoluteDepth * 2.0), -absoluteHeight / (absoluteDepth * 2.0), 1, absoluteDepth * 100);

		this.angled = false;

		this.yaw = 0;
		this.pitch = 0;
		this.roll = 0;
		this.xOffset = 0;
		this.yOffset = 0;
		this.zOffset = 0;

		this.bbx1 = 1;
		this.bby1 = 0;
		this.bbz1 = 0;
		this.bbx2 = 0;
		this.bby2 = 1;
		this.bbz2 = 0;
		this.bbx3 = 0;
		this.bby3 = 0;
		this.bbz3 = 1;
	}

	public void applyTransformations()
	{
		for (int i = this.transformations.size() - 1; i >= 0; i--)
		{
			this.transformations.get(i).apply();
		}
	}

	public void applyShadowTransformations()
	{
		for (int i = this.transformations.size() - 1; i >= 0; i--)
		{
			Transformation t = this.transformations.get(i);

			if (t.applyAsShadow)
				t.apply();
			else
				t.applyToWindow();
		}
	}

	public void loadPerspective()
	{
		setUpPerspective();

		if (this.drawingShadow)
		{
			applyShadowTransformations();
			for (Transformation t: this.lightBaseTransformation)
				t.apply();
		}
		else
		{
			applyTransformations();
			for (Transformation t: this.baseTransformations)
				t.apply();

			if (this.useShader)
			{
				float[] projMatrix = new float[16];
				glGetFloatv(GL_PROJECTION_MATRIX, projMatrix);

				glUniformMatrix4fv(this.shadowMap.normalProgramVPUniform, false, projMatrix);
			}
		}
	}

	public void drawImage(double x, double y, double sX, double sY, String image, boolean scaled)
	{
		drawImage(x, y, sX, sY, 0, 0, 1, 1, image, scaled);
	}

	public void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled)
	{
		drawImage(x, y, z, sX, sY, 0, 0, 1, 1, image, scaled);
	}

	public void drawImage(double x, double y, double sX, double sY, String image, double rotation, boolean scaled)
	{
		drawImage(x, y, sX, sY, 0, 0, 1, 1, image, rotation, scaled);
	}

	public void drawImage(double x, double y, double z, double sX, double sY, String image, double rotation, boolean scaled)
	{
		drawImage(x, y, z, sX, sY, 0, 0, 1, 1, image, rotation, scaled);
	}

	public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
	{
		if (this.drawingShadow)
			return;

		if (!textures.containsKey(image))
			createImage(image);

		loadPerspective();

		glMatrixMode(GL_MODELVIEW);
		enableTexture();

		glEnable(GL_BLEND);
		this.setTransparentBlendFunc();

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
		disableTexture();

		if (useShader)
			GL20.glUniform1i(textureFlag, 0);
	}

	public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled)
	{
		if (this.drawingShadow)
			return;

		if (!textures.containsKey(image))
			createImage(image);

		loadPerspective();

		glMatrixMode(GL_MODELVIEW);
		enableTexture();

		glEnable(GL_BLEND);
		this.setTransparentBlendFunc();

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
		glVertex2d(rotateX(-width / 2, -height / 2, x, rotation), rotateY(-width / 2, -height / 2, y, rotation));
		glTexCoord2d(u1, v2);
		glVertex2d(rotateX(width / 2, -height / 2, x, rotation), rotateY(width / 2, -height / 2, y, rotation));
		glTexCoord2d(u2, v2);
		glVertex2d(rotateX(width / 2, height / 2, x, rotation), rotateY(width / 2, height / 2, y, rotation));
		glTexCoord2d(u2, v1);
		glVertex2d(rotateX(-width / 2, height / 2, x, rotation), rotateY(-width / 2, height / 2, y, rotation));

		glEnd();

		glMatrixMode(GL_PROJECTION);
		disableTexture();
	}

	public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
	{
		this.drawImage(x, y, z, sX, sY, u1, v1, u2, v2, image, scaled, true);
	}

	public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest)
	{
		if (this.drawingShadow)
			return;

		if (!textures.containsKey(image))
			createImage(image);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		if (depthtest)
			enableDepthtest();

		loadPerspective();

		glMatrixMode(GL_MODELVIEW);

		enableTexture();
		glEnable(GL_BLEND);
		this.setTransparentBlendFunc();

		glDepthMask(false);

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
		disableTexture();

		glDepthMask(true);

		if (depthtest)
			disableDepthtest();
	}

	public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled)
	{
		this.drawImage(x, y, z, sX, sY, u1, v1, u2, v2, image, rotation, scaled, true);
	}

	public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled, boolean depthtest)
	{
		if (this.drawingShadow)
			return;

		if (!textures.containsKey(image))
			createImage(image);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		if (depthtest)
			enableDepthtest();

		loadPerspective();
		glMatrixMode(GL_MODELVIEW);

		enableTexture();
		glEnable(GL_BLEND);
		this.setTransparentBlendFunc();

		glDepthMask(false);

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
		glVertex3d(rotateX(-width / 2, -height / 2, x, rotation), rotateY(-width / 2, -height / 2, y, rotation), z);
		glTexCoord2d(u1, v2);
		glVertex3d(rotateX(width / 2, -height / 2, x, rotation), rotateY(width / 2, -height / 2, y, rotation), z);
		glTexCoord2d(u2, v2);
		glVertex3d(rotateX(width / 2, height / 2, x, rotation), rotateY(width / 2, height / 2, y, rotation), z);
		glTexCoord2d(u2, v1);
		glVertex3d(rotateX(-width / 2, height / 2, x, rotation), rotateY(-width / 2, height / 2, y, rotation), z);

		glEnd();

		glMatrixMode(GL_PROJECTION);
		disableTexture();

		glDepthMask(true);

		if (depthtest)
			disableDepthtest();
	}

	public double rotateX(double px, double py, double posX, double rotation)
	{
		return (px * Math.cos(rotation) - py * Math.sin(rotation)) + posX;
	}

	public double rotateY(double px, double py, double posY, double rotation)
	{
		return (py * Math.cos(rotation) + px * Math.sin(rotation)) + posY;
	}

	@Override
	public String getClipboard()
	{
		String s = GLFW.glfwGetClipboardString(window);

		if (s != null)
			return s;
		else
			return "";
	}

	@Override
	public void setClipboard(String s)
	{
		GLFW.glfwSetClipboardString(window, s);
	}

	@Override
	public void setVsync(boolean enable)
	{
		if (enable)
			GLFW.glfwSwapInterval(1);
		else
			GLFW.glfwSwapInterval(0);
	}

	@Override
	public ArrayList<Integer> getRawTextKeys()
	{
		return this.textValidPressedKeys;
	}

	@Override
	public String getKeyText(int key)
	{
		return getTextKeyText(key);
	}

	@Override
	public String getTextKeyText(int key)
	{
		String s = glfwGetKeyName(key, 0);

		if (s == null || (key >= InputCodes.KEY_KP_0 && key <= InputCodes.KEY_KP_EQUAL))
		{
			String s2 = BaseWindow.keyNames.get(key);
			if (s2 == null)
				return "Key " + key;
			else
				return s2;
		}
		else
			return s;
	}

	@Override
	public int translateKey(int key)
	{
		return key;
	}

	@Override
	public int translateTextKey(int key)
	{
		return key;
	}

	@Override
	public void transform(double[] matrix)
	{
		glMultMatrixd(matrix);
	}

	@Override
	public void calculateBillboard()
	{
		angled = !(yaw == 0 && pitch == 0 && roll == 0);

		double a = Math.cos(-roll);
		double b = Math.sin(-roll);
		double c = Math.cos(-pitch);
		double d = Math.sin(-pitch);
		double e = Math.cos(-yaw);
		double f = Math.sin(-yaw);

		bbx1 = e * a - b * d * f;
		bby1 = -a * d * f - e * b;
		bbz1 = -c * f;
		bbx2 = b * c;
		bby2 = a * c;
		bbz2 = -d;
		bbx3 = a * f + e * b * d;
		bby3 = e * a * d - b * f;
		bbz3 = e * c;
	}

	@Override
	public double getEdgeBounds()
	{
		return 0;
	}

	@Override
	public void setBatchMode(boolean enabled, boolean quads, boolean depth)
	{
		this.setBatchMode(enabled, quads, depth, false);
	}

	@Override
	public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow)
	{
		this.setBatchMode(enabled, quads, depth, glow, !(this.colorA < 1 || glow));
	}

	@Override
	public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow, boolean depthMask)
	{
		this.batchMode = enabled;

		if (enabled)
		{
			if (!depthMask)
				glDepthMask(false);

			if (depth)
			{
				enableDepthtest();
				glDepthFunc(GL_LEQUAL);
			}

			if (glow)
				this.setGlowBlendFunc();
			else
				this.setTransparentBlendFunc();

			if (quads)
				glBegin(GL_QUADS);
			else
				glBegin(GL_TRIANGLES);
		}
		else
		{
			GL11.glEnd();
			disableDepthtest();
			glDepthMask(true);
			this.setTransparentBlendFunc();
		}
	}

	@Override
	public void addVertex(double x, double y, double z)
	{
		glVertex3d(x, y, z);
	}

	@Override
	public void addVertex(double x, double y)
	{
		glVertex2d(x, y);
	}

	@Override
	public void openLink(URL url) throws Exception
	{
		String[] cmd;

		if (os.contains("win"))
			cmd = new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()};
		else if (os.contains("mac"))
			cmd = new String[]{"open", url.toString()};
		else
		{
			String s = url.toString();

			if ("file".equals(url.getProtocol()))
				s = s.replace("file:", "file://");

			cmd = new String[]{"xdg-open", s};
		}

		Runtime.getRuntime().exec(cmd);
	}

	@Override
	public void setShadowQuality(double quality)
	{
		if (quality <= 0)
		{
			this.shadowMap.quality = 1;
			this.useShader = false;
		}
		else
		{
			this.shadowMap.quality = quality;
			this.useShader = true;
		}
	}

	@Override
	public double getShadowQuality()
	{
		if (!this.useShader)
			return 0;
		else
			return this.shadowMap.quality;
	}

	@Override
	public void setLighting(double light, double glowLight, double shadow, double glowShadow)
	{
		if (useShader)
		{
			GL20.glUniform1f(this.lightFlag, (float) light);
			GL20.glUniform1f(this.glowLightFlag, (float) glowLight);
			GL20.glUniform1f(this.shadowFlag, (float) shadow);
			GL20.glUniform1f(this.glowShadowFlag, (float) glowShadow);
		}
	}

	public void enableTexture()
	{
		glEnable(GL_TEXTURE_2D);

		if (useShader)
		{
			GL20.glUniform1i(textureFlag, 1);
			GL20.glActiveTexture(GL13.GL_TEXTURE0);
		}
	}

	public void disableTexture()
	{
		glDisable(GL_TEXTURE_2D);

		if (useShader)
		{
			GL20.glUniform1i(textureFlag, 0);

			glEnable(GL_TEXTURE_2D);
			GL20.glActiveTexture(GL13.GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, this.shadowMap.depthTexture);
		}
	}

	public void enableDepthtest()
	{
		glEnable(GL_DEPTH_TEST);

		if (useShader)
			GL20.glUniform1i(depthFlag, 1);
	}

	public void disableDepthtest()
	{
		glDisable(GL_DEPTH_TEST);

		if (useShader)
			GL20.glUniform1i(depthFlag, 0);
	}

	public void setGlowBlendFunc()
	{
		glBlendFunc(GL_SRC_COLOR, GL_ONE);
	}

	public void setLightBlendFunc()
	{
		glBlendFunc(GL_DST_COLOR, GL_ONE);
	}

	public void setTransparentBlendFunc()
	{
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
}
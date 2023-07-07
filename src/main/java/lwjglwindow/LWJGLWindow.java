package lwjglwindow;

import basewindow.*;
import basewindow.transformation.Transformation;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.ALC11;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLWindow extends BaseWindow
{
	protected long window;
	protected GLFWVidMode vidmode;

	protected String audioDevice;

	protected double[] mx = new double[1];
	protected double[] my = new double[1];

	protected int[] w = new int[1];
	protected int[] h = new int[1];

	protected int[] prevPosX = new int[1];
	protected int[] prevPosY = new int[1];

	protected int[] prevSizeX = new int[1];
	protected int[] prevSizeY = new int[1];

	protected HashMap<String, Integer> textures = new HashMap<>();
	protected HashMap<String, Integer> textureSX = new HashMap<>();
	protected HashMap<String, Integer> textureSY = new HashMap<>();

	public boolean batchMode = false;
	public boolean batchQuads = false;
	public boolean batchDepth = false;
	public boolean batchDepthMask = false;
	public boolean batchGlow = false;

	protected boolean shadowsEnabled = false;

	protected boolean upscaleImages = false;

	protected int lightTex;

	public ShaderHandler shaderHandler;

	double bbx1 = 1;
	double bby1 = 0;
	double bbz1 = 0;
	double bbx2 = 0;
	double bby2 = 1;
	double bbz2 = 0;
	double bbx3 = 0;
	double bby3 = 0;
	double bbz3 = 1;

	public String currentTexture = null;

	ArrayList<double[]> scaledLights = new ArrayList<>();

	public LWJGLWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
	{
		super(name, x, y, z, u, d, w, vsync, showMouse);

		this.audioDevice = ALC11.alcGetString(NULL, ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);

		try
		{
			this.soundPlayer = new SoundPlayer(this);
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

		this.shapeDrawer = new ImmediateModeModelPart.ImmediateModeShapeDrawer(this);

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

			if (key == GLFW_KEY_BACKSPACE && (action == GLFW_PRESS || action == GLFW_REPEAT))
				this.inputCodepoints.add('\b');
		});

		glfwSetCharCallback(window, (window, codepoint) ->
		{
			this.inputCodepoints.add((char) codepoint);
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

	protected static String getLogInfo(int obj)
	{
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	protected String readFileAsString(String filename) throws Exception
	{
		StringBuilder source = new StringBuilder();

		InputStream in = this.getResource(filename);

		Exception exception = null;

		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

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

	protected void setUpShaders()
	{
		try
		{
			this.shaderShadowMap = new ShaderShadowMap(this);
			this.shaderShadowMap.initialize();

			this.shaderBase = new ShaderBase(this);
			this.shaderBase.initialize();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected void loop()
	{
		GL.createCapabilities();

		this.setUpShaders();

		this.shaderHandler = new ShaderHandler(this);
		this.shapeRenderer = new ImmediateModeShapeRenderer(this);
		this.lightTex = glGenTextures();
		this.textures.put("lights", this.lightTex);

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		boolean shouldClose = false;

		while (!shouldClose)
		{
			shouldClose = this.tick(false);
		}

		this.windowHandler.onWindowClose();

		if (this.soundsEnabled)
			this.soundPlayer.exit();

		System.exit(0);
	}

	protected boolean tick(boolean resizing)
	{
		this.startTiming();

		String audio = ALC11.alcGetString(NULL, ALC11.ALC_DEFAULT_ALL_DEVICES_SPECIFIER);

		if (!(audio == null && this.audioDevice == null || (this.audioDevice != null && this.audioDevice.equals(audio))))
		{
			this.soundPlayer = new SoundPlayer(this);
		}

		this.audioDevice = audio;

		SoundPlayer soundPlayer = (SoundPlayer) this.soundPlayer;

		if (soundPlayer != null)
		{
			soundPlayer.update();
		}

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glfwGetWindowSize(window, w, h);

		if (w[0] > 0 || h[0] > 0)
			this.hasResized = absoluteWidth != w[0] || absoluteHeight != h[0];

		if (w[0] > 0)
			absoluteWidth = w[0];

		if (h[0] > 0)
			absoluteHeight = h[0];

		glfwGetCursorPos(window, mx, my);
		absoluteMouseX = mx[0];
		absoluteMouseY = my[0];

		if (constrainMouse)
		{
			if (absoluteMouseX < 0)
				setCursorPos(0, absoluteMouseY);

			if (absoluteMouseY < 0)
				setCursorPos(absoluteMouseX, 0);

			if (absoluteMouseX > absoluteWidth)
				setCursorPos(absoluteWidth, absoluteMouseY);

			if (absoluteMouseY > absoluteHeight)
				setCursorPos(absoluteMouseX, absoluteHeight);

			glfwSetWindowSizeLimits(window, (int) absoluteWidth, (int) absoluteHeight, (int) absoluteWidth, (int) absoluteHeight);
		}
		else
			glfwSetWindowSizeLimits(window, GLFW_DONT_CARE, GLFW_DONT_CARE, GLFW_DONT_CARE, GLFW_DONT_CARE);

		glfwGetFramebufferSize(window, w, h);

		this.updater.update();

		if (shadowsEnabled)
			this.shaderHandler.renderShadowMap();

		this.shaderHandler.renderNormal();

		glfwSwapBuffers(window);

		if (!resizing)
			glfwPollEvents();

		boolean shouldClose = glfwWindowShouldClose(window);

		if (shouldClose)
		{
			shouldClose = windowHandler.attemptCloseWindow();

			if (!shouldClose)
			{
				glfwSetWindowShouldClose(window, false);
			}
		}

		this.stopTiming();

		return shouldClose;
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
		absoluteMouseX = x;
		absoluteMouseY = y;
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

			float[] d = new float[1];
			float[] d2 = new float[1];
			glfwGetMonitorContentScale(glfwGetPrimaryMonitor(), d, d2);

			glfwSetWindowMonitor(this.window, glfwGetPrimaryMonitor(), 0, 0, (int) (this.vidmode.width() * d[0]), (int) (this.vidmode.height() * d2[0]), this.vidmode.refreshRate());
		}
		else
			glfwSetWindowMonitor(this.window, NULL, this.prevPosX[0], this.prevPosY[0], this.prevSizeX[0], this.prevSizeY[0], this.vidmode.refreshRate());

		this.setVsync(this.vsync);
	}

	@Override
	public void setOverrideLocations(ArrayList<String> loc, BaseFileManager fileManager)
	{
		this.overrideLocations = loc;
		fileManager.setOverrideLocations(loc);
	}

	public void setColor(double r, double g, double b, double a, double glow)
	{
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = a / 255;
		this.glow = glow;

		glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

		if (!drawingShadow)
			this.currentBaseShader.glow.set((float) glow);
	}

	public void setColor(double r, double g, double b, double a)
	{
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = a / 255;

		glColor4d(this.colorR, this.colorG, this.colorB, this.colorA);

		if (!drawingShadow)
			this.currentBaseShader.glow.set(0);
	}

	public void setColor(double r, double g, double b)
	{
		this.colorR = r / 255;
		this.colorG = g / 255;
		this.colorB = b / 255;
		this.colorA = 1;

		glColor3d(this.colorR, this.colorG, this.colorB);

		if (!drawingShadow)
			this.currentBaseShader.glow.set(0);
	}

	protected void createImage(String image)
	{
		this.createImage(image, null);
	}

	public void createImage(String image, InputStream in)
	{
		try
		{
			if (in == null)
				in = this.getResource(image);
			else
				image = "/" + image;

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
			System.err.println("Failed to load: " + image);
			e.printStackTrace();
		}
	}

	@Override
	public void setUpscaleImages(boolean upscale)
	{
		this.upscaleImages = upscale;
	}

	public double scaleLight(double in)
	{
		double scale = 32;
		double lower = -scale / 2 + 0.5;
		double upper = scale / 2 + 0.5;

		if (in < lower || in > upper)
			return -1;
		else
			return (in - lower) / scale;
	}

	public void createLights(ArrayList<double[]> lights, double scale)
	{
		this.scaledLights.clear();

		for (double[] d: lights)
		{
			double x = scaleLight(d[0] / absoluteWidth) * 256;
			double y = scaleLight(d[1] / absoluteHeight) * 256;
			double z = scaleLight(d[2] / absoluteDepth) * 256;

			if (x >= 0 && y >= 0 && z >= 0)
			{
				d[0] = x;
				d[1] = y;
				d[2] = z;
				this.scaledLights.add(d);
			}
		}

		ByteBuffer buf = ByteBuffer.allocateDirect(16 * this.scaledLights.size());

		for (double[] l : this.scaledLights)
		{
			double x = l[0];
			double y = l[1];
			double z = l[2];
			double b = l[3];

			buf.put((byte) x);
			buf.put((byte) y);
			buf.put((byte) z);
			buf.put((byte) b);

			buf.put((byte) ((x % 1.0) * 256));
			buf.put((byte) ((y % 1.0) * 256));
			buf.put((byte) ((z % 1.0) * 256));
			buf.put((byte) ((b % 1.0) * 256));

			buf.put((byte) (((x / 256) % 1.0) * 65536));
			buf.put((byte) (((y / 256) % 1.0) * 65536));
			buf.put((byte) (((z / 256) % 1.0) * 65536));
			buf.put((byte) (((b / 256) % 1.0) * 65536));

			buf.put((byte) l[4]);
			buf.put((byte) l[5]);
			buf.put((byte) l[6]);
			buf.put((byte) 0);
		}

		buf.flip();

//		glUniform1i(this.lightsCountFlag, lights.size());
//		glUniform1f(this.scaleFlag, (float) scale);
//		glUniform1i(this.lightsFlag,2);

		glEnable(GL_TEXTURE_2D);
		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, this.lightTex);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, lights.size() * 4, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		glActiveTexture(GL_TEXTURE0);
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

			if (!mac)
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
		try (InputStream in = this.getResource(fileName);
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

	public InputStream getResource(String path) throws FileNotFoundException
	{
		return ComputerFileManager.getResource(this.overrideLocations, path);
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

			if (!drawingShadow)
			{
				float[] projMatrix = new float[16];
				glGetFloatv(GL_PROJECTION_MATRIX, projMatrix);

				//glUniformMatrix4fv(this.shaderHandler.normalProgramVPUniform, false, projMatrix);
			}
		}
	}

	public void clearDepth()
	{
		glClear(GL_DEPTH_BUFFER_BIT);
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
	public ArrayList<Character> getRawTextKeys()
	{
		return this.inputCodepoints;
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

	public void setDrawOptions(boolean depth, boolean glow)
	{
		this.batchDepth = depth;
		this.batchGlow = glow;

		if (depth)
		{
			enableDepthtest();
			glDepthFunc(GL_LEQUAL);
		}
		else
			disableDepthtest();

		if (glow)
			this.setGlowBlendFunc();
		else
			this.setTransparentBlendFunc();
	}

	public void setDrawOptions(boolean depth, boolean glow, boolean depthMask)
	{
		this.batchDepth = depth;
		this.batchGlow = glow;
		this.batchDepthMask = depthMask;

		glDepthMask(depthMask);

		if (depth)
		{
			enableDepthtest();
			glDepthFunc(GL_LEQUAL);
		}
		else
			disableDepthtest();

		if (glow)
			this.setGlowBlendFunc();
		else
			this.setTransparentBlendFunc();
	}

	public void setTexture(String image)
	{
		this.setTexture(image, true);
	}

	public void setTexture(String image, boolean batch)
	{
		if (image.equals(this.currentTexture))
			return;

		if (batch)
			this.shapeRenderer.setBatchMode(false, this.batchQuads, this.batchDepth, this.batchGlow, this.batchDepthMask);

		this.currentTexture = image;

		if (!textures.containsKey(image))
			createImage(image);

		glMatrixMode(GL_MODELVIEW);
		enableTexture();

		glEnable(GL_BLEND);
		this.setTransparentBlendFunc();

		glBindTexture(GL_TEXTURE_2D, textures.get(image));

		if (batch)
			this.shapeRenderer.setBatchMode(true, this.batchQuads, this.batchDepth, this.batchGlow, this.batchDepthMask);
	}

	public void setTextureCoords(double u, double v)
	{
		glTexCoord2d(u, v);
	}

	public void stopTexture()
	{
		if (this.currentTexture == null)
			return;

		this.currentTexture = null;
		glMatrixMode(GL_PROJECTION);

		this.shapeRenderer.setBatchMode(false, this.batchQuads, this.batchDepth, this.batchGlow, this.batchDepthMask);
		this.shapeRenderer.setBatchMode(true, this.batchQuads, this.batchDepth, this.batchGlow, this.batchDepthMask);

		disableTexture();
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
	public void setResolution(int x, int y)
	{
		glfwSetWindowSize(window, x, y);
	}

	@Override
	public void setShadowQuality(double quality)
	{
		if (quality <= 0)
		{
			this.shaderHandler.quality = 1;
			this.shadowsEnabled = false;
		}
		else
		{
			this.shaderHandler.quality = quality;
			this.shadowsEnabled = true;
		}
	}

	@Override
	public double getShadowQuality()
	{
		if (!this.shadowsEnabled)
			return 0;
		else
			return this.shaderHandler.quality;
	}

	@Override
	public void setLighting(double light, double glowLight, double shadow, double glowShadow)
	{
		this.currentBaseShader.light.set((float) light);
		this.currentBaseShader.glowLight.set((float) glowLight);
		this.currentBaseShader.shade.set((float) shadow);
		this.currentBaseShader.glowShade.set((float) glowShadow);
	}

	@Override
	public void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess)
	{
		this.setMaterialLights(ambient, diffuse, specular, shininess, -1, 1, true);
	}

	@Override
	public void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess, double minBound, double maxBound, boolean negative)
	{
		if (this.drawingShadow)
			return;

		/*this.currentBaseShader.customLight.set(true);
		this.currentBaseShader.lightAmbient.set(ambient[0], ambient[1], ambient[2]);
		this.currentBaseShader.lightDiffuse.set(diffuse[0], diffuse[1], diffuse[2]);
		this.currentBaseShader.lightSpecular.set(specular[0], specular[1], specular[2]);
		this.currentBaseShader.shininess.set((float) shininess);
		this.currentBaseShader.minBrightness.set((float) minBound);
		this.currentBaseShader.maxBrightness.set((float) maxBound);
		this.currentBaseShader.negativeBrightness.set(negative);*/
	}

	@Override
	public void setCelShadingSections(float sections)
	{
		if (this.drawingShadow)
			return;

		//this.currentBaseShader.celsections.set(sections);
	}

	@Override
	public void disableMaterialLights()
	{
		if (this.drawingShadow)
			return;

		//this.currentBaseShader.customLight.set(false);
	}

	@Override
	public void addMatrix()
	{
		glPushMatrix();
	}

	@Override
	public void removeMatrix()
	{
		glPopMatrix();
	}

	@Override
	public void setMatrixProjection()
	{
		glMatrixMode(GL_PROJECTION);
	}

	@Override
	public void setMatrixModelview()
	{
		glMatrixMode(GL_MODELVIEW);
	}

	@Override
	public ModelPart createModelPart()
	{
		return new ImmediateModeModelPart(this);
	}

	@Override
	public ModelPart createModelPart(Model model, ArrayList<ModelPart.Shape> shapes, Model.Material material)
	{
		return new VBOModelPart(this, model, shapes, material);
	}

	@Override
	public PosedModel createPosedModel(Model m)
	{
		return new VBOPosedModel(m);
	}

	public void enableTexture()
	{
		glEnable(GL_TEXTURE_2D);

		if (!drawingShadow)
			this.currentBaseShader.texture.set(true);
		else
			this.currentShadowMapShader.texture.set(true);

		GL20.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void disableTexture()
	{
		this.currentTexture = null;
		glDisable(GL_TEXTURE_2D);

		if (!drawingShadow)
			this.currentBaseShader.texture.set(false);
		else
			this.currentShadowMapShader.texture.set(false);

		glEnable(GL_TEXTURE_2D);
		GL20.glActiveTexture(GL13.GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, this.shaderHandler.depthTexture);
	}

	public void enableDepthtest()
	{
		glEnable(GL_DEPTH_TEST);

		if (!drawingShadow)
			this.currentBaseShader.depthtest.set(true);
	}

	public void disableDepthtest()
	{
		glDisable(GL_DEPTH_TEST);

		if (!drawingShadow)
			this.currentBaseShader.depthtest.set(false);
	}

	public void enableDepthmask()
	{
		glDepthMask(true);
	}

	public void disableDepthmask()
	{
		glDepthMask(false);
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

	public int createVBO()
	{
		return GL15.glGenBuffers();
	}

	public void freeVBO(int i)
	{
		GL15.glDeleteBuffers(i);
	}

	public void vertexBufferData(int id, Buffer buffer)
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		if (buffer instanceof IntBuffer)
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (IntBuffer) buffer, GL15.GL_STATIC_DRAW);
		else if (buffer instanceof FloatBuffer)
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (FloatBuffer) buffer, GL15.GL_STATIC_DRAW);
	}

	public void vertexBufferDataDynamic(int id, Buffer buffer)
	{
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);

		if (buffer instanceof IntBuffer)
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (IntBuffer) buffer, GL15.GL_DYNAMIC_DRAW);
		else if (buffer instanceof FloatBuffer)
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (FloatBuffer) buffer, GL15.GL_DYNAMIC_DRAW);
	}

	@Override
	public BaseShapeBatchRenderer createShapeBatchRenderer(boolean dynamic)
	{
		return new DummyShapeBatchRenderer(true);
	}

	@Override
	public BaseShapeBatchRenderer2 createShapeBatchRenderer2()
	{
		return new VBOShapeBatchRenderer2(this);
	}


	@Override
	public BaseStaticBatchRenderer createStaticBatchRenderer(ShaderProgram shader, boolean color, String texture, boolean normal, int vertices)
	{
		return new VBOStaticBatchRenderer(this, shader, color, texture, normal, vertices);
	}

	@Override
	public BaseShaderUtil getShaderUtil(ShaderProgram p)
	{
		return new ShaderUtil(this, p);
	}

	@Override
	public void setShader(ShaderBase s)
	{
		ShaderBase old = this.currentBaseShader;
		this.currentBaseShader = s;
		this.currentShader = s;
		s.set();

		if (old != null)
			s.copyUniformsFrom(old, ShaderBase.class);
	}

	@Override
	public void setShader(ShaderShadowMap s)
	{
		ShaderShadowMap old = this.currentShadowMapShader;
		this.currentShadowMapShader = s;
		this.currentShader = s;
		s.set();

		if (old != null)
			s.copyUniformsFrom(old, ShaderShadowMap.class);
	}
}
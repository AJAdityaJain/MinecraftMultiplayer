package client.rendering;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;

public class DisplayManager {
	public static final int FPS_CAP = 600 ;
	public static final int WIDTH = 1920;
	public static final int HEIGHT = 1080;
	public static final int WIDTH_HALF = WIDTH/2;
	public static final int HEIGHT_HALF = HEIGHT/2;
	public static final float FOV = 90;
	public static final float NEAR_PLANE = 0.1f;
	public static final float FAR_PLANE = 1000;

	public static void createDisplay(){		
		ContextAttribs attrib = new ContextAttribs(3,3)
		.withForwardCompatible(true)
		.withProfileCore(true);

		try {
			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
			Display.create(new PixelFormat(), attrib);
//			org.lwjgl.opengl.Display.setResizable(true);
		} catch (LWJGLException e) {
			System.out.println("Display creation failed");
			System.exit(-1);
		}

		Display.setTitle("Client");
		GL11.glViewport(0,0, org.lwjgl.opengl.Display.getWidth(), org.lwjgl.opengl.Display.getHeight());
		GL11.glClearColor(.2f,.1f, .2f, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glEnable(GL11.GL_CULL_FACE);
//		GL11.glPolygonMode( GL11.GL_FRONT_AND_BACK, GL11.GL_LINE );

		GL11.glCullFace(GL11.GL_BACK);
		GL11.glFrontFace(GL11.GL_CCW);
	}
	
	public static void updateDisplay(){

		Display.sync(FPS_CAP);
		Display.update();
		
	}
	
	public static void closeDisplay(){
		Display.destroy();
	}

}

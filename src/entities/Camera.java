package entities;

import client.rendering.DisplayManager;
import client.util.Maths;
import network.TCPClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import server.Map;
import server.block.BlockState;

import static client.util.Maths.TAU;

public class Camera extends DynamicEntity{

	private static final float jumpSpeed = 5f;
	private static final float uK = 1-0.1f;//Coefficient Of Kinetic Friction

	private final Vector3f camVelocity = new Vector3f();
	private boolean inGame = true;
	private boolean lClick = false;
	private boolean rClick = false;

	@Override
	public Vector3f getVelocity() {
		Vector3f vel = super.getVelocity();
		vel.y += camVelocity.y;
		vel.x += camVelocity.x;
		vel.z += camVelocity.z;
		return vel;
	}

	@Override
	public void  stopVelocityX() {
		super.stopVelocityX();
		camVelocity.x = 0;
	}

	@Override
	public void  stopVelocityY() {
		super.stopVelocityY();
		camVelocity.y = 0;
	}

	@Override
	public void stopVelocityZ() {
		super.stopVelocityZ();
		camVelocity.z = 0;
	}

	@Override
	public void tick(float delta_time){
		velocity.x += delta_time * acceleration.x;
		velocity.y += delta_time * acceleration.y;
		if (!onGround) velocity.y -= delta_time *g;
		velocity.z += delta_time * acceleration.z;
		position.x += delta_time * (velocity.x + camVelocity.x);
		position.y += delta_time * (velocity.y + camVelocity.y);
		position.z += delta_time * (velocity.z + camVelocity.z);
	}

	public Camera(){
        super(null, new Vector3f(16 * 10,14f,16*10), new Vector3f(.6f,1.8f,.6f), 0, 160 * 3.14f/180, 0, 6.5f);
    }

	public boolean input(Map world, TCPClient client, float delta_time) {
		boolean blockChange = false;
		camVelocity.x=0f;
		camVelocity.y=0f;
		camVelocity.z=0f;

		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			camVelocity.x+= speed * (float) Math.sin(rotY);
			camVelocity.z-= speed * (float) Math.cos(rotY);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			camVelocity.x-= speed * (float) Math.sin(rotY);
			camVelocity.z+= speed * (float) Math.cos(rotY);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			camVelocity.x-= speed * (float) Math.cos(rotY);
			camVelocity.z-= speed * (float) Math.sin(rotY);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			camVelocity.x+= speed * (float) Math.cos(rotY);
			camVelocity.z+= speed * (float) Math.sin(rotY);
		}
		if(onGround) {
			camVelocity.x *= uK;
			camVelocity.z *= uK;
		}

		if(onGround && Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			onGround = false;
			velocity.y+= jumpSpeed;
		}

		if(Mouse.isButtonDown(0))
			lClick = true;
		else if (lClick) {
			lClick = false;

			Vector3f hit = new Vector3f();
			if(Maths.drawLine(world,hit,position.x, position.y, position.z, -rotX, -rotY, 10,true) != null){
				BlockState state = new BlockState(BlockState.BlockEnum.AIR);
				world.setBlock((int) hit.x, (int) hit.y, (int) hit.z, state);
				client.updateBlock(state, (int) hit.x, (int) hit.y, (int) hit.z);
			}
			blockChange = true;


		}
		if(Mouse.isButtonDown(1))
			rClick = true;
		else if (rClick) {
			rClick = false;

			Vector3f hit = new Vector3f();
			if(Maths.drawLine(world,hit,position.x, position.y, position.z, -rotX, -rotY, 10,false) != null){
				BlockState state = new BlockState(BlockState.BlockEnum.DIRT);
				world.setBlock((int) hit.x, (int) hit.y, (int) hit.z, state);
				client.updateBlock(state, (int) hit.x, (int) hit.y, (int) hit.z);
			}
			blockChange = true;

		}


		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			inGame = !inGame;

		if(inGame) {
			int x = Mouse.getX()- DisplayManager.WIDTH_HALF;
			int y = Mouse.getY()- DisplayManager.HEIGHT_HALF;

			rotY+=x*delta_time;
			rotX-=y*delta_time;
			rotX = Math.clamp(rotX, -1.5f, 1.5f);
			rotY %= TAU;

			Mouse.setCursorPosition(DisplayManager.WIDTH_HALF, DisplayManager.HEIGHT_HALF);
		}
		return blockChange;
	}

}

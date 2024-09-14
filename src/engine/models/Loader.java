package engine.models;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.newdawn.slick.opengl.PNGDecoder;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL42.glTexStorage3D;
import static org.lwjgl.opengl.GL43.GL_TEXTURE_2D_ARRAY;

public class Loader {
	
	private static final List<VAO> vaos = new ArrayList<>();
	private static final List<Integer> textures = new ArrayList<>();

	public static int loadAtlas(){
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG",
					new FileInputStream("res/textures/atlas.png"));
		} catch (Exception e) {
			System.out.println("Tried to load texture atlas.png , didn't work");
			System.exit(-1);
		}
		textures.add(texture.getTextureID());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		return texture.getTextureID();
	}



	public static int loadTexture() {

		//create a texture
		int id = glGenTextures();

		//bind the texture
		glBindTexture(GL_TEXTURE_2D_ARRAY, id);

		int numSlices = 2;

		String[] filename = {"dirt", "stone", "grass"};
		glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, 16, 16, numSlices);

		try {
			for (int i = 0; i < numSlices; i++) {
				PNGDecoder decoder = new PNGDecoder(new java.io.FileInputStream("res/textures/" + filename[i] + ".png"));
				ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
				decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.RGBA);
				buffer.flip();
				glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, 16, 16, 1, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
			}
		} catch (IOException e) {
			System.out.println("Tried to load texture array, didn't work");
			System.exit(-1);
		}
		//set the texture parameters, can be GL_LINEAR or GL_NEAREST
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);

		//upload texture
//		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		return id;
	}


	public static void cleanUp(){
		for(VAO vao:vaos){
			vao.clean();
		}
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
	}

	public static VAO createVAO(float[] positions, float[] textureCoords, int[] indices){
		VAO vao = new VAO();

		int vaoID = GL30.glGenVertexArrays();
		vao.vaoID = vaoID;
		GL30.glBindVertexArray(vaoID);

		vao.eboID = bindIndicesBuffer(indices);
		vao.vboPosID = storeDataInAttributeList(0,3,positions);
		vao.vboTexID = storeDataInAttributeList(1,3,textureCoords);
		vao.indicesCount = indices.length;

		GL30.glBindVertexArray(0);
		vaos.add(vao);

		return vao;
	}
	private static int storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data){
		int vboID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);buffer.flip();

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber,coordinateSize,GL11.GL_FLOAT,false,0,0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}
	private static int bindIndicesBuffer(int[] indices){
		int eboID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);

		IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
		buffer.put(indices);buffer.flip();

		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		return eboID;
	}
}

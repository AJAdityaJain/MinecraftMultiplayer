package engine.models;

public class TexturedModel {

	private final int vaoID;
	private final int vertexCount;
	private final int textureID;
	
	public TexturedModel(int vaoID, int vertexCount,int texture){
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
		this.textureID = texture;
	}

	public int getTextureID() {
		return textureID;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}
}

package engine.models;

public class TexturedModel {

	private final VAO vao;
	private final int textureID;
	
	public TexturedModel(VAO vao,int texture){
		this.vao = vao;
		this.textureID = texture;
	}
	public void bind(){
		vao.bind(textureID);
	}
	public void unbind(){
		vao.unbind();
	}

	public void render(){
		vao.render();
	}

	public int getTextureID() {
		return textureID;
	}

	public VAO getVao() {
		return vao;
	}

}

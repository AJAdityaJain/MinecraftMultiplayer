#version 330 core

in vec3 position;
in vec3 textureCoordinates;

out vec3 uv;
out vec3 xyz;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){
    xyz = position;
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(position,1.0);
	uv = textureCoordinates;
}
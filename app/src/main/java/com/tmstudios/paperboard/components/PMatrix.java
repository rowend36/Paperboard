package com.tmstudios.paperboard.components;
import android.graphics.*;

public class PMatrix
{
	//Manages an abstraction
	//of matrix between opengl
	//and android implementations
	public final float hardMat[]  = new float[16];
	public final Matrix mat = new Matrix();
	public Matrix getMatrix(){
		return mat;
	}
	public float[] getMatArray(){
		
		return hardMat;
	}
	public void fromMatrix(final float[] mat,int offset){
		float[] matr = new float[9];
		matr[Matrix.MTRANS_X]=mat[offset+12];
		matr[Matrix.MTRANS_Y]=mat[offset+13];
		
		matr[Matrix.MSCALE_X]=mat[offset+0];
		matr[Matrix.MSCALE_Y]=mat[offset+5];
		
		matr[Matrix.MSKEW_X]=mat[offset+4];
		matr[Matrix.MSKEW_Y]=mat[offset+1];
		
		matr[Matrix.MPERSP_0]=mat[offset+3];
		matr[Matrix.MPERSP_1]=mat[offset+7];
		matr[Matrix.MPERSP_2]=mat[offset+15];
		this.mat.setValues(matr);
		for(int i=0;i<16;i++){
			hardMat[i]=mat[offset+i];
		}
	}
	public void fromMatrix(final Matrix matrix){
		android.opengl.Matrix.setIdentityM(hardMat,0);
		float[] mat = new float[16];
		matrix.getValues(mat);
		this.mat.setValues(mat);
		hardMat[12]=mat[Matrix.MTRANS_X];
		hardMat[13]=mat[Matrix.MTRANS_Y];

		hardMat[0]=mat[Matrix.MSCALE_X];
		hardMat[5]=mat[Matrix.MSCALE_Y];

		hardMat[4]=mat[Matrix.MSKEW_X];
		hardMat[1]=mat[Matrix.MSKEW_Y];

		hardMat[3]=mat[Matrix.MPERSP_0];
		hardMat[7]=mat[Matrix.MPERSP_1];
		hardMat[15]=mat[Matrix.MPERSP_2];
	}
	public void updateHard(){
		throw new RuntimeException("unimplemented");
	}
	public void updateSoft(){
		throw new RuntimeException("unimplemented");
	}
	public void setScale(float x,float y){
		hardMat[0]=x;
		hardMat[5]=y;
		mat.setScale(x,y);
	}
	public void setTranslate(float x,float y){
		hardMat[12]=x;
		hardMat[13]=y;
		mat.setTranslate(x,y);
	}
	public void setRotate(float degrees){
		mat.setRotate(degrees);
		float[] matrix = new float[16];
		mat.getValues(matrix);
		hardMat[4]=matrix[Matrix.MSKEW_X];
		hardMat[1]=matrix[Matrix.MSKEW_Y];
	}
	public void setRotate(float degrees,float cx,float cy){
		mat.setRotate(degrees,cx,cy);
		float[] matrix = new float[16];
		mat.getValues(matrix);
		hardMat[4]=matrix[Matrix.MSKEW_X];
		hardMat[1]=matrix[Matrix.MSKEW_Y];
		hardMat[12]=matrix[Matrix.MTRANS_X];
		hardMat[13]=matrix[Matrix.MTRANS_Y];
		
	}
	public void rotate(float degrees){
		mat.postRotate(degrees);
		float[] matrix = new float[16];
		mat.getValues(matrix);
		hardMat[4]=matrix[Matrix.MSKEW_X];
		hardMat[1]=matrix[Matrix.MSKEW_Y];
	}

	public void rotate(float degrees,float cx,float cy){
		mat.postRotate(degrees,cx,cy);
		float[] matrix = new float[16];
		mat.getValues(matrix);
		hardMat[4]=matrix[Matrix.MSKEW_X];
		hardMat[1]=matrix[Matrix.MSKEW_Y];
		hardMat[12]=matrix[Matrix.MTRANS_X];
		hardMat[13]=matrix[Matrix.MTRANS_Y];
		
	}
	public void translate(float x,float y){
		hardMat[12]+=x;
		hardMat[13]+=y;
		mat.postTranslate(x,y);
		
	}
	public void scale(float x,float y){
		hardMat[0]*=x;
		hardMat[5]*=y;
		mat.postScale(x,y);
	}
	public void scale(float scale){
		scale(scale,scale);
	}
	public void setScale(float scale){
		setScale(scale,scale);
	}
}

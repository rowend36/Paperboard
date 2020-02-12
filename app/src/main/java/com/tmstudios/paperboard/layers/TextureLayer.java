package com.tmstudios.paperboard.layers;
import android.graphics.*;
import com.tmstudios.paperboard.*;
import com.android.grafika.gles.*;
import android.util.*;

public class TextureLayer extends SoftwareLayer
{

	public AbstractHardwareLayer mHardLayer;
	protected int width;
	protected int height;
	public int mHardTexture;

	private float[] points=null;

	private boolean mSelected;
	private float[] map_points=null;
	protected RectF rectF;

	protected float[] rect_points;
	public TextureLayer(LayerManager b, int width, int height, AbstractHardwareLayer hardLayer)
	{
		super(b);
		this.width = width;
		this.height = height;
		//mAngle=4;
		mHardLayer = hardLayer;
		mHardLayer.mDrawable = new Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE);
		float temp=this._z;
		this._z = mHardLayer._z;
		mHardLayer._z = temp;
		b.add(mHardLayer);
		modelMatrix.postTranslate(50,50);
		rect_points = new float[]{0,0,500,500,0,500,500,0};
		map_points = new float[]{0,0,500,500,0,500,500,0};
		points = new float[2];
		rectF = new RectF(0, 0, 500, 500);
	}

	@Override
	public boolean onLongPress(float x, float y)
	{
		
		if(intersects(x,y))
		{
			mSelected=true;
			return false;
		}
		return super.onLongPress(x, y);
		
	}
	
	public boolean intersects(float x, float y)
	{
		points[0]=x;points[1]=y;
		Matrix m = new Matrix();
		modelMatrix.invert(m);
		m.mapPoints(points);
		return rectF.contains(points[0], points[1]);
	}

	@Override
	public void onMove(float x, float y,float vx,float vy)
	{
		// TODO: Implement this method
		//super.onMoveTo(x, y);
		if(mSelected){
			modelMatrix.postTranslate(vx,vy);
		}
	}
	

	@Override
	public void onUp(float x, float y)
	{
		// TODO: Implement this method
		super.onUp(x, y);
		mSelected=false;
	}
	
	@Override
	public void softDraw(Canvas canvas, Matrix root)
	{
		int displayH=-getManager().displayH;
		int displayW=getManager().displayW;
		Matrix d = new Matrix();
		d.setConcat(root,modelMatrix);
		if(mSelected)
		{
			Paint p = new Paint();
		   	p.setColor(Color.GRAY);
			canvas.setMatrix(d);
			
			p.setColor(Color.RED);
			p.setStrokeWidth(20/d.mapRadius(1));
		
		p.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rectF, p);
		canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2, p);
		}
		d.mapPoints(map_points,rect_points);
		
		for (int i =0;i < 8;i += 2)
		{
			map_points[i] = 2.0f * map_points[i] / displayW - 1.0f;
			map_points[i + 1] = 2.0f * map_points[i + 1] / displayH + 1.0f;
		}
		Matrix c = new Matrix();
		Trace.beginSection("Poly to Poly");
		c.setPolyToPoly(new float[]{-1,-1,1,1,-1,1,1,-1}, 0, map_points, 0, 4);
		Trace.endSection();
		//root.mapRect(r);
		//RectF dest = new RectF(2.0f*r.left/displayW-1.0f,2.0f*r.top/displayH+1.0f,2.0f*r.right/displayW-1.0f,2.0f*r.bottom/displayH+1.0f);
		//dest.sort();

		//c.setRectToRect(new RectF(-1,-1,1,1),dest,Matrix.ScaleToFit.FILL);
		setHard(c);
	}
	public void setHardTexture(int mOffscreenTexture)
	{
		this.mHardTexture=mOffscreenTexture;
		mHardLayer.mTextureId = mOffscreenTexture;
	}
	
	private void setHard(Matrix c)
	{
		// TODO: Implement this method
		float[] hardMat = mHardLayer.mapMatrix;
		android.opengl.Matrix.setIdentityM(hardMat, 0);
		float[] mat = new float[16];
		c.getValues(mat);
		hardMat[12] = mat[Matrix.MTRANS_X];
		hardMat[13] = mat[Matrix.MTRANS_Y];

		hardMat[0] = mat[Matrix.MSCALE_X];
		hardMat[5] = mat[Matrix.MSCALE_Y];

		hardMat[4] = mat[Matrix.MSKEW_X];
		hardMat[1] = mat[Matrix.MSKEW_Y];

		hardMat[3] = mat[Matrix.MPERSP_0];
		hardMat[7] = mat[Matrix.MPERSP_1];
		hardMat[15] = mat[Matrix.MPERSP_2];
	}


}

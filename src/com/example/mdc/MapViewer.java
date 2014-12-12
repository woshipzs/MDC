package com.example.mdc;

import java.io.File;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class MapViewer extends Activity implements OnTouchListener, OnClickListener {

    Matrix matrix = new Matrix();  
    Matrix savedMatrix = new Matrix();  
    DisplayMetrics dm;  
    ImageView imgView;  
    Bitmap bitmap; 
    Button zoomIn;
    Button zoomOut;
    Button back;

    private float scaleWidth = 1;
    private float scaleHeight = 1;
    private Bitmap zoomedBMP;
    private int zoom_level = 0;
    private static final double ZOOM_IN_SCALE = 1.025;//放大系数
    private static final double ZOOM_OUT_SCALE = 0.975;//缩小系数
    
    float minScaleR = 1.0f;  
  
    static final float MAX_SCALE = 10f;  
  

    static final int NONE = 0;  

    static final int DRAG = 1;  

    static final int ZOOM = 2;  
      
    int mode = NONE;  
  
    PointF prev = new PointF();  
    PointF mid = new PointF();  
    float dist = 1f;  
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        

		
     // get the map
        //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map); 

     // bitmap = BitmapFactory.decodeFile("/src/com/maps/map.jpg");
        imgView = (ImageView) findViewById(R.id.mapView);  
        imgView.setImageBitmap(bitmap);  
        imgView.setOnTouchListener(this);
        
        dm = new DisplayMetrics();  
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
        minZoom();  
        center();  
        imgView.setImageMatrix(matrix);  
        
        zoomIn = (Button) findViewById(R.id.zoomin);
        zoomIn.setOnClickListener(this);
        
        zoomOut = (Button) findViewById(R.id.zoomout);
        zoomOut.setOnClickListener(this);
        
        back = (Button) findViewById(R.id.backtomain);
        back.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0){
				finish();
			}
		});
        



        
    }
	
	 
    public void SureOnClick(View v)  
    {  
          
    }  
      
    public boolean onTouch(View v, MotionEvent event) {  
  
        switch (event.getAction() & MotionEvent.ACTION_MASK) {    
        case MotionEvent.ACTION_DOWN:  
            savedMatrix.set(matrix);  
            prev.set(event.getX(), event.getY());  
            mode = DRAG;  
            break;   
        case MotionEvent.ACTION_POINTER_DOWN:  
            dist = spacing(event);  
           
            if (spacing(event) > 10f) {  
                savedMatrix.set(matrix);  
                midPoint(mid, event);  
                mode = ZOOM;  
            }  
            break;  
        case MotionEvent.ACTION_UP:  
        case MotionEvent.ACTION_POINTER_UP:  
            mode = NONE;  
            //savedMatrix.set(matrix);  
            break;  
        case MotionEvent.ACTION_MOVE:  
            if (mode == DRAG) {  
                matrix.set(savedMatrix);  
                matrix.postTranslate(event.getX() - prev.x, event.getY()  
                        - prev.y);  
            } else if (mode == ZOOM) {  
                float newDist = spacing(event);  
                if (newDist > 10f) {  
                    matrix.set(savedMatrix);  
                    float tScale = newDist / dist;  
                    matrix.postScale(tScale, tScale, mid.x, mid.y);  
                }  
            }  
            break;  
        }  
        imgView.setImageMatrix(matrix);  
       // CheckView();  
        return true;  
    }  
  

    private void CheckView() {  
        float p[] = new float[9];  
        matrix.getValues(p);  
        if (mode == ZOOM) {  
            if (p[0] < minScaleR) {  
                
                matrix.setScale(minScaleR, minScaleR);  
            }  
            if (p[0] > MAX_SCALE) {  
 
                matrix.set(savedMatrix);  
            }  
        }  
        center();  
    }  
  


  
    private void minZoom() {  
        minScaleR = Math.min(  
                (float) dm.widthPixels / (float) bitmap.getWidth(),  
                (float) dm.heightPixels / (float) bitmap.getHeight());  
        if (minScaleR < 1.0) {  
            matrix.postScale(minScaleR, minScaleR);  
        }  
    }  
  
    private void center() {  
        center(true, true);  
    }  

    protected void center(boolean horizontal, boolean vertical) {  
  
        Matrix m = new Matrix();  
        m.set(matrix);  
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        m.mapRect(rect);  
  
        float height = rect.height();  
        float width = rect.width();  
  
        float deltaX = 0, deltaY = 0;  
  
        if (vertical) {  
            int screenHeight = dm.heightPixels;  
            if (height < screenHeight) {  
                deltaY = (screenHeight - height) / 2 - rect.top;  
            } else if (rect.top > 0) {  
                deltaY = -rect.top;  
            } else if (rect.bottom < screenHeight) {  
                deltaY = imgView.getHeight() - rect.bottom;  
            }  
        }  
  
        if (horizontal) {  
            int screenWidth = dm.widthPixels;  
            if (width < screenWidth) {  
                deltaX = (screenWidth - width) / 2 - rect.left;  
            } else if (rect.left > 0) {  
                deltaX = -rect.left;  
            } else if (rect.right < screenWidth) {  
                deltaX = screenWidth - rect.right;  
            }  
        }  
        matrix.postTranslate(deltaX, deltaY);  
    }  
  
    private float spacing(MotionEvent event) {  
        float x = event.getX(0) - event.getX(1);  
        float y = event.getY(0) - event.getY(1);  
        return FloatMath.sqrt(x * x + y * y);  
    }  
  
    private void midPoint(PointF point, MotionEvent event) {  
        float x = event.getX(0) + event.getX(1);  
        float y = event.getY(0) + event.getY(1);  
        point.set(x / 2, y / 2);  
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 if(v == zoomIn){
             enlarge();
     }else if (v == zoomOut) {
             small();
     }
	}  
	

	private void small() {
	        int bmpWidth = bitmap.getWidth();
	        int bmpHeight = bitmap.getHeight();
	        scaleWidth = (float) (scaleWidth * ZOOM_OUT_SCALE);
	        scaleHeight = (float) (scaleHeight * ZOOM_OUT_SCALE);
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleWidth, scaleHeight);
	        zoomedBMP = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix,
	                        true);
	        imgView.setImageBitmap(zoomedBMP);  
	       
	}

	private void enlarge() {
	        try {
	                int bmpWidth = bitmap.getWidth();
	                int bmpHeight = bitmap.getHeight();
	                scaleWidth = (float) (scaleWidth * ZOOM_IN_SCALE);
	                scaleHeight = (float) (scaleHeight * ZOOM_IN_SCALE);
	                Matrix matrix = new Matrix();
	                matrix.postScale(scaleWidth, scaleHeight);
	                zoomedBMP = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix,
	                                true);
	                
	                imgView.setImageBitmap(zoomedBMP); 
 
	        } catch (Exception e) {
	        	
	        }

	}
}

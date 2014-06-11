package org.opencv.samples.tutorial3;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

public class Tutorial3View extends JavaCameraView implements PictureCallback {

    private static final String TAG = "Sample::Tutorial3View";
    private String mPictureFileName;
	private Mat curMat;
	private Mat mIntermediateMat;

    public Tutorial3View(Context context, AttributeSet attrs) {
        super(context, attrs);
       
    }

    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
        mCamera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        //try {
        	
        	Bitmap bmp = BitmapFactory.decodeByteArray(data , 0, data.length);
        	this.curMat = new Mat(bmp.getHeight(),bmp.getWidth(),CvType.CV_8UC3);
        	Bitmap myBitmap32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        	Utils.bitmapToMat(myBitmap32, this.curMat);

        	Imgproc.cvtColor(this.curMat, this.curMat, Imgproc.COLOR_BGR2RGB,4);
            //Mat bgrMat = Highgui.imdecode(this.curMat, Highgui.IMREAD_COLOR);
        	
    		org.opencv.core.Size sizeRgba = this.curMat.size();
        	
   		 	int rows = (int) sizeRgba.height;
   	        int cols = (int) sizeRgba.width;

   	        int width = cols * 3 / 4;
   	        int height = rows * 3 / 4;
   		
   	        Mat rgbaInnerWindow;
            
   	        org.opencv.core.Rect roi = new org.opencv.core.Rect(new Point(2*cols/8, 2*rows/8), new Point(width,height));
   	        rgbaInnerWindow = this.curMat.submat(roi);

			//Imgproc.resize(rgbaInnerWindow, this.curMat, this.curMat.size());

			Highgui.imwrite(this.mPictureFileName, rgbaInnerWindow);
			/*
			MatOfByte matOfByte = new MatOfByte();
			Highgui.imencode(".jpg", mIntermediateMat, matOfByte);
            byte[] byteArray = matOfByte.toArray();
			*/
   	        Bitmap bmpOut = Bitmap.createBitmap(rgbaInnerWindow.cols(), rgbaInnerWindow.rows(), Bitmap.Config.ARGB_8888);
   	        Utils.matToBitmap(rgbaInnerWindow, bmpOut);
 	        
            //FileOutputStream fos = new FileOutputStream(this.mPictureFileName+'d');;
			//fos .write(bitmapToByteArray(bmpOut));
            //fos.close();
            //rgbaInnerWindow.release();

        //} catch (java.io.IOException e) {
        //    Log.e("PictureDemo", "Exception in photoCallback", e);
        //}

    }
    
    public static byte[] bitmapToByteArray(Bitmap bm) {
        // Create the buffer with the correct size
        int iBytes = bm.getWidth() * bm.getHeight() * 4;
        ByteBuffer buffer = ByteBuffer.allocate(iBytes);

        // Log.e("DBG", buffer.remaining()+""); -- Returns a correct number based on dimensions
        // Copy to buffer and then into byte array
        bm.copyPixelsToBuffer(buffer);
        // Log.e("DBG", buffer.remaining()+""); -- Returns 0
        return buffer.array();
    }
}

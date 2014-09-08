package com.dip.angrycamera.angrycamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;
import org.opencv.android.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class ColorDetectionActivity extends Activity implements CvCameraViewListener2 {

    private Mat             mRgba;
    private Mat             mPig01, mPig02, mPig03;
    private Mat             mBird;
    private Scalar          mBlobColorRgba;
    private ColorDetector   mDetector;
    private Mat             mSpectrum;
    private Size            SPECTRUM_SIZE;
    private Scalar          CONTOUR_COLOR;
    private int             camWidth, camHeight, squareSize, circleRadius, circleOffset;
    private Scalar          circleColor;
    private Boolean         isGalaxy = false, isNexus = false;
    private int             posx,posy;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            mOpenCvCameraView.enableView(); // Enables camera connection
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.color_detection);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_detection);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        camWidth = width;
        camHeight = height;
        if(camWidth == 800 && camHeight == 480){
            isGalaxy = true;
            squareSize = 50;
            circleRadius = 80;
            circleOffset = 10;
            posx=70;
            posy=100;
            mPig01 = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,255,255));
            mPig02 = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,255,255));
            mPig03 = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,255,255));
            mBird  = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,0,0));
        }
        if(camWidth == 1280 && camHeight == 960){
            isNexus = true;
            squareSize = 120;
            circleRadius = 150;
            circleOffset = 30;
            mPig01 = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,255,255));
            mPig02 = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,255,255));
            mPig03 = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,255,255));
            mBird  = new Mat(squareSize,squareSize, CvType.CV_8UC4, new Scalar(255,0,0));
        }
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(46,190,187);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
        //mPig = Highgui.imread("C:\\pig.png",Highgui.CV_LOAD_IMAGE_COLOR);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mDetector.setHsvColor();
        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
        mRgba = inputFrame.rgba();
        circleColor = new Scalar(180,180,180);

        // Filtro Gaussiano
        Size k = new Size(3,3);
        Imgproc.GaussianBlur(mRgba, mRgba, k, 2);

        // Obtener el color y dibujar el contorno del area detectada
        mDetector.analize(mRgba);
        List<MatOfPoint> contours = mDetector.getContours();
        Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR,3);

        try {

            if (isGalaxy) {
                //Cambiar las posiciones de los cuadrados a partir de la pantalla
                Mat roi01 = mRgba.submat(250, 250 + squareSize, 550, 550 + squareSize);
                Mat roi02 = mRgba.submat(400, 400 + squareSize, 500, 500 + squareSize);
                Mat roi03 = mRgba.submat(150, 150 + squareSize, 650, 650 + squareSize);
                mPig01 = Utils.loadResource(this, R.drawable.pig50);
                mPig02 = Utils.loadResource(this, R.drawable.pig50);
                mPig03 = Utils.loadResource(this, R.drawable.pig50);
                Imgproc.resize(mPig01, mPig01, new Size(50, 50));
                Imgproc.resize(mPig02, mPig02, new Size(50, 50));
                Imgproc.resize(mPig03, mPig03, new Size(50, 50));
                mPig01.copyTo(roi01);
                mPig02.copyTo(roi02);
                mPig03.copyTo(roi03);
            } else if (isNexus) {
                Mat roi01 = mRgba.submat(250, 250 + squareSize, 940, 940 + squareSize);
                Mat roi02 = mRgba.submat(420, 420 + squareSize, 1010, 1010 + squareSize);
                Mat roi03 = mRgba.submat(590, 590 + squareSize, 1080, 1080 + squareSize);
                mPig01 = Utils.loadResource(this, R.drawable.pig120);
                mPig02 = Utils.loadResource(this, R.drawable.pig120);
                mPig03 = Utils.loadResource(this, R.drawable.pig120);
                Imgproc.resize(mPig01, mPig01, new Size(120, 120));
                Imgproc.resize(mPig02, mPig02, new Size(120, 120));
                Imgproc.resize(mPig03, mPig03, new Size(120, 120));
                mPig01.copyTo(roi01);
                mPig02.copyTo(roi02);
                mPig03.copyTo(roi03);
            }
        }catch(IOException e){}

        try {
            int x = (int) mDetector.getPoint().x;
            int y = (int) mDetector.getPoint().y;
            System.out.println("COORD X: " + x);
            System.out.println("COORD Y: " + y);

            Mat roiBird = mRgba.submat(y,y+squareSize,x,x+squareSize);
            mBird.copyTo(roiBird);
            if(x>circleOffset && x<(circleOffset+(circleRadius*2)-squareSize) && y>camHeight-circleOffset-(circleRadius*2) && y <camHeight-circleOffset-squareSize)
                circleColor = new Scalar(0, 0, 255);

        }catch (Exception e) {}


        Core.circle(mRgba, new Point(circleOffset+circleRadius,camHeight-circleOffset-circleRadius),circleRadius,circleColor, 3);
        return mRgba;
    }
}


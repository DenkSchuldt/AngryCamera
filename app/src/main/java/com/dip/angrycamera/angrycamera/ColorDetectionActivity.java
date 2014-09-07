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
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

public class ColorDetectionActivity extends Activity implements CvCameraViewListener2 {

    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private ColorDetector        mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

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
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(46,190,187);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mDetector.setHsvColor();
        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
        mRgba = inputFrame.rgba();

        try {
            Mat m = Utils.loadResource(this, R.drawable.fotoc);
            m.setTo(new Scalar(255,225,255));
            Mat img = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());

           // Imgproc.resize(m, m, new Size (50, 50));

            //Imgproc.cvtColor(m, m, Imgproc.COLOR_GRAY2RGBA);
            m.copyTo(img);
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        Size k = new Size(3,3);
        Imgproc.GaussianBlur(mRgba, mRgba, k, 2);

        mDetector.analize(mRgba);
        List<MatOfPoint> contours = mDetector.getContours();
        Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR,3);
        //Imgproc.Canny(mRgba, mRgba, 100,3);

        Core.circle(mRgba, new Point(170,780), 150, new Scalar(0,0,255), 3);



        // Dibuja el cuadradito
        /*Mat colorLabel = mRgba.submat(4, 68, 4, 68);
        colorLabel.setTo(mBlobColorRgba);*/

        // Dibuja el espectro
        /*Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);*/
        return mRgba;
    }


}


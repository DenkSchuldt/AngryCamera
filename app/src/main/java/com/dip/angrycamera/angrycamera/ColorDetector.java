package com.dip.angrycamera.angrycamera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class ColorDetector {

    private static double mMinArea = 0.01;
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mBorder = new ArrayList<MatOfPoint>();

    // Cache
    Mat mZindex = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();
    Point point;

    /*
     * {120, [1,10]} : Cyan - (46,190,187)
     * {70, [1,40]} : Green - (0,255,255)
     * {} : Yellow (20,100,100) - (30,255,255)
     */

    public void setHsvColor() {
        double minH = 120;
        Mat spectrumHsv = new Mat(1,10, CvType.CV_8UC3);
        for (int j = 0; j < 200; j++) {
            spectrumHsv.put(0, j, new byte[] {(byte)(minH+j), (byte)255, (byte)255});
        }
        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public void analize(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mZindex);
        Imgproc.pyrDown(mZindex, mZindex);
        Imgproc.cvtColor(mZindex, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);
        Core.inRange(mHsvMat, new Scalar(120, 100, 100), new Scalar(179, 255, 255), mMask); // real blue
        //Core.inRange(mHsvMat, new Scalar(100, 135, 135), new Scalar(140, 255, 255), mMask); // Cyan
        Imgproc.dilate(mMask, mDilatedMask, new Mat());
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        Iterator<MatOfPoint> contour = contours.iterator();

        while (contour.hasNext()) {
            MatOfPoint container = contour.next();
            double area = Imgproc.contourArea(container);
            if (area > maxArea)
                maxArea = area;
        }
        mBorder.clear();
        contour = contours.iterator();
        while (contour.hasNext()) {
            MatOfPoint max = contour.next();
            if (Imgproc.contourArea(max) > mMinArea*maxArea) {
                Core.multiply(max, new Scalar(4,4), max);
                mBorder.add(max);
                Point[] pArray = max.toArray();
                point = pArray[0];
                break; // the biggest
            }
        }
    }

    public List<MatOfPoint> getContours() {
        return mBorder;
    }
    public Mat getSpectrum() { return mSpectrum; }
    public Point getPoint() { return point; }
}

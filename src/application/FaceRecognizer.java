package application;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.cvarrToMat;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class FaceRecognizer {
    /**
     * face recognizer using the LBPH
     */
    private LBPHFaceRecognizer faceRecognizer;
    /**
     * access the root of the application
     */
    private File root;
    /**
     * images using mat vector
     */
    private MatVector images;

    /**
     * getting the labels
     */
    private Mat labels;


    /**
     * initializing the  class
     * extraction the images from the local folder and intiliazing the recognition process
     */
    public void init() {
        // mention the directory the faces has been saved
        String trainingFolder = "./faces";

        root = new File(trainingFolder);

        FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        File[] imageFiles = root.listFiles(imgFilter);

        assert imageFiles != null;
        this.images = new MatVector(imageFiles.length);

        this.labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();

        int counter = 0;
        // reading face images from the folder

        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            int label = Integer.parseInt(image.getName().split("-")[0]);
            images.put(counter, img);
            labelsBuf.put(counter, label);

            counter++;
        }

        this.faceRecognizer = createLBPHFaceRecognizer();
        this.faceRecognizer.train(images, labels);
    }

    /**
     * recognition method
     *
     * @param faceData pass the image to be recognize
     * @return the predict label
     */
    public int recognize(IplImage faceData) {
        Mat faces = cvarrToMat(faceData);

        cvtColor(faces, faces, CV_BGR2GRAY);

        IntPointer label = new IntPointer(1);
        DoublePointer confidenceLevel = new DoublePointer(0);
        this.faceRecognizer.predict(faces, label, confidenceLevel);
        int predictedLabel = label.get(0);
        //Confidence value less than 60 means face is known
        //Confidence value greater than 60 means face is unknown
        if (confidenceLevel.get(0) > 60) {
            return -1;
        }

        return predictedLabel;
    }
}

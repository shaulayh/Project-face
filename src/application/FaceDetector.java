package application;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class FaceDetector implements Runnable {

    private Database database = new Database();

    private FaceRecognizer faceRecognizer = new FaceRecognizer();

    private OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
    private Java2DFrameConverter paintConverter = new Java2DFrameConverter();
    private ArrayList<String> output = new ArrayList<>();

//    private Exception exception = null;

    private int count = 0;
    public String classifierName;
    public File classifierFile;


    public boolean saveFace = false;
    public boolean isRecFace = false;

    public boolean isEyeDetection = false;
    public boolean isSmile = false;
    private boolean stop = false;

    private CvHaarClassifierCascade classifier = null;
    private CvHaarClassifierCascade classifierEye = null;
    private CvHaarClassifierCascade classifierSideFace = null;
    private CvHaarClassifierCascade classifierSmile = null;
    private CvHaarClassifierCascade classifierEyeglass = null;


    public CvMemStorage storage = null;
    private FrameGrabber grabber = null;
    private IplImage grabbedImage = null, temp, temp2, grayImage = null, smallImage = null;
    public ImageView frames2;
    public ImageView frames;


    private CvSeq faces = null;
    private CvSeq eyes = null;
    private CvSeq smile = null;


    int recogniseCode;
    public int code;
    public int reg;
    public int age;

    public String firstName; //first name
    public String lastName; //last name
    public String occupation; //section
    public String name;

    private Person person = new Person();

    private TerminalMonitor terminalMonitor = TerminalMonitor.getInstance();
    private ArrayList<String> toPrintInTerminal = new ArrayList<>();

    public void init() {
        faceRecognizer.init();

        setClassifier("haar/haarcascade_frontalface_alt.xml");
        setClassifierSideFace("haar/haarcascade_profileface.xml");
        setClassifierSmile("haar/haarcascade_smile.xml");

    }

    public void start() {
        try {
            new Thread(this).start();
        } catch (Exception e) {
//            if (exception == null) {
//                exception = e;
//
//            }
            terminalMonitor.addNewMessage(e.getMessage());
        }
    }

    public void run() {
        try {
            terminalMonitor.addNewMessage("face detection is on now");
            try {
                grabber = OpenCVFrameGrabber.createDefault(0); //parameter 0 default camera , 1 for secondary

                grabber.setImageWidth(700);
                grabber.setImageHeight(700);
                grabber.start();

                grabbedImage = grabberConverter.convert(grabber.grab());

                storage = CvMemStorage.create();
            } catch (Exception e) {
                System.out.println("error occured here" + e);
                if (grabber != null)
                    grabber.release();
                grabber = new OpenCVFrameGrabber(0);
                grabber.setImageWidth(700);
                grabber.setImageHeight(700);
                grabber.start();
                grabbedImage = grabberConverter.convert(grabber.grab());

            }
//            int count = 15;
            grayImage = cvCreateImage(cvGetSize(grabbedImage), 8, 1); //converting image to grayscale

            //reducing the size of the image to speed up the processing
            smallImage = cvCreateImage(cvSize(grabbedImage.width() / 4, grabbedImage.height() / 4), 8, 1);

            stop = false;
            boolean fetching = true;

            while (!stop && fetching && (count < 15)) {
                System.out.println(count);
                try {
                    fetching = ((grabbedImage = grabberConverter.convert(grabber.grab())) != null);

                } catch (Exception e) {
                    stop();
                    break;
                }
                Frame frame = grabberConverter.convert(grabbedImage);
                BufferedImage image = paintConverter.getBufferedImage(frame, 2.2 / grabber.getGamma());
                Graphics2D regPerson = image.createGraphics();

                if (faces == null) {
                    cvClearMemStorage(storage);

                    //creating a temporary image
                    try {
                        temp = cvCreateImage(cvGetSize(grabbedImage), grabbedImage.depth(), grabbedImage.nChannels());
                    } catch (RuntimeException ex) {
                        System.out.println("the exception incase of error " + ex.getMessage());
                    }


                    cvCopy(grabbedImage, temp);

                    cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
                    cvResize(grayImage, smallImage, CV_INTER_AREA);


                    //face detection
                    try {
                        faces = cvHaarDetectObjects(smallImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
                    } catch (Exception ex) {
                        System.out.println("Error with face detection " + ex.getMessage());

                    }
                    CvPoint org = null;
                    if (grabbedImage != null) {

                        if (isEyeDetection) {        //eye detection logic
                            eyes = cvHaarDetectObjects(smallImage, classifierEye, storage, 1.1, 3,
                                    CV_HAAR_DO_CANNY_PRUNING);

                            if (eyes.total() == 0) {
                                eyes = cvHaarDetectObjects(smallImage, classifierEyeglass, storage, 1.1, 3,
                                        CV_HAAR_DO_CANNY_PRUNING);

                            }

                            printResult(eyes, eyes.total(), regPerson);

                        }

                        if (isSmile) {
                            try {
                                smile = cvHaarDetectObjects(smallImage, classifierSmile, storage, 1.1, 3,
                                        CV_HAAR_DO_CANNY_PRUNING);

                                if (smile != null) {
                                    printResult(smile, smile.total(), regPerson);
                                }
                            } catch (Exception e) {

                                e.printStackTrace();
                            }

                        }

                        if (faces.total() == 0) {
                            faces = cvHaarDetectObjects(smallImage, classifierSideFace, storage, 1.1, 3,
                                    CV_HAAR_DO_CANNY_PRUNING);

                        }

                        if (faces != null) {
                            regPerson.setColor(Color.BLUE);
                            regPerson.setStroke(new BasicStroke(3));
                            int totalFaces = faces.total();

                            for (int i = 0; i < totalFaces; i++) {

                                //printing rectange box where face detected frame by frame
                                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                                regPerson.drawRect((r.x() * 4), (r.y() * 4), (r.width() * 4), (r.height() * 4));

                                CvRect re = new CvRect((r.x() * 4), r.y() * 4, (r.width() * 4), r.height() * 4);

                                cvSetImageROI(temp, re);

                                org = new CvPoint(r.x(), r.y());

                                if (isRecFace) {
                                    String names = "Searching!!!";
                                    this.recogniseCode = faceRecognizer.recognize(temp);

                                    //getting recognised user from the database

                                    if (recogniseCode != -1) {
                                        database.init();
                                        ArrayList<String> user = new ArrayList<String>();
                                        user = database.getUser(this.recogniseCode);
                                        this.output = user;
                                        System.out.println("error " + this.output);
                                        if (user.size() > 1) {
                                            names = user.get(1) + " " + user.get(2);
                                        }
                                    }

                                    //printing recognised person name into the frame
                                    regPerson.setColor(Color.WHITE);
                                    regPerson.setFont(new Font("Arial Black", Font.BOLD, 20));

                                    regPerson.drawString(names, (int) (r.x() * 6.5), r.y() * 4);

                                }

                                if (saveFace) { //saving captured face to the disk
                                    //keep it in mind that face code should be unique to each person
                                    String fName = "faces/" + code + "-" + firstName + "_" + lastName + "_" + count + ".jpg";
                                    cvSaveImage(fName, temp);
                                    count++;
                                }
                            }
                            this.saveFace = false;
                            faces = null;
                        }

                        WritableImage showFrame = SwingFXUtils.toFXImage(image, null);

                        javafx.application.Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                frames.setImage(showFrame);
                            }
                        });
                    }
                    cvReleaseImage(temp);
                }

            }
        } catch (Exception e) {
//            e.printStackTrace();
//            if (exception == null) {
//                exception = e;
//            }
            toPrintInTerminal.add(e.getMessage());
        }
    }

    public void stop() {
        stop = true;

        grabbedImage = grayImage = smallImage = null;
        if (grabber != null) {
            try {
                grabber.stop();
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {

                e.printStackTrace();
            }
            try {
                grabber.release();
            } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {

                e.printStackTrace();
            }
        }
        grabber = null;
    }

    public void setClassifier(String name) {

        try {

            setClassifierName(name);
            classifierFile = Loader.extractResource(classifierName, null, "classifier", ".xml");

            if (classifierFile == null || classifierFile.length() <= 0) {
                throw new IOException("Could not extract \"" + classifierName + "\" from Java resources.");
            }

            // Preload the opencv_objdetect module to work around a known bug.
            Loader.load(opencv_objdetect.class);
            classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
            classifierFile.delete();
            if (classifier.isNull()) {
                throw new IOException("Could not load the classifier file.");
            }

        } catch (Exception e) {
//            if (exception == null) {
//                exception = e;
//
//            }
            toPrintInTerminal.add(e.getMessage());
        }

    }

    public void setClassifierSmile(String name) {

        try {
            setClassifierName(name);
            classifierFile = Loader.extractResource(classifierName, null, "classifier", ".xml");

            if (classifierFile == null || classifierFile.length() <= 0) {
                throw new IOException("Could not extract \"" + classifierName + "\" from Java resources.");
            }

            // Preload the opencv_objdetect module to work around a known bug.
            Loader.load(opencv_objdetect.class);
            classifierSmile = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
            classifierFile.delete();
            if (classifier.isNull()) {
                throw new IOException("Could not load the classifier file.");
            }

        } catch (Exception e) {
//            if (exception == null) {
//                exception = e;

//            }
            toPrintInTerminal.add(e.getMessage());
        }


    }

    public void printResult(CvSeq data, int total, Graphics2D g2) {
        for (int j = 0; j < total; j++) {
            CvRect eye = new CvRect(cvGetSeqElem(eyes, j));

            g2.drawOval((eye.x() * 4), (eye.y() * 4), (eye.width() * 4), (eye.height() * 4));

        }
    }

    public void setClassifierSideFace(String name) {
        try {

            classifierName = name;
            classifierFile = Loader.extractResource(classifierName, null, "classifier", ".xml");

            if (classifierFile == null || classifierFile.length() <= 0) {
                throw new IOException("Could not extract \"" + classifierName + "\" from Java resources.");
            }

            // Preload the opencv_objdetect module to work around a known bug.
            Loader.load(opencv_objdetect.class);
            classifierSideFace = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
            classifierFile.delete();
            if (classifier.isNull()) {
                throw new IOException("Could not load the classifier file.");
            }

        } catch (Exception e) {
//            if (exception == null) {
//                exception = e;
//            }
            toPrintInTerminal.add(e.getMessage());
        }

    }

    public String getClassifierName() {
        return classifierName;
    }

    public void setClassifierName(String classifierName) {
        this.classifierName = classifierName;
    }


    public ArrayList<String> getOutput() {
        return output;
    }

    public void clearOutput() {
        this.output.clear();
    }

    public void setOutput(ArrayList<String> output) {
        this.output = output;
    }

    public int getRecogniseCode() {
        return recogniseCode;
    }

    public void setRecogniseCode(int recogniseCode) {
        this.recogniseCode = recogniseCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getReg() {
        return reg;
    }

    public void setReg(int reg) {
        this.reg = reg;
    }


    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public void setFrame(ImageView frame) {
        this.frames = frame;
    }

    public void setSaveFace(Boolean f) {
        this.saveFace = f;
    }


    public void setIsRecFace(Boolean isRecFace) {
        this.isRecFace = isRecFace;
    }


    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public ArrayList<String> getToPrintInTerminal() {
        return toPrintInTerminal;
    }

    public void setToPrintInTerminal(ArrayList<String> toPrintInTerminal) {
        this.toPrintInTerminal = toPrintInTerminal;
    }
}

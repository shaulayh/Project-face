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
 * face detector class use to  detect face , it implements the runnable class.
 * get the connection to database
 * and the face recognition class for extension
 *
 * @author Azeez G. Shola
 * @version 1.0
 */
public class FaceDetector implements Runnable {

    /**
     * database  object
     */
    private Database database = new Database();

    /**
     * face recognition object
     */
    private FaceRecognizer faceRecognizer = new FaceRecognizer();

    /**
     * frame converter ,image converter
     */
    private OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
    /**
     * frame converter
     */
    private Java2DFrameConverter paintConverter = new Java2DFrameConverter();

    /**
     * list of output
     */
    private ArrayList<String> output = new ArrayList<>();


    private int count = 0;
    public String classifierName;
    public File classifierFile;


    public boolean saveFace = false;
    public boolean isRecFace = false;

    private boolean stop = false;

    private CvHaarClassifierCascade classifier = null;
    private CvHaarClassifierCascade classifierSideFace = null;


    public CvMemStorage storage = null;
    private FrameGrabber grabber = null;
    private IplImage grabbedImage = null, temp, grayImage = null, smallImage = null;

    public ImageView frames;


    private CvSeq faces = null;
    private CvSeq eyes = null;


    int recogniseCode;
    public int code;
    public int reg;
    public String firstName;
    public String lastName;
    public String occupation;
    public String name;

    private Person person = new Person();

    private TerminalMonitor terminalMonitor = TerminalMonitor.getInstance();
    private ArrayList<String> toPrintInTerminal = new ArrayList<>();

    /**
     * to load the classifiers and initialize  the face recognition
     */
    public void init() {
        faceRecognizer.init();
        setClassifier("haar/haarcascade_frontalface_alt.xml");
        setClassifierSideFace("haar/haarcascade_profileface.xml");
    }

    /**
     * start the face detection thread
     */
    public void start() {
        try {
            new Thread(this).start();
        } catch (Exception e) {
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
                terminalMonitor.addNewMessage("error occurred here" + e);
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
                        terminalMonitor.addNewMessage("the exception in case of error " + ex.getMessage());
                    }

                    cvCopy(grabbedImage, temp);
                    cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
                    cvResize(grayImage, smallImage, CV_INTER_AREA);
                    //face detection
                    try {
                        faces = cvHaarDetectObjects(smallImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
                    } catch (Exception ex) {
                        terminalMonitor.addNewMessage("Error with face detection " + ex.getMessage());
                    }
                    CvPoint org = null;
                    if (grabbedImage != null) {
                        if (faces.total() == 0) {
                            faces = cvHaarDetectObjects(smallImage, classifierSideFace, storage, 1.1, 3,
                                    CV_HAAR_DO_CANNY_PRUNING);

                        }

                        if (faces != null) {
                            regPerson.setColor(Color.BLUE);
                            regPerson.setStroke(new BasicStroke(3));
                            int totalFaces = faces.total();

                            for (int i = 0; i < totalFaces; i++) {

                                //printing rectangle box where face detected frame by frame
                                CvRect r = new CvRect(cvGetSeqElem(faces, i));
                                regPerson.drawRect((r.x() * 4), (r.y() * 4), (r.width() * 4), (r.height() * 4));

                                CvRect re = new CvRect((r.x() * 4), r.y() * 4, (r.width() * 4), r.height() * 4);

                                cvSetImageROI(temp, re);

                                org = new CvPoint(r.x(), r.y());

                                if (isRecFace) {
                                    String names = "Searching!!!";
                                    this.recogniseCode = faceRecognizer.recognize(temp);

                                    //fetching information of recognize user from database
                                    if (recogniseCode != -1) {
                                        database.init();
                                        ArrayList<String> user;
                                        user = database.getUser(this.recogniseCode);
                                        this.output = user;
                                        System.out.println("faces " + this.output);
                                        if (user.size() > 1) {
                                            names = user.get(1) + " " + user.get(2);
                                        }
                                    }

                                    //printing recognised person name into the frame
                                    regPerson.setColor(Color.WHITE);
                                    regPerson.setFont(new Font("Arial Black", Font.BOLD, 20));

                                    regPerson.drawString(names, (int) (r.x() * 6.5), r.y() * 4);

                                }

                                if (saveFace) {
                                    String fName = "faces/" + code + "-" + firstName + "_" + lastName + "_" + count + ".jpg";
                                    cvSaveImage(fName, temp);
                                    count++;
                                }
                            }
                            this.saveFace = false;
                            faces = null;
                        }

                        WritableImage showFrame = SwingFXUtils.toFXImage(image, null);

                        javafx.application.Platform.runLater(() -> frames.setImage(showFrame));
                    }
                    cvReleaseImage(temp);
                }

            }
        } catch (Exception e) {
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
            toPrintInTerminal.add(e.getMessage());
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
            toPrintInTerminal.add(e.getMessage());
        }

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

package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class SampleController {


    /**
     * field to access the training data
     */
    public String filePath = "./faces";

    /**
     * pane for data
     */
    public AnchorPane pdPane;

    /**
     * start button to on the WebCam
     */
    @FXML
    private Button startCam;

    /**
     * start button to stop the WebCam
     */
    @FXML
    private Button stopBtn;

    /**
     * button to save the face to database
     */
    @FXML
    private Button saveBtn;

    /**
     * button to start the recognition process
     */
    @FXML
    private Button recogniseBtn;
    /**
     * button to stop the recognition
     */
    @FXML
    private Button stopRecBtn;

    /**
     * button to stop the recognition
     */
    @FXML
    private Button saveBtn2;

    /**
     * imageview to display the image live
     */
    @FXML
    private ImageView frame;

    /**
     * data panel
     */
    @FXML
    private TitledPane dataPane;
    /**
     * input field for first name
     */
    @FXML
    private TextField firstName;

    /**
     * input field for last name
     */
    @FXML
    private TextField lastName;
    /**
     * input field for special code to face
     */
    @FXML
    private TextField code;

    /**
     * input field for recognition code
     */
    @FXML
    private TextField reg;
    /**
     * input field for occupation of person
     */
    @FXML
    private TextField occupation;

    /**
     * list of all the logs in the terminal
     */
    @FXML
    public ListView<String> logList;
    /**
     * list of output faces
     */
    @FXML
    public ListView<String> output;

    /**
     * indicator of the progress
     */
    @FXML
    public ProgressIndicator pb;
    /**
     * label of the face
     */
    @FXML
    public Label savedLabel;
    /**
     * warning label incase of wrong input
     */
    @FXML
    public Label warning;

    /**
     * face detector object
     */
    private FaceDetector faceDetector = new FaceDetector();

    /**
     * database object to store person information
     */
    private Database database = new Database();        //Creating Database object

    /**
     * user list
     */
    ArrayList<String> user = new ArrayList<>();

    /**
     * display images
     */
    private ImageView imageView;
    /**
     * event  longs
     */
    public static ObservableList<String> event = FXCollections.observableArrayList();
    /**
     * output event logs
     */
    public static ObservableList<String> outEvent = FXCollections.observableArrayList();

    /**
     * random number generator for code
     */
    private RandomGenerator randomGenerator = new RandomGenerator();
    /**
     * validating database
     */
    public boolean isDbReady = false;

    /**
     * terminal collector info
     */
    private TerminalMonitor terminalMonitor = TerminalMonitor.getInstance();
    /**
     * number of captions for recognition
     */
    static int repeat = 10;

    /**
     * log information to terminal
     *
     * @param data to be log
     */
    public void terminalLog(String data) {

        Instant now = Instant.now();

        String logs = now.toString() + ":\n" + data;

        event.add(logs);

        logList.setItems(event);
    }

    /**
     *  start camera live
     * @throws SQLException when the database error occurs
     */
    @FXML
    protected void startCamera() throws SQLException {

        faceDetector.init();
        faceDetector.setFrame(frame);
        faceDetector.start();
        if (!database.init()) {
            terminalLog("Error: Database Connection Failed ! ");
            for (String data : terminalMonitor.getToPrintInTerminal()) {
                terminalLog(data);
            }
        } else {
            isDbReady = true;
            terminalLog("Success: Database Connection Successful ! ");

            for (String data : terminalMonitor.getToPrintInTerminal()) {
                terminalLog(data);
            }
        }

        //*******************************************************************************************
        //Activating other buttons

        code.setText(String.valueOf(Integer.parseInt(String.valueOf(randomGenerator.getRandomCode()))));
        startCam.setVisible(false);
        stopBtn.setVisible(true);
        saveBtn.setDisable(false);

        if (isDbReady) {
            recogniseBtn.setDisable(false);
        }

        dataPane.setDisable(false);

        if (stopRecBtn.isDisable()) {
            stopRecBtn.setDisable(false);
        }

        String path = filePath;

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        //Image reader from the mentioned folder
        assert listOfFiles != null;
        for (final File file : listOfFiles) {
            imageView = createImageView(file);
        }
        terminalLog("WebCam is live , Put your face in the box!");
        terminalLog(" Face Detection is on!!!");

    }

    int count = 0;

    @FXML
    protected void faceRecognise() {


        faceDetector.setIsRecFace(true);


        recogniseBtn.setText("Get Face Data");

        //Getting detected faces
        user = faceDetector.getOutput();

        if (user.size() > 0) {
            String temp = "********* Face Result: " + user.get(1) + " " + user.get(2) + " *********";
            outEvent.add(temp);

            String n1 = "First Name\t\t:\t" + user.get(1);

            outEvent.add(n1);

            output.setItems(outEvent);

            String n2 = "Last Name\t\t:\t" + user.get(2);

            outEvent.add(n2);

            output.setItems(outEvent);

            String fc = "Face Code\t\t:\t" + user.get(0);

            outEvent.add(fc);

            output.setItems(outEvent);

            String r = "Reg no\t\t\t:\t" + user.get(3);

            outEvent.add(r);

            output.setItems(outEvent);
            String s = "Section\t\t\t:\t" + user.get(5);

            outEvent.add(s);

            output.setItems(outEvent);


        }

        count++;

        terminalLog("Face Recognition Activated, Put Your Face in the box!");
        stopRecBtn.setDisable(false);

    }

    @FXML
    protected void stopRecognise() {

        faceDetector.setIsRecFace(false);
        faceDetector.clearOutput();

        this.user.clear();

        recogniseBtn.setText("Recognise Face");

        stopRecBtn.setDisable(true);

        terminalLog("Face Recognition Deactivated !");

    }

    @FXML
    protected void takeFiveCaption() {
        saveFace();
    }


    @FXML
    protected void saveFace() {

        //Input Validation
        if (firstName.getText().trim().isEmpty() ||
                reg.getText().trim().isEmpty() ||
                code.getText().trim().isEmpty()) {

            new Thread(() -> {

                try {
                    warning.setVisible(true);

                    Thread.sleep(2000);

                    warning.setVisible(false);

                } catch (InterruptedException ex) {
                    terminalLog(ex.getMessage());
                }

            }).start();

        } else {
            //Progressbar
            pb.setVisible(true);

            savedLabel.setVisible(true);
            repeat--;
            saveBtn2.setText("Save-" + repeat);
            new Thread(() -> {
                try {

                    Person person = new Person();
                    System.out.println(" check if is still new " + person);
                    // enter the face to training  data
                    faceDetector.setFirstName(firstName.getText());
                    faceDetector.setLastName(lastName.getText());
                    faceDetector.setCode(Integer.parseInt(code.getText()));
                    faceDetector.setOccupation(occupation.getText());
                    faceDetector.setReg(Integer.parseInt(reg.getText()));
                    faceDetector.setPerson(person);

                    // Save person information to database
                    person.setFirstName(firstName.getText());
                    person.setLastName(lastName.getText());
                    person.setCode(Integer.parseInt(code.getText()));
                    person.setOccupation(occupation.getText());
                    person.setReg(Integer.parseInt(reg.getText()));

//                    code.setText("");
//                    firstName.setText("");
//                    lastName.setText("");
                    database.insert(person);

                    Platform.runLater(() -> pb.setProgress(100));


                    savedLabel.setVisible(true);
                    Thread.sleep(2000);
                    if (repeat < 1) {
                        saveBtn2.setDisable(true);
                        saveBtn.setDisable(true);
                    }

                    Platform.runLater(() -> pb.setVisible(false));


                    Platform.runLater(() -> savedLabel.setVisible(false));

                } catch (InterruptedException | NullPointerException | NumberFormatException ex) {
                    savedLabel.setText("Error: " + ex.getMessage());
                    terminalLog("error: " + ex.getMessage());
                }

            }).start();
            faceDetector.setSaveFace(true);
        }

    }

    @FXML
    protected void stopCam() throws SQLException {

        faceDetector.stop();

        startCam.setVisible(true);
        stopBtn.setVisible(false);

        /* this.saveFace=true; */

        terminalLog("Cam Stream Stopped!");

        recogniseBtn.setDisable(true);
        saveBtn.setDisable(true);
        dataPane.setDisable(true);
        stopRecBtn.setDisable(true);

        database.db_close();
        terminalLog("Database Connection Closed");
        isDbReady = false;
    }


    private ImageView createImageView(final File imageFile) {

        try {
            final Image img = new Image(new FileInputStream(imageFile), 120, 0, true, true);
            imageView = new ImageView(img);

            imageView.setStyle("-fx-background-color: BLACK");
            imageView.setFitHeight(120);

            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return imageView;
    }

}

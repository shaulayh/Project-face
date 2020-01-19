package application;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class RandomGenerator {


    /**
     * generating new random number integer
     *
     * @return random number of integer
     */
    public static int getRandom() {

        Random rd = new Random(); // creating Random object
        return rd.nextInt(Integer.MAX_VALUE) + 1;
    }


    /**
     * to make sure number is not repeated, each number must
     * be distinct
     *
     * @return random number without repeat
     */
    public int getRandomCode() {

        ArrayList<Integer> codes = new ArrayList<>();
        String trainingFolder = "./faces";
        File root = new File(trainingFolder);
        FilenameFilter imgFilter = (dir, name) -> {
            name = name.toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
        };

        File[] imageFiles = root.listFiles(imgFilter);

        assert imageFiles != null;

        for (File image : imageFiles) {
            int label = Integer.parseInt(image.getName().split("-")[0]);
            codes.add(label);
        }

        LinkedHashSet<Integer> hashSet = new LinkedHashSet<>(codes);

        int number = getRandom();

        while (hashSet.contains(number)) {
            number = getRandom();
        }
        return number;
    }
}

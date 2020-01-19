package application;


import java.util.Random;

public class RandomGenerator {

    private static int postRandIt;

    public static void main(String[] args) {
        Random rd = new Random(); // creating Random object
        postRandIt = rd.nextInt(Integer.MAX_VALUE) + 1;
        System.out.println(postRandIt);
    }


    public int getRandomCode() {

        Random rd = new Random(); // creating Random object
        return rd.nextInt(Integer.MAX_VALUE) + 1;
    }
}

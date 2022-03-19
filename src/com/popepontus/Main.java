package com.popepontus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

/*
1. Boot the program via command line w/ arg for file path & time to recall the function in minutes
2. With file, generate random number for the sub counts
3. Write results to file
4. Re-call the function as specified in second arg
*/


public class Main{

    //format for the args written into the command line: file_path minutes_to_recall_function max_num_unique_values
    //                                                   upper_sub_bound, lower_sub_bound

    public static void main(final String[] args) {

        //first assert that the number of args passed through is at least two
        //if not, kill execution with error message
        if (args.length < 5) {
            System.out.println(" ERROR: need 3 args for file path and interval between executions");
            System.exit(-1);
        }

        //grab the file path from the passed in args
        String path = args[0];
        //grab the number of minutes to wait to call this function after execution
        int minutesToRecall = Integer.parseInt(args[1]);
        //grab the max number of unique values before repeats are allowed
        int maxUniques = Integer.parseInt(args[2]);
        //grab the bounds for the subs
        int uBound = Integer.parseInt(args[3]);
        int lBound = Integer.parseInt(args[4]);

        //if number of minutes between intervals provided negative, kill exec w/ error message
        if (minutesToRecall < 0) {
            System.out.println(" ERROR: interval period negative");
            System.exit(-2);
        }

        if (maxUniques < 0) {
            System.out.println(" ERROR: max number of uniques must be a positive number");
            System.exit(-3);
        }

        if (lBound < 0 || uBound < 0) {
            System.out.println(" ERROR: Given boundaries for sub counts invalid");
            System.exit(-4);
        }

        //we convert the period between executions from minutes to milliseconds
        //60 seconds in 1 minute, 1000 milliseconds in 1 second
        long period = (long) minutesToRecall * 60 * 1000;

        Timer timer = new Timer();
        SubTask task = new SubTask(path, maxUniques, uBound, lBound);

        timer.schedule(task, 0, period);

    }

}

class SubTask extends TimerTask {
    private static String path = "";
    private final ArrayList<Map.Entry<Integer, Integer>> listOfSubs = new ArrayList<>();
    private final int maxBeforeRepeat;
    private final int lower;
    private final int upper;

    //generates a totally legitimate count of the current subscribers between 0 and half a million
    private int generateRandSub(int upper, int lower) { return (int)(Math.random() * (upper + 1) + lower); }

    @Override
    public void run() {
        //holds the list of previous sub counts; meant to help prevent repeats from occurring too early

        //create new file object under the data sheet and create the file writer
        File file = new File(path);
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if the file exists at the specified path, we truncate the contents completely
        if (!file.exists()) {
            try {
                assert fw != null;
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                boolean fileCreated = file.createNewFile();

                if (fileCreated) System.out.println("* New File Created from path " + path);
                else System.out.println("* File Already Exists at path " + path);

            } catch (IOException e) { e.printStackTrace(); }
        }

        //now the fun part: we make the totally legitimate sub count
        //doesn't matter if the denominator > numerator tbh
        //TODO: allow the user to specify the upper and lower bounds for the sub count

        int numer = this.generateRandSub(upper, lower);
        int denom = this.generateRandSub(upper, lower);

        Map.Entry<Integer, Integer> entry = new SimpleEntry<>(numer, denom);

        //we continue to generate new counts so long as we get different counts
        while (listOfSubs.contains(entry)) {
            numer = this.generateRandSub(upper, lower);
            denom = this.generateRandSub(upper, lower);
            //remake the old object b/c I can't change the key alongside the value
            entry = new SimpleEntry<>(numer, denom);
        }

        //once we confirm that the old value is in fact unique, we add it in to the list
        listOfSubs.add(entry);

        //with the count made, we write to file
        String newCount = "Legit Subs:\n" + numer + "/" + denom;

        try {
            assert fw != null;
            fw.write(newCount + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if we hit the max size of repeats, we remove the last one and go from there
        if (listOfSubs.size() >= maxBeforeRepeat) listOfSubs.remove(listOfSubs.size() - 1);

    }

    public SubTask(String p, int m, int u, int l) {
        path = p;
        maxBeforeRepeat = m;
        lower = l;
        upper = u;
    }
}

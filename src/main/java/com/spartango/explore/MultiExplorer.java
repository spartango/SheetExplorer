package com.spartango.explore;

import com.irislabs.sheet.FileSheet;
import com.irislabs.sheet.Sheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

/**
 * Author: spartango
 * Date: 3/8/15
 * Time: 15:06.
 */
public class MultiExplorer {
    public static void main(String[] args) throws IOException, InterruptedException, InvalidFormatException {

        for (int instance = 0; instance < 6; instance++) {
            System.out.println("Loading file");
            final Sheet sheet = new FileSheet("/Users/spartango/Dropbox/Harvard/GOV 1430/lab3/data.csv.txt", ",");
            System.out.println("Loaded file: fields -> " + sheet.fields());
            final Explorer explorer = new Explorer(sheet, 8080 + instance);
        }

        synchronized (args) {
            System.out.println("Ready!");
            args.wait();
        }

        System.out.println("Stopping servers");
    }
}

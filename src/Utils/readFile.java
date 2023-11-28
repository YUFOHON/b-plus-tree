package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class readFile {

    public static String[] readData(String filePath) {
        ArrayList<String> lines = new ArrayList<>();

        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lines.add(line);
            }

            scanner.close();

            System.out.println("Successfully read " + filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return lines.toArray(new String[0]);
    }

}
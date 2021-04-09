package com.fyp.shoemaker.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileUtil {

    public static long getFileSize(String fullPath) throws IOException {
        return Files.size(Paths.get(fullPath));
    }

    public static void partitionFile(String path, int n) {

        File csvFile = new File(path);

        if (csvFile.isFile()) {

            int numOfLine = 0;
            // create BufferedReader and read data from csv
            try (BufferedReader csvReader = new BufferedReader(new FileReader(path))) {
                while (csvReader.readLine() != null) {
                    numOfLine++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try (BufferedReader csvReader = new BufferedReader(new FileReader(path))) {
                String[] tmp = path.split("-tmp.csv");
                String row = "";
                for (int i = 0; i < n; i++) {
                    try (FileWriter writer = new FileWriter(tmp[0] + "-" + i + ".csv")) {
                        int lineProceed = 0;
                        while (lineProceed < numOfLine / (n - i)) {
                            row = csvReader.readLine();
                            writer.append(row + '\n');
                            lineProceed++;
                        }
                        numOfLine -= lineProceed;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File not found");
        }
    }

    public static void mergeFile(String[] path, String fileName) {

        for(int i = 0; i < path.length; i++) {
            File csvFile = new File(path[i]);
            if (!csvFile.isFile()) {
                System.out.println("File :" + path[i] + " not found");
                return;
            }
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            for(int i = 0; i < path.length; i++) {
                try (BufferedReader csvReader = new BufferedReader(new FileReader(path[i]))) {
                    String row = "";
                    while ((row = csvReader.readLine()) != null) {
                        writer.append(row + "\n");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}

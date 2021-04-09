package com.fyp.elf.util;

import org.jblas.DoubleMatrix;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class MatrixUtil {

    public static DoubleMatrix parseLineToMatrix(String line) {
        String[] data = line.split(",");
        double[][] rowData = new double[1][data.length];
        for(int i = 0; i < data.length; i++) {
            rowData[0][i] = Double.parseDouble(data[i]);
        }
        return (new DoubleMatrix(rowData));
    }

    /*
        Read the csv file A line by line instead of reading the whole file
     */
    public static void computeTaskWithBatch(String pathToFileA, String pathToFileB, String pathToResult) {

        try (FileWriter writer = new FileWriter(pathToResult)) {

            File csvFileA = new File(pathToFileA);
            if (csvFileA.isFile()) {
                // read matrix B
                DoubleMatrix B = DoubleMatrix.loadCSVFile(pathToFileB);

                // create BufferedReader and read data from csv
                BufferedReader csvReader = new BufferedReader(new FileReader(pathToFileA));
                String row = "";
                while ((row = csvReader.readLine()) != null) {
                    DoubleMatrix rowMat = parseLineToMatrix(row).mmul(B);
                    for(int i = 0; i < rowMat.length; i++) {
                        writer.append(String.valueOf(rowMat.get(0, i)));
                        if(i < rowMat.length - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }
                csvReader.close();
            }
            else {
                System.out.println("CSV File A not found");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
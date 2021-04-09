package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class MatrixUtil {
    public static void genRandMat(int row, int col, int limit, int precision, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for(int i = 0; i < row; i++) {
                for(int j = 0; j < col; j++) {
                    double val = Math.random() * limit;
                    // 0 or lower means it is not double
                    if(precision <= 0) {
                        writer.append(String.valueOf(Math.floor(val)));
                    }
                    else {
                        writer.append(String.format("%." + precision + "f", val));
                    }
                    if(j < col - 1) {
                        writer.append(',');
                    }
                }
                writer.append('\n');
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}

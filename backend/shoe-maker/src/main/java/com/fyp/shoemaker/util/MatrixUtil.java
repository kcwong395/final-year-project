package com.fyp.shoemaker.util;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.jblas.ranges.IntervalRange;

import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MatrixUtil {

    public static DoubleMatrix fill0Rows(DoubleMatrix mat, int k) {
        return DoubleMatrix.concatVertically(mat, DoubleMatrix.zeros(k - mat.rows % k, mat.columns));
    }

    // deprecated
    public static DoubleMatrix[] partitionMatrix(DoubleMatrix A, int k) {
        DoubleMatrix[] res = new DoubleMatrix[k];
        for(int i = 0; i < k; i++) {
            IntervalRange r = new IntervalRange(i * A.rows / k, (i + 1) * A.rows / k);
            IntervalRange c = new IntervalRange(0, A.columns);
            res[i] = A.get(r, c);
        }
        return res;
    }

    public static DoubleMatrix[] encodeMatrix(int n, int k, DoubleMatrix[] A, DoubleMatrix encMat) {

        DoubleMatrix[] encA = new DoubleMatrix[n];

        for(int i = 0; i < n; i++) {
            encA[i] = A[0].mmul(encMat.get(i, 0));
            for(int j = 1; j < k; j++) {
                encA[i] = encA[i].add(A[j].mmul(encMat.get(i, j)));
            }
        }
        return encA;
    }

    public static DoubleMatrix decodeMatrix(DoubleMatrix encMat, int n, int k, Map<Integer, DoubleMatrix> ans) {

        LocalDateTime init = LocalDateTime.now();

        Map<Integer, DoubleMatrix> ansList = new ConcurrentHashMap<>();
        int ansCnt = 0;
        for(int i = 0; i < n; i++) {
            if(ans.containsKey(i) && ansCnt < k) {
                System.out.println(i + " is added");
                ansList.put(i, ans.get(i));
                ansCnt++;
            }
        }

        // Construct inverse matrix
        DoubleMatrix tmp = null;
        for(Integer id : ansList.keySet()) {
            if(tmp == null) {
                tmp = encMat.getRow(id);
            }
            else {
                tmp = DoubleMatrix.concatVertically(tmp, encMat.getRow(id));
            }
        }
        DoubleMatrix invMat = Solve.solve(tmp, DoubleMatrix.eye(tmp.rows));

        // Aggregate encoded result
        DoubleMatrix[] encA = new DoubleMatrix[k];
        int cnt = 0;
        for(int i = 0; i < n; i++) {
            if(ansList.containsKey(i)) {
                encA[cnt++] = ansList.get(i);
            }
        }

        // Decode it
        DoubleMatrix decA = null;
        for(int i = 0; i < k; i++) {
            if(decA == null) {
                tmp = encA[0].mmul(invMat.get(i, 0));
                for(int j = 1; j < k; j++) {
                    tmp = tmp.add(encA[j].mmul(invMat.get(i, j)));
                }
                decA = tmp;
            }
            else {
                tmp = encA[0].mmul(invMat.get(i, 0));
                for(int j = 1; j < k; j++) {
                    tmp = tmp.add(encA[j].mmul(invMat.get(i, j)));
                }
                decA = DoubleMatrix.concatVertically(decA, tmp);
            }
        }
        return decA;
    }

    // Should not use this, takes very long
    public static void saveCSV(DoubleMatrix mat, String fileName, boolean append) {
        try (FileWriter writer = new FileWriter(fileName, append)) {
            for (int i = 0; i < mat.rows; i++) {
                for (int j = 0; j < mat.columns; j++) {
                    writer.append(String.valueOf(mat.get(i, j)));
                    if (j < mat.columns - 1) {
                        writer.append(',');
                    }
                }
                writer.append("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

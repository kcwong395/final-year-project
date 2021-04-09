import utils.MatrixUtil;

import java.util.HashMap;
import java.util.Map;

public class generator {
    public static void main(String[] args) {
        Map<String, Integer> m = new HashMap<>();
        m.put("G", 500000);
        m.put("L", 50000);
        m.put("M", 5000);
        m.put("T", 500);

        MatrixUtil.genRandMat(m.get(args[0]), 500, 100, 0, "A.csv");
        MatrixUtil.genRandMat(500, 500, 100, 0, "B.csv");
    }
}

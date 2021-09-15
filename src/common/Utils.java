package common;


import java.util.HashMap;
import java.util.List;

public class Utils {
    /**
     * 对专家处理时间排序
     */
    /**
     * 这个操作到底在干啥
     * 把每个专家能够操作的任务按照解决时间进行排序？
     * @param list
     * @param processTimeMatrix
     */
    public void shellSort1(List<List<Integer>> list, int[][] processTimeMatrix) {
        for (int p = 0; p < list.size(); p++) {
            for (int step = list.get(p).size(); step > 0; step /= 2) {
                for (int i = step; i < list.get(p).size(); i++) {
                    int value = processTimeMatrix[list.get(p).get(i) - 1][p];
                    int q = list.get(p).get(i);
                    int j = i - step;
                    for (; j >= 0 && processTimeMatrix[list.get(p).get(j) - 1][p] > value; j -= step) {
                        list.get(p).set(j + step, list.get(p).get(j));
                    }
                    list.get(p).set(j + step, q);
                }
            }
        }
    }

    /**
     * 对任务的最晚响应时间排序
     * 这里使用辅助数组c记录下了所有的下标变换的情况
     */

    public int[] shellSort2(int a[]) {
        int[] b = a.clone();
        int[] c = new int[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = i + 1;
        }
        for (int step = b.length / 2; step > 0; step /= 2) {
            for (int i = step; i < b.length; i++) {
                int p = b[i];
                int q = c[i];
                int j;
                for (j = i - step; j >= 0 && b[j] > p; j -= step) {
                    b[j + step] = b[j];
                    c[j + step] = c[j];
                }
                b[j + step] = p;
                c[j + step] = q;
            }
        }
        return c;
    }


}

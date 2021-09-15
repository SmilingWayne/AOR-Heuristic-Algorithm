package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class Input {
    private String pathWorkOrder;
    private String pathProcess;

    private int[][] processTimeMatrix;
    private int[][] workOrder;


    /**
     * 构造函数
     *
     * @param pathWorkOrder WorkOrder表
     * @param pathProcess   ProcessMatrix表
     */
    public Input(String pathWorkOrder, String pathProcess) {
        this.pathWorkOrder = pathWorkOrder;
        this.pathProcess = pathProcess;

    }

    /**
     * 读取数据，数据存在Input的属性当中，get调用
     */
    public void readData() {
        /**表work order**/
        List<String[]> workOrderMatrix = new ArrayList<String[]>();
        try {
            File file = new File(this.pathWorkOrder);
            FileInputStream in = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new UnicodeReader(in, "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] items = line.replaceAll(" ", "").split(",");
                workOrderMatrix.add(items);
            }
            br.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int taskNum = workOrderMatrix.size();
        this.workOrder = new int[taskNum][4];
        for (int index = 0; index < taskNum; index++) {
            int taskId = Integer.parseInt(workOrderMatrix.get(index)[0]);
            int createTime = Integer.parseInt(workOrderMatrix.get(index)[1]);
            int questionId = Integer.parseInt(workOrderMatrix.get(index)[2]);
            int sla = Integer.parseInt(workOrderMatrix.get(index)[3]);

            this.workOrder[index][0] = taskId;
            this.workOrder[index][1] = createTime;
            this.workOrder[index][2] = questionId;
            this.workOrder[index][3] = sla;
        }

        /**表 process **/
        List<String[]> processMatrix = new ArrayList<String[]>();
        try {
            File file = new File(this.pathProcess);
            FileInputStream in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new UnicodeReader(in, "utf-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = line.replaceAll(" ", "").split(",");
                processMatrix.add(items);
            }
            reader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int questionsNum = processMatrix.get(0).length - 1;
        int expertsNum = processMatrix.size() - 1;
        this.processTimeMatrix = new int[expertsNum][questionsNum];
        for (int expert = 0; expert < expertsNum; expert++) {
            for (int question = 0; question < questionsNum; question++) {
                this.processTimeMatrix[expert][question] = Integer.parseInt(processMatrix.get(expert + 1)[question + 1]);
            }
        }
    }

    public int[][] getWorkOrder() {
        return workOrder;
    }

    public int[][] getProcessTimeMatrix() {
        return processTimeMatrix;
    }


}



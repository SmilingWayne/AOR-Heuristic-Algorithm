package common;

import java.util.*;

public class Reference {
    private List<List<Integer>> validQuestionOfExpert;
    private int[][] processTimeMatrix;
    private int[][] workOrder;
    private List<List<Integer>> expertsSortByProcessTime;
    private int[] tasksSortByMaxResTime;
    private int taskNum;    // 任务的数量
    private int expertNum;      // 专家的数量
    private int[][] matrix4TaskPara ;
    private int[][] matrix4WeightedTaskPara = new int[taskNum][2];

    /**
     * 这个函数的任务：给最初的8840个任务分配相对应的参数
     */
    public void createParaTaskSequence(){
        this.matrix4TaskPara = new int[taskNum][5];
        //System.out.println(this.matrix4TaskPara.length);
        for(int i = 0; i < taskNum; i ++ ){
            int[] paraArr = new int[5];
            //System.out.println(this.matrix4TaskPara.length);
            int taskType = this.getTaskQuestion(i + 1);
            paraArr[0] = this.getTaskCreateTime(i + 1);
            int targetExpert = expertsSortByProcessTime.get(taskType - 1).get(0);
            paraArr[1] = this.getProcessTimeMatrix()[targetExpert - 1][taskType - 1];
            paraArr[2] = this.getTaskSla(i + 1);
            paraArr[3] = this.expertsSortByProcessTime.get(taskType - 1).size();
            int secondMinProcessTimeExpert = this.expertsSortByProcessTime.get(taskType - 1).get(1);
            int minProcessTimeExpert = this.expertsSortByProcessTime.get(taskType - 1).get(0);
            paraArr[4] = this.getProcessTimeMatrix()[secondMinProcessTimeExpert - 1][taskType - 1]
                    - this.getProcessTimeMatrix()[minProcessTimeExpert - 1][taskType - 1];
            for(int j = 0 ; j < 5; j ++ ){
                this.matrix4TaskPara[i][j] = paraArr[j];
            }
        }
    }

    public void sortByMultipleIn(int[] weights){
        this.matrix4WeightedTaskPara = new int[taskNum][2];
        for(int i = 0; i < taskNum; i ++ ){
            this.matrix4WeightedTaskPara[i][0] = i + 1;
            for(int j = 0 ; j < 5; j ++ ){
                this.matrix4WeightedTaskPara[i][1] += (this.matrix4TaskPara[i][j] * weights[j]);
            }
        }
        Arrays.sort(this.matrix4WeightedTaskPara, (a,b) ->{
           if(a[1] - b[1] > 0){
               return 1;
           }
           else if(a[1] == b[1]){
               return 0;
           }
           else {
               return -1;
           }
        });
        for(int i = 0 ; i < this.matrix4WeightedTaskPara.length; i ++ ) {
            this.tasksSortByMaxResTime[i] = this.matrix4WeightedTaskPara[i][0];
        }
    }

    /**
     * 0.3 0.2 0.3 1.9
     * @return
     */

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public int getExpertNum() {
        return expertNum;
    }

    public void setExpertNum(int expertNum) {
        this.expertNum = expertNum;
    }
    // work order 矩阵存放任务的信息
    public Reference(int[][] processTimeMatrix, int[][] workOrder) {
        this.taskNum = workOrder.length;
        this.expertNum = processTimeMatrix.length;
        this.processTimeMatrix = processTimeMatrix;
        this.workOrder = workOrder;
        int[] taskMaxResTime = new int[workOrder.length];
        this.expertsSortByProcessTime = new ArrayList<List<Integer>>();
        this.tasksSortByMaxResTime = new int[workOrder.length];
        Utils utils = new Utils();
        // 排序
        // fill the list
        for (int q = 0; q < processTimeMatrix[0].length; q++) {
            List<Integer> expertForQuestion = new ArrayList<Integer>();
            for (int i = 0; i < processTimeMatrix.length; i++) {
                if (processTimeMatrix[i][q] < 999999) {
                    // expert ID = i+1, cause in this matrix, row index is the (expert id - 1)
                    expertForQuestion.add(i + 1);
                    // 这里给出的是每个专家适合做的任务有哪些，如果是999999的就不加
                }
            }
            this.expertsSortByProcessTime.add(expertForQuestion);
        }
        /*
        expertSortByProcessTime 里面 序号对应着专家号
         */
        for (int j = 0; j < workOrder.length; j++) {
            int maxResponseTime = this.workOrder[j][1] + this.workOrder[j][3];
            taskMaxResTime[j] = maxResponseTime;
        }

//        int[][] dummy = this.getWorkOrder();
//        for(int i = 0 ; i < dummy.length; i ++ ){
//            for(int j = 0; j < dummy[i].length; j ++ ){
//                System.out.print(dummy[i][j] + " ");
//            }
//            System.out.println("");
//        }
//        Arrays.sort(dummy, (a,b)->{
//           if(a[1] == b[1] && a[3] > b[3]){
//               return -1;
//           }
//           if(a[1] > b[1]){
//               return 1;
//           }
//           return -1;
//        });
//        int[] temp2 = new int[dummy.length];
//        for(int i = 0; i < temp2.length; i ++ ){
//            temp2[i] = dummy[i][0];
//        }
//        tasksSortByMaxResTime = temp2;

        //to sort
        /*
            这里对专家处理时间排序了
         */
        utils.shellSort1(expertsSortByProcessTime, processTimeMatrix);
        /**
         * 按照最大剩余时间排序
         */
        tasksSortByMaxResTime = utils.shellSort2(taskMaxResTime);
        /**
         * 先尝试修改一下数据的输入的情况
         */

        //构造validQuestionOfExpert
        /**
         * valid这函数是在干啥？
         * 针对每一个专家给出他所有可以做的工作？
         */
        this.validQuestionOfExpert = new ArrayList<List<Integer>>();
        for (int expert = 1; expert <= this.expertNum; expert++) {
            List<Integer> questionForOneExpert = new ArrayList<Integer>();
            for (int question = 1; question <= processTimeMatrix[0].length; question++) {
                if (processTimeMatrix[expert - 1][question - 1] != 999999) {
                    questionForOneExpert.add(question);
                }
            }
            this.validQuestionOfExpert.add(questionForOneExpert);
        }

    }

    /**
     * statistics
     * two targets:
     *      calculate how many tasks for each type
     *      calculate how many task
     *
     */
    public void displayTasksStatistics(){
        int[] typesArr = new int[this.processTimeMatrix[0].length];
        for(int i = 0; i < taskNum; i ++ ){
            int taskType = workOrder[i][2];
            typesArr[taskType - 1] += 1;
        }
        System.out.println("\n\tTypes-Tasks statistics...");
        for(int i = 0 ; i < processTimeMatrix[0].length; i ++ ){
            System.out.println("\t\tType (" + (i+1) + ") has " + typesArr[i] + " tasks.");
        }
        int[] typesMinProcessTime = new int[this.processTimeMatrix[0].length];
        System.out.println("\n\tTypes-mini-processTime statistics...");
        for(int i = 0; i < this.processTimeMatrix[0].length; i ++ ){
            int expert = this.expertsSortByProcessTime.get(i).get(0);
            typesMinProcessTime[i] = this.processTimeMatrix[expert - 1][i];
            System.out.println("\t\tType (" + (i+1) + ") minimum process time is " + typesMinProcessTime[i] + ".");
        }
        System.out.println("\n\tTypes-available experts statistics...");
        int[] typesAvailableExperts = new int[this.processTimeMatrix[0].length];
        for(int i = 0; i < this.processTimeMatrix[0].length; i ++ ){
            typesAvailableExperts[i] = this.expertsSortByProcessTime.get(i).size();
            System.out.println("\t\tType ("+(i+1)+") available experts are " + typesAvailableExperts[i] + ".");
        }
        System.out.println("\n\tExperts available tasks statistics...");
        int[] expertsAvailableTypes = new int[this.processTimeMatrix.length];
        for(int i = 0 ; i < this.processTimeMatrix.length; i ++ ){
            for(int j = 0 ; j < this.processTimeMatrix[0].length; j ++ ){
                if(this.expertsSortByProcessTime.get(j).contains(i+1)){
                    expertsAvailableTypes[i] += 1;
                }
            }
            System.out.println("\t\tExpert("+(i+1)+") can process "+expertsAvailableTypes[i] + " tasks.");
        }
    }

    public void sortTasksByWeights(int[] weights){
        this.createParaTaskSequence();
        this.sortByMultipleIn(weights);
    }

    public int[][] getProcessTimeMatrix() {
        return processTimeMatrix;
    }

    public int[][] getWorkOrder() {
        return workOrder;
    }

    public void setProcessTimeMatrix(int[][] processTimeMatrix) {
        this.processTimeMatrix = processTimeMatrix;
    }

    public void setWorkOrder(int[][] workOrder) {
        this.workOrder = workOrder;
    }


    public List<List<Integer>> getExpertsSortByProcessTime() {
        return expertsSortByProcessTime;
    }

    public int[] getTasksSortByMaxResTime() {
        return tasksSortByMaxResTime;
    }

    public void setExpertsSortByProcessTime(List<List<Integer>> expertsSortByProcessTime) {
        this.expertsSortByProcessTime = expertsSortByProcessTime;
    }

    public void setTasksSortByMaxResTime(int[] tasksSortByMaxResTime) {
        this.tasksSortByMaxResTime = tasksSortByMaxResTime;
    }

    public int getTaskCreateTime(int taskId) {
        return this.workOrder[taskId - 1][1];
    }

    public int getTaskSla(int taskId) {
        return this.workOrder[taskId - 1][3];
    }

    public int getTaskQuestion(int taskId) {
        return this.workOrder[taskId - 1][2];
    }

    public List<List<Integer>> getValidQuestionOfExpert() {
        return validQuestionOfExpert;
    }
}

package common;


import java.util.*;

public class Solution {
    Reference reference;
    private List<List<Integer>> solution = new ArrayList<List<Integer>>();//todo hashmap(key:task_id)

    private Map<Integer, List<int[]>> map4TaskExpertSequence = new HashMap<>();
    // 这里加一下注释
    //private Map<Integer, Map<Integer, Integer>> map4ExpertTaskStartTime = new HashMap<>();
    private int[][] expertProcessTaskNum;
    public void displayExpertProcessTaskNum() {
        for (int i = 0; i < 133; i++ ) {
            for (int j = 492; j < 4651; j++) {
                System.out.print(this.expertProcessTaskNum[i][j] + " ");
            }
            System.out.println("");
        }
    }
    public Solution(Reference reference) {
        this.expertProcessTaskNum = new int[reference.getExpertNum()][10000];
        this.reference = reference;
    }
    // 构造函数只是针对expertProcessTaskNum的
    // 这里注意一下参数是startTime 和 endTime
    //
    // 下面这里好像暂时不需要
//    public void setMap4ExpertTaskStartTime(int expertId, int taskId, int startTime){
//        if(!this.map4ExpertTaskStartTime.containsKey(expertId)){
//            Map<Integer, Integer> temp = new HashMap<>();
//            temp.put(taskId, startTime);
//        }
//        else{
//            this.map4ExpertTaskStartTime.get(expertId).put(taskId, startTime);
//        }
//    }
//    public void getMap4ExpertTaskStartTime(int expertId, int taskId, int startTime){
//        if(!this.map4ExpertTaskStartTime.containsKey(expertId)){
//            System.out.println(expertId + " 专家没有解决任务！");
//            return;
//        }
//        if(!this.map4ExpertTaskStartTime.get(expertId).containsKey(taskId)){
//            System.out.println(expertId + " 专家未解决" + taskId + "任务");
//            return;
//        }
//
//
//    }
    public void addExpertProcessTaskNum(int expertId, int startTime, int endTime) {
        for (int i = startTime; i < endTime; i++) {
            expertProcessTaskNum[expertId - 1][i] += 1;
        }
    }

    public void subExpertProcessTaskNum(int expertId, int startTime, int endTime) {
        for (int i = startTime; i < endTime; i++) {
            expertProcessTaskNum[expertId - 1][i] -= 1;
        }
    }

    public void addSchedule(int taskId, int expertId, int startTime, int endTime) {
        List<Integer> schedule = new ArrayList<Integer>();
        schedule.add(taskId);
        schedule.add(expertId);
        schedule.add(startTime);
        schedule.add(endTime);
        this.solution.add(schedule);
        addExpertProcessTaskNum(expertId, startTime, endTime);
        //System.out.println("allocate Task(" + taskId + ") to Expert(" + expertId + "), startTime: " + startTime + ", endTime: " + endTime);
    }

    public int[][] getSolutionMatrix() {
        int scheduleNum = this.solution.size();
        int elementNum = this.solution.get(0).size();
        int[][] solutionMatrix = new int[scheduleNum][elementNum];
        for (int i = 0; i < scheduleNum; i++) {
            for (int j = 0; j < elementNum; j++)
                solutionMatrix[i][j] = this.solution.get(i).get(j);
        }
        return solutionMatrix;
    }

    public List<List<Integer>> getSolution() {
        return solution;
    }


    public void shellSort(int[][] solution, int index) {

        for (int step = solution.length / 2; step > 0; step /= 2) {
            for (int i = step; i < solution.length; i++) {
                int[] list1 = solution[i];
                int value = list1[index];
                int j;
                for (j = i - step; j >= 0 && solution[j][index] > value; j -= step) {
                    int[] list2 = solution[j];
                    solution[j + step] = list2;
                }
                solution[j + step] = list1;
            }
        }
    }


//    public boolean checkSolution(Reference reference) {
//        int size = this.solution.size();
//        int[] taskAllocateTimes = new int[reference.getTaskNum()];
//        int[] taskAllocate = new int[reference.getTaskNum()];
//        int[] taskTimes = new int[reference.getTaskNum()];
//        int[] expertProcessingNum = new int[reference.getExpertNum()];
//        int[][] solutionMatrix = getSolutionMatrix();
//        int[][] solutionStartSort = new int[solutionMatrix.length][4];
//        int[][] solutionEndSort = new int[solutionMatrix.length][4];
//        for(int i=0;i<solutionMatrix.length;i++){
//            solutionEndSort[i]=solutionMatrix[i].clone();
//            solutionStartSort[i]=solutionMatrix[i].clone();
//        }
//        this.shellSort(solutionEndSort, 3);
//        this.shellSort(solutionStartSort, 2);
//        for (int i = 0; i < size; i++) {
//            taskAllocateTimes[this.solution.get(i).get(0) - 1]++;
//            if (taskAllocateTimes[this.solution.get(i).get(0) - 1] > 5) {
//                System.out.println("任务调度次数超过限额: " + solution.get(i).get(0));
//                return false;
//            }
//        }
//        int i = 0;
//        int j = 0;
//        for (int time = solutionStartSort[0][2]; i < size; time++) {
//            for (; i < size && solutionEndSort[i][3] == time; i++) {
//                expertProcessingNum[solutionEndSort[i][1] - 1]--;
//                taskTimes[solutionEndSort[i][0] - 1]--;
//            }
//            for (; j < size && solutionStartSort[j][2] == time; j++) {
//                taskAllocate[solutionStartSort[j][0] - 1] = 1;
//                if (solutionStartSort[j][1] > reference.getExpertNum() || solutionStartSort[j][1] < 1) {
//                    System.out.println("存在无效专家id: " + solutionStartSort[j][1]);
//                    return false;
//                }
//                if (solutionStartSort[j][0] > reference.getTaskNum() || solutionStartSort[j][0] < 1) {
//                    System.out.println("存在无效任务id: " + solutionStartSort[j][0]);
//                    return false;
//                }
//                expertProcessingNum[solutionStartSort[j][1] - 1]++;
//                taskTimes[solutionStartSort[j][0] - 1]++;
//                if (taskTimes[solutionStartSort[j][0] - 1] > 1) {
//                    System.out.println("同一任务同一时间对应多个小二id: " + solutionStartSort[j][0]);
//                    return false;
//                }
//                if (expertProcessingNum[solutionStartSort[j][1] - 1] > 3) {
//                    System.out.println("小二并发处理量超限: " + (solutionStartSort[j][1]) + " " + solutionStartSort[j][0]);
//                    return false;
//                }
//            }
//        }
//        for (int p = 0; p < reference.getExpertNum(); p++) {
//            if (taskAllocate[p] == 0) {
//                System.out.println("存在未分配的任务: " + p);
//                return false;
//            }
//        }
//        return true;
//
//    }

    /**
     * 这个是统计最后得分的函数？
     * @param reference
     * @return
     */

    public double[] accumulateScore(Reference reference) {
        double score = 0;
        int[][] processTimeMatrix = reference.getProcessTimeMatrix();
        int[][] workOrder = reference.getWorkOrder();
        int[] expertProcessTime = new int[reference.getExpertNum()];
        int[] taskProcessTime = new int[reference.getTaskNum()];
        int[] taskValidTime = new int[reference.getTaskNum()];
        int[] taskProcessStartTime = new int[reference.getTaskNum()];
        Iterator<List<Integer>> iterator = this.solution.iterator();
        while (iterator.hasNext()) {
            List<Integer> list = iterator.next();
            int time = list.get(3) - list.get(2);
            int i = list.get(1) - 1;
            int j = list.get(0) - 1;
            expertProcessTime[i] += time;
            taskProcessTime[j] += time;
            if (processTimeMatrix[i][workOrder[j][2] - 1] == time) {
                taskValidTime[j] = time;
            }
            if (taskProcessStartTime[j] != 0) {
                taskProcessStartTime[j] = Math.min(taskProcessStartTime[j], list.get(2));
            } else {
                taskProcessStartTime[j] = list.get(2);
            }
        }
        int totalProcessingTime = 0;
        double averageLoad = 0;
        double loadStandardDev = 0;
        for (int i = 0; i < reference.getExpertNum(); i++) {
            totalProcessingTime += expertProcessTime[i];
        }
        averageLoad = totalProcessingTime / (double) (60 * 8 * 3 * reference.getExpertNum());
        for (int i = 0; i < reference.getExpertNum(); i++) {
            loadStandardDev += Math.pow(expertProcessTime[i] / (double) (60 * 8 * 3) - averageLoad, 2);
        }
        loadStandardDev = Math.sqrt(loadStandardDev / reference.getExpertNum());
        double timeout = 0;
        double efficiency = 0;
        int stayTime = 0;
        List<List<Integer>> expertsSortByProcessTime = reference.getExpertsSortByProcessTime();
        Iterator<List<Integer>> iterator1 = this.solution.iterator();
        for (int i = 0; i < workOrder.length; i++) {
            int question = workOrder[i][2];
            efficiency += processTimeMatrix[expertsSortByProcessTime.get(question - 1).get(0) - 1][question - 1];
        }
        while (iterator1.hasNext()) {
            List<Integer> list = iterator1.next();
            int time = list.get(3) - list.get(2);
        }
        for (int i = 0; i < reference.getTaskNum(); i++) {
            int p = Math.max(taskProcessStartTime[i] - workOrder[i][1] - workOrder[i][3], 0);
            timeout += p / (double) (workOrder[i][3]);
            stayTime += taskProcessTime[i] + taskProcessStartTime[i] - workOrder[i][1];
        }
        timeout /= reference.getTaskNum();
        efficiency /= stayTime;
//        System.out.println("M bar " + timeout);
//        System.out.println("R bar " + efficiency);
//        System.out.println("L StanDev " + loadStandardDev);
        score = 1000 * efficiency - 99 * timeout - 99 * loadStandardDev;
        double[] result = new double[4];
        result[0] = score;
        result[1] = timeout;
        result[2] = efficiency;
        result[3] = loadStandardDev;
        return result;
    }

    public void setSolution(List<List<Integer>> solution) {
        this.solution = solution;
    }

    // 检查在一个时间点内专家的任务分配是否超过3个

    public boolean expertIdleInPeriod(int expertId, int startTime, int endTime) {
        for (int i = startTime; i < endTime; i++) {
            if (expertProcessTaskNum[expertId - 1][i] >= 3) {
                return false;
            }
        }
        return true;
    }

    /**
     * 这个整理函数负责把前后序号相同的结合在一起
     */

    public void tidyUp(){
        for(int i = 0 ; i < solution.size(); i ++ ){
            int taskId = solution.get(i).get(0), expertId = solution.get(i).get(1),
                    startTime = solution.get(i).get(2), endTime = solution.get(i).get(3);
            int[] temp = new int[]{startTime, endTime, expertId};
            if(!this.map4TaskExpertSequence.containsKey(taskId)){
                this.map4TaskExpertSequence.put(taskId, new ArrayList<int[]>());
            }
            this.map4TaskExpertSequence.get(taskId).add(temp);
        }
        for(Integer i : this.map4TaskExpertSequence.keySet()){
            Collections.sort(this.map4TaskExpertSequence.get(i), (a,b)->{
                if(a[0] >= b[0]){
                    return 1;
                }
                return -1;
            });
        }
//        for(int i = 0; i < this.map4TaskExpertSequence.get(4868).size(); i ++ ){
//            System.out.println(this.map4TaskExpertSequence.get(4868).get(i)[0]);
//        }
    }

    public void mergeSame(){
        for(Integer taskId : this.map4TaskExpertSequence.keySet()){
            List<int[]> tempList = this.map4TaskExpertSequence.get(taskId);
            if(tempList.size() == 1){
                continue;
            }
            for(int i = 0 ; i < tempList.size() - 1; i ++ ){
                if(tempList.get(i)[2] != tempList.get(i + 1)[2]){
                    continue;
                }
                int expertId = tempList.get(i)[2] ;
                int totalEndTime = tempList.get(tempList.size() - 1)[1];
                int editedEndTime = tempList.get(i)[1], deletedStartTime = tempList.get(i)[0], deletedEndTime = tempList.get(i)[1];
                while(i + 1 < tempList.size() && expertId == tempList.get(i + 1)[2]){
                    this.removeSchedule(taskId, expertId , tempList.get(i + 1)[0], tempList.get(i + 1)[1]);
                    editedEndTime = tempList.get(i + 1)[1];
                    i ++ ;
                }
                i -- ;
                this.removeSchedule(taskId, expertId, deletedStartTime,deletedEndTime);
                //System.out.println(expertId + "  " + taskId +" " +  deletedStartTime + "  " +  deletedEndTime + " " +  (this.reference.getTaskQuestion(taskId) - 1));
                this.addSchedule(taskId, expertId, deletedStartTime, Math.min(
                        deletedStartTime + this.reference.getProcessTimeMatrix()[expertId - 1][this.reference.getTaskQuestion(taskId) - 1],
                        Math.min(totalEndTime, editedEndTime)));
                //this.addSchedule(taskId, expertId, deletedStartTime, editedEndTime);
            }
        }
    }
//    public int getTaskExpertByEndTime(int taskId, int endTime){
//        if(!this.map4TaskExpertSequence.containsKey(taskId)){
//            System.out.println("任务序号有误!");
//            return -1;
//        }
//        if(!this.map4TaskExpertSequence.get(taskId).containsKey(endTime)){
//            System.out.println("任务(" + taskId  + ")结束时间有误");
//            return -1;
//        }
//        return this.map4TaskExpertSequence.get(taskId).get(endTime);
//    }

    public void removeSchedule(int taskId, int expertId, int startTime, int endTime) {
        Iterator<List<Integer>> iterator = this.solution.iterator();
        boolean remove = false;//判断是否存在此任务，能够被移除
        while (iterator.hasNext()) {
            List<Integer> list = iterator.next();
            if (list.get(0) == taskId && list.get(1) == expertId && list.get(2) == startTime && list.get(3) == endTime) {
                iterator.remove();
                remove = true;
                break;
            }
        }
        subExpertProcessTaskNum(expertId, startTime, endTime);
    }

    // Solution 中存放的是什么东西？

    // 这里的list就是指每一项任务

    public int getTaskReallocateTimes(int taskId) {
        int times = 0;
        for (List<Integer> list : this.solution
        ) {
            if (list.get(0) == taskId)
                times++;
        }
        return times;
    }


    public void exchangeTask(int task1, int task2) {
        for (List<Integer> taskItem : this.solution) {
            if (taskItem.get(0) == task1) {
                taskItem.set(0, task2);
                continue;
            }
            if (taskItem.get(0) == task2) {
                taskItem.set(0, task1);
//                continue;
            }
        }
    }

    public int removeSchedule(int taskId, int startTime) {
        Iterator<List<Integer>> iterator = this.solution.iterator();
        int endTime = 0;
        int expertId = 0;
        boolean remove = false;//判断是否存在此任务，能够被移除
        while (iterator.hasNext()) {
            List<Integer> list = iterator.next();
            if (list.get(0) == taskId && list.get(2) == startTime) {
                endTime = list.get(3);
                expertId = list.get(1);
                iterator.remove();
                remove = true;
                break;
            }
        }
        subExpertProcessTaskNum(expertId, startTime, endTime);
        return expertId;
    }

    public int findTaskOrder(int expertId, int startTime) {
        int i = 0;
        for (List<Integer> taskItem : this.solution) {
            i++;
            if (taskItem.get(1) == expertId && taskItem.get(2) == startTime) {

                return i;
            }
        }
        return -1;
    }


    public void checkContinuousAllocate(Reference reference) {//判断是否连续分配给同一个专家
        ArrayList<HashMap<Integer, Integer>> allocate = new ArrayList<HashMap<Integer, Integer>>();//存放的是每个任务 什么时间 哪个专家处理
        int[][] matrix = getSolutionMatrix();
        for (int i = 1; i <= reference.getWorkOrder().length; i++) {
            allocate.add(new HashMap<Integer, Integer>());
        }
        for (int i = 0; i < matrix.length; i++) {
            int task = matrix[i][0];
            int expert = matrix[i][1];
            int start = matrix[i][2];
            allocate.get(task - 1).put(start, expert);
        }
        List<List<Integer>> remove = new ArrayList<List<Integer>>();
        //遍历每一个任务 检查是否有连续分配的情况
        for (int i = 1; i <= reference.getWorkOrder().length; i++) {
            HashMap<Integer, Integer> allo = allocate.get(i - 1);
            //按照处理任务的先后顺序给专家排序
            List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(allo.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o1.getKey() - o2.getKey();
                }
            });
            ArrayList<Integer> start = new ArrayList<Integer>();
            ArrayList<Integer> experts = new ArrayList<Integer>();
            Iterator<Map.Entry<Integer, Integer>> iterator = list.iterator();
            while (iterator.hasNext()) {
                int time = iterator.next().getKey();
                int exp = allo.get(time);
                start.add(time);
                experts.add(exp);
            }
            for (int j = 0; j < experts.size() - 1; j++) {
                if ((int)experts.get(j) == (int)experts.get(j + 1)) {
                    //System.out.println("任务 (" + i + "), 被连续分配给两个专家" + " " + (int)experts.get(j) +  " " + experts.get(j + 1) );
                    for (int h = 0; h < solution.size(); h++) {
                        int taskId = solution.get(h).get(0);
                        int exp = solution.get(h).get(1);
                        int st = solution.get(h).get(2);
                        if (taskId == i && exp == experts.get(j) && st == start.get(j + 1)) {
                            remove.add(solution.get(h));
                        }
                    }
                }
            }
        }
        solution.removeAll(remove);
    }
}

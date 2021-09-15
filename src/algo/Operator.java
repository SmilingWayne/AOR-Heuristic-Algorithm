package algo;

import common.Middleware;
import common.Reference;
import common.Solution;

import java.util.*;
public class Operator {
    Middleware middleware;
    Reference reference;
    Solution solution;
    Set<Integer> operated_Set;

    public Operator(Middleware middleware, Reference reference, Solution solution){
        this.middleware = middleware;
        this.reference = reference;
        this.solution = solution;
        this.operated_Set = new HashSet<>();
    }

    /**
     * perTurb
     *      原理：随机选择两个任务，交换处理专家
     *          剪枝：如果两个任务出现时间大于500，舍去，重新选择
     *               如果两个任务专家相同，舍去
     *               如果两个任务之间处理时间相差过大，舍去
     *          备选原理：待定
     */
    public void perturb(){
        System.out.println("Before perturb, length is  " + middleware.getArr4ExceedTask().size());
        Random r = new Random(System.currentTimeMillis());
//        Set<Integer> toBDeleted = new HashSet<>();
//        List<Integer> tempExceedTask = middleware.getArr4ExceedTask();
        for(int i = 0; i < 30; i ++ ){
            int firstTask = r.nextInt(reference.getTaskNum());
            int secondTask = r.nextInt(reference.getTaskNum());
            if(firstTask == secondTask || firstTask == 0 || secondTask == 0){
                i -- ;
                continue;
            }
            int firstExpert = middleware.getFirstExpertForTask(firstTask);
            int secondExpert = middleware.getFirstExpertForTask(secondTask);
            if(firstExpert == secondExpert){
                i -- ;
                continue;
            }
            if(Math.abs(reference.getTaskSla(firstTask) + reference.getTaskCreateTime(firstTask) -
                    reference.getTaskSla(secondTask) - reference.getTaskCreateTime(secondTask)) > 500){
                i -- ;
                continue;
            }
            int firstTaskType = reference.getTaskQuestion(firstTask);
            int secondTaskType = reference.getTaskQuestion(secondTask);
            int[][] processTimeMatrix = reference.getProcessTimeMatrix();
            int firstExpert4SecondTask = processTimeMatrix[firstExpert - 1][secondTaskType - 1];
            int secondExpert4FirstTask = processTimeMatrix[secondExpert - 1][firstTaskType - 1];
            if(firstExpert4SecondTask > 200 || secondExpert4FirstTask > 200){
                i -- ;
                continue;
            }
            int firstCreateTime = reference.getTaskCreateTime(firstTask);
            int secondCreateTime = reference.getTaskCreateTime(secondTask);
            int firstTaskNewStartTime = 0;
            int secondTaskNewStartTime = 0;
            String swap_judge = "";
            int flag_1 = -1;
            int flag_2 = -1;
            for(int t = firstCreateTime; ; t ++ ){
                if(solution.expertIdleInPeriod(secondExpert, t, t + secondExpert4FirstTask)){
                    if( t <= reference.getTaskCreateTime(firstTask) + reference.getTaskSla(firstTask)){
                        if(middleware.getArr4ExceedTask().contains(firstTask)){
                            middleware.removeExceedTaskExpertPos(firstTask);
                            middleware.removeMap4ExpertNonFeasibleTask(firstExpert, firstTask);
                            swap_judge = swap_judge + "preTask";

                            middleware.removeArr4ExceedTask(firstTask);
                        }
                    }
                    else{
                        List<Integer> experts = reference.getExpertsSortByProcessTime().get(firstTaskType - 1);
                        for(int k = 0; k < experts.size(); k ++ ){
                            if(experts.get(k) == secondExpert){
                                flag_1 = k;
                                break;
                            }
                        }
                        if(middleware.getArr4ExceedTask().contains(firstTask)){
                            middleware.setMap4ExceedTaskExpertPos(firstTask, flag_1);
                        }
                        else{
                            middleware.setMap4ExceedTaskExpertPos(firstTask, flag_1);
                            middleware.setArr4ExceedTask(firstTask);
                            if(!middleware.getArr4ExceedTask().contains(firstTask)) {
                                middleware.removeMap4ExpertFeasibleTask(firstExpert, firstTask);
                            }
                            middleware.setMap4ExpertNonFeasibleTask(secondExpert, firstTask);
                        }
                    }
                    firstTaskNewStartTime = t;
                    break;
                }
            }
            // 上面的是针对firstTask进行处理的情况
            for(int t = secondCreateTime; ; t ++ ){
                if(solution.expertIdleInPeriod(firstExpert, t, t + firstExpert4SecondTask)){
                    if( t <= reference.getTaskCreateTime(secondTask) + reference.getTaskSla(secondTask)){
                        if(middleware.getArr4ExceedTask().contains(secondTask)){
                            middleware.removeExceedTaskExpertPos(secondTask);
                            middleware.removeMap4ExpertNonFeasibleTask(secondExpert, secondTask);
                            swap_judge = swap_judge + "_taskId";
                            middleware.removeArr4ExceedTask(secondTask);
                        }
                    }
                    else{
                        List<Integer> experts = reference.getExpertsSortByProcessTime().get(secondTaskType - 1);
                        for(int k = 0; k < experts.size(); k ++ ){
                            if(experts.get(k) == firstExpert){
                                flag_2 = k;
                                break;
                            }
                        }
                        if(middleware.getArr4ExceedTask().contains(secondTask)){
                            middleware.setMap4ExceedTaskExpertPos(firstTask, flag_2);
                        }
                        else{
                            middleware.setMap4ExceedTaskExpertPos(secondTask, flag_2);
                            middleware.setArr4ExceedTask(secondTask);
                            middleware.removeMap4ExpertFeasibleTask(secondExpert, secondTask);
                            middleware.setMap4ExpertNonFeasibleTask(firstExpert, secondTask);
                        }
                    }
                    secondTaskNewStartTime = t;
                    break;
                }
            }
            two_task_swap(firstTask, secondTask, firstExpert, secondExpert, secondTaskNewStartTime,
                    secondTaskNewStartTime + firstExpert4SecondTask,
                    firstTaskNewStartTime, firstTaskNewStartTime + secondExpert4FirstTask, swap_judge);
        }
        middleware.displayMap4ExpertsNonFeasibleTasks();
        System.out.println("Oversize is "+ middleware.getArr4ExceedTask().size());

    }
    /**
     * 这个算子效果很差劲，舍去它！
     */
    public void two_switch_2(){
        // todo 对于每一个专家看看能不能通过前后转换调动增加
        // List<Integer> exceedList = middleware.getArr4ExceedTask();

        for(int i = 0; i < reference.getExpertNum(); i ++ ){
            Set<Integer> toBDeleted = new HashSet<>();
            List<Integer> expertNonFeasible = middleware.getMap4ExpertNonFeasibleTask(i + 1);
            if(expertNonFeasible.isEmpty()){
                continue;
            }
            List<Integer> expertFeasible = middleware.getMap4ExpertFeasibleTask(i + 1);
            if(expertFeasible.isEmpty()){
                continue;
            }
            for(int j = 0; j < expertNonFeasible.size(); j ++ ){
                int taskId = expertNonFeasible.get(j);
                int currentTaskStartTime = middleware.getTaskStartTime(taskId);
                List<Integer> preTasks = new ArrayList<Integer>(middleware.getExpertTaskEndTime(i + 1, currentTaskStartTime - 1));
                if(preTasks.isEmpty()){
                    continue;
                }
                //System.out.println("PreTasks size = " + preTasks.size());
                for(int k = 0 ; k < preTasks.size(); k ++ ){
                    int preTask = preTasks.get(k);
                    if(middleware.getMap4ExpertFeasibleTask(i + 1).contains(preTask)){
                        //System.out.println("Find One " + preTasks.size());
                        int preTaskFinalTime = reference.getTaskCreateTime(preTask) + reference.getTaskSla(preTask);
                        int preTaskStartTime = middleware.getTaskStartTime(preTask);
                        int currentCreateTime = reference.getTaskCreateTime(taskId);
                        int currentFinalTime = reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId);
                        int currentEndTime = middleware.getTaskEndTime(taskId);
                        if(currentCreateTime <= preTaskStartTime && preTaskStartTime <= currentFinalTime
                        && currentTaskStartTime <= preTaskFinalTime){
                            //System.out.println("Find!");
                            this.two_task_swap(preTask, taskId, i + 1, i + 1,
                                    preTaskStartTime, preTaskStartTime + currentEndTime - currentTaskStartTime,
                                    preTaskStartTime + currentEndTime - currentTaskStartTime + 1, currentEndTime, "taskId");
                            toBDeleted.add(taskId);
                        }
                    }
                }
            }
            middleware.removeArr4ExceedTask(toBDeleted);
            middleware.removeExceedTaskExpertPos(toBDeleted);
            //System.out.println("toBdeleted size = " + toBDeleted.size());
//            if(toBDeleted.size() > 0){
//                System.out.println(toBDeleted);
//            }
            middleware.removeMap4ExpertNonFeasibleTask(i + 1, toBDeleted);
        }
        System.out.println("Now the nonFeasible task is " + middleware.getArr4ExceedTask().size());
    }

    /**
     * 这个是通过把专家无脑向前面塞，看能不能填入前面，让任务的开始时间靠近响应时间的末尾
     */
    public void allocateInvalid(){
        List<Integer> exceedList = middleware.getArr4ExceedTask();
        Set<Integer> toBDeleted = new HashSet<>();
        Collections.sort(exceedList, (a,b) ->{
            if(middleware.getTaskVaildStartTime(a) - (reference.getTaskSla(a) + reference.getTaskCreateTime(a)) <
            middleware.getTaskVaildStartTime(b) - (reference.getTaskSla(b) + reference.getTaskCreateTime(b))){
                return 1;
            }
            else if(middleware.getTaskVaildStartTime(a) - (reference.getTaskSla(a) + reference.getTaskCreateTime(a)) ==
                    middleware.getTaskVaildStartTime(b) - (reference.getTaskSla(b) + reference.getTaskCreateTime(b))){
                if(a >= b){
                    return 1;
                }
                else{
                    return -1;
                }
            }
            else{
                return -1;
            }
        });
//        for(int i = 0; i < exceedList.size(); i ++ ) {
//            System.out.println(exceedList.get(i) + " " +
//                    (middleware.getTaskVaildStartTime(exceedList.get(i)) - (reference.getTaskSla(exceedList.get(i)) + reference.getTaskCreateTime(exceedList.get(i)))));
//        }
        /*
         这里说明上面的排序是正确的
         */
        /* 这里重新试一下启发算法思路
         */
        for(int i = 0; i < exceedList.size() ; i ++ ) {

            int taskId = exceedList.get(i);
            //System.out.println(taskId);
            int taskCreatedTime = reference.getTaskCreateTime(taskId);
            int taskStartTime = middleware.getTaskStartTime(taskId);
            int taskFinalTime = taskCreatedTime + reference.getTaskSla(taskId);
            List<Integer> originalAverageList = middleware.getBelowAverageWorkingTimeExpert();
            //System.out.println("total below " + originalAverageList.size());
            Collections.sort(originalAverageList, (a,b)->{
                if(middleware.getExpertProcessTime(a) > middleware.getExpertProcessTime(b)){
                    return 1;
                }
                else if(middleware.getExpertProcessTime(a) == middleware.getExpertProcessTime(b)){
                    if(a >= b){
                        return 1;
                    }
                    else{
                        return -1;
                    }
                }
                else{
                    return -1;
                }
            });

//            for(int p = 0; p < originalAverageList.size(); p ++ ){
//                System.out.println(originalAverageList.get(p) + " value : " + middleware.getExpertProcessTime(originalAverageList.get(p)));
//            }
//            break;
//            Collections.sort(avgArr, new Comparator<int>(){
//                public int compare(int a, int b){
//                    if(middleware.getExpertProcessTime(a) > middleware.getExpertProcessTime(b)){
//                        return 1;
//                    }
//                    return -1;
//                }
//            });
            // 这里有一个坑 lambda 表达式的逻辑准确性

            List<Integer> belowAverageList = new ArrayList<Integer>(originalAverageList);
            //List<Integer> belowAverageList = middleware.getBelowAverageWorkingTimeExpert();
            int j = taskStartTime;
            //int preExpert = -1;
            for(int k = 0 ; k < belowAverageList.size(); k ++ ) {
                if(middleware.getTaskAllocateTimes(taskId) == 5 ){
                    continue;
                }
                if(belowAverageList.get(k) == middleware.getTaskPreExpert(taskId)){
                    continue;
                }
                if (middleware.getExpertProcessTime(belowAverageList.get(k)) > (int) (middleware.totalExpertProcessTime / (reference.getExpertNum()))) {
                    continue;
                }
                int addingExpert = belowAverageList.get(k);
                if(!solution.expertIdleInPeriod(addingExpert, j - 1, j) ){
                    continue;
                }
                int postStartTime = j - 1;
                boolean satisfied = false;
                while(postStartTime > taskFinalTime){
                    if(!solution.expertIdleInPeriod(addingExpert ,postStartTime, j)){
                        postStartTime += 1;
                        break;
                    }
                    else{
                        postStartTime -= 1;
                    }
                }
                if(postStartTime == taskFinalTime){
                    if(solution.expertIdleInPeriod(addingExpert, postStartTime, j))
                        satisfied = true;
                    else{
                        postStartTime += 1;
                    }
                }
                //middleware.addTotalExpertProcessTime(j - postStartTime);
                this.allocateInvalidOperation(addingExpert, taskId, postStartTime, j);
                j = postStartTime;
                if(satisfied){
                    toBDeleted.add(taskId);
                    // todo 思考一下这边逻辑是否需要补充
                    //middleware.setMap4Type2Task(reference.getTaskQuestion(taskId), taskId);
                    /*
                    这里深层次涉及到一个逻辑问题：必须在switch的函数执行完了之后才能继续执行本算子，因为在switch函数中
                    有一个swap_Task_2的操作会改变TaskStartTime和TaskValidStartTime 而这种情况下是不能直接改变的
                    这里需要仔细仔细仔细思考
                     */
                    break;
                }
            }
        }
        middleware.removeExceedTaskExpertPos(toBDeleted);
        middleware.removeArr4ExceedTask(toBDeleted);
        System.out.println("\tAllocation of Invalid Tasks terminated. \n\tNow we have exceed Tasks: " + middleware.getArr4ExceedTask().size());
    }

    /**
     * 这个是进行补全操作的函数
     * @param addingExpert
     * @param taskId
     * @param addingStartTime
     * @param addingEndTime
     */
    public void allocateInvalidOperation(int addingExpert, int taskId, int addingStartTime, int addingEndTime){
        //System.out.println("Exchange " + taskId);
        solution.addSchedule(taskId, addingExpert, addingStartTime, addingEndTime);
        int addingProcessTime = addingEndTime - addingStartTime;
        middleware.updateExpertProcessTime(addingExpert, addingProcessTime, "add");
        middleware.setTaskPreExpert(taskId, addingExpert);
        middleware.totalExpertProcessTime += addingProcessTime;
        middleware.updateTaskAllocateTimes(taskId, 1, "add");
        middleware.updateFirstExpertForTask(taskId, addingExpert);
        middleware.updateTaskStartTime(taskId, addingStartTime);
    }


    /**TODO 这边别忘记提一下。最开始的时候4316/8840个任务满足："最佳专家处理"的原则，优化之后 数据是3690/8840
     * 这个函数统计了一共有多少个任务是不符合"最高效的专家处理"这个要求的
     * 一共有4316个任务没有符合，占比近50%
     */
    public void displayTempResult(){
        List<List<Integer>> expertsSortByProcessTime = reference.getExpertsSortByProcessTime();
        int[] minTime = new int[reference.getProcessTimeMatrix()[0].length];
        for(int i = 0; i < minTime.length; i ++ ){
            minTime[i] = reference.getProcessTimeMatrix()[expertsSortByProcessTime.get(i).get(0) - 1][i];
        }
        int count = 0;
        for(int i = 0; i < reference.getTaskNum(); i ++ ){
            int tempTaskEndTime = middleware.getTaskEndTime(i + 1);
            int tempTaskStartTime = middleware.getTaskStartTime(i + 1);
            int timeConsumed = tempTaskEndTime - tempTaskStartTime;
            int type = reference.getTaskQuestion(i + 1);
            int min = minTime[type - 1];
            if(min < timeConsumed){
                middleware.addNotPerfectExpertTask(i + 1);
                count ++ ;
            }
        }
        System.out.println("\t" + count + " tasks don't fit the best_expert_for_task principle");
    }

    public void ejectionChain_test2(){
        Random r = new Random((System.currentTimeMillis()));
        int length = middleware.getArr4NotExceedTask().size();
        int firstTaskId = 0, secondTaskId = 0, firstExpert = 0 ,secondExpert = 0, firstTaskStartTime = 0, secondTaskStartTime = 0;
        int firstTaskType = 0, secondTaskType = 0, firstTaskSla = 0;
        Set<Integer> not_repeat = new HashSet<>();
        int[] minTime = new int[reference.getProcessTimeMatrix()[0].length];
        for(int i = 0; i < minTime.length; i ++ ){
            minTime[i] = reference.getProcessTimeMatrix()[reference.getExpertsSortByProcessTime().get(i).get(0) - 1][i];
        }
        length = middleware.getArr4NotExceedTask().size() ;
        boolean judge = false;
        firstTaskId = middleware.getArr4NotExceedTask().get(r.nextInt(length));
        while(this.operated_Set.contains(firstTaskId)) {
            firstTaskId = middleware.getArr4NotExceedTask().get(r.nextInt(length));
        }
        this.operated_Set.add(firstTaskId);
        not_repeat.add(firstTaskId);
        int k = 0;
        int count = 0 ;
        while(true) {
            count += 1;
            //judge = false;
            for (k = 0; k < 40; k++) {
                firstTaskType = reference.getTaskQuestion(firstTaskId);
                firstExpert = middleware.getFirstExpertForTask(firstTaskId);
                firstTaskSla = reference.getTaskSla(firstTaskId);
                Set<Integer> tabu_set_3 = new HashSet<>();
                length = middleware.getArr4NotExceedTask().size();
                secondTaskId = middleware.getArr4NotExceedTask().get(r.nextInt(length));
                secondTaskType = reference.getTaskQuestion(secondTaskId);
                secondExpert = middleware.getFirstExpertForTask(secondTaskId);

                if (tabu_set_3.contains(secondTaskId)
                        || firstTaskId == secondTaskId
                        || firstExpert == secondExpert
                        || reference.getProcessTimeMatrix()[secondExpert - 1][firstTaskType - 1] >= 200) {
                    if(!tabu_set_3.contains(secondTaskId)){
                        tabu_set_3.add(secondTaskId);
                    }
                    //System.out.println("Stopped by tabu");
                    continue;
                }

                int firstTaskCreateTime = reference.getTaskCreateTime(firstTaskId);
                int firstTaskEndTime = middleware.getTaskEndTime(firstTaskId);
                int secondTaskCreateTime = reference.getTaskCreateTime(secondTaskId);
                int secondTaskEndTime = middleware.getTaskEndTime(secondTaskId);
                int secondTaskSla = reference.getTaskSla(secondTaskId);
                firstTaskStartTime = middleware.getTaskStartTime(firstTaskId);
                secondTaskStartTime = middleware.getTaskStartTime(secondTaskId);

                solution.removeSchedule(firstTaskId, firstExpert, firstTaskStartTime, firstTaskEndTime);
                solution.removeSchedule(secondTaskId, secondExpert, secondTaskStartTime, secondTaskEndTime);
                middleware.updateExpertProcessTime(firstExpert, firstTaskEndTime - firstTaskStartTime, "sub");
                middleware.updateExpertProcessTime(secondExpert, secondTaskEndTime - secondTaskStartTime, "sub");

                List<Integer> experts = reference.getExpertsSortByProcessTime().get(secondTaskType - 1);
                int[][] processTimeMatrix = reference.getProcessTimeMatrix();

                int T = Integer.MAX_VALUE;
                int startTime = 0;
                int endTime = 0;
                int expert = 0;
                for (int i : experts) {
                    if (i == secondExpert) {
                        continue;
                    }
                    int processTime = processTimeMatrix[i - 1][secondTaskType - 1];
                    for (int t = secondTaskCreateTime; t < secondTaskCreateTime + secondTaskSla; t++) {
                        if (t + processTime >= T) {
                            break;
                        } else {
                            boolean canDeal = solution.expertIdleInPeriod(i, t, t + processTime);
                            if (canDeal) {
                                T = t + processTime;
                                startTime = t;
                                endTime = T;
                                expert = i;
                                break;
                            }
                        }
                    }
                }
                if(startTime == 0){
                    tabu_set_3.add(secondTaskId);
                    solution.addSchedule(firstTaskId, firstExpert, firstTaskStartTime, firstTaskEndTime);
                    solution.addSchedule(secondTaskId, secondExpert, secondTaskStartTime, secondTaskEndTime);
                    middleware.updateExpertProcessTime(firstExpert, firstTaskEndTime - firstTaskStartTime, "add");
                    middleware.updateExpertProcessTime(secondExpert, secondTaskEndTime - secondTaskStartTime, "add");
                    continue;
                }
                int compare = endTime - secondTaskEndTime;
                judge = false;
                int secondExpertNewStartTime = 0;
                int secondExpertProcessTime = reference.getProcessTimeMatrix()[secondExpert - 1][firstTaskType - 1];
                // 把第二个专家放在第一个任务上
                for (int t = firstTaskCreateTime; t < firstTaskCreateTime + firstTaskSla; t++) {
                    if (solution.expertIdleInPeriod(secondExpert, t, t + secondExpertProcessTime)) {
                        compare += (t + secondExpertProcessTime - firstTaskEndTime);
                        secondExpertNewStartTime = t;
                        if (compare < 0) {
                            judge = true;
                        }
                        break;
                    }
                }
                if (judge) {
                    // 检查下为啥会secondExpertNewStartTime = 0 judge 必须及时更新
                    System.out.println("\t\tEjection Chain Update, secondExpertNewStartTime = " + secondExpertNewStartTime);
                    solution.addSchedule(secondTaskId, expert, startTime, endTime);
                    middleware.updateExpertProcessTime(expert, endTime - startTime, "add");
                    middleware.updateTaskStartTime(secondTaskId, startTime);
                    middleware.updateTaskEndTime(secondTaskId, endTime);
                    middleware.updateFirstExpertForTask(secondTaskId, expert);
                    middleware.updateTaskValidStartTime(secondTaskId, startTime);
                    middleware.setTaskPreExpert(secondTaskId, expert);
                    // 下面要开始写第一个的实现
                    // 最后检查有没有符合条件的，从无法满足响应时间变成可以满足响应时间的情况产生
                    solution.addSchedule(firstTaskId, secondExpert, secondExpertNewStartTime,
                            secondExpertNewStartTime + secondExpertProcessTime);
                    middleware.updateExpertProcessTime(secondExpert, secondExpertProcessTime, "add");
                    middleware.updateTaskStartTime(firstTaskId, secondExpertNewStartTime);
                    middleware.updateTaskEndTime(firstTaskId, secondExpertNewStartTime + secondExpertProcessTime);
                    middleware.updateFirstExpertForTask(firstTaskId, secondExpert);
                    middleware.updateTaskValidStartTime(firstTaskId, secondExpertNewStartTime);
                    middleware.setTaskPreExpert(firstTaskId, secondTaskId);
                    /*
                        最后到这里需要检查一下是否又变得可行
                        这里还要检查一下是否已经变成最优的情况
                    */
                    if (middleware.getNotPerfectExpertTask().contains(firstTaskId) && secondExpertProcessTime == minTime[firstTaskType - 1]) {
                        middleware.removeNotPerfectExpertTask(firstTaskId);
                    } else if (!middleware.getNotPerfectExpertTask().contains(firstTaskId) && secondExpertProcessTime != minTime[firstTaskType - 1]) {
                        middleware.addNotPerfectExpertTask(firstTaskId);
                    }
                    if (middleware.getNotPerfectExpertTask().contains(secondTaskId) && endTime - startTime == minTime[secondTaskType - 1]) {
                        middleware.removeNotPerfectExpertTask(secondTaskId);
                    } else if (!middleware.getNotPerfectExpertTask().contains(secondTaskId) && endTime - startTime != minTime[secondTaskType - 1]) {
                        middleware.addNotPerfectExpertTask(secondTaskId);
                    }
//                    if (secondExpertNewStartTime <= firstTaskCreateTime + reference.getTaskSla(firstTaskId)) {
//                        middleware.addTask2STimeB4MaxResTime(firstTaskId); // 待定
//                        middleware.setResTimeMap(firstTaskId, secondExpert); // 待定
//                        //middleware.setMap4Type2Task(firstTaskType, firstTaskId);//记录满足条件算子
//                        middleware.setMap4ExpertFeasibleTask(secondExpert, firstTaskId);// 专家交换算子
//                        //middleware.removeMap4ExpertNonFeasibleTask(firstExpert, firstTaskId);
//                        //middleware.setMap4ExpertFeasibleTask(secondExpert, firstTaskId);
//                        middleware.removeArr4ExceedTask(firstTaskId);
//
//                    }
//                    if (startTime <= secondTaskCreateTime + reference.getTaskSla(secondTaskId)) {
//                        middleware.addTask2STimeB4MaxResTime(secondTaskId); // 待定
//                        middleware.setResTimeMap(secondTaskId, expert); // 待定
//
//                        middleware.setMap4ExpertFeasibleTask(expert, secondTaskId);// 专家交换算子
//                        middleware.removeMap4ExpertNonFeasibleTask(secondExpert, secondTaskId);
//
//                        middleware.removeArr4ExceedTask(secondTaskId);
//                    }
                    firstTaskId = secondTaskId;
                    not_repeat.add(secondTaskId);
                    //System.out.println("**********");
                    break;
                } else {
                    solution.addSchedule(firstTaskId, firstExpert, firstTaskStartTime, firstTaskEndTime);
                    solution.addSchedule(secondTaskId, secondExpert, secondTaskStartTime, secondTaskEndTime);
                    middleware.updateExpertProcessTime(firstExpert, firstTaskEndTime - firstTaskStartTime, "add");
                    middleware.updateExpertProcessTime(secondExpert, secondTaskEndTime - secondTaskStartTime, "add");

                }
            }
            if(k == 200){
                firstTaskId = middleware.getArr4NotExceedTask().get(r.nextInt(middleware.getArr4NotExceedTask().size()));
                while(not_repeat.contains(firstTaskId)) {
                    firstTaskId = middleware.getArr4NotExceedTask().get(r.nextInt(middleware.getArr4NotExceedTask().size()));
                }
                continue;
            }
            if(count >= 10){
                break;
            }
        }
    }
    // 如何进行交叉比对？ 有点复杂啊
    public void ejectionChain(){
        Random r = new Random((System.currentTimeMillis()));
        int length = middleware.getArr4ExceedTask().size();
        int firstTaskId = 0, secondTaskId = 0, firstExpert = 0 ,secondExpert = 0, firstTaskStartTime = 0, secondTaskStartTime = 0;
        int firstTaskType = 0, secondTaskType = 0;
        while(true){
            int[] minTime = new int[reference.getProcessTimeMatrix()[0].length];
            for(int i = 0; i < minTime.length; i ++ ){
                minTime[i] = reference.getProcessTimeMatrix()[reference.getExpertsSortByProcessTime().get(i).get(0) - 1][i];
            }
            length = middleware.getArr4ExceedTask().size();
            boolean judge = false;
            firstTaskId = middleware.getArr4ExceedTask().get( r.nextInt(length));
            secondTaskId = middleware.getArr4ExceedTask().get( r.nextInt(length) );

            firstTaskType = reference.getTaskQuestion(firstTaskId);
            secondTaskType = reference.getTaskQuestion(secondTaskId);
            firstExpert = middleware.getFirstExpertForTask(firstTaskId);
            secondExpert = middleware.getFirstExpertForTask(secondTaskId);
            //System.out.println(firstTaskId + "  " + secondTaskId);
            if(firstTaskId == secondTaskId
                    //|| firstTaskType == secondTaskType
                    || firstExpert == secondExpert
                    || reference.getProcessTimeMatrix()[secondExpert - 1][firstTaskType - 1] >= 180){
                continue;
            }
            //System.out.println(firstTaskId + "  " + secondTaskId);
            int firstTaskCreateTime = reference.getTaskCreateTime(firstTaskId);
            int firstTaskEndTime = middleware.getTaskEndTime(firstTaskId);
            int secondTaskCreateTime = reference.getTaskCreateTime(secondTaskId);
            int secondTaskEndTime = middleware.getTaskEndTime(secondTaskId);
            firstTaskStartTime = middleware.getTaskStartTime(firstTaskId);
            secondTaskStartTime = middleware.getTaskStartTime(secondTaskId);

            solution.removeSchedule(firstTaskId, firstExpert , firstTaskStartTime, firstTaskEndTime);
            solution.removeSchedule(secondTaskId, secondExpert, secondTaskStartTime, secondTaskEndTime);
            middleware.updateExpertProcessTime(firstExpert, firstTaskEndTime - firstTaskStartTime, "sub");
            middleware.updateExpertProcessTime(secondExpert, secondTaskEndTime - secondTaskStartTime, "sub");

            List<Integer> experts = reference.getExpertsSortByProcessTime().get(secondTaskType - 1);
            int[][] processTimeMatrix = reference.getProcessTimeMatrix();

            int T = Integer.MAX_VALUE;
            int startTime = 0;
            int endTime = 0;
            int expert = 0;
            for (int i : experts) {
                if(i == secondExpert ){
                    continue;
                }
                int processTime = processTimeMatrix[i - 1][secondTaskType - 1];
                for (int t = secondTaskCreateTime; t < Integer.MAX_VALUE; t++) {
                    if (t + processTime >= T) {
                        break;
                    } else {
                        boolean canDeal = solution.expertIdleInPeriod(i, t, t + processTime);
                        if (canDeal) {
                            T = t + processTime;
                            startTime = t;
                            endTime = T;
                            expert = i;
                            break;
                        }
                    }
                }
            }
            int compare = endTime - secondTaskEndTime;

            int secondExpertNewStartTime = 0;
            int secondExpertProcessTime = reference.getProcessTimeMatrix()[secondExpert - 1][firstTaskType - 1];
            // 把第二个专家放在第一个任务上
            for(int t = firstTaskCreateTime; t < firstTaskEndTime ; t ++ ){
                if(solution.expertIdleInPeriod(secondExpert, t , t + secondExpertProcessTime)){
                    compare += (t + secondExpertProcessTime - firstTaskEndTime);
                    secondExpertNewStartTime = t;
                    if(compare < 0){
                        judge = true;
                    }

                    break;
                }
            }
            //System.out.println(" total " + compare +  " " + firstTaskId + " " + middleware.getNotPerfectExpertTask().contains(firstTaskId));
            //System.out.println(firstTaskId + "  " + secondTaskId + "  " + compare) ;
            if(judge){
                /*
                先写第二个任务的
                 */
                //System.out.println("Find!");
                solution.addSchedule(secondTaskId, expert, startTime, endTime );
                middleware.updateExpertProcessTime(expert, endTime - startTime, "add");
                middleware.updateTaskStartTime(secondTaskId, startTime);
                middleware.updateTaskEndTime(secondTaskId, endTime);
                middleware.updateFirstExpertForTask(secondTaskId, expert);
                middleware.updateTaskValidStartTime(secondTaskId, startTime);
                middleware.setTaskPreExpert(secondTaskId, expert);
                /**
                 * 这里有两行涉及了two_switch_2的几个hashMap算子就先没有加入
                 */
                // 下面要开始写第一个的实现
                // 最后检查有没有符合条件的，从无法满足响应时间变成可以满足响应时间的情况产生
                solution.addSchedule(firstTaskId, secondExpert, secondExpertNewStartTime,
                        secondExpertNewStartTime + secondExpertProcessTime);
                middleware.updateExpertProcessTime(secondExpert, secondExpertProcessTime, "add");
                middleware.updateTaskStartTime(firstTaskId, secondExpertNewStartTime);
                middleware.updateTaskEndTime(firstTaskId, secondExpertNewStartTime + secondExpertProcessTime);
                middleware.updateFirstExpertForTask(firstTaskId, secondExpert);
                middleware.updateTaskValidStartTime(firstTaskId, secondExpertNewStartTime);
                middleware.setTaskPreExpert(firstTaskId, secondTaskId);
                /*
                最后到这里需要检查一下是否又变得可行
                这里还要检查一下是否已经变成最优的情况
                 */
                if(middleware.getNotPerfectExpertTask().contains(firstTaskId) &&  secondExpertProcessTime == minTime[firstTaskType - 1]){
                    middleware.removeNotPerfectExpertTask(firstTaskId);
                }
                else if(!middleware.getNotPerfectExpertTask().contains(firstTaskId) && secondExpertProcessTime != minTime[firstTaskType - 1]){
                    middleware.addNotPerfectExpertTask(firstTaskId);
                }
                if(middleware.getNotPerfectExpertTask().contains(secondTaskId) && endTime - startTime == minTime[secondTaskType - 1]){
                    middleware.removeNotPerfectExpertTask(secondTaskId);
                }
                else if(!middleware.getNotPerfectExpertTask().contains(secondTaskId) && endTime - startTime != minTime[secondTaskType - 1]){
                    middleware.addNotPerfectExpertTask(secondTaskId);
                }
                if(secondExpertNewStartTime <= firstTaskCreateTime + reference.getTaskSla(firstTaskId)){
                    middleware.addTask2STimeB4MaxResTime(firstTaskId); // 待定
                    middleware.setResTimeMap(firstTaskId, secondExpert); // 待定
                    //middleware.setMap4Type2Task(firstTaskType, firstTaskId);//记录满足条件算子
                    middleware.setMap4ExpertFeasibleTask(secondExpert, firstTaskId);// 专家交换算子
                    middleware.removeMap4ExpertNonFeasibleTask(firstExpert, firstTaskId);
                    //middleware.setMap4ExpertFeasibleTask(secondExpert, firstTaskId);
                    middleware.removeArr4ExceedTask(firstTaskId);

                }
                if(startTime <= secondTaskCreateTime + reference.getTaskSla(secondTaskId)){
                    middleware.addTask2STimeB4MaxResTime(secondTaskId); // 待定
                    middleware.setResTimeMap(secondTaskId, expert); // 待定

                    middleware.setMap4ExpertFeasibleTask(expert, secondTaskId);// 专家交换算子
                    middleware.removeMap4ExpertNonFeasibleTask(secondExpert, secondTaskId);

                    middleware.removeArr4ExceedTask(secondTaskId);
                }
                break;
            }
            else{
                solution.addSchedule(firstTaskId, firstExpert , firstTaskStartTime, firstTaskEndTime);
                solution.addSchedule(secondTaskId, secondExpert, secondTaskStartTime, secondTaskEndTime);
                middleware.updateExpertProcessTime(firstExpert, firstTaskEndTime - firstTaskStartTime, "add");
                middleware.updateExpertProcessTime(secondExpert, secondTaskEndTime - secondTaskStartTime, "add");
            }
        }
    }

    /**
     * @param preTask first task ID
     * @param taskId second Task ID
     * @param preTaskExpert first Task Expert
     * @param currentTaskExpert second Task expert
     * @param swapCurrentStartTime new start time for second task
     * @param swapCurrentEndTime new end time for second task
     * @param swapPreTaskStartTime new start time for first task
     * @param swapPreTaskEndTime new end time for first task
     * @param judge decide which to be removed from out-of-responding time list . "taskId" and "preTask"
     */
    public void two_task_swap(int preTask, int taskId, int preTaskExpert, int currentTaskExpert, int swapCurrentStartTime, int swapCurrentEndTime,
                              int swapPreTaskStartTime ,int swapPreTaskEndTime, String judge){
        int preTaskStartTime = middleware.getTaskStartTime(preTask);
        int preTaskEndTime = middleware.getTaskEndTime(preTask);
        int currentStartTime = middleware.getTaskStartTime(taskId);
        int currentEndTime = middleware.getTaskEndTime(taskId);
        solution.removeSchedule(preTask, preTaskExpert, preTaskStartTime, preTaskEndTime);
        solution.removeSchedule(taskId, currentTaskExpert, currentStartTime, currentEndTime);
        if(preTaskExpert != currentTaskExpert){
            // 统计总时间的处理结束
            middleware.updateExpertProcessTime(currentTaskExpert, currentEndTime - currentStartTime, "sub");
            middleware.updateExpertProcessTime(preTaskExpert, preTaskEndTime - preTaskStartTime, "sub");
            middleware.updateExpertProcessTime(currentTaskExpert, swapPreTaskEndTime - swapPreTaskStartTime, "add");
            middleware.updateExpertProcessTime(preTaskExpert, swapCurrentEndTime - swapCurrentStartTime, "add");
        }
        /**
         * 6整体
         */
        middleware.updateTaskStartTime(taskId, swapCurrentStartTime);
        middleware.updateTaskValidStartTime(taskId, swapCurrentStartTime);
        middleware.updateTaskEndTime(taskId, swapCurrentEndTime);
        middleware.updateTaskStartTime(preTask, swapPreTaskStartTime);
        middleware.updateTaskValidStartTime(preTask, swapPreTaskStartTime);
        middleware.updateTaskEndTime(preTask, swapPreTaskEndTime);
        middleware.setTaskPreExpert(preTask, currentTaskExpert);
        middleware.setTaskPreExpert(taskId, preTaskExpert);
        // if(middleware.getSTimeB4MaxResTime().contains())
        if(judge.equals("taskId")) {
            middleware.addTask2STimeB4MaxResTime(taskId);

            middleware.setMap4ExpertFeasibleTask(preTaskExpert, taskId);
            middleware.setResTimeMap(taskId, preTaskExpert);
        }
        else if(judge.equals("preTask")) {
            middleware.addTask2STimeB4MaxResTime(preTask);

            middleware.setMap4ExpertFeasibleTask(currentTaskExpert, preTask);
            middleware.setResTimeMap(preTask, currentTaskExpert);
        }
        else if(judge.equals("preTask_taskId")){
            middleware.addTask2STimeB4MaxResTime(taskId);

            middleware.setMap4ExpertFeasibleTask(preTaskExpert, taskId);
            middleware.setResTimeMap(taskId, preTaskExpert);
            middleware.addTask2STimeB4MaxResTime(preTask);

            middleware.setMap4ExpertFeasibleTask(currentTaskExpert, preTask);
            middleware.setResTimeMap(preTask, currentTaskExpert);
        }
        middleware.updateFirstExpertForTask(taskId, preTaskExpert);
        middleware.updateFirstExpertForTask(preTask, currentTaskExpert);

        //middleware.removeMap4ExpertTaskStartTime(currentTaskExpert, taskId, currentStartTime);
        //middleware.removeMap4ExpertTaskEndTime(currentTaskExpert, taskId, currentEndTime);
        //middleware.setMap4ExpertTaskStartTime(preTaskExpert, swapCurrentStartTime, taskId);
        //middleware.setMap4ExpertTaskEndTime(preTaskExpert, swapCurrentEndTime, taskId);

        //middleware.removeMap4ExpertTaskStartTime(preTaskExpert, preTask , preTaskStartTime);
        //middleware.removeMap4ExpertTaskEndTime(preTaskExpert, preTask, preTaskEndTime);
        //middleware.setMap4ExpertTaskEndTime(currentTaskExpert, swapPreTaskEndTime, preTask);
        //middleware.setMap4ExpertTaskStartTime(currentTaskExpert, swapPreTaskStartTime, preTask);

        solution.addSchedule(preTask, currentTaskExpert, swapPreTaskStartTime, swapPreTaskEndTime);
        solution.addSchedule(taskId, preTaskExpert, swapCurrentStartTime, swapCurrentEndTime);

        //middleware.setMap4Type2Task(reference.getTaskQuestion(taskId), taskId);//记录满足条件算子

    }

    /**
     * 这个算子的实现：查看金奖攻略exchange算子
     * https://tianchi.aliyun.com/notebook-ai/detail?spm=5176.12586969.1002.21.68701486MFdvxB&postId=152815
     * 注意是网站上第一个exchange算子 原则还是使得"超时响应时间最短"
     */
    public void two_switch(){
        int index = 0;
        int destination = middleware.getArr4ExceedTask().size();
        while(index < destination){
            int taskId = middleware.getArr4ExceedTask().get(index);
            int taskType = reference.getWorkOrder()[taskId - 1][2];
            int currentExpert = middleware.getFirstExpertForTask(taskId);
            int currentStartTime = middleware.getTaskStartTime(taskId);
            int currentEndTime = middleware.getTaskEndTime(taskId);
            // 上面获取了这个任务的基本信息
            List<Integer> sameTypeTask = middleware.getMap4Type2Task(taskType);
            if(sameTypeTask.isEmpty()){
                continue;
            }

            for(int j = 0 ; j < sameTypeTask.size(); j ++ ){
                int switchTask = sameTypeTask.get(j);
                int switchExpert = middleware.getFirstExpertForTask(switchTask);
                //int switchTaskSlaTime = reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask);
                if(switchTask == taskId){
                    continue;
                }
                if(currentStartTime >= reference.getTaskCreateTime(switchTask)
                        && reference.getTaskCreateTime(taskId) <= middleware.getTaskStartTime(switchTask)
                        && currentStartTime - (reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId)) +
                        Math.max(0 , middleware.getTaskStartTime(switchTask) - (reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask)))
                        >
                        Math.max(0 , middleware.getTaskStartTime(switchTask) - (reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId)))
                                + Math.max(0 , middleware.getTaskStartTime(taskId) - (reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask)))) {
                    int switchTaskStartTime = middleware.getTaskStartTime(switchTask);
                    int switchTaskEndTime = middleware.getTaskEndTime(switchTask);
                    List<Integer> experts = reference.getExpertsSortByProcessTime().get(taskType - 1);
                    int flag_start = -1;
                    int flag_switch = -1;
                    for(int k = 0; k < experts.size(); k ++ ){
                        if(experts.get(k) == switchTask){
                            flag_start = k;
                        }
                        if(experts.get(k) == taskId){
                            flag_switch = k;
                        }
                        if(flag_start != -1 && flag_switch != -1){
                            break;
                        }
                    }
                    if(middleware.getTaskStartTime(switchTask) - (reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId)) <= 0) {
                        // 现在的任务，本来超时现在不超时
                        if(middleware.getTaskStartTime(taskId) - (reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask)) <= 0) {
                            //交换的那个任务本来超时现在不超时
                            if(middleware.getArr4ExceedTask().contains(switchTask)){
                                middleware.removeMap4ExpertNonFeasibleTask(switchExpert, switchTask);
                                middleware.removeExceedTaskExpertPos(switchTask);
                                middleware.removeArr4ExceedTask(switchTask);
                                destination -- ;
                            }
                            //交换的那个任务本来不超时现在还不超时
                            else{
                                middleware.removeMap4ExpertFeasibleTask(switchExpert, switchTask);
                            }
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "preTask_taskId");
                        }
                        else{
                            // 交换的任务本来超时现在超时
                            if(middleware.getArr4ExceedTask().contains(switchTask)){
                                middleware.removeMap4ExpertNonFeasibleTask(switchExpert, switchTask);

                            }
                            // 交换的任务本来不超时现在超时
                            else{
                                middleware.removeMap4ExpertFeasibleTask(switchExpert, switchTask);
                                middleware.getArr4ExceedTask().add(switchTask);
                                destination += 1;
                            }
                            middleware.setMap4ExpertNonFeasibleTask(currentExpert, switchTask);
                            middleware.setMap4ExceedTaskExpertPos(switchTask, flag_switch);
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "taskId");
                        }
                        middleware.removeMap4ExpertNonFeasibleTask(currentExpert, taskId);
                        middleware.removeExceedTaskExpertPos(taskId);
                        middleware.removeArr4ExceedTask(taskId);
                        index -- ;
                        destination -- ;
                    }
                    else{
                        // 现在的任务，本来超时，现在还超时。。。
                        middleware.removeMap4ExpertNonFeasibleTask(currentExpert, taskId);
                        middleware.setMap4ExpertNonFeasibleTask(switchExpert, taskId);
                        if(middleware.getTaskStartTime(taskId) - (reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask)) <= 0) {
                            //交换的那个任务本来超时现在不超时
                            if(middleware.getArr4ExceedTask().contains(switchTask)){
                                middleware.removeMap4ExpertNonFeasibleTask(switchExpert, switchTask);
                                middleware.removeExceedTaskExpertPos(switchTask);
                                middleware.removeArr4ExceedTask(switchTask);
                                destination -- ;
                            }
                            // 交换的任务本来不超时现在还不超时
                            else{
                                middleware.removeMap4ExpertFeasibleTask(switchExpert, switchTask);
                                //middleware.setMap4ExpertFeasibleTask(currentExpert, switchTask);
                            }
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "preTask");
                            //toBDeleted.add(switchTask);
                            //middleware.removeExceedTaskExpertPos(switchTask);
                            middleware.setMap4ExceedTaskExpertPos(taskId, flag_start);
                        }
                        else{
                            // 交换的任务本来超时现在还超时
                            if(middleware.getArr4ExceedTask().contains(switchTask)){
                                middleware.removeMap4ExpertNonFeasibleTask(switchExpert, switchTask);
                                middleware.setMap4ExpertNonFeasibleTask(currentExpert, switchTask);
                            }
                            // 交换的任务本来不超时现在超时
                            else{
                                middleware.removeMap4ExpertFeasibleTask(switchExpert, switchTask);
                                middleware.setMap4ExpertNonFeasibleTask(currentExpert, switchTask);
                                destination += 1;
                                middleware.getArr4ExceedTask().add(switchTask);
                            }
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "None");
                            middleware.setMap4ExceedTaskExpertPos(switchTask, flag_switch);

                        }
                        middleware.setMap4ExceedTaskExpertPos(taskId, flag_start);
                    }
                    break;
                }
            }
            index ++ ;
        }
        System.out.println("\tNow we have Exceed Tasks: " + middleware.getArr4ExceedTask().size());
    }

    /**
     * TODO 这个是为数不多的我们的原创算子！！
     *
     *  操作原理：考虑所有没有在响应时间内完成的任务，从响应时间结束的时刻向前追溯，看看是否存在哪个专家，虽然不是最高效的，但是能提高总体得分的情况
     *  如果找到，就交换。
     */
    public void two_exchange(){
        List<Integer> exceedList = middleware.getArr4ExceedTask();
        Set<Integer> toBDeleted = new HashSet<>();
        //System.out.println("The exceedlist length is " + exceedList.size());
        for(int i = 0; i < exceedList.size(); i ++ ){
            int taskId = exceedList.get(i);
            int startPoint = middleware.getMap4ExceedTaskExpertPos(taskId);
            int taskType = reference.getWorkOrder()[taskId - 1][2];
            List<Integer> experts = reference.getExpertsSortByProcessTime().get(taskType - 1);
            for(int j = 0; j < startPoint + 10; j ++ ){
                int currentExpert = middleware.getFirstExpertForTask(taskId);
                int preExecuteTime = reference.getProcessTimeMatrix()[currentExpert - 1][taskType - 1];
                if(j >= experts.size()){
                    break;
                }
                int createTime = reference.getTaskCreateTime(taskId);
                int endTime = createTime + reference.getTaskSla(taskId);
                // 这个是打算插入的新专家的安排时间
                // 要获取这个任务的 **** 产生时间 *****
                for(int k = createTime; k <= endTime; k ++ ){
                    int operationTime = reference.getProcessTimeMatrix()[experts.get(j) - 1][taskType - 1];
                    if(solution.expertIdleInPeriod(experts.get(j), k, k + operationTime)){
                        double[] temp_score = solution.accumulateScore(reference);
                        int original_start = middleware.getTaskStartTime(taskId);
                        int original_end = middleware.getTaskEndTime(taskId);
                        solution.removeSchedule(taskId, currentExpert, original_start, original_end);
                        solution.addSchedule(taskId, experts.get(j), k, k + operationTime);
                        middleware.updateExpertProcessTime(currentExpert, preExecuteTime, "sub");
                        middleware.updateExpertProcessTime(experts.get(j), operationTime, "add");
                        int temp_task_startTime = middleware.getTaskStartTime(taskId);
                        int temp_task_validTaskStartTime = middleware.getTaskVaildStartTime(taskId);
                        int temp_task_endTime = middleware.getTaskEndTime(taskId);
                        middleware.updateTaskStartTime(taskId, k);
                        middleware.updateFirstExpertForTask(taskId, experts.get(j));
                        middleware.updateTaskValidStartTime(taskId, k);
                        middleware.updateTaskEndTime(taskId, k + operationTime);
                        // 这里还要调整schedule, 没结束呢！
                        double[] second_temp_score = solution.accumulateScore(reference);
                        if(second_temp_score[0] < temp_score[0]){
                            solution.removeSchedule(taskId, experts.get(j), k, k + operationTime);
                            solution.addSchedule(taskId, currentExpert, original_start, original_end);
                            middleware.updateExpertProcessTime(currentExpert, preExecuteTime, "add");
                            middleware.updateExpertProcessTime(experts.get(j), operationTime, "sub");
                            middleware.updateTaskStartTime(taskId, temp_task_startTime);
                            middleware.updateFirstExpertForTask(taskId, currentExpert);
                            middleware.updateTaskValidStartTime(taskId, temp_task_validTaskStartTime);
                            middleware.updateTaskEndTime(taskId, temp_task_endTime);
                            break;
                        }
                        toBDeleted.add(taskId);
                        middleware.addTask2STimeB4MaxResTime(taskId);
                        middleware.setResTimeMap(taskId, experts.get(j));
                        middleware.setTaskPreExpert(taskId, experts.get(j));
                        middleware.removeMap4ExpertNonFeasibleTask(currentExpert, taskId);
                        middleware.setMap4ExpertFeasibleTask(experts.get(j), taskId);
                        //middleware.setMap4ExpertFeasibleTask(experts.get(j), taskId);

                        //middleware.removeMap4ExpertTaskStartTime(currentExpert, taskId, original_start);
                        middleware.removeMap4ExpertTaskEndTime(currentExpert, taskId, original_end);
                        //middleware.setMap4ExpertTaskStartTime(experts.get(j), k , taskId);
                        middleware.setMap4ExpertTaskEndTime(experts.get(j), k + operationTime, taskId);
                        //System.out.println("After the deletion, exceed length is " + middleware.Map4ExceedTaskExpertPos.size());
                        break;
                    }
                }
            }
        }
        middleware.removeExceedTaskExpertPos(toBDeleted);
        middleware.removeArr4ExceedTask(toBDeleted);
        System.out.println("\tFind second available operation terminated.\n\tNow we have exceeding tasks:" + middleware.getArr4ExceedTask().size());
//        for(int i = 0; i < middleware.getTaskAllocateTimes().length; i ++ ){
//            System.out.print(middleware.getTaskAllocateTimes(i + 1) + " ");
//        }
    }

    /**
     * 这个函数的作用：在所有的invalidProcess处理之后，看有没有什么是可以直接把任务加到最前面减少不平衡的情况
     * 但是调试下来效果比较差（只有2个任务得到了优化），但是为了保证模块的完整性，仍选择保留
     */
    public void addExtraInvalidProcess(){
        List<Integer> extraInvalidTask = middleware.calculateAllExtraAllocationInvalidTask();
        Set<Integer> toBDeleted = new HashSet<>();
        List<Integer> belowAvgProcessingTimeExpert = middleware.getBelowAverageWorkingTimeExpert();
        //System.out.println(extraInvalidTask.size() + " extra");
        // why 30 ?
        // possible adding: 这里可以添加针对belowList的处理，看一下结果是否会变得更好
        for(int i = 0 ; i < extraInvalidTask.size(); i ++ ){
            int taskId = extraInvalidTask.get(i);
            int taskStartTime = middleware.getTaskStartTime(taskId);
            int taskCreateTime = reference.getTaskCreateTime(taskId);

            int j = taskStartTime;
            if(middleware.getTaskAllocateTimes(taskId) == 5){
                continue;
            }
            for(int k = 0 ; k < belowAvgProcessingTimeExpert.size(); k ++ ){
                int expertId = belowAvgProcessingTimeExpert.get(k);
                if(middleware.getTaskPreExpert(taskId) == expertId){
                    continue; // 这里是不能连续分配到同一个专家的约束
                }
                if(middleware.getTaskAllocateTimes(taskId) == 5){
                    break;
                }
                if(solution.expertIdleInPeriod(expertId, j - 1, j)){
                    int tempStart = j - 1;
                    boolean satisfied = false;
                    while(tempStart > taskCreateTime){
                        if(!solution.expertIdleInPeriod(expertId ,tempStart, j)){
                            tempStart += 1;
                            break;
                        }
                        else{
                            tempStart -= 1;
                        }
                    }
                    if(tempStart == taskCreateTime){
                        if(solution.expertIdleInPeriod(expertId, tempStart, j))
                            satisfied = true;
                        else{
                            tempStart += 1;
                        }
                    }
                    //middleware.addTotalExpertProcessTime(j - postStartTime);
                    //System.out.println("Swaping!");
                    this.allocateInvalidOperation(expertId, taskId, tempStart, j);
                    j = tempStart;
                    if(satisfied){
                        toBDeleted.add(taskId);
                        // todo 思考一下这边逻辑是否需要补充
                        //middleware.setMap4Type2Task(reference.getTaskQuestion(taskId), taskId);
                        break;
                    }
                }
            }
        }
        middleware.deleteExtraAllocationInvalidTask(toBDeleted);
    }

    /**
     * 这个函数作用：展示每一个超过响应时间的任务的序号
     */
    public void displayExceedTaskId(){
        for(int i = 0; i < middleware.getArr4ExceedTask().size(); i ++ ){
            System.out.println(middleware.getArr4ExceedTask().get(i) + " ");
        }
    }
    public void displayTaskHaveRemainingTime(){
        for(int i = 0 ; i < reference.getTaskNum(); i ++ ){
            if(middleware.getTaskAllocateTimes(i + 1) < 5
                    && middleware.getTaskStartTime(i + 1) > reference.getTaskCreateTime(i + 1) + reference.getTaskSla(i + 1)){
                System.out.println(i + 1);
            }
        }
    }


    /**
     * 重定位：一个经典算子
     * 也可以查阅
     * https://tianchi.aliyun.com/notebook-ai/detail?spm=5176.12586969.1002.21.68701486MFdvxB&postId=152815
     * 的relocate算子：就是对每个无效处理都寻找别的可替代的无效处理专家，看能否提高总得分
     */
    public void relocate(){
        double[] recordExpertProcessTime = new double[reference.getExpertNum()];

        List<List<Integer>> allInvalidAllocation = new ArrayList<>();
        List<List<Integer>> allSolution = solution.getSolution();
        for(int i = 0 ; i < allSolution.size(); i ++ ){
            int expertId = allSolution.get(i).get(1);
            recordExpertProcessTime[expertId - 1] += (allSolution.get(i).get(3) - allSolution.get(i).get(2));
        }
        double mean = 0;
        for(int i = 0 ; i < recordExpertProcessTime.length; i ++ ){
            //recordExpertProcessTime[i] /= (60*8*3);
            mean += recordExpertProcessTime[i];
        }
        mean /= (60*8*3);
        mean /= reference.getExpertNum();
//        double all = 0;
//        for(int i = 0 ; i < recordExpertProcessTime.length; i ++ ){
//            all += Math.pow(recordExpertProcessTime[i] - mean, 2);
//        }
//        double sigma = Math.sqrt(all/reference.getExpertNum());
        for(int i = 0; i < allSolution.size(); i ++ ){
            int taskId = allSolution.get(i).get(0), expertId = allSolution.get(i).get(1);
            int startTime = allSolution.get(i).get(2), endTime = allSolution.get(i).get(3);
            if(startTime != middleware.getTaskVaildStartTime(taskId)) {
                allInvalidAllocation.add(new ArrayList<>(allSolution.get(i)));
            }
        }
        for(int i = 0; i < allInvalidAllocation.size(); i ++ ){
            int expertId = allInvalidAllocation.get(i).get(1);
            int taskId = allInvalidAllocation.get(i).get(0);
            int preStartTime = allInvalidAllocation.get(i).get(2);
            int preEndTime = allInvalidAllocation.get(i).get(3);
            for(int j = 0 ; j < reference.getExpertNum(); j ++ ){
                if(j + 1 == expertId){
                    // 这里注意一下 一定需要把j + 1 作为专家序号
                    continue;
                }
                if(solution.expertIdleInPeriod(j + 1, preStartTime, preEndTime)){
                    //System.out.println("FInd idle");
                    int processTime = preEndTime - preStartTime;
                    if(Math.pow(( (recordExpertProcessTime[expertId - 1] - processTime)/(60*8*3) - mean) ,2) +
                            Math.pow(( (recordExpertProcessTime[j] + processTime)/(60*8*3) - mean), 2) <
                            Math.pow((recordExpertProcessTime[expertId - 1]/(60*8*3) - mean) ,2) +
                                    Math.pow((recordExpertProcessTime[j]/(60*8*3) - mean), 2)) {
                        //System.out.println(taskId);
                        // 这里先行判断一下
                        int tempPreStartTime = preStartTime;
                        solution.removeSchedule(taskId, expertId, preStartTime, preEndTime);
                        solution.addSchedule(taskId, j + 1, preStartTime, preEndTime);
                        middleware.updateExpertProcessTime(expertId, processTime, "sub");
                        middleware.updateExpertProcessTime(j + 1, processTime, "add");
                        recordExpertProcessTime[expertId - 1] -= processTime;
                        recordExpertProcessTime[j] += processTime;
                        if (preStartTime == middleware.getTaskStartTime(taskId)) {
                            middleware.updateFirstExpertForTask(taskId, j + 1);
                            middleware.setTaskPreExpert(taskId, j + 1);
                        }

                        expertId = j + 1;
                    }
                    /*
                    这里还要加上removeSchedule
                     */
                }
            }
        }
        solution.tidyUp();
        solution.mergeSame();
        //System.out.println(solution.getSolution().size());
        //System.out.println();
    }

}

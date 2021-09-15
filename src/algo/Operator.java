package algo;

import common.Middleware;
import common.Reference;
import common.Solution;

import java.util.*;
public class Operator {
    Middleware middleware;
    Reference reference;
    Solution solution;

    public Operator(Middleware middleware, Reference reference, Solution solution){
        this.middleware = middleware;
        this.reference = reference;
        this.solution = solution;
    }

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
            List<Integer> belowAverageList = new ArrayList<Integer>(originalAverageList);
            int j = taskStartTime;
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
                    break;
                }
            }
        }
        middleware.removeExceedTaskExpertPos(toBDeleted);
        middleware.removeArr4ExceedTask(toBDeleted);
        System.out.println("\tExceeding tasks: " + middleware.getArr4ExceedTask().size());
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
            middleware.setMap4Type2Task(reference.getTaskQuestion(taskId), taskId);
            middleware.setMap4ExpertFeasibleTask(preTaskExpert, taskId);
            middleware.setResTimeMap(taskId, preTaskExpert);
        }
        else if(judge.equals("preTask")) {
            middleware.addTask2STimeB4MaxResTime(preTask);
            middleware.setMap4Type2Task(reference.getTaskQuestion(preTask), preTask);
            middleware.setMap4ExpertFeasibleTask(currentTaskExpert, preTask);
            middleware.setResTimeMap(preTask, currentTaskExpert);
        }
        else if(judge.equals("preTask_taskId")){
            middleware.addTask2STimeB4MaxResTime(taskId);
            middleware.setMap4Type2Task(reference.getTaskQuestion(taskId), taskId);
            middleware.setMap4ExpertFeasibleTask(preTaskExpert, taskId);
            middleware.setResTimeMap(taskId, preTaskExpert);
            middleware.addTask2STimeB4MaxResTime(preTask);
            middleware.setMap4Type2Task(reference.getTaskQuestion(preTask), preTask);
            middleware.setMap4ExpertFeasibleTask(currentTaskExpert, preTask);
            middleware.setResTimeMap(preTask, currentTaskExpert);
        }
        middleware.updateFirstExpertForTask(taskId, preTaskExpert);
        middleware.updateFirstExpertForTask(preTask, currentTaskExpert);

        middleware.removeMap4ExpertTaskStartTime(currentTaskExpert, taskId, currentStartTime);
        middleware.removeMap4ExpertTaskEndTime(currentTaskExpert, taskId, currentEndTime);
        middleware.setMap4ExpertTaskStartTime(preTaskExpert, swapCurrentStartTime, taskId);
        middleware.setMap4ExpertTaskEndTime(preTaskExpert, swapCurrentEndTime, taskId);

        middleware.removeMap4ExpertTaskStartTime(preTaskExpert, preTask , preTaskStartTime);
        middleware.removeMap4ExpertTaskEndTime(preTaskExpert, preTask, preTaskEndTime);
        middleware.setMap4ExpertTaskEndTime(currentTaskExpert, swapPreTaskEndTime, preTask);
        middleware.setMap4ExpertTaskStartTime(currentTaskExpert, swapPreTaskStartTime, preTask);

        solution.addSchedule(preTask, currentTaskExpert, swapPreTaskStartTime, swapPreTaskEndTime);
        solution.addSchedule(taskId, preTaskExpert, swapCurrentStartTime, swapCurrentEndTime);

        //middleware.setMap4Type2Task(reference.getTaskQuestion(taskId), taskId);//记录满足条件算子

    }

    public void exchange(){
        List<Integer> exceedList = middleware.getArr4ExceedTask();
        Set<Integer> toBDeleted = new HashSet<>();
        for(int i = 0 ; i < exceedList.size(); i ++ ){
            int taskId = exceedList.get(i);
            int taskType = reference.getWorkOrder()[taskId - 1][2];
            int currentExpert = middleware.getFirstExpertForTask(taskId);
            int currentStartTime = middleware.getTaskStartTime(taskId);
            int currentEndTime = middleware.getTaskEndTime(taskId);
            List<Integer> sameTypeTask = middleware.getMap4Type2Task(taskType);
            if(sameTypeTask.isEmpty()){
                continue;
            }
            //System.out.println("The sameTypeTask size is " + sameTypeTask.size());
            for(int j = 0 ; j < sameTypeTask.size(); j ++ ){
                int switchTask = sameTypeTask.get(j);
                int switchExpert = middleware.getFirstExpertForTask(switchTask);
                int switchTaskSlaTime = reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask);
                if(switchTask == taskId){
                    continue;
                }
                //currentStartTime <= switchTaskSlaTime
                //                        && reference.getTaskCreateTime(taskId) <= middleware.getTaskStartTime(switchTask)
                //                        && reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId)  >= middleware.getTaskStartTime(switchTask)
                /*
                currentStartTime <= switchTaskSlaTime
                        && reference.getTaskCreateTime(taskId) <= middleware.getTaskStartTime(switchTask)
                        && currentStartTime - (reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId)) >
                        Math.max(0 , middleware.getTaskStartTime(switchTask) - (reference.getTaskCreateTime(taskId) + reference.getTaskSla(taskId)))
                 */
                if(//reference.getTaskCreateTime(switchTask ) >= currentStartTime
                        currentStartTime <= switchTaskSlaTime
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
                        if(middleware.getTaskStartTime(taskId) - (reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask)) <= 0) {
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "preTask_taskId");
                            toBDeleted.add(taskId);
                            toBDeleted.add(switchTask);
                            //middleware.removeExceedTaskExpertPos(taskId);
                            //middleware.removeExceedTaskExpertPos(switchTask);
                        }
                        else{
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "taskId");
                            toBDeleted.add(taskId);
                            //middleware.removeExceedTaskExpertPos(taskId);
                            middleware.setMap4ExceedTaskExpertPos(switchTask, flag_switch);
                        }
                    }
                    else{
                        if(middleware.getTaskStartTime(taskId) - (reference.getTaskCreateTime(switchTask) + reference.getTaskSla(switchTask)) <= 0) {
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "preTask");
                            toBDeleted.add(switchTask);
                            //middleware.removeExceedTaskExpertPos(switchTask);
                            middleware.setMap4ExceedTaskExpertPos(taskId, flag_start);
                        }
                        else{
                            this.two_task_swap(switchTask, taskId, switchExpert, currentExpert,
                                    switchTaskStartTime, switchTaskEndTime, currentStartTime, currentEndTime, "None");
                            middleware.setMap4ExceedTaskExpertPos(taskId, flag_start);
                            middleware.setMap4ExceedTaskExpertPos(switchTask, flag_switch);
                        }
                    }
                    //ddleware.removeArr4ExceedTask(toBDeleted); // 这里删除操作直接调用middle执行完毕了
                    break;
                }
            }
            middleware.removeArr4ExceedTask(toBDeleted);
            middleware.removeExceedTaskExpertPos(toBDeleted);
            middleware.removeMap4ExpertNonFeasibleTask(currentExpert, toBDeleted);
        }

        for(Integer tobDeleted : toBDeleted){
            middleware.setMap4Type2Task(reference.getTaskQuestion(tobDeleted), tobDeleted);
        }
        System.out.println("\tExceeding tasks :" + exceedList.size());
    }

    /**
     *  操作原理：考虑所有没有在响应时间内完成的任务，从响应时间结束的时刻向前追溯，看看是否存在哪个专家，虽然不是最高效的，但是能提高总体得分的情况
     *  如果找到，就交换。
     */
    public void two_exchange(){
        List<Integer> exceedList = middleware.getArr4ExceedTask();
        Set<Integer> toBDeleted = new HashSet<>();
        //System.out.println("The exceedlist length is " + exceedList.size());
        for(int i = 0; i < exceedList.size(); i ++ ){
            int taskId = exceedList.get(i);
            //System.out.println(taskId);
            int startPoint = middleware.getMap4ExceedTaskExpertPos(taskId);
            //System.out.println("*****" + startPoint + "****" ) ;
            int taskType = reference.getWorkOrder()[taskId - 1][2];
            //System.out.println("TaskType:" +  taskType);
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

                        middleware.removeMap4ExpertTaskStartTime(currentExpert, taskId, original_start);
                        middleware.removeMap4ExpertTaskEndTime(currentExpert, taskId, original_end);
                        middleware.setMap4ExpertTaskStartTime(experts.get(j), k , taskId);
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
     *
     */
    public void addExtraInvalidProcess(){
        List<Integer> extraInvalidTask = middleware.calculateAllExtraAllocationInvalidTask();
        Set<Integer> toBDeleted = new HashSet<>();
        List<Integer> belowAvgProcessingTimeExpert = middleware.getBelowAverageWorkingTimeExpert();

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
     *
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

    }

}

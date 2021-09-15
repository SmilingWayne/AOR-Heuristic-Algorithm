package algo;


import common.Middleware;
import common.Reference;
import common.Solution;

import java.util.*;


public class Initializer {
    /**
     * 这是目前最重要的一个核心处理函数
     * @param solution
     * @param reference
     * @param middleware
     */
    public void initialize(Solution solution, Reference reference, Middleware middleware) {
        int countExceed = 0;
        int[][] processTimeMatrix = reference.getProcessTimeMatrix();
        List<List<Integer>> expertsSortByProcessTime = reference.getExpertsSortByProcessTime();
        /*
        去掉999999的任务之后对专家按照能够处理的任务的时长排序
        expertSortByProcessTime 里面存储的第一层是专家的序号 第二层是专家处理的任务的序号
         */
        int[] tasksSortByMaxResTime = reference.getTasksSortByMaxResTime();
        for (int j : tasksSortByMaxResTime) {
            int startTime = 0;
            int endTime = 0;
            int T = Integer.MAX_VALUE;
            int question = reference.getTaskQuestion(j);    // 任务类型
            int createTime = reference.getTaskCreateTime(j);    // 任务创建时间
            int sla = reference.getTaskSla(j);  // 任务的松弛时间
            int expert = 0;
            int maxResponseTime = createTime + sla;
            List<Integer> experts = expertsSortByProcessTime.get(question - 1);
            // 这里直接获取了所有的能够直接做这个任务的专家的序号
            // 直接开始遍历向下做就行了

            int temp_expert_pos = 0;
            int final_expert_pos = 0;
            for (int i : experts) {
                int processTime = processTimeMatrix[i - 1][question - 1];
                for (int t = createTime; t < Integer.MAX_VALUE; t++) {
                    if (t + processTime >= T) {
                        // 这里进行遍历比较
                        break;
                    } else {
                        boolean canDeal = solution.expertIdleInPeriod(i, t, t + processTime);
                        if (canDeal) {
                            T = t + processTime;
                            startTime = t;
                            endTime = T;
                            expert = i;
                            final_expert_pos = temp_expert_pos;
                            break;
                            // 这里主要是确定expert的具体人选
                        }
                    }
                }
                temp_expert_pos ++ ;
            }
            solution.addSchedule(j, expert, startTime, endTime);
            // 这里更新了expert 的安排，加入了新的Schedule
            // 更新 expertProcessTime, taskAllocateTimes, taskStartTime, firstExpertForTask
            middleware.updateExpertProcessTime(expert, endTime - startTime, "add");
            middleware.updateTaskAllocateTimes(j, 1, "add");
            middleware.updateTaskStartTime(j, startTime);
            middleware.updateFirstExpertForTask(j, expert);
            middleware.updateTaskValidStartTime(j, startTime);
            middleware.updateTaskEndTime(j, endTime);

            middleware.setMap4ExpertTaskEndTime(expert, endTime, j); // 设置专家在某时刻的结束任务
            middleware.setMap4ExpertTaskStartTime(expert, startTime, j); // 设置专家在某时刻的开始任务
            middleware.setTaskPreExpert(j, expert);
            if (startTime <=  maxResponseTime) {
                middleware.addTask2STimeB4MaxResTime(j); // 待定
                middleware.setResTimeMap(j, expert); // 待定
                middleware.setMap4Type2Task(question, j);//记录满足条件算子
                middleware.setMap4ExpertFeasibleTask(expert, j);// 专家交换算子
            }
            else{
                middleware.setArr4ExceedTask(j);//超时任务算子
                middleware.setMap4ExceedTaskExpertPos(j, final_expert_pos);//向前转移算子
                middleware.setMap4ExpertNonFeasibleTask(expert, j);//专家交换算子
                countExceed ++ ;
            }
        }
        System.out.println("\tExceed tasks : "  + countExceed);
    }
}


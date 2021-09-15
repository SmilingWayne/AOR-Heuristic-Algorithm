package common;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Check {
    public void checkSolution(AlgoParam param,Input input) throws Exception {

        ArrayList<ArrayList<Integer>> sol = new ArrayList<ArrayList<Integer>>();
        String path = param.path_result_sol+"/result.csv";
        BufferedReader bw1 = new BufferedReader(new FileReader(path));
        String line = null;
        int c = 0;
        bw1.readLine();
        while ((bw1.readLine()) != null) {
            c += 1;
        }
        bw1.close();
        BufferedReader bw2 = new BufferedReader(new FileReader(path));
        bw2.readLine();
        for (int i = 0; i < c; i++) {
            ArrayList<Integer> schedule = new ArrayList<Integer>();
            line = bw2.readLine();
            String[] subs = line.split(",");
            for (int j = 0; j < 4; j++) {
                schedule.add(Integer.parseInt(subs[j]));
            }
            sol.add(schedule);
        }
        bw2.close();

        //检查任务调度次数
        HashMap<Integer, ArrayList> tasks = new HashMap<Integer, ArrayList>();//每一个任务分配的专家集合
        for (int i = 0; i < sol.size(); i++) {
            if (!tasks.containsKey(sol.get(i).get(0))) {
                ArrayList<Integer> expert = new ArrayList<Integer>();
                expert.add(sol.get(i).get(1));
                tasks.put(sol.get(i).get(0), expert);
            } else {
                ArrayList<Integer> expert = tasks.get(sol.get(i).get(0));
                expert.add(sol.get(i).get(1));
                if (expert.size() > 5) {
                    System.out.println(sol.get(i).get(0));
                    for(int g = 0 ; g < expert.size(); g ++ ) {
                        System.out.println(expert.get(g));
                    }
                    throw new Exception("任务调度次数超过限额" + "task:\t" + (i + 1));
                }
                tasks.put(sol.get(i).get(0), expert);
            }
        }
        //检查任务同一时间是否对应多个小二、任务调度时间是否小于任务到达时间 、是否存在未分配的任务、小二并发处理量是否超限
        ArrayList<HashMap<Integer, Integer>> expert_process_time = new ArrayList<HashMap<Integer, Integer>>();//所有专家在每个时刻任务个数的记录
        for (int i = 0; i < input.getProcessTimeMatrix().length; i++) {
            expert_process_time.add(new HashMap<Integer, Integer>());
        }
        for (int i = 1; i <= input.getWorkOrder().length; i++) {
            int type = input.getWorkOrder()[i - 1][2];
            HashMap<Integer, Integer> process = new HashMap<Integer, Integer>();//当前任务 什么时间 什么专家接受
            HashMap<Integer, Integer> end_time = new HashMap<Integer, Integer>();//当前任务 key:开始时间 value:结束时间
            for (int j = 0; j < sol.size(); j++) {
                int taskId = sol.get(j).get(0);
                if (taskId != i) {
                    continue;
                }
                int expert = sol.get(j).get(1);
                int time = sol.get(j).get(2);
                int endTime = sol.get(j).get(3);
                if (time < input.getWorkOrder()[taskId - 1][1]) {
                    throw new Exception("任务调度时间小于任务到达时间" + "task:\t" + i + "\t产生时间：" + +input.getWorkOrder()[taskId - 1][1] + "\t调度时间：\t" + time);
                }
                if (process.containsKey(time) && process.get(time) != expert) {
                    throw new Exception("同一任务同一时间对应多个小二id" + "任务：" + i + "\t时间：\t" + time);
                } else {
                    process.put(time, expert);
                    end_time.put(time,endTime);
                }
            }
            if (process.size() == 0) {
                throw new Exception("存在未分配的任务" + "\t任务：\t" + i);
            }
            //把处理这个任务的专家 按照开始时间排序
            List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(process.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o1.getKey() - o2.getKey();
                }
            });
            Iterator<Map.Entry<Integer, Integer>> iterator = list.iterator();
            ArrayList<Integer> start_time = new ArrayList<Integer>();
            ArrayList<Integer> experts = new ArrayList<Integer>();
            ArrayList<Integer> endTime = new ArrayList<Integer>();
            while (iterator.hasNext()) {
                int time = iterator.next().getKey();
                start_time.add(time);
                experts.add(process.get(time));
                endTime.add(end_time.get(time));
            }
            for (int h = 0; h < experts.size() - 1; h++) {
                if ((int) experts.get(h) == (int) experts.get(h + 1)) {
                    throw new Exception("任务被连续分配给同一个专家" + "\t任务：" + i + "\t专家：" + experts.get(h));
                }
            }
            for (int h = 0; h < experts.size() - 1; h++) {
                int exp = experts.get(h);
                int duration = start_time.get(h + 1) - start_time.get(h);
                if (duration >= input.getProcessTimeMatrix()[exp - 1][type - 1]) {
                    throw new Exception("处理完的任务被再次分配" + "\t任务：" + i);
                }
                if((int)start_time.get(h + 1) != (int) endTime.get(h)){
//                    System.out.println(start_time.get(h+1) + " " + endTime.get(h));
//                    for(int k = 0; k < sol.size(); k ++ ){
//                        if(sol.get(k).get(0) == 625){
//                            System.out.println(sol.get(k).get(0) + " " + sol.get(k).get(1) + " " + sol.get(k).get(2) + " " + sol.get(k).get(3));
//                        }
//                    }
                    // 这里有一个大坑:Integer 直接获取Integer进行比较有时候会出错,最好强制转换为Int再操作
                    //System.out.println(sol.size());
                    System.out.println(start_time.get(h + 1) + "  "  + end_time.get(h));
                    throw new Exception("任务此次处理的结束时间和下一次处理的开始时间不一致" + "\t任务：" + i+ "\t此次处理专家：" + experts.get(h)+ "\t下一次处理专家：" + experts.get(h+1));
                }
            }
            int expert = experts.get(experts.size() - 1);
            int processTime=endTime.get(experts.size() - 1)-start_time.get(experts.size() - 1) ;
            if(processTime!=input.getProcessTimeMatrix()[expert - 1][type - 1]){
                System.out.println("真实处理时间 " + processTime + "   " + " 当前处理时间 " +  input.getProcessTimeMatrix()[expert - 1][type - 1]);
                throw new Exception("有效处理的时长有误" + "\t任务：" + i);
            }
            for (int h = 0; h < experts.size() - 1; h++) {
                int exp = experts.get(h);
                for (int time = start_time.get(h); time < start_time.get(h + 1); time++) {
                    if (expert_process_time.get(exp - 1).containsKey(time)) {
                        int num = expert_process_time.get(exp - 1).get(time);
                        if (num + 1 > 3) {
                            //System.out.println(num);
                            throw new Exception("小二并发处理量超限" + "exp:\t" + exp + "\ttime:" + time);
                        } else {
                            expert_process_time.get(exp - 1).put(time, num + 1);
                        }
                    } else {
                        expert_process_time.get(exp - 1).put(time, 1);
                    }
                }
            }
            int exp = experts.get(experts.size() - 1);
            int start = start_time.get(experts.size() - 1);
            for (int time = start; time < start + input.getProcessTimeMatrix()[exp - 1][type - 1]; time++) {
                if (expert_process_time.get(exp - 1).containsKey(time)) {
                    int num = expert_process_time.get(exp - 1).get(time);
                    if (num + 1 > 3) {
                        throw new Exception("小二并发处理量超限" + "exp:\t" + exp + "\ttime:" + time);
                    } else {
                        expert_process_time.get(exp - 1).put(time, num + 1);
                    }
                } else {
                    expert_process_time.get(exp - 1).put(time, 1);
                }
            }
        }
        //检查是否存在无效id
        for (int i = 0; i < sol.size(); i++) {
            int taskId = sol.get(i).get(0);
            if (taskId < 1 || taskId > input.getWorkOrder().length) {
                throw new Exception("存在无效id" + "\tid:" + taskId);
            }
        }
        System.out.println("csv check feasible: true");
    }
}


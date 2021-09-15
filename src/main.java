import algo.Initializer;
import algo.Operator;
import common.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Ref;
import java.util.List;
import java.util.*;


public class main {
    static AlgoParam param;
    public static void main(String[] args) throws Exception {

         param = new AlgoParam(
                "ServiceSchedule",       // problem_name
                "191870180 191870037",     // TODO: your student id
                "汪智笑+费爱跃",   // TODO: your name
                "algo_demo",        // TODO: your algo_name
                60,            // time_limit = 60 seconds
                "process_time_matrix.csv",     //process_time_input
                "work_order.csv",  // work_order_input
                "data/"   // path_data
        );
        preparePaths(param);
        long start = System.currentTimeMillis();
        Input input = new Input(param.path_data+param.work_order_input, param.path_data+param.process_time_input);
        input.readData();

        Initializer initializer = new Initializer();
        Reference reference = new Reference(input.getProcessTimeMatrix(), input.getWorkOrder());
        Solution solution = new Solution(reference);
        Middleware middleware = new Middleware(reference);
        /**
         * TODO
         *
         */
        //getTaskParam(reference, initializer, solution); // 这个函数是Task调参的时候使用的，多粒度调参获取最佳结果
        //testParam4Tasks(reference, initializer, solution); // 这个是调参函数的简易包装
        reference.sortTasksByWeights(new int[]{91,191,95,87,36}); // 五个变量分别是到达时间、最短处理时间、忍耐时间、可服务专家数量和后悔值五个因素
        System.out.println("\t\tProcess Time limit : 120s");
        initializer.initialize(solution, reference, middleware);
//        Operator opt = new Operator(middleware, reference, solution);
//        opt.perturb();
//        opt.two_switch();
//        opt.two_exchange();
        solution = opt_module(middleware, reference, solution);

        //Operator opt = new Operator(middleware, reference, solution);
//        opt.perturb();
//        opt.two_switch();
//        opt.two_exchange();
//        Middleware mid1 = middleware;
//        Reference ref1 = reference;
//        Solution sol1 = solution;
//        double temp_max = sol1.accumulateScore(ref1)[0];
//        for(int i = 0 ; i < 10; i ++ ){
//            Operator temp_opt = new Operator(mid1, ref1, sol1);
//            temp_opt.perturb();
//            temp_opt.two_switch();
//            temp_opt.two_exchange();
//            //System.out.println(sol1.accumulateScore(ref1)[0]);
//            double ans = sol1.accumulateScore(ref1)[0];
//            if(ans > temp_max){
//                solution.setSolution(sol1.getSolution());
//            }
////            mid1 = middleware;
////            ref1 = reference;
////            sol1 = solution;
//        }
        //solution = opt_module(middleware, reference, solution);
//        for(int i = 0; i < 50; i ++ ) {
//            opt.ejectionChain();
//        }
//        System.out.println("Ejection chain finished");
        //solution = opt_module(middleware,reference,solution);
        //opt.two_switch_2(); 效果很差，舍去
        //todo neighborhood operators
        // neighborhood operators?
        solution.checkContinuousAllocate(reference);
        double[] result;
        result = solution.accumulateScore(reference);
        System.out.println("方案评分: " + result[0]);
        System.out.println("R bar: " + result[2]);
        double time = 0.001 * (System.currentTimeMillis() - start);
        System.out.println("时间: " + time);
        output(param,time,result[0]);
        writePartialToCSV(param,solution);
        Check check = new Check();
        check.checkSolution(param,input);
//        int count = 0;
//        for(int i = 0; i < reference.getTaskNum() ; i ++ ){
//            int standardTime = ref1.getTaskCreateTime(i + 1) + ref1.getTaskSla(i + 1);
//            if(mid1.getTaskStartTime(i + 1) > standardTime){
//                count +=1;
//            }
//        }
//        System.out.println(count);
//        System.out.println("Statistics:\n\n");
//        reference.displayTasksStatistics();
    }
    /**
     * module for opt
     * @param middleware
     * @param reference
     * @param solution
     * After six main operators the opt_ans
     *
     * @return
     */
    static Solution opt_module(Middleware middleware, Reference reference,Solution solution){
        Operator opt = new Operator(middleware, reference, solution);
        for(int i = 0; i < 4000; i ++ ) {
            opt.ejectionChain_test2();
            //System.out.println(i);
        }
        System.out.println("Ejection chain terminated!");
        opt.two_switch();
        opt.allocateInvalid();
        opt.addExtraInvalidProcess();
        opt.relocate();
        return solution;
    }

    /**
     * 统计 这个和上面那个统计不大一样，是针对操作后的结果进行的统计
     * @param opt
     * @param middleware
     * @param solution
     */
    static void displayStatistics(Operator opt, Middleware middleware, Solution solution){
        System.out.println("\t\tAll exceeding-responding taskId:");
        opt.displayExceedTaskId();
        System.out.println("\t\tCount exceeding-responding taskType:");
        middleware.countExceedTaskType();
//        System.out.println("\t\tCount all experts parallel-tasks:");
//        solution.displayExpertProcessTaskNum();
    }

    /**
     * 下面的5个都是baseline 不需要管它
     * @param param
     */

    static void preparePaths(AlgoParam param) {
        try{
            File dir_result = new File("result");
            if(!dir_result.exists() || !dir_result.isDirectory()){
                dir_result.mkdir();
            }
            File dir_problem = new File(dir_result, param.problem_name);
            if(!dir_problem.exists() || dir_problem.isDirectory() == false){
                dir_problem.mkdir();
            }
            File dir_algo = new File(dir_problem, param.algo_name);
            if(dir_algo.exists() == false || dir_algo.isDirectory() == false){
                dir_algo.mkdir();
            }

            param.path_result_sol = dir_algo.getAbsolutePath();
            param.path_result_csv = dir_problem.getAbsolutePath() + "/" + param.csv_name();
            param.initial_result_csv();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    static void output(AlgoParam param,double time,double result){
        Log.start(new File(param.path_result_csv), true);
        Log.writeln(toCsvString(time ,result));
        Log.end();
    }


    static String toCsvString(double time,double result) {
        return  param.problem_name+ "," + param.author_id + "," + param.author_name + "," + param.algo_name + "," + result + "," + time;
    }

    static void writePartialToCSV(AlgoParam param,Solution solution) {
        try {
            File file = new File(param.path_result_sol+"/result.csv");
            FileWriter fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("task_id,expert_id,start_time,end_time");
            bw.newLine();
            for (List<Integer> list : solution.getSolution()) {
                String s = list.get(0) + "," + list.get(1) + "," + list.get(2)+ "," + list.get(3);
                bw.write(s);
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void getTaskParam(Reference reference, Initializer init, Solution sool){
        int[] parameter = new int[5];
        testParam4Tasks(reference, init, sool, parameter);
    }

    static void testParam4Tasks(Reference reference, Initializer init, Solution sool, int[] parameter){
        reference.createParaTaskSequence();
        double maxScore = 0;
        int cnt = 0;
        int imax = 0;
        int jmax = 0;
        int kmax = 0;
        int mmax = 0;
        for(int i = 90; i < 100; i ++ ){
            for(int j = 90; j < 100; j ++ ){
                for(int k = 90; k < 100; k ++ ){
                    for(int m = 90; m < 100; m ++ ){
                        parameter[4] = i;
                        parameter[0] = j;
                        parameter[1] = k;
                        parameter[2] = m;
                        parameter[3] = 500 - i - j - k - m;
                        reference.sortByMultipleIn(parameter);
                        Solution solution = new Solution(reference);
                        Middleware middleware = new Middleware(reference);
                        init.initialize(solution, reference, middleware);
                        double[] result;
                        result = solution.accumulateScore(reference);
                        if(result[0] > maxScore) {
                            System.out.println("方案评分: " + result[0]);
                            sool = solution;
                            imax = i;
                            jmax = j;
                            kmax = k;
                            mmax = m;
                            maxScore = result[0];
                        }
                        cnt ++ ;
                        System.out.println(cnt + " " + i + " " + j + " " + k + " " + m + " " + maxScore);
                    }
                }
            }
        }
        System.out.println(maxScore + " " + imax + " " + jmax + " " + kmax + " " + mmax);
        parameter[4] = imax; parameter[0] = jmax; parameter[1] = kmax; parameter[2] = mmax; parameter[3] = 500 - imax - jmax- kmax - mmax;
    }
}

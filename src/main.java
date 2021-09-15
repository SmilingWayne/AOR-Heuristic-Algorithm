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
                "41519161",     // TODO: your student id
                "Hu Qian",   // TODO: your name
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
        initializer.initialize(solution, reference, middleware);
        solution = opt_module(middleware, reference, solution);
        solution.checkContinuousAllocate(reference);
        double[] result;
        result = solution.accumulateScore(reference);
        System.out.println("R bar: " + result[2]);
        System.out.println("方案评分: " + result[0]);
        //输出
        double time = 0.001 * (System.currentTimeMillis() - start);
        System.out.println("时间: " + time);
        output(param,time,result[0]);
        writePartialToCSV(param,solution);
        Check check = new Check();
        check.checkSolution(param,input);
    }

    static Solution opt_module(Middleware middleware, Reference reference,Solution solution){
        Operator opt = new Operator(middleware, reference, solution);
        opt.exchange();
        opt.two_exchange();
        opt.allocateInvalid();
        opt.relocate();
        return solution;
    }

    /**
     * baseline
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
}

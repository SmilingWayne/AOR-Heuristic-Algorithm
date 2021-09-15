package common;
import java.util.*;
public class Middleware {
    Reference reference;
    private List<Integer> sTimeB4MaxResTime = new ArrayList<Integer>();//todo hashmap
    private Map<Integer, Integer> sTimeB4maxResTimeMap = new HashMap<>();
    private Map<Integer, List<Integer>> Map4Type2Task = new HashMap<>();
    private Map<Integer, Integer> Map4ExceedTaskExpertPos = new HashMap<>();
    private Map<Integer, List<Integer>> map4ExpertFeasibleTask = new HashMap<>();
    private Map<Integer, List<Integer>> map4ExpertNonFeasibleTask = new HashMap<>();
    private List<Integer> arr4ExceedTask = new ArrayList<>();

    private List<Integer> belowAverageWorkingTimeExpert = new ArrayList<>();

    private Map<Integer, Map<Integer, List<Integer>>> map4ExpertTaskEndTime = new HashMap<>();
    private Map<Integer, Map<Integer, List<Integer>>> map4ExpertTaskStartTime = new HashMap<>();

    private List<Integer> list4ExtraAllocationInvalidTask = new ArrayList<>();
    private Set<Integer> notPerfectExpertTask = new HashSet<>(); // 不是最好专家处理的任务 这个统计似乎没必要 但是还是加上了

    private int[] taskStartTime;
    private int[] expertProcessTime;
    private int[] taskAllocateTimes;
    private int[] firstExpertForTask;
    private int[] taskValidStartTime;
    private int[] taskEndTime;
    private int[] taskPreExpert;

    public int totalExpertProcessTime;

    public int[] getTaskValidStartTime() {
        return taskValidStartTime;
    }

    public Middleware(Reference reference) {
        this.taskStartTime = new int[reference.getTaskNum()];
        this.expertProcessTime = new int[reference.getExpertNum()];
        this.taskAllocateTimes = new int[reference.getTaskNum()];
        this.firstExpertForTask = new int[reference.getTaskNum()];
        this.taskValidStartTime = new int[reference.getTaskNum()];
        this.taskEndTime = new int[reference.getTaskNum()];
        this.taskPreExpert = new int[reference.getTaskNum()];
        this.reference = reference;
    }
    public void addList4ExtraAllocationInvalidTask(int taskId){
        this.list4ExtraAllocationInvalidTask.add(taskId);
    }
    public List<Integer> getList4ExtraAllocationInvalidTask(){
        return this.list4ExtraAllocationInvalidTask;
    }
    public List<Integer> calculateAllExtraAllocationInvalidTask(){
        for(int i = 0; i < this.arr4ExceedTask.size(); i ++ ){
            int tempId = this.arr4ExceedTask.get(i);
            if(this.getTaskAllocateTimes(tempId) == 5){
                continue;
            }
            this.addList4ExtraAllocationInvalidTask(tempId);
        }
        return getList4ExtraAllocationInvalidTask();
    }

    public void deleteExtraAllocationInvalidTask(Set<Integer> toBDeleted){
        Iterator<Integer> itor = this.getList4ExtraAllocationInvalidTask().iterator();
        while(itor.hasNext()){
            if(toBDeleted.contains(itor.next())){
                itor.remove();
            }
        }
    }

    public void setTaskPreExpert(int taskId, int expertId){
        this.taskPreExpert[taskId - 1] = expertId;
    }
    public int getTaskPreExpert(int taskId){
        return this.taskPreExpert[taskId - 1];
    }
    public void addNotPerfectExpertTask(int taskId){
        this.notPerfectExpertTask.add(taskId);
    }
    public Set<Integer> getNotPerfectExpertTask(){
        return this.notPerfectExpertTask;
    }
    public void removeNotPerfectExpertTask(int taskId){
        Iterator<Integer> itor = this.notPerfectExpertTask.iterator();
        while(itor.hasNext()){
            if(taskId == itor.next()){
                itor.remove();
                break;
            }
        }
    }

    public void addTotalExpertProcessTime(int add){
        this.totalExpertProcessTime += add;
    }

    public List<Integer> getBelowAverageWorkingTimeExpert(){
        int totalWorkingHrs = 0;
        int length = expertProcessTime.length;
        this.belowAverageWorkingTimeExpert.clear();
        double averageWorkingHrs = this.totalExpertProcessTime / length;
        for(int i = 0; i < length; i ++ ){
            if(expertProcessTime[i] < (int)averageWorkingHrs){
                this.belowAverageWorkingTimeExpert.add(i + 1);
            }
        }
        return this.belowAverageWorkingTimeExpert;
    }



    /**
     * 这个函数和map4expertTaskEndTime 的三个都是一样的
     * @param expertId
     * @param startTime
     * @param taskId
     */
    public void setMap4ExpertTaskStartTime(int expertId, int startTime, int taskId){
        if(this.map4ExpertTaskStartTime.isEmpty() || !this.map4ExpertTaskStartTime.containsKey(expertId)){
            Map<Integer, List<Integer>> temp = new HashMap<>();
            List<Integer> temp_arr = new ArrayList<>();
            temp_arr.add(taskId);
            temp.put(startTime, temp_arr);
            this.map4ExpertTaskStartTime.put(expertId, temp);
            return;
        }
        else{
            if(this.map4ExpertTaskStartTime.get(expertId).containsKey(startTime)){
                this.map4ExpertTaskStartTime.get(expertId).get(startTime).add(taskId);
            }
            else{
                List<Integer> temp_list = new ArrayList<>();
                temp_list.add(taskId);
                this.map4ExpertTaskStartTime.get(expertId).put(startTime, temp_list);
            }
        }
    }

    public List<Integer> getMap4ExpertTaskStartTime(int expertId, int startTime){
        if(!this.map4ExpertTaskStartTime.containsKey(expertId)){
            System.out.println(expertId + " 专家没有解决任务");
            return new ArrayList<Integer>();
        }
        else{
            if(!this.map4ExpertTaskStartTime.get(expertId).containsKey(startTime)){
                System.out.println(expertId + " 专家在 " + startTime + " 时间没有任务开始！" );
                return new ArrayList<Integer>();
            }
            else{
                return this.map4ExpertTaskStartTime.get(expertId).get(startTime);
            }
        }
    }

    public void removeMap4ExpertTaskStartTime(int expertId, int taskId, int pre_startTime){
        if(!this.map4ExpertTaskStartTime.containsKey(expertId) || !this.map4ExpertTaskStartTime.get(expertId).containsKey(pre_startTime)){
            System.out.println(expertId + " 专家没有解决任务" + " 或者在 " + pre_startTime + " 的时刻没有开始了的任务");
            return;
        }
        else{
            Iterator<Integer> itor = this.map4ExpertTaskStartTime.get(expertId).get(pre_startTime).iterator();
            while(itor.hasNext()){
                Integer value = itor.next();
                if(value == taskId){
                    itor.remove();
                    break;
                }
            }
            //this.setMap4ExpertTaskStartTime(expertId, new_startTime, taskId);
        }
    }
    public void setMap4ExpertTaskEndTime(int expertId, int endTime, int taskId){
        if(this.map4ExpertTaskEndTime.isEmpty() || !this.map4ExpertTaskEndTime.containsKey(expertId)){
            Map<Integer, List<Integer>> temp = new HashMap<>();
            List<Integer> temp_arr = new ArrayList<>();
            temp_arr.add(taskId);
            temp.put(endTime, temp_arr);
            this.map4ExpertTaskEndTime.put(expertId, temp);
            return;
        }
        else{
            if(this.map4ExpertTaskEndTime.get(expertId).containsKey(endTime)){
                this.map4ExpertTaskEndTime.get(expertId).get(endTime).add(taskId);
            }
            else{
                List<Integer> temp_list = new ArrayList<>();
                temp_list.add(taskId);
                this.map4ExpertTaskEndTime.get(expertId).put(endTime, temp_list);
            }
        }
    }

    /**
     * 这个函数用来Hash记录某个专家做的每个任务的结束时间，灵活动态调整
     * @param expertId
     * @param taskId
     * @param pre_endTime
     */
    public void removeMap4ExpertTaskEndTime(int expertId, int taskId, int pre_endTime){
        if(!this.map4ExpertTaskEndTime.containsKey(expertId) || !this.map4ExpertTaskEndTime.get(expertId).containsKey(pre_endTime)){
            System.out.println(expertId + " 专家没有解决任务" + " 或者在 " + pre_endTime + " 的时刻没有解决了的任务!!");
            return;
        }
        else{
            Iterator<Integer> itor = this.map4ExpertTaskEndTime.get(expertId).get(pre_endTime).iterator();
            while(itor.hasNext()){
                Integer value = itor.next();
                if(value == taskId){
                    itor.remove();
                    break;
                }
            }
        }

    }
    public List<Integer> getExpertTaskEndTime(int expertId, int endTime){
        if(!this.map4ExpertTaskEndTime.containsKey(expertId)){
            System.out.println(expertId + " 专家没有解决任务");
            return new ArrayList<Integer>();
        }
        else{
            if(!this.map4ExpertTaskEndTime.get(expertId).containsKey(endTime)){
                //System.out.println(expertId + " 专家在 " + endTime + " 时间没有任务完成！" );
                return new ArrayList<Integer>();
            }
            else{
                return this.map4ExpertTaskEndTime.get(expertId).get(endTime);
            }
        }
    }

    public void setMap4ExpertFeasibleTask(int expertId, int taskId){
        if(this.map4ExpertFeasibleTask.containsKey(expertId)){
            this.map4ExpertFeasibleTask.get(expertId).add(taskId);
        }
        else{
            List<Integer> temp = new ArrayList<>();
            temp.add(taskId);
            this.map4ExpertFeasibleTask.put(expertId, temp);
        }
    }

    public List<Integer> getMap4ExpertFeasibleTask(int expertId){
        if(!this.map4ExpertFeasibleTask.containsKey(expertId)){
            return new ArrayList<Integer>();
        }
        else{
            return this.map4ExpertFeasibleTask.get(expertId);
        }
    }

    /**
     * 删除一个特定的任务
     * @param expertId
     * @param taskId
     */
    public void removeMap4ExpertFeasibleTask(int expertId, int taskId){
        if(!this.map4ExpertFeasibleTask.containsKey(expertId)){
            System.out.println("执行错误，无法删除..");
            return;
        }
        else{
            Iterator<Integer> itor = this.map4ExpertFeasibleTask.get(expertId).iterator();
            while(itor.hasNext()){
                Integer item = itor.next();
                if(taskId == item){
                    itor.remove();
                    return;
                }
            }
        }
    }
    /**
     *
     * @param expertId
     * @param taskId
     */
    public void removeMap4ExpertFeasibleTask(int expertId, Set<Integer> taskId){
        if(!this.map4ExpertFeasibleTask.containsKey(expertId)){
            System.out.println("执行错误，无法删除.");
            return;
        }
        else{
            if(taskId.isEmpty()){
                return;
            }
            Iterator<Integer> itor = this.map4ExpertFeasibleTask.get(expertId).iterator();
            while(itor.hasNext()){
                Integer item =  itor.next();
                if(taskId.contains(item)){
                    itor.remove();
                }
            }
        }
    }


    public void setMap4ExpertNonFeasibleTask(int expertId, int taskId){
        if(this.map4ExpertNonFeasibleTask.containsKey(expertId)){
            this.map4ExpertNonFeasibleTask.get(expertId).add(taskId);
        }
        else{
            List<Integer> temp = new ArrayList<>();
            temp.add(taskId);
            this.map4ExpertNonFeasibleTask.put(expertId, temp);
        }
    }

    public List<Integer> getMap4ExpertNonFeasibleTask(int expertId){
        if(!this.map4ExpertNonFeasibleTask.containsKey(expertId)){
            return new ArrayList<Integer>();
        }
        else{
            return this.map4ExpertNonFeasibleTask.get(expertId);
        }
    }

    public void removeMap4ExpertNonFeasibleTask(int expertId, Set<Integer> taskId){
        if(!this.map4ExpertNonFeasibleTask.containsKey(expertId)){
            System.out.println("\t执行错误，无法删除" );
        }
        else{
            if(taskId.isEmpty()){
                return;
            }
            Iterator<Integer> itor = this.map4ExpertNonFeasibleTask.get(expertId).iterator();
            while(itor.hasNext()){
                Integer item = itor.next();
                if(taskId.contains(item)){
                    itor.remove();
                }
            }
        }
    }
    public void removeMap4ExpertNonFeasibleTask(int expertId, int taskid){
        if(!this.map4ExpertNonFeasibleTask.containsKey(expertId)){
            System.out.println("执行错误，无法删除!!!");
        }
        else{
            Iterator<Integer> itor = this.map4ExpertNonFeasibleTask.get(expertId).iterator();
            while(itor.hasNext()){
                //Integer item = itor.next();
                if(taskid == itor.next()){
                    itor.remove();
                }
            }
        }
    }
    public void setMap4Type2Task(int Type, int taskId){
        if(this.Map4Type2Task.containsKey(Type)){
            this.Map4Type2Task.get(Type).add(taskId);
        }
        else{
            List<Integer> temp = new ArrayList<>();
            temp.add(taskId);
            this.Map4Type2Task.put(Type, temp);
        }
    }


    public List<Integer> getMap4Type2Task(int Type){
        if(this.Map4Type2Task.containsKey(Type)){
            return this.Map4Type2Task.get(Type);
        }
        else{
            return new ArrayList<>();
        }
    }

    public int getFirstExpertForTask(int taskId) {
        return this.firstExpertForTask[taskId - 1];
    }
    public Map<Integer, Integer> getsTimeB4maxResTimeMap(){
        return this.sTimeB4maxResTimeMap;
    }
    public void setMap4ExceedTaskExpertPos(int exceedTask, int expertPos){
        this.Map4ExceedTaskExpertPos.put(exceedTask, expertPos);
    }
    public int getMap4ExceedTaskExpertPos(int exceedTask){
        return this.Map4ExceedTaskExpertPos.get(exceedTask);
    }
    public void removeExceedTaskExpertPos(Set<Integer> taskSet){
        if(taskSet.isEmpty()){
            return;
        }
        Iterator<Map.Entry<Integer, Integer>> itor = this.Map4ExceedTaskExpertPos.entrySet().iterator();
        while(itor.hasNext()){
            Map.Entry<Integer, Integer> item = itor.next();
            if(taskSet.contains(item.getKey())){
                itor.remove();
            }
        }
    }
    public void removeExceedTaskExpertPos(int taskId){
        Iterator<Map.Entry<Integer, Integer>> itor = this.Map4ExceedTaskExpertPos.entrySet().iterator();
        while(itor.hasNext()){
            Map.Entry<Integer, Integer> item = itor.next();
            if(item.getKey() == taskId){
                itor.remove();
                return;
            }
        }
    }
    public void setArr4ExceedTask(int exceedTask){
        this.arr4ExceedTask.add(exceedTask);
    }
    public List<Integer> getArr4ExceedTask(){
        return this.arr4ExceedTask;
    }
    public void removeArr4ExceedTask(Set<Integer> taskSet){
        if(taskSet.isEmpty()){
            return;
        }
        for (int i = 0; i < this.getArr4ExceedTask().size(); i++) {
            if(taskSet.contains(this.getArr4ExceedTask().get(i))){
                this.getArr4ExceedTask().remove(i);
                i--;   // 重点 - 一定要注意写!
            }
        }
    }
    public void removeArr4ExceedTask(int taskId){
        for (int i = 0; i < this.getArr4ExceedTask().size(); i++) {
            if(this.getArr4ExceedTask().get(i) == taskId){
                this.getArr4ExceedTask().remove(i);
                i--;   // 重点 - 一定要注意写!
            }
        }
    }
    public void setArr4ExceedTask(List<Integer> arr4ExceedTask){
        this.arr4ExceedTask = arr4ExceedTask;
    }
    public void setResTimeMap(int taskId, int expertId){
        this.sTimeB4maxResTimeMap.put(taskId, expertId);
    }

    public void setFirstExpertForTask(int[] firstExpertForTask) {
        this.firstExpertForTask = firstExpertForTask;
    }

    public int[] getTaskAllocateTimes() {
        return taskAllocateTimes;
    }

    public void setTaskAllocateTimes(int[] taskAllocateTimes) {
        this.taskAllocateTimes = taskAllocateTimes;
    }


    public void addTask2STimeB4MaxResTime(int task) {
        this.sTimeB4MaxResTime.add(task);
    }

    public void setSTimeB4MaxResTime(List<Integer> sTimeB4MaxResTime) {
        this.sTimeB4MaxResTime = sTimeB4MaxResTime;
    }

    public void setTaskStartTime(int[] taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public void setExpertProcessTime(int[] expertProcessTime) {
        this.expertProcessTime = expertProcessTime;
    }

    public List<Integer> getSTimeB4MaxResTime() {
        return sTimeB4MaxResTime;
    }

    public int getTaskStartTime(int taskId) {
        return this.taskStartTime[taskId - 1];
    }

    public int getTaskVaildStartTime(int taskId) {
        return this.taskValidStartTime[taskId - 1];
    }

    public int getTaskEndTime(int taskId) {
        return this.taskEndTime[taskId - 1];
    }

    public int[] getTaskStartTime() {
        return taskStartTime;
    }

    public int getExpertProcessTime(int expertId) {
        return this.expertProcessTime[expertId - 1];
    }

    /**
     *
     * @param expertId 专家ID
     * @param time  这个任务的工作时间
     * @param type  任务性质
     */
    public void updateExpertProcessTime(int expertId, int time, String type) {
        if (type.equals("add")) {
            this.expertProcessTime[expertId - 1] += time;
            this.totalExpertProcessTime += time;
        } else if (type.equals("sub")) {
            this.expertProcessTime[expertId - 1] -= time;
            this.totalExpertProcessTime -= time;
        } else if (type.equals("cover")) {
            this.expertProcessTime[expertId - 1] = time;
        }
    }
    public void displayExpertProcessTime(){
        for(int i = 0; i < this.expertProcessTime.length; i ++ ){
            System.out.println("\t\tExpert (" + (i + 1) + ") total processTime is " + expertProcessTime[i]);
        }
    }

    /**
     * 任务分配时间的计算
     * @param taskId
     * @param times
     * @param type
     */
    public void updateTaskAllocateTimes(int taskId, int times, String type) {
        if (type.equals("add")) {
            this.taskAllocateTimes[taskId - 1] += times;
        } else if (type.equals("sub")) {
            this.taskAllocateTimes[taskId - 1] -= times;
        } else if (type.equals("cover")) {
            this.taskAllocateTimes[taskId - 1] = times;
        }
    }

    public void updateTaskStartTime(int taskId, int startTime) {
        this.taskStartTime[taskId - 1] = startTime;
    }

    public void updateTaskValidStartTime(int taskId, int startTime) {
        this.taskValidStartTime[taskId - 1] = startTime;
    }

    public void updateTaskEndTime(int taskId, int endTime) {
        this.taskEndTime[taskId - 1] = endTime;
    }

    public void updateFirstExpertForTask(int taskId, int expert) {
        this.firstExpertForTask[taskId - 1] = expert;
    }
    // 更新了第一个做任务的专家

    public double getExpertAvgProcessTime(Reference reference) {
        double sum = 0;
        for (int i : expertProcessTime) {
            sum += i;
        }
        return sum / reference.getExpertNum();
    }

    public int getTaskAllocateTimes(int taskId) {
        return this.taskAllocateTimes[taskId - 1];
    }

    public int getMaxExpertsPTime() {
        int max = expertProcessTime[0];
        for (int i = 1; i < expertProcessTime.length; i++) {
            if (expertProcessTime[i] > max) {
                max = expertProcessTime[i];
            }
        }
        return max;
    }

    public int getMinExpertsPTime() {
        int min = expertProcessTime[0];
        for (int i = 1; i < expertProcessTime.length; i++) {
            if (expertProcessTime[i] < min) {
                min = expertProcessTime[i];
            }
        }
        return min;
    }
    public int[] getExpertsProcessTime() {
        return this.expertProcessTime;
    }
    public void displayS2Tasks(){
        for(int i = 0; i < sTimeB4MaxResTime.size(); i ++ ){
            System.out.print(sTimeB4MaxResTime.get(i) + " ");
        }
    }
}

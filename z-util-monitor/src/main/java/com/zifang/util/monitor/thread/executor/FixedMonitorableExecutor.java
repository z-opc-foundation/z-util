package com.zifang.util.monitor.thread.executor;

import com.zifang.util.json.model.JsonObject;
import com.zifang.util.monitor.thread.Status;
import com.zifang.util.monitor.thread.StatusLevel;
import com.zifang.util.monitor.thread.ThreadPoolStatus;
import com.zifang.util.monitor.thread.alarm.LogAlarmService;
import com.zifang.util.monitor.thread.alarm.ThreadPoolOvertimeAlarmPolicy;
import com.zifang.util.monitor.thread.constant.MonitorConstant;
import com.zifang.util.monitor.thread.utility.DateUtils;
import com.zifang.util.monitor.thread.utility.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 这是一个可以被监控的并且可捕捉到线程执行异常的线程池。<br/>
 * 主要针对ThreadPoolExecutor实现afterExecute方法。<br/>
 * 如果我们关心线程池执行的结果，则需要使用submit来提交task，
 * 那么在afterExecute中对异常的处理也需要通过Future接口调用get方法去取结果，才能拿到异常。<br/>
 * 如果我们不关心这个任务的结果，可以直接使用ExecutorService中的execute方法（实际是继承Executor接口）来直接去执行任务。<br/>
 * 这样的话，Runnable没有经过多余的封装，在runWorker中得到的异常也直接能在afterExecute中捕捉。<br/>
 *
 * @author zifang
 */
public class FixedMonitorableExecutor extends ThreadPoolExecutor implements MonitorableExecutor {

    /**
     * 日志对象。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedMonitorableExecutor.class);
    /**
     * 保存每个工作线程在方法中特定点的时间戳。
     */
    protected final ThreadLocal<Long> workerThreadTimeRecorder = new ThreadLocal<>();
    /**
     * 线程池配置。
     */
    private ThreadPoolConfigUnit threadPoolConfigUnit;
    /**
     * 线程池状态。
     */
    private ThreadPoolStatus status;

    /**
     * {@link ThreadPoolExecutor#ThreadPoolExecutor}
     *
     * @param threadPoolConfigUnit 组件配置。
     * @param workQueue            {@link ThreadPoolExecutor#ThreadPoolExecutor}
     * @param threadFactory        {@link ThreadPoolExecutor#ThreadPoolExecutor}
     */
    public FixedMonitorableExecutor(ThreadPoolConfigUnit threadPoolConfigUnit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory) {
        super(threadPoolConfigUnit.getPoolSize(),
                threadPoolConfigUnit.getPoolSize(), 0L, TimeUnit.MILLISECONDS, workQueue, threadFactory);
        status = new ThreadPoolStatus();
        this.threadPoolConfigUnit = threadPoolConfigUnit;
        if (threadPoolConfigUnit.getAlarmPolicy() == null) {
            threadPoolConfigUnit.setAlarmPolicy(new ThreadPoolOvertimeAlarmPolicy());
        }
        if (threadPoolConfigUnit.getAlarmService() == null) {
            threadPoolConfigUnit.setAlarmService(new LogAlarmService());
        }
    }

    @Override
    /**
     * execute方法。
     *      * @param command Runnable类型参数
     */
    public void execute(Runnable command) {
        //任务提交数+1
        this.status.getSubmitCount().getAndIncrement();
        super.execute(command);
    }

    @Override
    /**
     * beforeExecute方法。
     *      * @param t Thread类型参数
     * @param r Runnable类型参数
     */
    protected void beforeExecute(Thread t, Runnable r) {
        //任务启动数+1
        this.status.getStartCount().getAndIncrement();
        this.status.getLastStartTime().getAndSet(TimeUtil.getMillisTimestamp());
        workerThreadTimeRecorder.set(this.status.getLastStartTime().get());
        //线程状态执行前动作。
        taskStatusPreExecuteConfig(r);
    }

    /**
     * 主要用于捕捉线程池中的线程产生的异常。
     *
     * @param r 实际执行的任务。
     * @param t 产生的异常。
     */
    @Override
    /**
     * afterExecute方法。
     *      * @param r Runnable类型参数
     * @param t Throwable类型参数
     */
    protected void afterExecute(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    //get方法可以返回正常结束的执行结果，也可以抛出执行的异常。
                    future.get();
                    //成功执行次数+1
                    this.status.getSucessCount().getAndIncrement();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                t = e.getCause();
            }
        }
        if (t != null) {
            //失败执行次数+1
            this.status.getFailCount().getAndIncrement();
            LOGGER.error(t.getMessage(), t);
        }
        this.status.getLastFinishTime().getAndSet(TimeUtil.getMillisTimestamp());
        this.status.getTotalTimeConsuming().addAndGet(TimeUtil.getMillisTimestamp() -
                workerThreadTimeRecorder.get());
        taskStatusAfterExecuteConfig(r);
    }

    /**
     * 获取监控组件的状态。
     * <p>
     * 该方法会生成线程池中每个线程的状态、正在执行任务的状态以及线程池总体状态。
     *
     * @return 线程池的监控状态
     */
    public Status status() {
        generateThreadStatus(this.status.getThreadStatusMap());
        generateTaskStatus(this.status.getTaskStatusMap());
        return generatePoolStatus();
    }

    /**
     * 计算和生成线程池总体状态。
     *
     * @return 线程池状态。
     */
    private Status generatePoolStatus() {
        long submit = this.getTaskCount();
        long active = this.getActiveCount();
        long fail = this.status.getFailCount().get();
        long success = submit - fail - active;
        long waiting = this.getQueue().size();
        status.setLevel(StatusLevel.OK);
        //当最后一个启动的任务超过1个小时没有结束时，该线程池已经异常。
        if (this.threadPoolConfigUnit.getAlarmPolicy().needAlarm(status)) {
            status.setLevel(StatusLevel.ERROR);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("startupTime", DateUtils.getDateTimeString(
                status.getStartTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        jsonObject.put("lastStartJobTime", DateUtils.getDateTimeString(
                status.getLastStartTime().get(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        jsonObject.put("lastFinishJobTime", DateUtils.getDateTimeString(
                status.getLastFinishTime().get(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
        jsonObject.put("totalTimeConsuming", status.getTotalTimeConsuming().get());
        jsonObject.put("avgTimeConsuming", submit != 0 ? status.getTotalTimeConsuming().get() / submit : 0);
        jsonObject.put("submitCount", submit);
        jsonObject.put("successCount", success);
        jsonObject.put("failCount", fail);
        jsonObject.put("watingCount", waiting);
        jsonObject.put("active", active);
        jsonObject.put("alarmTimes", status.getAlarmTimes().get());
        status.setStatus(jsonObject.toString());
        return status;
    }

    /**
     * 计算和生成线程池中每个线程的状态。
     *
     * @param threadStatusMap 线程池中线程的状态。
     * @return 线程池状态。
     */
    private Status generateThreadStatus(Map<Thread, Map<String, Object>> threadStatusMap) {
        //TODO generateThreadStatus empty body
        return null;
    }

    /**
     * 计算和生成正在执行的任务的状态。
     *
     * @param taskStatusMap 正在执行的任务的状态。
     * @return 线程池状态。
     */
    private Status generateTaskStatus(Map<Runnable, Map<String, Object>> taskStatusMap) {
        if (taskStatusMap != null) {
            for (Iterator<Map.Entry<Runnable, Map<String, Object>>> iterator = taskStatusMap.entrySet().iterator();
                 iterator.hasNext(); ) {
                Map.Entry<Runnable, Map<String, Object>> entry = iterator.next();
                if (TimeUtil.getMillisTimestamp() - ((long) entry.getValue().get(MonitorConstant.TASK_START_TIME))
                        >= threadPoolConfigUnit.getThreadOvertimeThreshhold()) {
                    //当任务超时时
                    if (entry.getKey() instanceof RunnableFuture) {
                        //如果任务类型是RunnableFuture，即当任务是通过submit方法提交时，执行任务取消操作。
                        Future future = (RunnableFuture) entry.getKey();
                        if (future.cancel(true)) {
                            LOGGER.info("Task: {} canceled.", entry.getKey());
                        } else if (future.isCancelled() || future.isDone()) {
                            LOGGER.info("Task: {} already canceled.", entry.getKey());
                        }
                    } else {
                        //当任务超时，却不支持取消时，说明此任务是通过execute()方法提交的。
                        LOGGER.info("Irrevocable task: {} is overtime. submit() is necessary for revocable task.",
                                entry.getKey());
                    }
                }
            }
        }
        return this.status;
    }

    /**
     * 针对正在准备的任务，执行操作预处理。
     *
     * @param r 待执行任务。
     */
    private void taskStatusPreExecuteConfig(Runnable r) {
        //每一个任务被执行之前都要设置自己的状态。状态是一个map。
        Map<Runnable, Map<String, Object>> taskStatusMap = this.status.getTaskStatusMap();
        Map<String, Object> currentTaskStatus = taskStatusMap.get(r);
        if (currentTaskStatus == null) {
            currentTaskStatus = new HashMap<>();
            taskStatusMap.put(r, currentTaskStatus);
        }
        if (currentTaskStatus.get(MonitorConstant.TASK_START_TIME) == null ||
                currentTaskStatus.get(MonitorConstant.TASK_START_TIME) == MonitorConstant.INIT_STATUS) {
            //设置任务开始执行时间。
            currentTaskStatus.put(MonitorConstant.TASK_START_TIME, TimeUtil.getMillisTimestamp());
        }
    }

    /**
     * 针对已完成的任务，执行操作后处理。
     *
     * @param r 待执行任务。
     */
    private void taskStatusAfterExecuteConfig(Runnable r) {
        this.status.getTaskStatusMap().remove(r);
    }

    /**
     * 获取组件名称。
     *
     * @return 线程池名称
     */
    @Override
    /**
     * componentName方法。
     * @return String类型返回值
     */
    public String componentName() {
        return this.threadPoolConfigUnit.getPoolName();
    }

    /**
     * 触发告警。
     * <p>
     * 当线程池状态异常时调用此方法，通知告警服务进行告警。
     */
    @Override
    /**
     * alarm方法。
     */
    public void alarm() {
        this.status.getAlarmTimes().getAndIncrement();
        this.threadPoolConfigUnit.getAlarmService().alarm(this.threadPoolConfigUnit, this.status);
    }
}

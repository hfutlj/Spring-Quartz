package cn.lj.controller;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class JobController {

    @Autowired
    public Scheduler scheduler;

    @RequestMapping("/test")
    public void test() {
        System.out.println("hee");
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @RequestMapping("/getList")
    public List<JobDetail> getjobList() {
        List<JobDetail> jobList = new ArrayList<>();
        try {
            List<String> groupNames = scheduler.getJobGroupNames();
            for (String groupName : groupNames) {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
                for (JobKey jobKey : jobKeys) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    System.out.println(jobDetail.getJobClass());
                    jobList.add(jobDetail);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return jobList;
    }

    /**
     * 添加新Job
     */
    @RequestMapping("/addJob")
    public void addJob(@RequestParam String jobName, @RequestParam String jobClassPath, String jobGroup,
                       @RequestParam String cronExpression, @RequestParam String triggerName, String triggerGroup) {
        // 根据Job执行类的全路径获取执行类
        Class<? extends Job> jobClass;
        try {
            jobClass = (Class<? extends Job>) ClassLoader.getSystemClassLoader().loadClass(jobClassPath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //
        JobBuilder jobBuilder = JobBuilder.newJob(jobClass);
        jobGroup = StringUtils.isEmpty(jobGroup) ? "DEFAULT" : jobGroup;
        triggerGroup = StringUtils.isEmpty(triggerName) ? "DEFAULT" : triggerGroup;
        JobDetail jobDetail = jobBuilder.withIdentity(jobName, jobGroup).build();
        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();
        CronTrigger trigger = (CronTrigger) triggerBuilder
                .withIdentity(triggerName,triggerGroup)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/runOnce")
    public void runOnce() {
        try {
            // 这种触发方式会新建一个SimpleTrigger来触发该job，触发完成后
            // SimpleTrigger的状态为Complete，当job执行完成后会自动删除这个触发器
            scheduler.triggerJob(JobKey.jobKey("testJob"));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}

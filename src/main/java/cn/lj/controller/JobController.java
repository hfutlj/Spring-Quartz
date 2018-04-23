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
                       @RequestParam String cronString, @RequestParam String triggerName, String triggerGroup) {
        // 根据Job执行类的全路径获取执行类
        Class<? extends Job> jobClass;
        try {
            jobClass = (Class<? extends Job>) Thread.currentThread().getContextClassLoader().loadClass(jobClassPath);
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
                .withIdentity(triggerName, triggerGroup)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/deleteJob")
    public void deleteJob(@RequestParam String jobName, String jobGroup) {
        jobGroup = StringUtils.isEmpty(jobGroup) ? "DEFAULT" : jobName;
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            // 删除Job前停用所有与之相关的触发器
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                scheduler.pauseTrigger(trigger.getKey());
            }
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改CronTrigger的时间
     */
    @RequestMapping("/modifyTrigger")
    public void modifyTrigger(@RequestParam String triggerName, String triggerGroup, @RequestParam String cronString) {
        triggerGroup = StringUtils.isEmpty(triggerGroup) ? "DEFAULT" : triggerGroup;
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
            // 修改之前先停用当前trigger
            scheduler.pauseTrigger(triggerKey);
            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
                    .startNow()
                    .build();
            scheduler.rescheduleJob(triggerKey, cronTrigger);
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

package cn.lj.controller;

import cn.lj.entity.JobAndTriggers;
import cn.lj.entity.TriggerInfo;
import com.alibaba.fastjson.JSONObject;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @ResponseBody
    public String getjobList() {
        List<JobDetail> jobList = new ArrayList<>();
        List<JobAndTriggers> list = new ArrayList<>();

        try {
            List<String> groupNames = scheduler.getJobGroupNames();
            for (String groupName : groupNames) {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
                for (JobKey jobKey : jobKeys) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    System.out.println(jobDetail.getJobClass());
                    jobList.add(jobDetail);

                    JobAndTriggers jobAndTriggers = new JobAndTriggers();
                    jobAndTriggers.setName(jobKey.getName());
                    jobAndTriggers.setGroup(jobKey.getGroup());
                    jobAndTriggers.setJobClassPath(jobDetail.getJobClass().toString());
                    List<TriggerInfo> triggerInfoList = new ArrayList<>();
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        TriggerInfo triggerInfo = new TriggerInfo();
                        triggerInfo.setName(trigger.getKey().getName());
                        triggerInfo.setGroup(trigger.getKey().getGroup());
                        triggerInfo.setMiss_fire(trigger.getMisfireInstruction());
                        triggerInfo.setPre_fire_time(trigger.getPreviousFireTime().toString());
                        triggerInfo.setNext_fire_time(trigger.getNextFireTime().toString());
                        triggerInfo.setTriggerType(trigger.getDescription());
                        try {
                            triggerInfo.setCronString(((CronTrigger) trigger).getCronExpression());
                        } catch (ClassCastException e) {
                            triggerInfo.setCronString("非Cron类型Trigger");
                        }
                        triggerInfo.setState(scheduler.getTriggerState(trigger.getKey()).toString());
                        triggerInfoList.add(triggerInfo);
                    }
                    jobAndTriggers.setTriggerInfos(triggerInfoList);
                    list.add(jobAndTriggers);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(list);
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

    @RequestMapping("/pauseJob")
    public void pauseJob(String jobName, String jobGroup) {
        jobGroup = StringUtils.isEmpty(jobGroup) ? "DEFAULT" : jobGroup;
        try {
            scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/resumeJob")
    public void resumeJob(String jobName, String jobGroup){
        jobGroup = StringUtils.isEmpty(jobGroup) ? "DEFAULT" : jobGroup;
        try {
            scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}

package cn.lj.entity;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Job详情以及其包含的触发器列表
 */
@Component
public class JobAndTriggers {

    public String name;
    public String group;
    public String jobClassPath;
    public List<TriggerInfo> triggerInfos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getJobClassPath() {
        return jobClassPath;
    }

    public void setJobClassPath(String jobClassPath) {
        this.jobClassPath = jobClassPath;
    }

    public List<TriggerInfo> getTriggerInfos() {
        return triggerInfos;
    }

    public void setTriggerInfos(List<TriggerInfo> triggerInfos) {
        this.triggerInfos = triggerInfos;
    }
}

package cn.lj.entity;

import org.springframework.stereotype.Component;

/**
 * Trigger详情
 */
@Component
public class TriggerInfo {

    public String name;
    public String group;
    public String triggerType;
    public String pre_fire_time;
    public String next_fire_time;
    public String start_time;
    public String state;
    public int miss_fire;
    public String priority;
    public String cronString;


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

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getPre_fire_time() {
        return pre_fire_time;
    }

    public void setPre_fire_time(String pre_fire_time) {
        this.pre_fire_time = pre_fire_time;
    }

    public String getNext_fire_time() {
        return next_fire_time;
    }

    public void setNext_fire_time(String next_fire_time) {
        this.next_fire_time = next_fire_time;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getMiss_fire() {
        return miss_fire;
    }

    public void setMiss_fire(int miss_fire) {
        this.miss_fire = miss_fire;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCronString() {
        return cronString;
    }

    public void setCronString(String cronString) {
        this.cronString = cronString;
    }
}

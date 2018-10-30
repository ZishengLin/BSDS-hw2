package com.example.demo;

import javax.persistence.*;


@Entity
@Table(name = "user")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "userid", nullable = false)
    private int userid;

    @Column(name = "days", nullable = false)
    private int days;

    @Column(name = "timeinterval", nullable = false)
    private int timeinterval;

    @Column(name = "stepcount", nullable = false)
    private int stepcount;
    public User() {

    }

    public User(int id, int days, int time_interval, int step_count) {
        this.userid = id;
        this.days = days;
        this.timeinterval = time_interval;
        this.stepcount = step_count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getTimeinterval() {
        return timeinterval;
    }

    public void setTimeinterval(int timeinterval) {
        this.timeinterval = timeinterval;
    }

    public int getStepcount() {
        return stepcount;
    }

    public void setStepcount(int stepcount) {
        this.stepcount = stepcount;
    }
}
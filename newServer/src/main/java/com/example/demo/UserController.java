package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository users;


    @PostMapping(value = "/{id}/{day}/{timeInterval}/{stepCount}")
    public void postStep(@PathVariable("id") int id, @PathVariable("day") Integer days,  @PathVariable("timeInterval") Integer timeInterval,
                         @PathVariable("stepCount") Integer step) {
        User user = new User(id, days, timeInterval, step);
        try{
            users.save(user);
        }catch(Exception e) {

        }
    }

    @GetMapping(value = "/current/{id}")
    public Integer getCurStep(@PathVariable("id") int id) {
        List<User> ls = users.findAllByUserid(id);
        if (ls.size() == 0) {
            return -1;
        }
        Collections.sort(ls, (a, b)->(a.getDays() - b.getDays()));
        int day = ls.get(ls.size() - 1).getDays();
        return getStepByDay(id, day);
    }

    @GetMapping(value = "/single/{id}/{day}")
    public Integer getStepByDay(@PathVariable("id") Integer id, @PathVariable("day") Integer day) {

        List<User> ls = users.findAllByUseridAndDays(id, day);
        int sum = 0;
        for (User user: ls) {
            sum += user.getStepcount();
        }
        return sum;
    }


}

//    @GetMapping(value = "range/{id}/{startDay}/{numDays}")
//    public Integer getByRange(@PathVariable("id") Integer id, @PathVariable("startDay") Integer startDay

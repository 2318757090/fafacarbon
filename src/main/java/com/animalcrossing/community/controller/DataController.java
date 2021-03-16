package com.animalcrossing.community.controller;

import com.animalcrossing.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    @RequestMapping(path = "/data" ,method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    //统计网站UV
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd")Date fromDate,@DateTimeFormat(pattern = "yyyy-MM-dd")Date toDate){
        long uv = dataService.calculateUV(fromDate,toDate);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",fromDate);
        model.addAttribute("uvEndDate",toDate);
        //转发至data
        return "forward:/data";
        //return "/site/admin/data";
    }
    //统计活跃用户
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd")Date fromDate,@DateTimeFormat(pattern = "yyyy-MM-dd")Date toDate){
        long dau = dataService.calculateDAU(fromDate,toDate);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",fromDate);
        model.addAttribute("dauEndDate",toDate);
        //转发至data
        return "forward:/data";
        //return "/site/admin/data";
    }
}

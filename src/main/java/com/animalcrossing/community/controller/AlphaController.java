package com.animalcrossing.community.controller;

import com.animalcrossing.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;


    @RequestMapping("/hello")
    @ResponseBody
    public String saveHello(){
        return "Hello Spring";
    }

    @RequestMapping("/getData")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }
    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        System.out.println(request.getMethod());//请求行
        System.out.println(request.getServletPath());//请求行
        Enumeration<String> enumeration = request.getHeaderNames();//请求头
        while (enumeration.hasMoreElements()){
            String name=enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+"==="+value);
        }
        System.out.println(request.getParameter("code"));//请求体

        response.setContentType("text/html;chatset=utf-8");
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.write("<h1>hahahah</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //response直接像浏览器返回数据
    }
    //GET请求 students?current=2&limit=30 requestparam
    @RequestMapping(path="/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name="current",required = false,defaultValue = "1") int current,
            @RequestParam(name="limit",required = false,defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);

        return "some students";
    }
    //GET请求 /student/123 pathvariable
    @RequestMapping(path="/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a srudent";
    }
    //POST请求 这里不能访问 怀疑和security插件有关
    @RequestMapping(path="/student1",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){

        System.out.println(name);
        System.out.println(age);
        return "success";
    }
    //响应html
    @RequestMapping(path="/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","zhangsan");
        modelAndView.addObject("age",30);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }
    @RequestMapping(path="/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","北京工业");
        model.addAttribute("age",60);
        return "/demo/view";
    }
    //响应json java对象 json字符串 js对象
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","zhangsan");
        emp.put("age",30);
        emp.put("salary",8000.00);
        return emp;
    }
    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","zhangsan");
        emp.put("age",30);
        emp.put("salary",8000.00);
        list.add(emp);
        emp.put("name","lisi");
        emp.put("age",31);
        emp.put("salary",8000.00);
        list.add(emp);
        emp.put("name","fafa");
        emp.put("age",27);
        emp.put("salary",20000.00);
        list.add(emp);
        return list;
    }


}

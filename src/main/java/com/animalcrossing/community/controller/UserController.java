package com.animalcrossing.community.controller;

import com.animalcrossing.community.annotation.LoginRequired;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.service.FollowService;
import com.animalcrossing.community.service.LikeService;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.CommunityUtil;
import com.animalcrossing.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.Buffer;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;

    @LoginRequired
    @RequestMapping (path = "/setting" , method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }
    @LoginRequired
    @RequestMapping (path = "/upload" , method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model){
        //判断文件合法性
        if(headerImage==null){
            model.addAttribute("uploadMsg","您还没有选择图片");
            return "/site/setting";
        }
        //文件名称合法性
        String fileName = headerImage.getOriginalFilename();
        if(StringUtils.isBlank(fileName)){
            model.addAttribute("uploadMsg","文件格式错误，请选择以jpg/png/img/jpeg为后缀的图片文件");
            return "/site/setting";
        }
        //判断文件后缀合法
        System.out.println(fileName.lastIndexOf("."));
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(!suffix.equals(".jpg")&&!suffix.equals(".jpeg")&&!suffix.equals(".png")&&!suffix.equals(".img")){
            model.addAttribute("uploadMsg","文件格式错误，请选择以jpg/png/img/jpeg为后缀的图片文件");
            return "/site/setting";
        }
        //图片上传至服务器
        fileName = CommunityUtil.generateUUID()+suffix;
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        File dest = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("图片上传服务器失败"+e.getMessage());
            throw new RuntimeException("图片上传服务器失败",e);
        }
        //文件上传成功，修改数据库头像链接
        User user = hostHolder.getUser();
        userService.updateHeaderUrl(headerUrl,user.getId());
        model.addAttribute("uploadMsg","图片上传成功");
        return "site/setting";
    }
    @RequestMapping ( path = "/header/{filename}" , method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename")String fileName, HttpServletRequest request, HttpServletResponse response){
        fileName = uploadPath+"/"+fileName;
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        response.setContentType("image/"+suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(fileName);
                ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b=fis.read(buffer))!=-1) {
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像文件失败"+e.getMessage());
            e.printStackTrace();
        }

    }
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getUserProfile(@PathVariable("userId")int userId,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("likeCount",likeCount);
        //查询关注数量
        long followerCount = followService.findFollowerCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followerCount",followerCount);
        //查询粉丝数量
        long followeeCount = followService.findFolloweeCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followeeCount",followeeCount);
        //查询是否已关注当前用户

        boolean isFollowed = false;
        if(hostHolder.getUser()!=null){
            isFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("isFollowed",isFollowed);

        return "/site/profile";
    }
}

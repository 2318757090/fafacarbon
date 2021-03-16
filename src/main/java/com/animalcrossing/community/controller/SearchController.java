package com.animalcrossing.community.controller;

import com.animalcrossing.community.entity.DiscussPost;
import com.animalcrossing.community.entity.Page;
import com.animalcrossing.community.service.ElasticSearchService;
import com.animalcrossing.community.service.LikeService;
import com.animalcrossing.community.service.UserService;
import com.animalcrossing.community.util.CommunityConstant;
import com.animalcrossing.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String getSearchList(String keyword, Model model, Page page){
        //先搜索帖子
        List<DiscussPost> searchList = elasticSearchService.search(keyword, page.getCurrent()-1, page.getLimit() );
        List<Map<String,Object>> searchVoList = new ArrayList<>();
        if(searchList!=null){
            for (DiscussPost discussPost:searchList) {
                Map<String,Object> map = new HashMap<>();
                //帖子信息
                map.put("post",discussPost);
                //发帖者信息
                map.put("user",userService.findUserById(discussPost.getUserId()));
                //帖子点赞数
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                searchVoList.add(map);
            }
        }
        model.addAttribute("searchVoList",searchVoList);
        model.addAttribute("keyword",keyword);
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchList==null?0:elasticSearchService.getCount(keyword).intValue());
        return "/site/search";
    }
}

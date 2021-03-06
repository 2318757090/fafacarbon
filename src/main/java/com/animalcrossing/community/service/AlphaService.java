package com.animalcrossing.community.service;

import com.animalcrossing.community.dao.AlphaDao;
import com.animalcrossing.community.dao.DiscussPostMapper;
import com.animalcrossing.community.dao.UserMapper;
import com.animalcrossing.community.entity.DiscussPost;
import com.animalcrossing.community.entity.User;
import com.animalcrossing.community.util.CommunityUtil;
import org.apache.ibatis.transaction.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaService {
    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;



    public AlphaService(){
        System.out.println("实例化");
    }
    @PostConstruct
    public void init(){
        System.out.println("初始化Service");
    }
    @PreDestroy
    public void destory(){
        System.out.println("销毁");
    }
    public String find(){
        return alphaDao.select();
    }


    /**
     * 事务示例
     * 事务传播机制 在一个方法中调用别的组件中的方法 对其方法事务的设定
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED , propagation = Propagation.REQUIRED)
    public Object save1(){
        //新用户注册
        User user = new User();
        user.setEmail("Alpha@qq.com");
        user.setUsername("Alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword("123"+user.getSalt());
        user.setHeaderUrl("http://image.nowcoder.com/head/99t/png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //生成新帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("新人报道");
        discussPost.setContent("hahahahah");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        Integer.valueOf("abc");
        return "11";
    }
    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public String doInTransaction(TransactionStatus transactionStatus) {
                //新用户注册
                User user = new User();
                user.setEmail("Alpha@qq.com");
                user.setUsername("Alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword("123"+user.getSalt());
                user.setHeaderUrl("http://image.nowcoder.com/head/99t/png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
                //生成新帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("新人报道");
                discussPost.setContent("hahahahah");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);

                Integer.valueOf("abc");
                return "11";
            }
        });

    }



}

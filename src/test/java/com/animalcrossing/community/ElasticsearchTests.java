package com.animalcrossing.community;

import com.alibaba.fastjson.JSONObject;
import com.animalcrossing.community.dao.DiscussPostMapper;
import com.animalcrossing.community.dao.elasticsearch.DiscussPostRepository;
import com.animalcrossing.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;



    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }
    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
    }
    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("新人请多关照");
        discussPostRepository.save(discussPost);
    }
    @Test
    public void testDelete(){
        discussPostRepository.deleteAll();
    }
    @Test
    public void testSearch() throws IOException {
        SearchRequest request = new SearchRequest();
        //创建查询条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 使用QueryBuilders工具，精确查询term
        //QueryBuilders.matchAllQuery() 匹配所有
        MultiMatchQueryBuilder termQuery = QueryBuilders.
                multiMatchQuery("互联网寒冬","title","content");


        //配置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("content"); //绑定属性
        highlightBuilder.requireFieldMatch(true); //关闭多个高亮，只显示一个高亮
        highlightBuilder.preTags("<p style='color:red'>"); //设置前缀
        highlightBuilder.postTags("</p>"); //设置后缀
        sourceBuilder.highlighter(highlightBuilder);

//        sourceBuilder.sort(SortBuilders.fieldSort("type").order(SortOrder.DESC));
//        sourceBuilder.sort(SortBuilders.fieldSort("score").order(SortOrder.DESC));
//        sourceBuilder.sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));

        sourceBuilder.query(termQuery);//.sort("score",SortOrder.DESC).sort("createTime",SortOrder.DESC);
        sourceBuilder.sort("score",SortOrder.DESC);
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(sourceBuilder);

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        //获取结果对象
        SearchHits hits = response.getHits();

        System.out.println(JSONObject.toJSONString(hits));

        for (SearchHit searchHit : hits) {
            //获取高亮的html
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField name = highlightFields.get("name");

            //替换原有的字段
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

            if (name != null) {
                Text[] fragments = name.fragments();
                String light_name = "";
                for (Text fragment : fragments) {
                    light_name += fragment;
                }
                sourceAsMap.put("name", light_name); //进行替换
            }


            System.out.println(searchHit.getSourceAsMap());
        }
    }
}


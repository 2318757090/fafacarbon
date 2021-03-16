package com.animalcrossing.community.service;

import com.animalcrossing.community.dao.elasticsearch.DiscussPostRepository;
import com.animalcrossing.community.entity.DiscussPost;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    public void deleteDisscussPost(int id){
        discussPostRepository.deleteById(id);
    }
    public List<DiscussPost> search(String keywords,int current,int limit){
        //根据一个值查询多个字段  并高亮显示  这里的查询是取并集，即多个字段只需要有一个字段满足即可
        //需要查询的字段
        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title",keywords))
                .should(QueryBuilders.matchQuery("content",keywords));
        //构建高亮查询 结果排序和分页
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withHighlightFields(
                        new HighlightBuilder.Field("title")
                        ,new HighlightBuilder.Field("content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
 //               .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current,limit))
                .withHighlightBuilder(new HighlightBuilder().preTags("<span style='color:red'>").postTags("</span>"))
                .build();
        //查询
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        //得到查询返回的内容
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        //设置一个最后需要返回的实体类集合
        List<DiscussPost> posts = new ArrayList<>();
        //遍历返回的内容进行处理
        for(SearchHit<DiscussPost> searchHit:searchHits){
            //高亮的内容
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            //将高亮的内容填充到content中
            searchHit.getContent().setTitle(highlightFields.get("title")==null ? searchHit.getContent().getTitle():highlightFields.get("title").get(0));
            searchHit.getContent().setContent(highlightFields.get("content")==null ? searchHit.getContent().getContent():highlightFields.get("content").get(0));
            //放到实体类中
            posts.add(searchHit.getContent());
        }
        return posts;
    }
    public Long getCount(String keywords){
        //需要查询的字段
        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title",keywords))
                .should(QueryBuilders.matchQuery("content",keywords));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder).build();
        Long searchCount = elasticsearchRestTemplate.count(searchQuery, DiscussPost.class);
        return searchCount;

    }
}

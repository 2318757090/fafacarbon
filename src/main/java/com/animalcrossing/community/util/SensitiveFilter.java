package com.animalcrossing.community.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private String REPLACEMENT = "***";
    private TrieNode rootNode = new TrieNode();

    //根据敏感词文件生成前缀树
    //前缀树不需要反复生成 只在文件加载第一次时生成即可 所以使用postConstruct注解
    @PostConstruct
    public void init(){
        try(
                //将文件读到字节流中
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //因为是中文敏感词（原因存疑） 先读取到字符流中 再读取到缓冲流中提高效率
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                ) {
            String keyword;
            while ((keyword=br.readLine())!=null){
                this.addKeyword(keyword);
            }

        }catch (IOException e){
            logger.error("加载敏感词文件失败"+e.getMessage());
        }
    }
    //添加前缀树结点
    public void addKeyword(String keyword){
        //按字符检查 先检查有无根结点下的子节点
        TrieNode tempNode = rootNode;
        for (int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode==null){
                //生成新结点，为根结点的子节点
                subNode = new TrieNode();
                tempNode.setSubNode(c,subNode);
            }
            tempNode = subNode;
            if(i==keyword.length()-1){
                tempNode.setFinal(true);
            }

        }

    }
    //过滤敏感词函数
    public String filter(String text){
        //三个指针
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder builder = new StringBuilder();
        while(position<text.length()){
            //首先判断特殊符号 特殊符号跳过
            char c = text.charAt(position);
            if(isSymblo(c)){
                //是特殊符号 判断是否处于根结点
                if(tempNode==rootNode){
                    builder.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);

            //不是特殊符号
            if(tempNode==null){
                //不在前缀树中
                //以begin为开头的字符串不是敏感词
                builder.append(c);
                position=++begin;
                //重新指向根结点
                tempNode = rootNode;
            }else if(tempNode.isFinal()){
                //在前缀树中 且是叶子结点
                //是敏感词
                builder.append(REPLACEMENT);
                begin = ++position;
                //重新指向根结点
                tempNode = rootNode;
            }else{
                //在前缀树中 而且不是叶子结点
                position++;
            }
        }
        //循环结束 最后一个非敏感词字符串可能未被记录
        builder.append(text.substring(begin));
        return builder.toString();
    }
    public boolean isSymblo(char c){
        //isAsciiAlphanumeric 判断是不是特殊字符 是就返回false 不是就返回true 取反后 是特殊字符就返回true
        //0x9FFF～0x2E80东亚文字ascii码范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }

    //前缀树定义
    private class TrieNode{
        private boolean isFinal = false;
        private Map<Character,TrieNode> subNodes = new HashMap<>();
        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }
        public void setSubNode(char c,TrieNode node){
            subNodes.put(c,node);
        }
        public TrieNode getSubNode(char c){
            return subNodes.get(c);
        }

    }


}

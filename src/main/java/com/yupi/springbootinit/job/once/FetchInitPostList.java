package com.yupi.springbootinit.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  获取初始帖子列表
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Override
    public void run(String... args) {
        //请求地址
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        //请求参数
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
        String result = HttpRequest.post(url)
                .body(json)
                .execute().body();
        Map<String,Object> map = JSONUtil.toBean(result,Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<Post> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject recordTemplate = (JSONObject) record;
            Post post = new Post();
            post.setTitle(recordTemplate.getStr("title"));
            post.setContent(recordTemplate.getStr("content"));
            JSONArray tags = (JSONArray) recordTemplate.get("tags");
            List<String> tagList = JSONUtil.toList(tags, String.class);
            post.setTags(JSONUtil.toJsonStr(tagList));
            post.setUserId(1L);
            postList.add(post);
        }
        boolean res = postService.saveBatch(postList);
        if(res){
            log.info("获取帖子列表成功，条数：{}",postList.size());
        }else {
            log.error("获取帖子列表失败");
        }

    }
}
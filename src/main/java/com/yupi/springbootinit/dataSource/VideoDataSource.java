package com.yupi.springbootinit.dataSource;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.user.UserQueryRequest;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.model.vo.VideoVO;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class VideoDataSource implements DataSource<VideoVO> {
    @Override
    public Page<VideoVO> doSearch(String searchText, long pageNum, long pageSize) {
        //获取B站视频前，需要获取cookie
        String cookieUrl = "https://www.bilibili.com";
        HttpCookie cookie = HttpRequest.get(cookieUrl).execute().getCookie("buvid3");
//        System.out.println(cookie);
        String url2 = String.format("https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword=%s",searchText);
        String body = null;
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return HttpRequest.get(url2)
                        .cookie(cookie)
                        .execute().body();
            }
        };
        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
                //如果结果为空，则重试
                .retryIfResult(Predicates.<String>isNull())
                //发送IO异常重试
                .retryIfExceptionOfType(IOException.class)
                .retryIfRuntimeException()//发送运行时异常重试
                // 等待
                .withWaitStrategy(WaitStrategies.incrementingWait(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS))
                // 允许执行4次（首次执行 + 最多重试3次）
                .withStopStrategy(StopStrategies.stopAfterAttempt(4))
                .build();
        try {
            body = retryer.call(callable);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试失败");
        }
//        System.out.println(body);
        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map) map.get("data");
        JSONArray videos = (JSONArray) data.get("result");
        Page<VideoVO> page = new Page<>(pageNum,pageSize);
        List<VideoVO> videoVOList = new ArrayList<>();
        for (Object video : videos) {
            JSONObject videoObject = (JSONObject) video;
            VideoVO videoVO = new VideoVO();
            String titleTemp = videoObject.getStr("title");
            String title = titleTemp.replace("<em class=\"keyword\">", "").replace("</em>", "");
            videoVO.setTitle(title);
//            videoVO.setTitle(videoObject.getStr("title"));
            videoVO.setPic(videoObject.getStr("pic"));
            videoVO.setArcurl(videoObject.getStr("arcurl"));
            videoVO.setAuthor(videoObject.getStr("author"));
            videoVOList.add(videoVO);
        }
        page.setRecords(videoVOList);
        return page;
    }
}

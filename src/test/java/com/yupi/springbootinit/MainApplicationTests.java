package com.yupi.springbootinit;

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
import com.yupi.springbootinit.config.WxOpenConfig;
import javax.annotation.Resource;

import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.vo.VideoVO;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 主类测试
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;

    /*@Resource
    private Retryer<String> retryer;*/

    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }

    @Test
    void testGetPicture() throws IOException {
        int current = 1;
        String url = "https://cn.bing.com/images/search?q=小黑子&first=";
        Document doc = Jsoup.connect(url + current).get();
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            String m = element.select(".iusc").get(0).attr("m");
            Map<String,Object> map = JSONUtil.toBean(m,Map.class);
            String murl = (String) map.get("murl");
            String title = element.select(".inflnk").get(0).attr("aria-label");
            Picture picture = new Picture();
            picture.setUrl(murl);
            picture.setTitle(title);
            pictures.add(picture);
        }
        System.out.println(pictures);
    }

    @Test
    void testGetVideo(){
        //获取B站视频前，需要获取cookie
        String cookieUrl = "https://www.bilibili.com";
        HttpCookie cookie = HttpRequest.get(cookieUrl).execute().getCookie("buvid3");
        System.out.println(cookie);
        String url2 = "https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword="+"林高远";
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
                .retryIfResult(Predicates.<String>isNull())//如果结果为空，则重试
                .retryIfExceptionOfType(IOException.class)//发送IO异常重试
                .retryIfRuntimeException()//发送运行时异常重试
                .withWaitStrategy(WaitStrategies.incrementingWait(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS)) // 等待
                .withStopStrategy(StopStrategies.stopAfterAttempt(4))// 允许执行4次（首次执行 + 最多重试3次）
                .build();
        try {
            body = retryer.call(callable);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试失败");
        }

        System.out.println(body);
        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map) map.get("data");
        JSONArray videos = (JSONArray) data.get("result");
        Page<VideoVO> page = new Page<>(1,20);
        List<VideoVO> videoVOList = new ArrayList<>();
        for (Object video : videos) {
            JSONObject videoObject = (JSONObject) video;
            VideoVO videoVO = new VideoVO();
            String titleTemp = videoObject.getStr("title");
            String title = titleTemp.replace("<em class=\"keyword\">", "").replace("</em>", "");
            videoVO.setTitle(title);
            videoVO.setPic(videoObject.getStr("pic"));
            videoVO.setArcurl(videoObject.getStr("arcurl"));
            videoVO.setAuthor(videoObject.getStr("author"));
            videoVOList.add(videoVO);
        }
        page.setRecords(videoVOList);
    }

    @Test
    void testGetUser(){
        //获取B站视频前，需要获取cookie
        String cookieUrl = "https://www.bilibili.com";
        HttpCookie cookie = HttpRequest.get(cookieUrl).execute().getCookie("buvid3");
        System.out.println(cookie);
        String url2 = "https://api.bilibili.com/x/web-interface/search/type?search_type=bili_user&keyword="+"ikun";
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
                .retryIfResult(Predicates.<String>isNull())//如果结果为空，则重试
                .retryIfExceptionOfType(IOException.class)//发送IO异常重试
                .retryIfRuntimeException()//发送运行时异常重试
                .withWaitStrategy(WaitStrategies.incrementingWait(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS)) // 等待
                .withStopStrategy(StopStrategies.stopAfterAttempt(4))// 允许执行4次（首次执行 + 最多重试3次）
                .build();
        try {
            body = retryer.call(callable);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试失败");
        }

        System.out.println(body);
        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map) map.get("data");
    }
}

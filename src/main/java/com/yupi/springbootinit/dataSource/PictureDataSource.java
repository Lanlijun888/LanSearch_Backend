package com.yupi.springbootinit.dataSource;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicates;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Editor
 */
@Service
public class PictureDataSource implements DataSource<Picture> {

    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        long current = (pageNum - 1) * pageSize;
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", searchText, current);
        Document doc = null;
        Callable<Document> callable = new Callable<Document>() {
            @Override
            public Document call() throws Exception {
                return Jsoup.connect(url).get();
            }
        };
        Retryer<Document> retryer = RetryerBuilder.<Document>newBuilder()
                //如果结果为空，则重试
                .retryIfResult(Predicates.<Document>isNull())
                //发送IO异常重试
                .retryIfExceptionOfType(IOException.class)
                .retryIfRuntimeException()//发送运行时异常重试
                // 等待
                .withWaitStrategy(WaitStrategies.incrementingWait(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS))
                // 允许执行4次（首次执行 + 最多重试3次）
                .withStopStrategy(StopStrategies.stopAfterAttempt(4))
                .build();
        try {
            doc = retryer.call(callable);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试失败");
        }
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址（murl）
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            // 取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
            if (pictures.size() >= pageSize) {
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}

package com.yupi.springbootinit.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.dataSource.DataSource;
import com.yupi.springbootinit.dataSource.DataSourceRegistry;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.dto.search.SearchRequest;
import com.yupi.springbootinit.model.dto.user.UserQueryRequest;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.enums.SearchTypeEnum;
import com.yupi.springbootinit.model.vo.PostVO;
import com.yupi.springbootinit.model.vo.SearchVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.PictureService;
import com.yupi.springbootinit.service.PostService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * @author Editor
 */
@Component
@Slf4j
public class SearchFacade {
    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchVO searchAll(@RequestBody  SearchRequest searchRequest,
                                            HttpServletRequest request) {
        String type = searchRequest.getType();
        SearchTypeEnum enumByValue = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        SearchVO searchVO = new SearchVO();
        String searchText = searchRequest.getSearchText();
        long pageNum = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();
        if(enumByValue == null){
            //查询所有数据
            //查询用户线程
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(()->{
                Page<UserVO> userVOPage = dataSourceRegistry.getDataSourceByType("user").doSearch(searchText,pageNum,pageSize);
                return userVOPage;
            });

            //查询帖子线程
            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(()->{
                Page<PostVO> postVOPage = dataSourceRegistry.getDataSourceByType("post").doSearch(searchText,pageNum,pageSize);
                return postVOPage;
            });

            //查询图片线程
            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(()->{
                Page<Picture> picturePage = dataSourceRegistry.getDataSourceByType("picture").doSearch(searchText,1,20);
                return picturePage;
            });

            //查询图片线程
            CompletableFuture<Page<Picture>> videoTask = CompletableFuture.supplyAsync(()->{
                Page<Picture> picturePage = dataSourceRegistry.getDataSourceByType("video").doSearch(searchText,1,20);
                return picturePage;
            });

            CompletableFuture.allOf(userTask,postTask,pictureTask).join();
            try {
                Page<UserVO> userVOPage = userTask.get();
                Page<PostVO> postVOPage = postTask.get();
                Page<Picture> picturePage = pictureTask.get();
                searchVO.setUserVOList(userVOPage.getRecords());
                searchVO.setPostVOList(postVOPage.getRecords());
                searchVO.setPictureList(picturePage.getRecords());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统内部异常，请稍后再试");
            }
            return searchVO;
        }else {
            DataSource dataSource = dataSourceRegistry.getDataSourceByType(type);
            Page page = dataSource.doSearch(searchText, pageNum, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }
    }
}

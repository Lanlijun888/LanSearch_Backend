package com.yupi.springbootinit.dataSource;

import com.yupi.springbootinit.model.enums.SearchTypeEnum;
import com.yupi.springbootinit.service.PictureService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Editor
 *
 */
@Component
public class DataSourceRegistry {
    @Resource
    private PostDataSource postDataSource;
    @Resource
    private UserDataSource userDataSource;
    @Resource
    private PictureDataSource pictureDataSource;
    @Resource
    private VideoDataSource videoDataSource;

    private Map<String, DataSource<T>> typeDataSource;

    @PostConstruct
    public void doInit(){
        typeDataSource = new HashMap(){{
           put(SearchTypeEnum.POST.getValue(),postDataSource);
           put(SearchTypeEnum.USER.getValue(),userDataSource);
           put(SearchTypeEnum.PICTURE.getValue(),pictureDataSource);
           put(SearchTypeEnum.VIDEO.getValue(),videoDataSource);
        }};
    }

    public DataSource getDataSourceByType(String type){
        if(typeDataSource == null){
            return null;
        }
        return typeDataSource.get(type);
    }
}

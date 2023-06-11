package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * B站视频视图（脱敏）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class VideoVO implements Serializable {

    /**
     * 视频名称
     */
    private String title;
    /**
     * 视频地址
     */
    private String arcurl;

    /**
     * 视频封面
     */
    private String pic;
    /**
     * 视频作者
     */
    private String author;

    private static final long serialVersionUID = 1L;
}
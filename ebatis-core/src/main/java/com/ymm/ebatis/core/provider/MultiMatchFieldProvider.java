package com.ymm.ebatis.core.provider;

/**
 * @author 章多亮
 * @since 2020/1/17 9:17
 */
public interface MultiMatchFieldProvider extends Provider {
    /**
     * 获取待匹配的字段列表
     *
     * @return 字段列表
     */
    String[] getFields();
}

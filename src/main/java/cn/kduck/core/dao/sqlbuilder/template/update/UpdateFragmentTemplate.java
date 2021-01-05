package cn.kduck.core.dao.sqlbuilder.template.update;

import cn.kduck.core.dao.sqlbuilder.template.FragmentTemplate;

/**
 * 更新语句拼装模版，用于更新语句个性化调整参数。模版生成的SQL不受参数paramMap的影响，无论是否在paramMap中包含参数值，都会拼装模版生成的
 * SQL片段，而且会优先使用模版生成的SQL。
 * @author LiuHG
 */
public interface UpdateFragmentTemplate extends FragmentTemplate {

    /**
     * 需要定制化SQL的属性名
     * @return 字段属性名
     */
    String getAttrName();
}

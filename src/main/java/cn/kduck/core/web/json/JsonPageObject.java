package cn.kduck.core.web.json;

import cn.kduck.core.service.Page;

import java.util.List;

/**
 * 用于返回分页数据及结果集的Json对象，该对象在包含{@link JsonObject JsonObject}相同属性外，还提供了用于分页的数据信息：
 * <pre>
 *    pageSize: 每页显示的条数，由内部{@link Page Page}对象的pageSize属性决定，默认每页显示15条数据<br>
 *    currentPage: 当前数据所在页码，从1开始<br>
 *    maxPage: 最大页数<br>
 * <pre/>
 * @author LiuHG
 * @see JsonObject
 * @see Page
 */
public class JsonPageObject extends JsonObject{

    private final Page page;

    public JsonPageObject(Page page){
        this.page = page;
    }

    public JsonPageObject(Page page, List data){
        this.page = page;
        super.setData(data);
    }

    public int getPageSize() {
        return page.getPageSize();
    }

    public int getCurrentPage() {
        return page.getCurrentPage();
    }

    public long getCount() {
        return page.getCount();
    }

    public int getMaxPage() {
        return page.getMaxPage();
    }

}

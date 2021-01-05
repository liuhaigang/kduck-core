package cn.kduck.core.service;

/**
 * 页码对象，包含分页所需的所有信息，并内部提供的{@link PageUtils}工具类用于分页处理，默认为每页显示15条数据。
 * 为了安全考虑，框架默认配置了分页安全保护，每次分页查询最多不允许超过500条数据
 * @author LiuHG
 */
public class Page {

    public static final int DEFAULT_PAGE_SIZE = 15;

    private int pageSize = DEFAULT_PAGE_SIZE;
    private int currentPage = 1;
    private long count;
    private int maxPage;
    private int minPage = 1;
    private int firstResult = 0;

    private boolean recount;

    public Page(){
        recount = true;
    }

    public Page(boolean recount){
        this.recount = recount;
    }

    public Page(int currentPage){
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public int getMinPage() {
        return minPage;
    }

    public void calculate(long count) {
        PageUtils.calculate(this, count);
    }

    public boolean isRecount() {
        return recount;
    }

    /**
     * 分页工具类，根据记录总数设置{@link Page Page}中的页码数据，也可用于自定义分页逻辑后分页使用。
     *
     * @author LiuHG
     */
    public static class PageUtils {

        private PageUtils() {}

        public static void calculate(Page query, long count) {
            query.setCount(count);
            int pageSize = query.getPageSize();
            int minPage = query.getMinPage();
            int maxPage = (int) Math.max((count + pageSize - 1) / pageSize, 1);
            query.setMaxPage(maxPage);
            int currentPage = query.getCurrentPage();
            if (currentPage > maxPage) {
                currentPage = maxPage;
            } else if (currentPage < minPage) {
                currentPage = minPage;
            }
            query.setFirstResult((currentPage - 1) * pageSize);
        }

    }

}

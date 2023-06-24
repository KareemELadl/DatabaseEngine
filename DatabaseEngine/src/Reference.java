public class Reference {
    private int pageNum;
    private Tuple tuple;

    public Reference(int pageNum, Tuple tuple) {
        this.pageNum = pageNum;
        this.tuple = tuple;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Tuple getTuple() {
        return tuple;
    }

}

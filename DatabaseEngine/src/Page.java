import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Page implements Serializable {
    private Vector<Tuple> tuples;
    private int pageNumber;

    public void insertTuple(Tuple tuple) {
        tuples.add(tuple);
    }


    public Page(int pageNumber) {
        this.pageNumber = pageNumber;
        tuples = new Vector<>();
    }


    public int getPageNumber() {
        return pageNumber;
    }

    public Vector<Tuple> getTuples() {
        return tuples;
    }


}
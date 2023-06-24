import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

public class Tuple implements Serializable {
    private Hashtable<String,Object> data;
    private String tableName;

    public Tuple(Hashtable<String,Object> data , String tableName) {
        this.data = data;
        this.tableName = tableName;
    }

    public Hashtable<String,Object> getData() {
        return data;
    }

    public Object getValue(String key) {
        return data.get(key);
    }

    public void setValue(String column, Object value) {
        data.replace(column , value);
    }

    public int compareTo(Object o) throws DBAppException {
        String pk = DBApp.getPK(this.tableName);
        Object pkVal = data.get(pk);
        Tuple t = (Tuple) o;
        if (pkVal instanceof String)
            return ((String) pkVal).compareToIgnoreCase((String) t.getData().get(pk));
        else if (pkVal instanceof Integer){
            return (Integer) pkVal - (Integer) t.getData().get(pk);
        }
        else if (pkVal instanceof Double)
            return ((Double) pkVal).compareTo((Double) t.getData().get(pk));
        else
            return ((Date) pkVal).compareTo((Date) t.getData().get(pk));
    }
}
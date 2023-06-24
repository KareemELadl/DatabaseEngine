import java.io.Serializable;
import java.util.ArrayList;

public class OctreeReference implements Serializable {
    private String reference;
    private ArrayList<String> duplicates;

    public OctreeReference(String reference) {
        this.reference = reference;
        duplicates = new ArrayList<>();
    }

    public String getReference() {
        return reference;
    }

    public ArrayList<String> getDuplicates() {
        return duplicates;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

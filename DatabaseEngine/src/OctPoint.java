import java.io.Serializable;

public class OctPoint implements Serializable {

    private Object x;
    private Object y;
    private Object z;

    private boolean nullify = false;
    private OctreeReference ref;

    public OctPoint(Object x, Object y, Object z, OctreeReference ref){
        this.x = x;
        this.y = y;
        this.z = z;
        this.ref = ref;
    }

    public OctreeReference getRef() {
        return ref;
    }

    public OctPoint(Object x, Object y, Object z){
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public OctPoint(){
        nullify = true;
    }

    public Object getX(){
        return x;
    }

    public Object getY(){
        return y;
    }

    public Object getZ(){
        return z;
    }

    public boolean isNullified(){
        return nullify;
    }

    public String toString(){
        String write = "Point x: " + this.getX() + " Point y: " + this.getY() + " Point Z: " + this.getZ() + " Point Main Reference: " + this.getRef().getReference();
        for (int i = 0 ; i < this.getRef().getDuplicates().size() ; i++){
            write += "\nDuplicate Refs " + this.getRef().getDuplicates().get(i);
        }
        return write;
    }
}
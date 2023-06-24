import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;

public class Octree implements Serializable {
    static int maxEntriesInOctree = 256;
    private ArrayList<OctPoint> points;

    private OctPoint topLeftFront, bottomRightBack;

    private Octree[] children = new Octree[8];


    public Octree() {
        points = new ArrayList<>();
    }

//    public Octree(Object x, Object y, Object z) {
//        points = new ArrayList<>();
//        points.add(new OctPoint(x, y, z));
//        this.object = object;
//    }

    public Octree(Object x1, Object y1, Object z1, Object x2, Object y2, Object z2) throws OutOfBoundsException {
        if (compareObjects(x2, x1) < 0 || compareObjects(y2, y1) < 0 || compareObjects(z2, z1) < 0) {
            throw new OutOfBoundsException("The bounds are not properly set!");
        }

//        points = new ArrayList<>();
        topLeftFront = new OctPoint(x1, y1, z1);
        bottomRightBack = new OctPoint(x2, y2, z2);

        for (int i = 0; i <= 7; i++) {
            children[i] = new Octree();
        }
    }

    public void insert(Object x, Object y, Object z, OctreeReference object) throws DBAppException {
        if (find(x, y, z)) {
            OctPoint point = get(x, y, z);
            point.getRef().getDuplicates().add(object.getReference());
            if (object.getDuplicates().size() != 0) {
                for (int i = 0; i < object.getDuplicates().size(); i++) {
                    point.getRef().getDuplicates().add(object.getDuplicates().get(i));
                }
            }
            return;
        }

        if (!isValidPoint(x, y, z)) {
            throw new OutOfBoundsException("Insertion point is out of bounds! X: " + x + " Y: " + y + " Z: " + z + " Object Name: " + object.getClass().getName());
        }

        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

        int pos = getPosition(x, y, z, midx, midy, midz);

        if (children[pos].points == null) { // edit
//            children[pos] = new Octree<>(x, y, z, object);
            children[pos].insert(x, y, z, object);
        } else if (children[pos].points.size() < maxEntriesInOctree) {
            children[pos].points.add(new OctPoint(x, y, z, object));
        } else {

            ArrayList<Object> Xs = new ArrayList<>();
            ArrayList<Object> Ys = new ArrayList<>();
            ArrayList<Object> Zs = new ArrayList<>();
            ArrayList<OctreeReference> objects = new ArrayList<>();
            for (int i = 0; i < children[pos].points.size(); i++) {
                OctPoint t = children[pos].points.get(i);
                Xs.add(t.getX());
                Ys.add(t.getY());
                Zs.add(t.getZ());
                objects.add(t.getRef());
            }
//            Object x_ = children[pos].point.getX();
//            Object y_ = children[pos].point.getY();
//            Object z_ = children[pos].point.getZ();
//            T object_ = children[pos].object;
            children[pos].points = null;
            children[pos] = null;
            if (pos == OctLocations.TopLeftFront.getNumber()) {
                children[pos] = new Octree(topLeftFront.getX(), topLeftFront.getY(), topLeftFront.getZ(), midx, midy, midz);
            } else if (pos == OctLocations.TopRightFront.getNumber()) {
                children[pos] = new Octree(midx, topLeftFront.getY(), topLeftFront.getZ(), bottomRightBack.getX(), midy, midz);
            } else if (pos == OctLocations.BottomRightFront.getNumber()) {
                children[pos] = new Octree(midx, midy, topLeftFront.getZ(), bottomRightBack.getX(), bottomRightBack.getY(), midz);
            } else if (pos == OctLocations.BottomLeftFront.getNumber()) {
                children[pos] = new Octree(topLeftFront.getX(), midy, topLeftFront.getZ(), midx, bottomRightBack.getY(), midz);
            } else if (pos == OctLocations.TopLeftBack.getNumber()) {
                children[pos] = new Octree(topLeftFront.getX(), topLeftFront.getY(), midz, midx, midy, bottomRightBack.getZ());
            } else if (pos == OctLocations.TopRightBack.getNumber()) {
                children[pos] = new Octree(midx, topLeftFront.getY(), midz, bottomRightBack.getX(), midy, bottomRightBack.getZ());
            } else if (pos == OctLocations.BottomRightBack.getNumber()) {
                children[pos] = new Octree(midx, midy, midz, bottomRightBack.getX(), bottomRightBack.getY(), bottomRightBack.getZ());
            } else if (pos == OctLocations.BottomLeftBack.getNumber()) {
                children[pos] = new Octree(topLeftFront.getX(), midy, midz, midx, bottomRightBack.getY(), bottomRightBack.getZ());
            }
            for (int i = 0; i < Xs.size(); i++) {
                Object x_ = Xs.get(i);
                Object y_ = Ys.get(i);
                Object z_ = Zs.get(i);
                OctreeReference ref = objects.get(i);
                children[pos].insert(x_, y_, z_, ref);
            }
            children[pos].insert(x, y, z, object);
        }
    }

    public boolean find(Object x, Object y, Object z) {
        if (!isValidPoint(x, y, z)) return false;
        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

        int pos = getPosition(x, y, z, midx, midy, midz);

        if (children[pos].points == null)
            return children[pos].find(x, y, z);
        if (children[pos].points.size() == 0)
            return false;
        for (int i = 0; i < children[pos].points.size(); i++) {
            OctPoint point = children[pos].points.get(i);
            if (compareObjects(x, point.getX()) == 0
                    && compareObjects(y, point.getY()) == 0
                    && compareObjects(z, point.getZ()) == 0) {
                return true;
            }

        }
        return false;
    }

    public OctPoint get(Object x, Object y, Object z) {
        if (!isValidPoint(x, y, z)) return null;
        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

        int pos = getPosition(x, y, z, midx, midy, midz);

        if (children[pos].points == null)
            return children[pos].get(x, y, z);
        if (children[pos].points.size() == 0)
            return null;
        for (int i = 0; i < children[pos].points.size(); i++) {
            OctPoint point = children[pos].points.get(i);
            if (compareObjects(x, point.getX()) == 0
                    && compareObjects(y, point.getY()) == 0
                    && compareObjects(z, point.getZ()) == 0) {
                return children[pos].points.get(i);
            }
        }

        return null;
    }

    public boolean helperRange(Object x, Object minX, Object maxX, String operator) {
        int compareXMinx = compareObjects(x, minX);
        int compareXMaxx = compareObjects(x, maxX);


        boolean con1;

        if (operator.equals("<"))
            con1 = compareXMinx > 0;
        else if (operator.equals("<="))
            con1 = compareXMinx >= 0;
        else if (operator.equals(">"))
            con1 = compareXMaxx < 0;
        else if (operator.equals(">="))
            con1 = compareXMaxx <= 0;
        else if (operator.equals("!="))
            con1 = true;
        else  // =
            con1 = compareXMinx >= 0 && compareXMaxx <= 0;
        return con1;
    }

    public boolean helperRangeLoop(Object x, Object Comx, String operator) {
        boolean cond1;

        if (operator.equals("<"))
            cond1 = compareObjects(Comx, x) < 0;
        else if (operator.equals("<="))
            cond1 = compareObjects(Comx, x) <= 0;
        else if (operator.equals(">"))
            cond1 = compareObjects(Comx, x) > 0;
        else if (operator.equals(">="))
            cond1 = compareObjects(Comx, x) >= 0;
        else if (operator.equals("!="))
            cond1 = compareObjects(Comx, x) != 0;
        else
            cond1 = compareObjects(Comx, x) == 0;
        return cond1;
    }

    public ArrayList<OctPoint> getRange(Object x, Object y, Object z, String[] operations) {
        ArrayList res = new ArrayList<>();
        Object minX = topLeftFront.getX();
        Object minY = topLeftFront.getY();
        Object minZ = topLeftFront.getZ();
        Object maxX = bottomRightBack.getX();
        Object maxY = bottomRightBack.getY();
        Object maxZ = bottomRightBack.getZ();

        boolean con1 = helperRange(x, minX, maxX, operations[0]);
        boolean con2 = helperRange(y, minY, maxY, operations[1]);
        boolean con3 = helperRange(z, minZ, maxZ, operations[2]);

        if (con1 && con2 && con3) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].points == null) {
                    ArrayList<OctPoint> temp = children[i].getRange(x, y, z, operations);
                    res.addAll(temp);
                } else {
                    for (int j = 0; j < children[i].points.size(); j++) {
                        OctPoint op = children[i].points.get(j);
                        boolean cond1 = helperRangeLoop(x, op.getX(), operations[0]);
                        boolean cond2 = helperRangeLoop(y, op.getY(), operations[1]);
                        boolean cond3 = helperRangeLoop(z, op.getZ(), operations[2]);
                        if (cond1 && cond2 && cond3)
                            res.add(op);
                    }
                }
            }
        }
        return res;

    }

    public boolean remove(Object x, Object y, Object z) {
        if (!isValidPoint(x, y, z)) return false;
        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

        int pos = getPosition(x, y, z, midx, midy, midz);

        if (children[pos].points == null)
            return children[pos].remove(x, y, z);
        if (children[pos].points.size() == 0)
            return false;
        for (int i = 0; i < children[pos].points.size(); i++) {
            OctPoint point = children[pos].points.get(i);
            if (compareObjects(x, point.getX()) == 0
                    && compareObjects(y, point.getY()) == 0
                    && compareObjects(z, point.getZ()) == 0) {
                // compare reference
                children[pos].points.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean isValidPoint(Object x, Object y, Object z) {
        if (compareObjects(x, topLeftFront.getX()) < 0 || compareObjects(x, bottomRightBack.getX()) > 0
                || compareObjects(y, topLeftFront.getY()) < 0 || compareObjects(y, bottomRightBack.getY()) > 0
                || compareObjects(z, topLeftFront.getZ()) < 0 || compareObjects(z, bottomRightBack.getZ()) > 0) {
            System.out.println(x + "  " + topLeftFront.getX());
            System.out.println(y + "  " + topLeftFront.getY());
            System.out.println(z + "  " + topLeftFront.getZ());
            System.out.println(x + "  " + bottomRightBack.getX());
            System.out.println(y + "  " + bottomRightBack.getY());
            System.out.println(z + "  " + bottomRightBack.getZ());
            return false;
        }
        return true;
    }

    public int compareObjects(Object x, Object y) {
            if (x instanceof String) {
//                System.out.println(x);
//                System.out.println(y);
                return ((String) x).compareToIgnoreCase((String) y);
            }
            else if (x instanceof Integer) {
                return (Integer) x - (Integer) y;
            } else if (x instanceof Double)
                return ((Double) x).compareTo(Double.parseDouble(y + ""));
            else
                return ((Date) x).compareTo((Date) y);
    }

    public Object getMidPoint(Object x, Object y) {
        if (x instanceof String)
            return getMiddle((String) x , (String) y); // edit when mid point of strings available
        else if (x instanceof Integer) {
            Integer first = (Integer) x;
            Integer second = (Integer) y;
            return (first + second) / 2;
        } else if (x instanceof Double) {
            Double first = (Double) x;
            Double second = (Double) y;
            return (first + second) / 2;
        } else
            return getMidPointsofDates((Date) x, (Date) y);
    }

    public Date getMidPointsofDates(Date x, Date y) {
        long ms = (x.getTime() + y.getTime()) / 2;
        Date midPoint = new Date(ms);
//                System.out.println("Midpoint: " + result);
        return midPoint;


    }

    public int getPosition(Object x, Object y, Object z, Object midx, Object midy, Object midz) {
        int pos = -1;
        if (compareObjects(x, midx) <= 0) {
            if (compareObjects(y, midy) <= 0) {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.TopLeftFront.getNumber();
                else
                    pos = OctLocations.TopLeftBack.getNumber();
            } else {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.BottomLeftFront.getNumber();
                else
                    pos = OctLocations.BottomLeftBack.getNumber();
            }
        } else {
            if (compareObjects(y, midy) <= 0) {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBack.getNumber();
            } else {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber();
            }
        }
        return pos;
    }

    public static void main(String[] args) throws DBAppException {
//        OctreeReference ref = new OctreeReference("AA");
//        OctreeReference ref2 = new OctreeReference("BB");
        Octree tree = new Octree(0, 0, 0, 16, 16, 16);
        int count = 0;
        OctreeReference ref = new OctreeReference("aa");
        tree.insert(1, 4, 4, new OctreeReference("aa"));
        tree.insert(1, 4, 4, new OctreeReference("aa"));
        tree.insert(1, 4, 4, new OctreeReference("aa"));
        tree.insert(1, 6, 6, new OctreeReference("aa"));
        //tree.insert("f", 8, 8, "a");
        tree.insert(1, 9, 9, new OctreeReference("aa"));
        tree.insert(1, 10, 10, new OctreeReference("aa"));
        tree.insert(1, 11, 11, new OctreeReference("aa"));
        tree.insert(1, 12, 12, new OctreeReference("aa"));
        tree.insert(1, 13, 13, new OctreeReference("aa"));
        tree.insert(1, 14, 14, new OctreeReference("aa"));
        ArrayList<OctPoint> result = tree.getRange(0,6,6,new String[]{">",">",">"});
        for(int i =0 ;i<result.size();i++){
            System.out.println(result.get(i)+" ");
        }
    }

    public OctPoint getX(Object x) {
        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
//        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
//        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

//        int pos = getPosition(x, y, z, midx, midy, midz);

        ArrayList<Integer> positions = new ArrayList<>();
        if (compareObjects(x, midx) > 0) {
            /*                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBack.getNumber();
            } else {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber(); */
            positions.add(OctLocations.TopRightFront.getNumber());
            positions.add(OctLocations.TopRightBack.getNumber());
            positions.add(OctLocations.BottomRightFront.getNumber());
            positions.add(OctLocations.BottomRightBack.getNumber());

        } else {
            positions.add(OctLocations.TopLeftFront.getNumber());
            positions.add(OctLocations.TopLeftBack.getNumber());
            positions.add(OctLocations.BottomLeftFront.getNumber());
            positions.add(OctLocations.BottomLeftBack.getNumber());
        }
        for (int i = 0; i < positions.size(); i++) {
            int pos = positions.get(i);
            if (children[pos].points == null) {
                OctPoint point = children[pos].getX(x);
                if (point == null)
                    continue;
                else
                    return point;
            }
            if (children[pos].points.size() == 0)
                continue;
            for (int j = 0; j < children[pos].points.size(); j++) {
                OctPoint point = children[pos].points.get(j);
                if (compareObjects(x, point.getX()) == 0)
                {
                    return point;
                }
            }
        }

        return null;
    }
    public OctPoint getY(Object y) {
//        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
//        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

//        int pos = getPosition(x, y, z, midx, midy, midz);

        ArrayList<Integer> positions = new ArrayList<>();
        if (compareObjects(y, midy) > 0) {
            /*                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBack.getNumber();
            } else {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber(); */
            positions.add(OctLocations.BottomRightFront.getNumber());
            positions.add(OctLocations.BottomRightBack.getNumber());
            positions.add(OctLocations.BottomLeftFront.getNumber());
            positions.add(OctLocations.BottomLeftBack.getNumber());

        } else {
            positions.add(OctLocations.TopRightFront.getNumber());
            positions.add(OctLocations.TopRightBack.getNumber());
            positions.add(OctLocations.TopLeftFront.getNumber());
            positions.add(OctLocations.TopLeftBack.getNumber());
        }
        for (int i = 0; i < positions.size(); i++) {
            int pos = positions.get(i);
            if (children[pos].points == null) {
                OctPoint point = children[pos].getY(y);
                if (point == null)
                    continue;
                else
                    return point;
            }
            if (children[pos].points.size() == 0)
                continue;
            for (int j = 0; j < children[pos].points.size(); j++) {
                OctPoint point = children[pos].points.get(j);
                if (compareObjects(y, point.getY()) == 0)
                {
                    return point;
                }
            }
        }

        return null;
    }

    public OctPoint getZ(Object z) {
//        Object midx = getMidPoint(topLeftFront.getX(), bottomRightBack.getX());
//        Object midy = getMidPoint(topLeftFront.getY(), bottomRightBack.getY());
        Object midz = getMidPoint(topLeftFront.getZ(), bottomRightBack.getZ());

//        int pos = getPosition(x, y, z, midx, midy, midz);

        ArrayList<Integer> positions = new ArrayList<>();
        if (compareObjects(z, midz) > 0) {
            /*                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.TopRightFront.getNumber();
                else
                    pos = OctLocations.TopRightBack.getNumber();
            } else {
                if (compareObjects(z, midz) <= 0)
                    pos = OctLocations.BottomRightFront.getNumber();
                else
                    pos = OctLocations.BottomRightBack.getNumber(); */
            positions.add(OctLocations.TopRightBack.getNumber());
            positions.add(OctLocations.BottomRightBack.getNumber());
            positions.add(OctLocations.TopLeftBack.getNumber());
            positions.add(OctLocations.BottomLeftBack.getNumber());

        } else {
            positions.add(OctLocations.TopRightFront.getNumber());
            positions.add(OctLocations.BottomRightFront.getNumber());
            positions.add(OctLocations.TopLeftFront.getNumber());
            positions.add(OctLocations.BottomLeftFront.getNumber());
        }
        for (int i = 0; i < positions.size(); i++) {
            int pos = positions.get(i);
            if (children[pos].points == null) {
                OctPoint point = children[pos].getZ(z);
                if (point == null)
                    continue;
                else
                    return point;
            }
            if (children[pos].points.size() == 0)
                continue;
            for (int j = 0; j < children[pos].points.size(); j++) {
                OctPoint point = children[pos].points.get(j);
                if (compareObjects(z, point.getZ()) == 0) {
                    return point;
                }
            }
        }

        return null;
    }

    public void removeRef(Object x, Object y, Object z, String s) {
        OctPoint o = get(x, y, z);
        if (o == null) {
            System.out.println("REFERENCE DOESNT EXIST");
            return;
        }
        OctreeReference ref = o.getRef();
        String mainRef = ref.getReference();
        ArrayList<String> dups = ref.getDuplicates();
        if (mainRef.equals(s)) {
            if (dups.size() == 0) {
                remove(x, y, z);
                System.out.println("NODE DELETED FROM OCTREE SUCCESSFULLY");
                return;
            }
            ref.setReference(dups.get(0));
            dups.remove(0);
            System.out.println("NODE DELETED FROM OCTREE SUCCESSFULLY");
            return;
        }
        for (int i = 0; i < dups.size(); i++)
            if (dups.get(i).equals(s)) {
                dups.remove(i);
                System.out.println("NODE DELETED FROM OCTREE SUCCESSFULLY");
                return;
            }

    }
    public static String getMiddle(String first,String second) {
        int largerLength;
        int smallerLength;
        if (first.length() < second.length()){
            largerLength = second.length();
            smallerLength = first.length();
        }
        else {
            largerLength = first.length();
            smallerLength = second.length();
        }
        String result="";
        int mid=0;
        for(int i=0;i<smallerLength;i++){
            mid=( (int) (first.charAt(i)+second.charAt(i)) )/2;
            result+=(char)mid;
        }
        int size = result.length();
        for (int i = size ; i < largerLength ; i++){
            if (first.length() > second.length())
                result += first.charAt(i);
            else
                result += second.charAt(i);
        }
        return result;
    }
    public static void printOctree(Octree t){
        for (int i = 0 ; i < t.children.length ; i++){
            if (t.children[i].points == null){
                printHelper(t.children[i], "Children Of Octant " + i);
            }
            else {
                OctPoint min = t.children[i].topLeftFront;
                OctPoint max = t.children[i].bottomRightBack;
//                System.out.println("Octant : " + i);
//                System.out.println("Min X " + min.getX() + "Min y " + min.getY() + ", Min z " + min.getZ());
//                System.out.println("Max X " + max.getX() + "Max y " + max.getY() + ", Max z " + max.getZ());
                System.out.println("Children Of Octant " + i);
                for (int j = 0 ; j < t.children[i].points.size() ; j++){
                    System.out.print(t.children[i].points.get(j) + " ");
                }
                System.out.println();

            }
        }
    }
    public static void printHelper(Octree t , String level){
        for (int i = 0 ; i < t.children.length ; i++){
            if (t.children[i].points == null){
                printHelper(t.children[i], "Children Of Octant " + i + " Of " + level);
            }
            else {
//                System.out.println(t.children[i]);
                OctPoint min = t.children[i].topLeftFront;
                OctPoint max = t.children[i].bottomRightBack;
//                System.out.println("Octant : " + i);
//                System.out.println("Min X " + min.getX() + "Min y " + min.getY() + ", Min z " + min.getZ());
//                System.out.println("Max X " + max.getX() + "Max y " + max.getY() + ", Max z " + max.getZ());
                System.out.println(level);
                // first_name = "idrkec, last_name = "fcgcde", gpa : 0.85
                for (int j = 0 ; j < t.children[i].points.size() ; j++){
                    System.out.println(t.children[i].points.get(j) + " ");
                }
                System.out.println();

            }
        }
    }
}
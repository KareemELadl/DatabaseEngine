import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Table implements Serializable {
    static int PAGE_SIZE;
    String name;
//    String pk;
    //private int pageCount;
    Vector<Integer> pageNums;
    public Table(String name) {
        //pages = new Vector<>();
        this.name = name;
        this.pageNums = new Vector<>();
    }

    public void insertRec(Tuple t) throws DBAppException {
            if (this.pageNums.size() == 0) {
                createPage();
                Page p = getPageByNumber(this.pageNums.get(0));
                p.getTuples().add(t);
                savePage(p);
                insertInIndex(t,this.name,p.getPageNumber());
                return;
            }
            Page p = getPageByTuple(t);
            // if condition if p = null <<<<
            if (p == null) {
                Page page = getPageByNumber(this.pageNums.get(this.pageNums.size() - 1));
                if (page.getTuples().size() < Table.PAGE_SIZE) {
                    page.getTuples().add(t);
                    insertInIndex(t,this.name,page.getPageNumber());
                    savePage(page);
                    return;
                }
                createPage();
//                Page pg = getPageByNumber(this.pageNums.size() - 1);
                Page pg = getPageByNumber(this.pageNums.get(this.pageNums.size() - 1));
                pg.getTuples().add(t);
                insertInIndex(t,this.name,pg.getPageNumber());
                savePage(pg);
                return;
            }
            Tuple first = p.getTuples().get(0);
            if (t.compareTo(first) < 0){
                for (int i = 0 ; i < pageNums.size() ; i++){
                    int pgNum = pageNums.get(i);
                    if (pgNum == p.getPageNumber() && i!=1 && i!=0){
                        Page prevPage = getPageByNumber(i-1);
                        if (prevPage.getTuples().size() < Table.PAGE_SIZE){
                            prevPage.getTuples().add(t);
                            savePage(prevPage);
                            insertInIndex(t,this.name,prevPage.getPageNumber());
                            return;
                        }
                    }
                }
            }
            this.insertRec(t, p);
    }
    public void createPage() throws DBAppException{
        if (pageNums.size() == 0){
            Page p = new Page(1);
            this.pageNums.add(1);
            savePage(p);
            return;
        }
        Page p = new Page(pageNums.get(pageNums.size() - 1)+1);
        this.pageNums.add(p.getPageNumber());
        //pageCount++;
        // serialize page
        savePage(p);
    }
    public void savePage(Page p) throws DBAppException{
        try {
            // Serialize the object to a file
            FileOutputStream fileOut = new FileOutputStream("Pages/" + this.name + "" + p.getPageNumber() + ".class");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(p);
            out.close();
            fileOut.close();
            p = null;
            System.gc();
        } catch(IOException e) {
            throw new DBAppException(e);
        }
    }
    public Page getPageByNumber(int pageNumber) throws DBAppException {
        try {
            Page p = null;
            System.out.println("Pages/" + this.name + "" + pageNumber + ".class");
            FileInputStream fileIn = new FileInputStream("Pages/" + this.name + "" + pageNumber + ".class");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            p = (Page) in.readObject();
            in.close();
            fileIn.close();
            return p;
        }
        catch (Exception e){
            throw new DBAppException(e);
        }
    }
    public Page getPageByTuple(Tuple t) throws DBAppException {
        for (int i = 0 ; i < this.pageNums.size() ; i++){
            int pgNum = pageNums.get(i);
            Page p = getPageByNumber(pgNum);
            if (p.getTuples().get(p.getTuples().size() - 1).compareTo(t) >= 0) {
//                if (p.getTuples().get(0).compareTo(t) > 0 && i != 0){
//                    Page prev = getPageByNumber(pageNums.get(i-1));
//                    if (prev.getTuples().size() < PAGE_SIZE)
//                        return prev;
//                }
                return p;
            }
        }
        return null; // bigger than largest record in last page
    }

    public void insertRec(Tuple t , Page p) throws DBAppException {
        Vector<Tuple> tuples = p.getTuples();
        if (tuples.isEmpty()){
            tuples.add(t);
            insertInIndex(t,this.name,p.getPageNumber());
            savePage(p);
            return;
        }
        int low = 0 , high = tuples.size() - 1 , mid = (low + high) / 2;
        while (low <= high){
            mid = (low + high) / 2;
            Tuple tempTup = tuples.get(mid);
            int com = t.compareTo(tempTup);
            if (com == 0){
                throw new DBAppException("Existing PK");
            }
            if (com < 0)
                high = mid - 1;
            else
                low = mid + 1;
        }
        // now we insert it at mid value and shift all other record on cell down
        Tuple tempTup = tuples.get(mid);
        if (t.compareTo(tempTup) > 0)
            mid++;
        // if the page was full before inserting
        if (tuples.size() == Table.PAGE_SIZE) {
            // insert the last record to the next page
            if (p.getPageNumber() == this.pageNums.get(this.pageNums.size() - 1))
                createPage();
            int pageNum = 0;
            for (int i = 0 ; i < pageNums.size() ; i++){
                int pgNum = pageNums.get(i);
                if (p.getPageNumber() == pgNum){
                    pageNum = pageNums.get(i+1);
                    break;
                }
            }
            Page nextPage = getPageByNumber(pageNum); // edit
            this.insertRecFirst(tuples.get(tuples.size() - 1), nextPage);
            for (int i = tuples.size()-1 ; i > mid ; i--){
                tuples.set(i , tuples.get(i-1));
            }
            insertInIndex(t,this.name,p.getPageNumber());
            tuples.set(mid , t);
            savePage(p);
            return;
        }
        Tuple last = tuples.get(tuples.size() - 1);
        for (int i = tuples.size()-1 ; i > mid ; i--){
            tuples.set(i , tuples.get(i-1));
        }
        insertInIndex(t , this.name , p.getPageNumber());
        tuples.set(mid , t);
        tuples.add(last);
        savePage(p);
    }

    private void insertRecFirst(Tuple t, Page p) throws DBAppException {
        Vector<Tuple> tuples = p.getTuples();
        ArrayList<String> indices = DBApp.getIndexNames(this.name);
        String pk = DBApp.getPK(this.name);
        Object pkVal = t.getData().get(pk);
        for (int i = 0 ; i < indices.size() ; i++) {
            String[] indexCols = indices.get(i).split("-");
            Object x = t.getData().get(indexCols[0]);
            Object y = t.getData().get(indexCols[1]);
            Object z = t.getData().get(indexCols[2]);
//            boolean xNull = x == Values.NULL;
//            boolean yNull = y == Values.NULL;
//            boolean zNull = z == Values.NULL;
//            String tmpRef = reference + xNull + "," + yNull + "," + zNull;
            Octree tree = DBApp.getOctree(indices.get(i));
            OctPoint point = tree.get(x,y,z);
            OctreeReference ref = point.getRef();
            ArrayList<String> refs = ref.getDuplicates();
            String[] mainRefDet = ref.getReference().split(",");
            if (mainRefDet[1].equals(pkVal+"")){
                mainRefDet[0] = p.getPageNumber()+"";
                String newRef = String.join("," , mainRefDet);
                ref.setReference(newRef);
                DBApp.saveOctree(tree , indices.get(i));
                continue;
            }
            for (int j = 0 ; j < refs.size() ; j++){
                String[] refDet = refs.get(j).split(",");
                if (refDet[1].equals(pkVal+"")){
                    refDet[0] = p.getPageNumber()+"";
                    String newRef = String.join("," , refDet);
                    refs.remove(j);
                    refs.add(newRef);
                    DBApp.saveOctree(tree , indices.get(i));
                    break;
                }
            }
        }
        if (tuples.size() == Table.PAGE_SIZE) {
            // insert the last record to the next page
            if (p.getPageNumber() == this.pageNums.get(this.pageNums.size() - 1))
                createPage();
            int pageNum = 0;
            for (int i = 0 ; i < pageNums.size() ; i++){
                int pgNum = pageNums.get(i);
                if (p.getPageNumber() == pgNum){
                    pageNum = pageNums.get(i+1);
                    break;
                }
            }
            Page nextPage = getPageByNumber(pageNum); // edit
            this.insertRecFirst(tuples.get(tuples.size() - 1), nextPage);
            for (int i = tuples.size()-1 ; i > 0 ; i--){
                tuples.set(i , tuples.get(i-1));
            }
            tuples.set(0 , t);
            savePage(p);
            return;
        }
        Tuple last = null;
        if (tuples.size() != 0)
            last = tuples.get(tuples.size() - 1);
        for (int i = tuples.size()-1 ; i > 0 ; i--){
            tuples.set(i , tuples.get(i-1));
        }
        if (last != null) {
            tuples.set(0, t);
            tuples.add(last);
        }
        else
            tuples.add(t);
        savePage(p);
    }

    private void insertInIndex(Tuple t, String strTableName, int pageNumber) throws DBAppException {
        String pk = DBApp.getPK(strTableName);
        String reference = pageNumber + "," + t.getData().get(pk) + ",";
        ArrayList<String> indices = DBApp.getIndexNames(strTableName);
        for (int i = 0 ; i < indices.size() ; i++){
            String[] indexCols = indices.get(i).split("-");
            Object x = t.getData().get(indexCols[0]);
            Object y = t.getData().get(indexCols[1]);
            Object z = t.getData().get(indexCols[2]);
            boolean xNull = x == Values.NULL;
            boolean yNull = y == Values.NULL;
            boolean zNull = z == Values.NULL;
            String tmpRef = reference + xNull + "," + yNull + "," + zNull;
            Octree tree = DBApp.getOctree(indices.get(i));
            OctreeReference ref = new OctreeReference(tmpRef);
            tree.insert(x,y,z,ref);
            DBApp.saveOctree(tree , indices.get(i));
        }
    }

    public Tuple convertRefToTup(String ref) throws DBAppException {
        ArrayList<Hashtable<String , String>> dataOfTable = DBApp.readDataOfTable(this.name); //type max min pk
        String pk = DBApp.getPK(this.name);
        String pkType = dataOfTable.get(0).get(pk);
        return DBApp.convertRefToTuple(ref , pkType , this , pk);
    }

    public void deleteRecs(Hashtable<String , Object> htblColNameVal) throws DBAppException {
//        ArrayList<Hashtable<String , String>> dataOfTable = DBApp.readDataOfTable(this.name); //type max min pk
//        String pk = dataOfTable.get(3).get("pk");
        ArrayList<String> cols = new ArrayList<>(htblColNameVal.keySet());
        ArrayList<ArrayList> indices = DBApp.checkColsContainIndex(cols , this.name);
        if (indices.get(0).size() != 0){
            ArrayList<Integer> indicesStart = indices.get(0);
            ArrayList<String> indicesName = indices.get(1);
            ArrayList<String> references = new ArrayList<>();
            ArrayList<Integer> doneIndices = new ArrayList<>();
            ArrayList<Hashtable<String , String>> a = DBApp.readDataOfTable(this.name);//type max min pk
            for (int i = 0 ; i < indicesStart.size() ; i++){
                String[] inCols = indicesName.get(i).split("-");
                Object x = htblColNameVal.get(inCols[0]);
                Object y = htblColNameVal.get(inCols[1]);
                Object z = htblColNameVal.get(inCols[2]);
                boolean xNull = x == Values.NULL;
                boolean yNull = y == Values.NULL;
                boolean zNull = z == Values.NULL;
                if (x == Values.NULL){
                    x = DBApp.convertMaxToObject(inCols[0],this.name);
                }
                if (y == Values.NULL)
                    y = DBApp.convertMaxToObject(inCols[1], this.name);
                if (z == Values.NULL)
                    z = DBApp.convertMaxToObject(inCols[2] , this.name);
                Octree tree = DBApp.getOctree(indicesName.get(i));
                OctPoint tmp = tree.get(x,y,z);
                ArrayList<String> refs = new ArrayList<>();
                refs.addAll(tmp.getRef().getDuplicates());
                refs.add(tmp.getRef().getReference());
                for (int m = 0 ; m < refs.size() ; m++){
                    String tmpRef = refs.get(m);
                    if (!tmpRef.split(",")[2].toLowerCase().equals((xNull+"").toLowerCase())){
                        refs.remove(m);
                        m--;
                    }
                    if (!tmpRef.split(",")[3].toLowerCase().equals((yNull+"").toLowerCase())){
                        refs.remove(m);
                        m--;
                    }
                    if (!tmpRef.split(",")[4].toLowerCase().equals((zNull+"").toLowerCase())){
                        refs.remove(m);
                        m--;
                    }

                }
                if (references.size() == 0)
                    references = refs;
                else{
                    references.retainAll(refs);
                }
                doneIndices.add(i);
                tree = null;
                break;
            }
            for (int i = 0 ; i < references.size() ; i++){
                Tuple t = convertRefToTup(references.get(i));
                boolean delete = true;
                for (int j = 0 ; j < cols.size() ; j++){
                    if (doneIndices.contains(j)){
                        j+=2;
                        continue;
                    }
                    Object tmp = t.getData().get(cols.get(j));
                    if (DBApp.compareObjects(tmp , htblColNameVal.get(cols.get(j))) != 0){
                        delete = false;
                        break;
                    }
                }
                if (delete){
                    String[] refDetails = references.get(i).split(",");
                    int pageNum = Integer.parseInt(refDetails[0]);
                    String pk = DBApp.getPK(this.name);
                    String val = refDetails[1];
                    Page p = getPageByNumber(pageNum);
                    int targetIndex = -1;
                    int low = 0, high = p.getTuples().size() - 1, mid = (low + high) / 2;
                    while (low <= high) {
                        mid = (low + high) / 2;
                        Tuple tempTup = p.getTuples().get(mid);
                        Object midVal = tempTup.getValue(pk);
                        int com = comparePKs(val , midVal);
                        if (com == 0) {
                            targetIndex = mid;
                            break;
                        }
                        if (com < 0)
                            high = mid - 1;
                        else
                            low = mid + 1;
                    }
                    deleteRec(targetIndex , p , true);
                    for (int j = 0 ; j < indicesName.size() ; j++){
                        String[] inCols = indicesName.get(i).split("-");
                        Object x = htblColNameVal.get(inCols[0]);
                        Object y = htblColNameVal.get(inCols[1]);
                        Object z = htblColNameVal.get(inCols[2]);

                        if (x == Values.NULL){
                            x = DBApp.convertMaxToObject(inCols[0] , this.name);
                        }
                        if (y == Values.NULL)
                            y = DBApp.convertMaxToObject(inCols[1] , this.name);
                        if (z == Values.NULL)
                            z = DBApp.convertMaxToObject(inCols[2] , this.name);

                        Octree tree = DBApp.getOctree(indicesName.get(i));
                        tree.removeRef(x,y,z,references.get(i));
                        DBApp.saveOctree(tree , indicesName.get(i));
                        tree= null;
                    }
                }
            }
            return;
        }

        String pk = DBApp.getPK(this.name);
        if (htblColNameVal.keySet().contains(pk)){
            Tuple t = new Tuple(htblColNameVal , this.name);
            Page p = getPageByTuple(t);
            if (p==null){
                throw new DBAppException("NOT EXISTING PK");
            }
            // System.out.println(t.getData().get("id"));
            int index = binSearchRecs(p,t);
            if (index == -1) {
                throw new DBAppException("NOT EXISTING PK");
            }
            Tuple tup = p.getTuples().get(index);
            if (checkDelete(htblColNameVal , tup))
                deleteRec(index , p , true);
            return;
        }
        for (int i = 0 ; i < pageNums.size() ; i++){
            int size = pageNums.size();
            int pgNum = pageNums.get(i);
            Page p = getPageByNumber(pgNum);
            boolean pageAva = true;
            for (int j = 0 ; j < p.getTuples().size() ; j++){
                if (checkDelete(htblColNameVal , p.getTuples().get(j))){
                    deleteRec(j,p , pageAva);
                    j--;
                }
            }
            if (pageNums.size() < size)
                i--;
        }



    }
    public boolean checkDelete(Hashtable<String,Object> htblColNameVal , Tuple t){
        for (String key : htblColNameVal.keySet()){
            Object val = htblColNameVal.get(key);
            if (val == Values.NULL){
                if (t.getData().get(key) == Values.NULL){
                    continue;
                }
            }
            if (t.getData().get(key) == Values.NULL){
                return false;
            }
            if (val instanceof Integer){
                Integer value = (Integer) val;
                Integer temp = (Integer) t.getData().get(key);
                if (!value.equals(temp))
                    return false;
            }
            if (val instanceof Double){
                Double value = (Double) val;
                Double test = (Double) t.getData().get(key);
                if (!value.equals(test))
                    return false;
            }
            if (val instanceof String){
                String value = (String) val;
                if (!value.equals((String) t.getData().get(key)))
                    return false;
            }
            if (val instanceof Date){
                Date value = (Date) val;
                if (!value.equals ((Date) t.getData().get(key)))
                    return false;
            }
        }
        return true;
    }
    public int binSearchRecs(Page p , Tuple t) throws DBAppException {
        int low = 0 , high = p.getTuples().size() - 1 , mid = (low + high) / 2;
        while (low <= high){
            mid = (low + high) / 2;
            Tuple tempTup = p.getTuples().get(mid);
            int com = t.compareTo(tempTup);
            if (com == 0){
                return mid;
            }
            if (com < 0)
                high = mid - 1;
            else
                low = mid + 1;
        }
        return -1;
    }
    public void deleteRec(int i,Page p , boolean pageAva) throws DBAppException {
        //Page p = getPageByTuple(t);
        p.getTuples().remove(i);
        if (p.getTuples().size() == 0){
            String path = "Pages/" + this.name + "" + p.getPageNumber() + ".class";
            File file = new File(path);
            System.out.println(file.delete());
            pageAva = false;
            for (int j = 0 ; j < pageNums.size() ; j++) {
                int pg = pageNums.get(j);
                if (pg==p.getPageNumber()) {
                    this.pageNums.remove(j);
                    break;
                }
            }
        }
        else {
            savePage(p);
        }

    }
    public void updateRec(Tuple t,String val) throws DBAppException {
        String pk = DBApp.getPK(this.name);
        ArrayList<String> indexNames = DBApp.getIndexNames(this.name);
        boolean useIndex = false;
        String indexToUse = "";
        boolean isX = false;
        boolean isY = false;
        boolean isZ = false;
        for (int i = 0 ; i < indexNames.size() ; i++){
            String[] det = indexNames.get(i).split("-");
            if (det[0].equals(pk) || det[1].equals(pk) || det[2].equals(pk)){
                useIndex = true;
                indexToUse = indexNames.get(i);
                isX = det[0].equals(pk);
                isY = det[1].equals(pk);
                isZ = det[2].equals(pk);
                break;
            }
//            System.out.println(det[0]);
//            if (t.getData().get(det[0]) != null || t.getData().get(det[1]) != null || t.getData().get(det[2]) != null) {
//                indexToUse = indexNames.get(i);
//                useIndex = true;
//            }
        }
        if (useIndex){
            System.out.println(indexToUse);
            Octree tree = DBApp.getOctree(indexToUse);
            Object value = DBApp.parseHelper(val , pk , this.name);
            OctPoint point;
            if (isX){
               point = tree.getX(value);
            }
            else if (isY){
                point = tree.getY(value);
            }
            else{
                point = tree.getZ(value);
            }
            if (point == null)
                throw new DBAppException("PK DOESNT EXIST");
            ArrayList<OctPoint> arr = new ArrayList<>();
            arr.add(point);
            Tuple tempTup = DBApp.convertOctPointtoTuple(arr , this.name).get(0);
            Page p = getPageByTuple(tempTup);
            int index = binSearchRecs(p,tempTup);
            ArrayList<Hashtable<String, String>> a = DBApp.readDataOfTable(this.name);//type max min pk
            tempTup = p.getTuples().get(index);
            for (int i = 0; i < indexNames.size(); i++) {
                if (indexNames.get(i).equals(indexToUse))
                    continue;
                String[] indexDet = indexNames.get(i).split("-");
                if (t.getData().keySet().contains(indexDet[0]) || t.getData().keySet().contains(indexDet[1])
                        || t.getData().keySet().contains(indexDet[2])) {
                    Object x = tempTup.getData().get(indexDet[0]);
                    Object y = tempTup.getData().get(indexDet[1]);
                    Object z = tempTup.getData().get(indexDet[2]);
                    if (x == Values.NULL)
                        x = DBApp.convertMaxToObject(indexDet[0] , this.name);
                    if (y == Values.NULL)
                        y = DBApp.convertMaxToObject(indexDet[1] , this.name);
                    if (z == Values.NULL)
                        z = DBApp.convertMaxToObject(indexDet[2] , this.name);
                    tree = DBApp.getOctree(indexNames.get(i));
                    point = tree.get(x, y, z);
                    String mainRef = point.getRef().getReference();
                    if (mainRef.split(",")[1].equals(tempTup.getData().get(pk))) {
                        tree.removeRef(x, y, z, mainRef);
                        OctreeReference ref = new OctreeReference(mainRef);
                        tree.insert(tempTup.getData().get(indexDet[0]), tempTup.getData().get(indexDet[1]),
                                tempTup.getData().get((indexDet[2])), ref);
                        DBApp.saveOctree(tree, indexNames.get(i));
                        continue;
                    }
                    for (int j = 0; j < point.getRef().getDuplicates().size(); j++) {
                        String tempRef = point.getRef().getDuplicates().get(j);
                        if (tempRef.split(",")[1].equals(val)) {
                            tree.removeRef(x, y, z, tempRef);
                            OctreeReference ref = new OctreeReference(tempRef);
                            tree.insert(tempTup.getData().get(indexDet[0]), tempTup.getData().get(indexDet[1]),
                                    tempTup.getData().get((indexDet[2])), ref);
                            DBApp.saveOctree(tree, indexNames.get(i));
                            break;
                        }
                    }
                }
            }
            for (String str : t.getData().keySet()) {
                tempTup.getData().replace(str, t.getData().get(str));
            }

            savePage(p);
            return;

        }
        else {
            Page p = getPageContainingKey(val);
            if (p == null) {
                throw new DBAppException("PK DOESNT EXIST");
            }
            int low = 0, high = p.getTuples().size() - 1, mid = (low + high) / 2;
            while (low <= high) {
                mid = (low + high) / 2;
                Tuple tempTup = p.getTuples().get(mid);
                Object midVal = tempTup.getValue(pk);
                int com = comparePKs(val, midVal);
                Tuple old = new Tuple(new Hashtable<>(tempTup.getData()) , this.name);
                if (com == 0) {
                    for (String str : t.getData().keySet()) {
                        tempTup.getData().replace(str, t.getData().get(str));
                    }
                    ArrayList<Hashtable<String, String>> a = DBApp.readDataOfTable(this.name);//type max min pk

                    for (int i = 0; i < indexNames.size(); i++) {
                        String[] indexDet = indexNames.get(i).split("-");
                        if (t.getData().keySet().contains(indexDet[0]) || t.getData().keySet().contains(indexDet[1])
                                || t.getData().keySet().contains(indexDet[2])) {
//                            System.out.println(indexNames.get(i) + "ooo");
                            Object x = old.getData().get(indexDet[0]);
                            Object y = old.getData().get(indexDet[1]);
                            Object z = old.getData().get(indexDet[2]);
                            if (x == Values.NULL)
                                x = DBApp.convertMaxToObject(indexDet[0] , this.name);
                            if (y == Values.NULL)
                                y = DBApp.convertMaxToObject(indexDet[1] , this.name);
                            if (z == Values.NULL)
                                z = DBApp.convertMaxToObject(indexDet[2] , this.name);
                            Octree tree = DBApp.getOctree(indexNames.get(i));
                            System.out.println();
                            System.out.println(x + " " + y + " " + z);
                            OctPoint point = tree.get(x, y, z);
                            String mainRef = point.getRef().getReference();
                            if (mainRef.split(",")[1].equals(tempTup.getData().get(pk))) {
                                tree.removeRef(x, y, z, mainRef);
                                OctreeReference ref = new OctreeReference(mainRef);
//                                Object x1;
//                                Object y1;
//                                Object z1;
//                                if (t.getData().keySet().contains(indexDet[0]))
                                System.out.println(tempTup.getData().get(indexDet[0]));
                                System.out.println(tempTup.getData().get(indexDet[1]));
                                System.out.println(tempTup.getData().get(indexDet[2]));
                                tree.insert(tempTup.getData().get(indexDet[0]), tempTup.getData().get(indexDet[1]),
                                        tempTup.getData().get((indexDet[2])), ref);
                                DBApp.saveOctree(tree, indexNames.get(i));
                                continue;
                            }
                            for (int j = 0; j < point.getRef().getDuplicates().size(); j++) {
                                String tempRef = point.getRef().getDuplicates().get(j);
                                if (tempRef.split(",")[1].equals(val)) {
                                    tree.removeRef(x, y, z, tempRef);
                                    OctreeReference ref = new OctreeReference(tempRef);
                                    System.out.println(tempTup.getData().get(indexDet[0]));
                                    System.out.println(tempTup.getData().get(indexDet[1]));
                                    System.out.println(tempTup.getData().get(indexDet[2]));
                                    tree.insert(tempTup.getData().get(indexDet[0]), tempTup.getData().get(indexDet[1]),
                                            tempTup.getData().get((indexDet[2])), ref);
                                    DBApp.saveOctree(tree, indexNames.get(i));
                                    break;
                                }
                            }
                        }
                    }
                    for (String str : t.getData().keySet()) {
                        tempTup.getData().replace(str, t.getData().get(str));
                    }
                    savePage(p);
                    return;
                }
                if (com < 0)
                    high = mid - 1;
                else
                    low = mid + 1;
            }
            throw new DBAppException("PK DOESNT EXIST");
        }
    }
    public Page getPageContainingKey(String key) throws DBAppException {
        String pk = DBApp.getPK(this.name);
        ArrayList<Hashtable<String , String>> a = DBApp.readDataOfTable(this.name);//type max min pk
        for (int i = 0 ; i < pageNums.size() ; i++){
            Page page = getPageByNumber(pageNums.get(i));
            if (comparePKs(key , page.getTuples().get(0).getData().get(pk)) >= 0 &&
                    comparePKs(key , page.getTuples().get(page.getTuples().size() - 1).getData().get(pk)) <= 0)
                return page;
        }

        return null;
    }

    public Vector<Integer> getPageNums() {
        return pageNums;
    }

    public int comparePKs(String stringPK , Object realPK) throws DBAppException {
        if (realPK instanceof String){
            return stringPK.compareToIgnoreCase((String) realPK);
        }
        else if (realPK instanceof Integer){
            Integer sPK = Integer.parseInt(stringPK);
            Integer rPK = (Integer) realPK;
            return sPK.compareTo(rPK);
        }
        if (realPK instanceof Double){
            Double sPK = Double.parseDouble(stringPK);
            Double rPK = (Double) realPK;
            return sPK.compareTo(rPK);
        }
        else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date sPK = dateFormat.parse(stringPK);
                Date rPK = (Date) realPK;
                return sPK.compareTo(rPK);
            } catch (Exception e) {
                throw new DBAppException("NOT A DATE");
            }
        }

    }


}
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DBApp {
    static HashSet<String> tableNames = new HashSet<>();

    public static void readTableNames() throws DBAppException {
        BufferedReader br = null;
        String itemsPath = "metadata.csv";
        try {
            br = new BufferedReader(new FileReader(itemsPath));
            String line = br.readLine();
            while (line != null) {
                String[] data = line.split(",");
                String name = data[0];
                tableNames.add(name);
                line = br.readLine();
            }
        } catch (Exception e) {
            throw new DBAppException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                throw new DBAppException(e);
            }
        }
        for (String name : tableNames) {

        }
    }

    public void writeNewColumn(String tableName, String columnName, String columnType,
                               String clusteringKey, String indexName, String indexType,
                               String min, String max) throws DBAppException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        String filename = "metadata.csv";
        try {
            fw = new FileWriter(filename, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            String toWrite = tableName + "," + columnName + "," + columnType + "," +
                    clusteringKey + "," + indexName + "," + indexType + "," + min + "," + max;
            pw.println(toWrite);
            pw.flush();


        } catch (Exception e) {
            throw new DBAppException(e);
        } finally {
            try {
                pw.close();
                bw.close();
                fw.close();
            } catch (Exception e) {

                throw new DBAppException();
            }
        }
    }


    public void init() {
        Properties props = new Properties();
        try {
            InputStream input = new FileInputStream("resources/DBApp.config");
            props.load(input);
            Table.PAGE_SIZE = Integer.parseInt(props.getProperty("MaximumRowsCountinTablePage"));
            Octree.maxEntriesInOctree = Integer.parseInt(props.getProperty("MaximumEntriesinOctreeNode"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File("metadata.csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File tablesFolder = new File("Tables");
        if (!tablesFolder.isDirectory())
            tablesFolder.mkdir();
        File pagesFolder = new File("Pages");
        if (!pagesFolder.isDirectory())
            pagesFolder.mkdir();
        File OctreeFolder = new File("Octrees");
        if (!OctreeFolder.isDirectory())
            OctreeFolder.mkdir();

    }

    public void createTable(String strTableName,
                            String strClusteringKeyColumn, // id
                            Hashtable<String, String> htblColNameType,
                            Hashtable<String, String> htblColNameMin,
                            Hashtable<String, String> htblColNameMax)
            throws DBAppException {
        if (tableNames.size() == 0)
            readTableNames();
        if (tableNames.contains(strTableName)) {
            throw new DBAppException("ALREADY EXISTING TABLE");
        } else {
            if (htblColNameType.size() != htblColNameMin.size() || htblColNameType.size() != htblColNameMax.size()) {
                throw new DBAppException("MISSING INFO");
            }
            for (String columnName : htblColNameType.keySet()) { // columnName : id , name , gpa
                String type = htblColNameType.get(columnName) + "";
                String min = htblColNameMin.get(columnName) ;
                String max = htblColNameMax.get(columnName);
                boolean clustering = columnName.equals(strClusteringKeyColumn);
                writeNewColumn(strTableName, columnName, type, clustering + "", "null", "null", min, max);
            }
            Table t = new Table(strTableName);
            try {
                // Serialize the object to a file
                System.out.println(tableNames.size());
                FileOutputStream fileOut = new FileOutputStream("Tables/" + strTableName + ".class");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(t);
                out.close();
                fileOut.close();
            } catch (IOException e) {
                throw new DBAppException(e);
            }
            tableNames.add(strTableName);

        }
        // check tablename existence
    }

    public void insertIntoTable(String strTableName,
                                Hashtable<String, Object> htblColNameValue)
            throws DBAppException {
        readTableNames();
        if (!tableNames.contains(strTableName))
            throw new DBAppException("THERE IS NO SUCH TABLE");
        String pk = getPK(strTableName);
        if (!htblColNameValue.keySet().contains(pk)) {
            throw new DBAppException("CANT INSERT WITHOUT PK");
        }
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        Hashtable<String, String> htblColNameType = dataOfTable.get(0);
//        String pk = dataOfTable.get(3).get("pk");

        for (String str : htblColNameType.keySet()) {
            if (!htblColNameValue.keySet().contains(str)) {
                htblColNameValue.put(str, Values.NULL);
            }
        }
        verifyTuple(strTableName, htblColNameValue);
        Table t1 = getTable(strTableName);
        Tuple t = new Tuple(htblColNameValue, strTableName);
        t1.insertRec(t);
        saveTable(t1);
    }

    public void verifyTuple(String strTableName, Hashtable<String, Object> htblColNameValue) throws DBAppException {
        BufferedReader br = null;
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        Hashtable<String, String> htblColNameType = dataOfTable.get(0);
        Hashtable<String, String> htblColNameMax = dataOfTable.get(1);
        Hashtable<String, String> htblColNameMin = dataOfTable.get(2);
        String pk = dataOfTable.get(3).get("pk");
        readTableNames();
        if (!tableNames.contains(strTableName))
            throw new DBAppException("THERE IS NO SUCH TABLE");
        for (String str : htblColNameValue.keySet()) {
            if (!htblColNameType.keySet().contains(str))
                throw new DBAppException("THIS COLUMN DOESNT EXIST");
        }
        for (String str : htblColNameValue.keySet()) {
            if (htblColNameValue.get(str) == Values.NULL && !str.equals(pk))
                continue;
            else if (htblColNameValue.get(str) == Values.NULL && str.equals(pk))
                throw new DBAppException("PK CAN'T BE NULL");
            if (htblColNameType.get(str).toLowerCase().equals("java.lang.string")) {
                String doubleValue = null;
                if (htblColNameValue.get(str) instanceof TerminalNodeImpl){
                    doubleValue = ((TerminalNodeImpl)htblColNameValue.get(str)).getText();
                }
                else if (!(htblColNameValue.get(str) instanceof String))
                    throw new DBAppException("Invalid DataTypes");
                boolean flag = false;
                if (doubleValue != null)
                    flag = true;
                String val = "";
                if (!flag) {
                    val = (String) htblColNameValue.get(str);
                }
                else {
                    if (doubleValue != null)
                        val = doubleValue;
                }
                String min = htblColNameMin.get(str);
                String max = htblColNameMax.get(str);
                if (val.compareToIgnoreCase(min) < 0 || val.compareToIgnoreCase(max) > 0)
                    throw new DBAppException("VALUE OUT OF RANGE");
            } else if (htblColNameType.get(str).toLowerCase().equals("java.lang.integer")) {
                Integer doubleValue = null;
                if (htblColNameValue.get(str) instanceof TerminalNodeImpl){
                    doubleValue = Integer.parseInt(((TerminalNodeImpl)htblColNameValue.get(str)).getText());
                }
//                System.out.println(htblColNameType.get(str) + " " + str);
                else if (!(htblColNameValue.get(str) instanceof Integer))
                    throw new DBAppException("Invalid DataTypes");
                boolean flag = false;
                if (doubleValue != null)
                    flag = true;
                Integer val = null;
                if (!flag)
                    val = (Integer) htblColNameValue.get(str);
                else
                    val = doubleValue;

                Integer min = Integer.parseInt(htblColNameMin.get(str));
                Integer max = Integer.parseInt(htblColNameMax.get(str));
                if (val.compareTo(min) < 0 || val.compareTo(max) > 0)
                    throw new DBAppException("VALUE OUT OF RANGE");
            } else if (htblColNameType.get(str).toLowerCase().equals("java.lang.double")) {
                Double doubleValue = null;
                if (htblColNameValue.get(str) instanceof TerminalNodeImpl){
                    doubleValue = Double.parseDouble(((TerminalNodeImpl)htblColNameValue.get(str)).getText());
                }
                else if (!(htblColNameValue.get(str) instanceof Double || htblColNameValue.get(str) instanceof Integer))
                    throw new DBAppException("Invalid DataTypes");
                Double val = 1.0;
                if (doubleValue != null)
                    val = doubleValue;
                else if (htblColNameValue.get(str) instanceof Integer) {
                    val = Double.valueOf((Integer) htblColNameValue.get(str));
                } else
                    val = (Double) htblColNameValue.get(str);
                Double min = Double.parseDouble(htblColNameMin.get(str));
                Double max = Double.parseDouble(htblColNameMax.get(str));
                if (val.compareTo(min) < 0 || val.compareTo(max) > 0)
                    throw new DBAppException("VALUE OUT OF RANGE");
            }
            if (htblColNameType.get(str).toLowerCase().equals("java.util.date")) {
                String dateText = ((TerminalNodeImpl) htblColNameValue.get(str)).getText();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Specify the desired date format
                Date date = null;

                try {
                    date = dateFormat.parse(dateText);
                    // Use the 'date' object as needed
                } catch (ParseException e) {
                    // Handle the exception if the text cannot be parsed into a valid date
                }
                if (!(htblColNameValue.get(str) instanceof Date))
                    throw new DBAppException("Invalid DataTypes");
                boolean flag = false;
                if (date != null)
                    flag = true;
                Date val = null;
                if (!flag)
                    val = (Date) htblColNameValue.get(str);
                else
                    val = date;

                Date min = null;
                Date max = null;
                try {
                    min = dateFormat.parse(htblColNameMin.get(str));
                    max = dateFormat.parse(htblColNameMax.get(str));
                } catch (Exception e) {
                    throw new DBAppException(e);
                }
                if (val.compareTo(min) < 0 || val.compareTo(max) > 0)
                    throw new DBAppException("VALUE OUT OF RANGE");
            }


        }
    }

    public static ArrayList<Hashtable<String, String>> readDataOfTable(String strTableName) throws DBAppException {
        BufferedReader br = null;
        String itemsPath = "metadata.csv";
        Hashtable<String, String> htblColNameType = new Hashtable<>();
        Hashtable<String, String> htblColNameMax = new Hashtable<>();
        Hashtable<String, String> htblColNameMin = new Hashtable<>();
        String pk = "";
        try {
            br = new BufferedReader(new FileReader(itemsPath));
            String line = br.readLine();
            while (line != null) {
                String[] data = line.split(",");
                String name = data[0];
                if (name.equals(strTableName)) {
                    htblColNameType.put(data[1], data[2]);
                    htblColNameMin.put(data[1], data[6]);
                    htblColNameMax.put(data[1], data[7]);
                    if (data[3].equals("true"))
                        pk = data[1];
                }
                line = br.readLine();
            }
            ArrayList<Hashtable<String, String>> res = new ArrayList<>();
            res.add(htblColNameType);
            res.add(htblColNameMax);
            res.add(htblColNameMin);
            Hashtable<String, String> pktbl = new Hashtable<>();
            pktbl.put("pk", pk);
            res.add(pktbl);
            br.close();
            return res;
        } catch (Exception e) {
            throw new DBAppException(e);
        }

    }

    public void updateTable(String strTableName,
                            String strClusteringKeyValue,
                            Hashtable<String, Object> htblColNameValue)
            throws DBAppException {
        Table t = getTable(strTableName);
        verifyTuple(strTableName, htblColNameValue);
        verifyPK(strTableName, strClusteringKeyValue);
        Tuple tuple = new Tuple(htblColNameValue, strTableName);
        t.updateRec(tuple, strClusteringKeyValue);
        saveTable(t);
    }

    private void verifyPK(String strTableName, String strClusteringKeyValue) throws DBAppException {
        ArrayList<Hashtable<String, String>> data = readDataOfTable(strTableName); //type max min pk
        Hashtable<String, String> type = data.get(0);
        String pk = data.get(3).get("pk");
        String pkType = type.get(pk);
        if (pkType.toLowerCase().equals("java.lang.string")) {
            return;
        } else if (pkType.toLowerCase().equals("java.lang.integer")) {
            try {
                Integer test = Integer.parseInt(strClusteringKeyValue);
            } catch (Exception e) {
                throw new DBAppException("Incompatible types String and integer");
            }
        } else if (pkType.toLowerCase().equals("java.lang.double")) {
            try {
                Double test = Double.parseDouble(strClusteringKeyValue);
            } catch (Exception e) {
                throw new DBAppException("Incompatible types string and double");
            }
        } else if (pkType.toLowerCase().equals("java.util.date")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date test = dateFormat.parse(strClusteringKeyValue);
            } catch (Exception e) {
                throw new DBAppException("NOT A DATE");
            }
        }

    }

    public void deleteFromTable(String strTableName,
                                Hashtable<String, Object> htblColNameValue)
            throws DBAppException {
        Table table = getTable(strTableName);
        table.deleteRecs(htblColNameValue);
        saveTable(table);
    }

    public static void main(String[] args) throws Exception {
//        DBApp t = new DBApp();
//        t.init();

        DBApp db = new DBApp();
        db.init();
        //String[] indexCols = {"first_name" , "last_name" , "gpa"};
        //db.createIndex("students" , indexCols);
        Hashtable<String , String> htblcolNameMin = new Hashtable<>();
        Hashtable<String , String> htblcolNameMax = new Hashtable<>();
        Hashtable<String , String> htblcolNameType = new Hashtable<>();
        Hashtable<String , Object> htblColNameValue = new Hashtable<>();



        htblColNameValue.clear();
        htblColNameValue.put("age" , 50);
        db.updateTable("Bonus" , "55" , htblColNameValue);
//        htblColNameValue.put("age" , 39);
//        db.updateTable("Bonus" , "64" ,htblColNameValue );

//        htblColNameValue.put("age" , Values.NULL);
//        db.deleteFromTable("Bonus" , htblColNameValue);
        db.displayTable("Bonus");

        StringBuffer tt = new StringBuffer("SELECT * FROM Bonus WHERE id < 14;");
        Iterator it = db.parseSQL(tt);
        while(it.hasNext()){
                            Tuple t = (Tuple) it.next();
                String write = "";
                for (String col : t.getData().keySet()){
                    write += "col: " + col + ", value: " + t.getData().get(col) + ",";
                }
                System.out.println(write);
        }


//        htblcolNameMin.put("id" , "1");
//        htblcolNameMin.put("gpa" , "0.7");
//        htblcolNameMin.put("age" , "20");
//
//        htblcolNameMax.put("id" , "500");
//        htblcolNameMax.put("gpa" , "5.0");
//        htblcolNameMax.put("age" , "80");
//
//        htblcolNameType.put("id" , "java.lang.integer");
//        htblcolNameType.put("gpa" , "java.lang.double");
//        htblcolNameType.put("age" , "java.lang.integer");
//
//        db.createTable("Bonus" , "id", htblcolNameType , htblcolNameMin , htblcolNameMax);



//        String table = "students";
//        Hashtable<String, Object> row = new Hashtable();
////        first_name = "idrkec, last_name = "fcgcde", gpa : 0.85
//        row.put("first_name", "mBqxHR");
//        row.put("last_name", "UAKfUJ");
//        row.put("gpa" , 0.81);
////        row.put("gpa" , 0.8);
////        row.put("gpa", 0.80);
//        //  mBqxHR Point y: UAKfUJ Point Z: 0.81
//
//            SQLTerm[] arrSQLTerms;
//	        arrSQLTerms = new SQLTerm[3];
//	        arrSQLTerms[0] = new SQLTerm("students" , "first_name" , "=" , row.get("first_name"));
//	        arrSQLTerms[0]._strTableName = "students";
//	        arrSQLTerms[0]._strColumnName= "first_name";
//	        arrSQLTerms[0]._strOperator = "=";
//	        arrSQLTerms[0]._objValue = "HHmIaM";
//
//        arrSQLTerms[1] = new SQLTerm("students" , "first_name" , "=" , row.get("first_name"));
//        arrSQLTerms[1]._strTableName = "students";
//        arrSQLTerms[1]._strColumnName= "last_name";
//        arrSQLTerms[1]._strOperator = "=";
//        arrSQLTerms[1]._objValue ="qotMtt";
//
//        arrSQLTerms[2] = new SQLTerm("students" , "first_name" , "=" , row.get("first_name"));
//        arrSQLTerms[2]._strTableName = "students";
//        arrSQLTerms[2]._strColumnName= "gpa";
//        arrSQLTerms[2]._strOperator = "=";
//        arrSQLTerms[2]._objValue =2.74;
//            String[] op = {"AND" , "AND"};
//
//            Iterator it = db.selectFromTable(arrSQLTerms , op);
//            while(it.hasNext()){
//                Tuple t = (Tuple) it.next();
//                String write = "";
//                for (String col : t.getData().keySet()){
//                    write += "col: " + col + ", value: " + t.getData().get(col) + ",";
//                }
//                System.out.println(write);
//            }

//	        arrSQLTerms[1] = new SQLTerm("" , "" , "" , 1.3);
//	        arrSQLTerms[1]._strTableName = "students";
//	        arrSQLTerms[1]._strColumnName= "gpa";
//	        arrSQLTerms[1]._strOperator = "!=";
//	        arrSQLTerms[1]._objValue = row.get("gpa");
//        db.deleteFromTable("students" ,row);
//        db.displayTable("students");


        //row.put("first_name", "Nora");
        //row.put("id", "74-0725");

        //Date dob = new Date(1995 - 1900, 4 - 1, 1);
       // row.put("dob", dob);
    //    row.put("gps", 1.1);
//        db.insertIntoTable(table , row);
        //printOctree("first_name-last_name-gpa-students");


        // createCoursesTable(db);
       // createPCsTable(db);
        //createTranscriptsTable(db);
     //   createStudentTable(db);
     //   insertPCsRecords(db,200);
     //   insertTranscriptsRecords(db,200);
     //    insertStudentRecords(db,200);
      //  insertCoursesRecords(db,200);

//        dbApp.createTable(strTableName, "z", htblColNameType, htblColNameMin, htblColNameMax);
       // Hashtable htblColNameValue = new Hashtable();
//        dbApp.createIndex( strTableName, new String[] {"id","age","gpa"} );
//        htblColNameValue.put("id", new Integer(2343432));
//        htblColNameValue.put("name", new String("Ahmed Noor"));
//        htblColNameValue.put("gpa", new Double(0.95));
//        for (int i = 0; i < 600; i++) {
//            if (i > 590) {
//                htblColNameValue.put("a", 5);
//                htblColNameValue.put("f", "kareem");
//                htblColNameValue.put("c", 4.5);
//
//            } else if (i > 550 && i <= 560) {
//
//            } else {
//                htblColNameValue.put("a", i + 10);
//                htblColNameValue.put("f", "kareem" + i);
//                htblColNameValue.put("c", i + 4.5);
//            }
//            htblColNameValue.put("z", i);
//            htblColNameValue.put("y", i + 424);
//            htblColNameValue.put("x", i + 12);
////            htblColNameValue.put("f" , "kareem" + i);
//            htblColNameValue.put("e", i + 300);
//            htblColNameValue.put("d", i + 15);
//            htblColNameValue.put("b", i);
//
//
            //dbApp.insertIntoTable(strTableName, htblColNameValue);
//            htblColNameValue.clear();
//        }
//        System.out.println("PHASE 1");
//        String[] cols = {"a", "f", "c"};
//        dbApp.createIndex(strTableName, cols);
//        String[] cols2 = {"y", "z", "x"};
//        dbApp.createIndex(strTableName, cols2);
//        System.out.println("PHASE 2");
//
//        for (int i = 1000; i > 600; i--) {
//            htblColNameValue.put("a", i + 10);
//            htblColNameValue.put("f", "kareem" + i);
//            htblColNameValue.put("c", i + 4.5);
//            htblColNameValue.put("z", i);
//            htblColNameValue.put("y", i - 323 );
//            htblColNameValue.put("x", i + 10);
////            htblColNameValue.put("f" , "kareem" + i);
//            htblColNameValue.put("e", i + 300);
//            htblColNameValue.put("d", i + 15);
//            htblColNameValue.put("b", i);
//
//
//            dbApp.insertIntoTable(strTableName, htblColNameValue);
//            htblColNameValue.clear();
//        }
//        System.out.println("PHASE 3");
//        htblColNameValue.clear();
//            htblColNameValue.put("a", 5);
//            htblColNameValue.put("f" , "kareem");
//            htblColNameValue.put("c" , 4.5);
//            htblColNameValue.put("e" , 995);
//            dbApp.deleteFromTable(strTableName , htblColNameValue);
//            htblColNameValue.clear();
//        System.out.println("PHASE 4");
//        for (int i = 100 ; i >= 0 ; i--){
//            htblColNameValue.put("z" , i);
//            dbApp.deleteFromTable(strTableName , htblColNameValue);
//            htblColNameValue.clear();
//        }
//        System.out.println("PHASE 5");
//
//        for (int i = 200 ; i < 300 ; i++){
//            htblColNameValue.put("f" , "maryam");
//            dbApp.updateTable(strTableName , i+"" , htblColNameValue);
//        }
//        System.out.println("PHASE 6");
//        SQLTerm s1 = new SQLTerm(strTableName , "z" , "!=" , 900);
//        SQLTerm s2 = new SQLTerm(strTableName , "gpa" , "<=" , 1450.5);
//        SQLTerm s3 = new SQLTerm(strTableName , "id" , "<=" , 1450);
  //      SQLTerm[] s = new SQLTerm[1];
       // StringBuffer sb = new StringBuffer("SELECT * FROM Student WHERE b >= 0 ;");
//        s[1] = s2;
//        s[2] = s1;
    //    s[0] = s1;
      //  String[] operators = {};
      //  Iterator<Tuple> it = dbApp.parseSQL(sb);
        //int count = 0;
//
   //     while (it.hasNext()){
//            System.out.println("HI");
     //       count++;
       //     it.next();
        //}
  //      System.out.println(count + "<< COUNT");
//        dbApp.insertIntoTable(strTableName, htblColNameValue);
//        htblColNameValue.clear();
//        htblColNameValue.put("id" , 78452);
//        dbApp.deleteFromTable(strTableName  , htblColNameValue);
//        String[] cols = {"id" , "name" , "gpa"};
//        dbApp.createIndex(strTableName , cols);
//        Octree tr = dbApp.getOctree("aa");
//        System.out.println(tr);
//        htblColNameValue.put("id", new Integer(453455));
//        htblColNameValue.put("name", new String("Ahmed Noor"));
//        htblColNameValue.put("gpa", new Double(0.95));
//        dbApp.insertIntoTable(strTableName, htblColNameValue);
//        htblColNameValue.clear();
//        htblColNameValue.put("id", new Integer(5674567));
//        htblColNameValue.put("name", new String("Dalia Noor"));
//        htblColNameValue.put("gpa", new Double(1.25));
//        dbApp.insertIntoTable(strTableName, htblColNameValue);
//        htblColNameValue.clear();
//        htblColNameValue.put("id", new Integer(23498));
//        htblColNameValue.put("name", new String("John Noor"));
//        htblColNameValue.put("gpa", new Double(1.5));
//        dbApp.insertIntoTable(strTableName, htblColNameValue);
//        htblColNameValue.clear();
//        htblColNameValue.put("id", new Integer(78452));
//        htblColNameValue.put("name", new String("Zaky Noor"));
//        htblColNameValue.put("gpa", new Double(0.88));
//        dbApp.insertIntoTable(strTableName, htblColNameValue);
//        ArrayList<String> a = new ArrayList();
//        a.add("id");
//        a.add("gpa");
//        a.add("name");

//        t.selectFromTable(s , operators);
//        System.out.println();
        //db.displayTable("students");
    }

    private static void  insertCoursesRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader coursesTable = new BufferedReader(new FileReader("courses_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = coursesTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");


            int year = Integer.parseInt(fields[0].trim().substring(0, 4));
            int month = Integer.parseInt(fields[0].trim().substring(5, 7));
            int day = Integer.parseInt(fields[0].trim().substring(8));

            Date dateAdded = new Date(year - 1900, month - 1, day);

            row.put("date_added", dateAdded);

            row.put("course_id", fields[1]);
            row.put("course_name", fields[2]);
            row.put("hours", Integer.parseInt(fields[3]));

            dbApp.insertIntoTable("courses", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        coursesTable.close();
    }

    private static void  insertStudentRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader studentsTable = new BufferedReader(new FileReader("students_table.csv"));
        String record;
        int c = limit;
        if (limit == -1) {
            c = 1;
        }

        Hashtable<String, Object> row = new Hashtable<>();
        while ((record = studentsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("id", fields[0]);
            row.put("first_name", fields[1]);
            row.put("last_name", fields[2]);

            int year = Integer.parseInt(fields[3].trim().substring(0, 4));
            int month = Integer.parseInt(fields[3].trim().substring(5, 7));
            int day = Integer.parseInt(fields[3].trim().substring(8));

            Date dob = new Date(year - 1900, month - 1, day);
            row.put("dob", dob);

            double gpa = Double.parseDouble(fields[4].trim());

            row.put("gpa", gpa);

            dbApp.insertIntoTable("students", row);
            row.clear();
            if (limit != -1) {
                c--;
            }
        }
        studentsTable.close();
    }
    private static void insertTranscriptsRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader transcriptsTable = new BufferedReader(new FileReader("transcripts_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = transcriptsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("gpa", Double.parseDouble(fields[0].trim()));
            row.put("student_id", fields[1].trim());
            row.put("course_name", fields[2].trim());

            String date = fields[3].trim();
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8));

            Date dateUsed = new Date(year - 1900, month - 1, day);
            row.put("date_passed", dateUsed);

            dbApp.insertIntoTable("transcripts", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        transcriptsTable.close();
    }
    private static void insertPCsRecords(DBApp dbApp, int limit) throws Exception {
        BufferedReader pcsTable = new BufferedReader(new FileReader("pcs_table.csv"));
        String record;
        Hashtable<String, Object> row = new Hashtable<>();
        int c = limit;
        if (limit == -1) {
            c = 1;
        }
        while ((record = pcsTable.readLine()) != null && c > 0) {
            String[] fields = record.split(",");

            row.put("pc_id", Integer.parseInt(fields[0].trim()));
            row.put("student_id", fields[1].trim());

            dbApp.insertIntoTable("pcs", row);
            row.clear();

            if (limit != -1) {
                c--;
            }
        }

        pcsTable.close();
    }
    private static void createTranscriptsTable(DBApp dbApp) throws Exception {
        // Double CK
        String tableName = "transcripts";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("gpa", "java.lang.Double");
        htblColNameType.put("student_id", "java.lang.String");
        htblColNameType.put("course_name", "java.lang.String");
        htblColNameType.put("date_passed", "java.util.Date");

        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("gpa", "0.7");
        minValues.put("student_id", "43-0000");
        minValues.put("course_name", "AAAAAA");
        minValues.put("date_passed", "1990-01-01");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("gpa", "5.0");
        maxValues.put("student_id", "99-9999");
        maxValues.put("course_name", "zzzzzz");
        maxValues.put("date_passed", "2020-12-31");

        dbApp.createTable(tableName, "gpa", htblColNameType, minValues, maxValues);
    }

    private static void createStudentTable(DBApp dbApp) throws Exception {
        // String CK
        String tableName = "students";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("id", "java.lang.String");
        htblColNameType.put("first_name", "java.lang.String");
        htblColNameType.put("last_name", "java.lang.String");
        htblColNameType.put("dob", "java.util.Date");
        htblColNameType.put("gpa", "java.lang.Double");

        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("id", "43-0000");
        minValues.put("first_name", "AAAAAA");
        minValues.put("last_name", "AAAAAA");
        minValues.put("dob", "1990-01-01");
        minValues.put("gpa", "0.7");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("id", "99-9999");
        maxValues.put("first_name", "zzzzzz");
        maxValues.put("last_name", "zzzzzz");
        maxValues.put("dob", "2000-12-31");
        maxValues.put("gpa", "5.0");

        dbApp.createTable(tableName, "id", htblColNameType, minValues, maxValues);
    }
    private static void createPCsTable(DBApp dbApp) throws Exception {
        // Integer CK
        String tableName = "pcs";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("pc_id", "java.lang.Integer");
        htblColNameType.put("student_id", "java.lang.String");


        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("pc_id", "0");
        minValues.put("student_id", "43-0000");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("pc_id", "20000");
        maxValues.put("student_id", "99-9999");

        dbApp.createTable(tableName, "pc_id", htblColNameType, minValues, maxValues);
    }
    private static void createCoursesTable(DBApp dbApp) throws Exception {
        // Date CK
        String tableName = "courses";

        Hashtable<String, String> htblColNameType = new Hashtable<String, String>();
        htblColNameType.put("date_added", "java.util.Date");
        htblColNameType.put("course_id", "java.lang.String");
        htblColNameType.put("course_name", "java.lang.String");
        htblColNameType.put("hours", "java.lang.Integer");


        Hashtable<String, String> minValues = new Hashtable<>();
        minValues.put("date_added", "1901-01-01");
        minValues.put("course_id", "0000");
        minValues.put("course_name", "AAAAAA");
        minValues.put("hours", "1");

        Hashtable<String, String> maxValues = new Hashtable<>();
        maxValues.put("date_added", "2020-12-31");
        maxValues.put("course_id", "9999");
        maxValues.put("course_name", "zzzzzz");
        maxValues.put("hours", "24");

        dbApp.createTable(tableName, "date_added", htblColNameType, minValues, maxValues);

    }

    public static Table getTable(String tableName) throws DBAppException {
        try {
            readTableNames();
            if (!tableNames.contains(tableName))
                throw new DBAppException("no such table");
            Table t = null;
            FileInputStream fileIn = new FileInputStream("Tables/" + tableName + ".class");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            t = (Table) in.readObject();
            in.close();
            fileIn.close();
            return t;
        } catch (Exception e) {
            throw new DBAppException(e);
        }
    }

    public void saveTable(Table t) throws DBAppException {
        try {
            // Serialize the object to a file
            FileOutputStream fileOut = new FileOutputStream("Tables/" + t.name + ".class");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(t);
            out.close();
            fileOut.close();
            t = null;
            System.gc();
        } catch (IOException e) {
            throw new DBAppException(e);
        }
    }

    public void processUpdate(String sql) throws DBAppException {
        CharStream input = CharStreams.fromString(sql);
        gLexer t = new gLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(t);
        gParser parser = new gParser(tokens);
        ParseTree tree = parser.updateStatement();
        ParseTree set = tree.getChild(3);
        System.out.println(set.getChildCount());
        Hashtable<String, Object> htblColNameVal = new Hashtable<>();
        for (int i = 0; i < set.getChildCount(); i++) {
            if (set.getChild(i).toString().contains(","))
                continue;
            ParseTree setItem = set.getChild(i);
            String col = setItem.getChild(0).getChild(0).toString();
            String op = setItem.getChild(1).toString();
            Object val = setItem.getChild(2).getChild(0).getChild(0);
            if (!op.trim().equals("=")) {
                System.out.println(op);
                System.out.println("ERROR");
                return;
            }
            htblColNameVal.put(col, val);
        }
        ParseTree condition = tree.getChild(5).getChild(0);
        System.out.println("-----------------------------------------------------------");
        String op = condition.getChild(1).getChild(0).toString();
        System.out.println(op + "FJPFP");
        if (!op.trim().equals("=")) {
            System.out.println(op);
            System.out.println("ERROR");
            return;
        }
        String pkName = condition.getChild(0).getChild(0).getChild(0).toString();
        String tableName = tree.getChild(1).getChild(0).toString();
        System.out.println(pkName);
        System.out.println(tableName);
        //Table table = getTable("aa");
//        if (!table.pk.equals(pkName)){
//            System.out.println("ERROR");
//            return;
//        }
        String pkVal = condition.getChild(2).getChild(0).getChild(0).toString();

    }
//
    public void processInsert(String sql) throws DBAppException {
        CharStream input = CharStreams.fromString(sql);
        gLexer t = new gLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(t);
        gParser parser = new gParser(tokens);
        ParseTree tree = parser.insertStatement();
        ParseTree columns = tree.getChild(4);
        ParseTree values = tree.getChild(8);
        if (columns.getChildCount() != values.getChildCount()) {
            System.out.println(columns.getChildCount());
            System.out.println(values.getChildCount());
            System.out.println("ERROR !!!!!!!!!!!!!!!!!");
            return;
        }
        String tableName = tree.getChild(2).getChild(0).toString();
        System.out.println(tableName);
        Hashtable<String, Object> htblColNameValue = new Hashtable<>();
        for (int i = 0; i < values.getChildCount(); i++) {
            if (values.getChild(i).toString().contains(","))
                continue;
            String column = columns.getChild(i).getChild(0).toString();
            Object value = values.getChild(i).getChild(0).getChild(0);
            //System.out.println("Column" + i + ":" + column);
            //System.out.println("Value" + i + ": " + value);
            htblColNameValue.put(column, value);
        }
        for (String col : htblColNameValue.keySet()) {
            System.out.println(col + "  " + htblColNameValue.get(col));
//        System.out.println(htblColNameValue.get(col).getClass().getSimpleName());
        }
        Hashtable<String, Object> htblVal = retValues(htblColNameValue , tableName);
        this.insertIntoTable(tableName , htblVal);


    }

    private Hashtable<String, Object> retValues(Hashtable<String, Object> htblColNameValue, String tableName) throws DBAppException {
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(tableName); //type max min pk
        Hashtable<String , Object> res = new Hashtable<>();
        for (String col : htblColNameValue.keySet()){
            if (dataOfTable.get(0).get(col).equalsIgnoreCase("java.lang.string")){
                res.put(col , ((TerminalNodeImpl)htblColNameValue.get(col)).getText());
            }
            else if (dataOfTable.get(0).get(col).equalsIgnoreCase("java.lang.integer")){
                res.put(col , Integer.parseInt(((TerminalNodeImpl)htblColNameValue.get(col)).getText()) );
            }
            else if (dataOfTable.get(0).get(col).equalsIgnoreCase("java.lang.double")){
                res.put(col ,  Double.parseDouble(((TerminalNodeImpl)htblColNameValue.get(col)).getText()));
            }
            else {
                String dateText = ((TerminalNodeImpl) htblColNameValue.get(col)).getText();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Specify the desired date format
                Date date = null;

                try {
                    date = dateFormat.parse(dateText);
                    htblColNameValue.put(col , date);
                    // Use the 'date' object as needed
                } catch (ParseException e) {
                    // Handle the exception if the text cannot be parsed into a valid date
                }
            }
        }
        return res;
    }

    //
    public void processDelete(String sql) throws DBAppException {
        CharStream input = CharStreams.fromString(sql);
        gLexer t = new gLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(t);
        gParser parser = new gParser(tokens);
        ParseTree tree = parser.deleteStatement();
        ParseTree condition = tree.getChild(4);
        String tableName = tree.getChild(2).getChild(0).toString();
        Hashtable<String, Object> htblColNameValue = new Hashtable<>();
        for (int i = 0; i < condition.getChildCount(); i++) {
            if (condition.getChild(i).toString().contains(","))
                continue;
            //String name = condition.getChild(i).getChild(0).getChild(0).getChild(0).getChild(0).toString();
            //Object value = condition.getChild(i).getChild(0).getChild(2).getChild(0).getChild(0);
            String name = condition.getChild(i).getChild(0).getChild(0).getChild(0).getChild(0).toString();
            String op = condition.getChild(i).getChild(0).getChild(1).getChild(0).toString();
            Object value = condition.getChild(i).getChild(0).getChild(2).getChild(0).getChild(0);
            if (!op.trim().equals("=")) {
                System.out.println("ERROR");
                return;
            }
            htblColNameValue.put(name, value);
        }
        for (String col : htblColNameValue.keySet())
            System.out.println(col + "   " + htblColNameValue.get(col));
        this.deleteFromTable(tableName , retValues(htblColNameValue , tableName));
    }

    public void displayTable(String tableName) throws DBAppException {
        Table t = getTable(tableName);
        for (int i = 0; i < t.pageNums.size(); i++) {
            int pgNum = t.pageNums.get(i);
            Page p = t.getPageByNumber(pgNum);
            System.out.println("----------- Start of page " + i + "----------------");
            for (int j = 0; j < p.getTuples().size(); j++) {
                Hashtable<String, Object> htbl = p.getTuples().get(j).getData();
                for (String col : htbl.keySet()) {
                    System.out.println(col + ":" + htbl.get(col));
                }
                System.out.println("-------------------------------------------------");
            }
            System.out.println("----------- end of page " + i + "-----------------");
        }
    }
    public static void printOctree(String name) throws DBAppException {
        Octree t = getOctree(name);
        Octree.printOctree(t);
    }

    public static String getPK(String strTableName) throws DBAppException {
        BufferedReader br = null;
        String itemsPath = "metadata.csv";
        String pk = "";
        try {
            br = new BufferedReader(new FileReader(itemsPath));
            String line = br.readLine();
            while (line != null) {
                String[] data = line.split(",");
                String name = data[0];
                if (name.equals(strTableName)) {
                    if (data[3].equals("true"))
                        return data[1];
                }
                line = br.readLine();
            }
            br.close();
            return pk;
        } catch (Exception e) {
            throw new DBAppException(e);
        }

    }

    public void createIndex(String strTableName,
                            String[] strarrColName) throws DBAppException {
        if (strarrColName.length != 3) {
            throw new DBAppException("OCTREE MUST BE ON 3 COLUMNS");
        }
        // verify existence of columns
        verifyIndex(strarrColName, strTableName);
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        Hashtable<String, String> max = dataOfTable.get(1);
        Hashtable<String, String> min = dataOfTable.get(2);
        Hashtable<String, String> type = dataOfTable.get(0);
        for (int i = 0; i < strarrColName.length; i++) {
            if (type.get(strarrColName[i]) == null)
                throw new DBAppException("COLUMN DOESNOT EXIST");
        }
        String col1 = strarrColName[0];
        String col2 = strarrColName[1];
        String col3 = strarrColName[2];

        Object min1 = retrieveMinMax(col1, strTableName, min, type);
        Object min2 = retrieveMinMax(col2, strTableName, min, type);
        Object min3 = retrieveMinMax(col3, strTableName, min, type);
        Object max1 = retrieveMinMax(col1, strTableName, max, type);
        Object max2 = retrieveMinMax(col2, strTableName, max, type);
        Object max3 = retrieveMinMax(col3, strTableName, max, type);

        Octree octree = new Octree(min1, min2, min3, max1, max2, max3);
        String indexName = strarrColName[0] + "-" + strarrColName[1] + "-" + strarrColName[2] + "-" + strTableName;

        Table t = getTable(strTableName);
        String pk = getPK(strTableName);
        for (int i = 0; i < t.getPageNums().size(); i++) {
            Page p = t.getPageByNumber(t.getPageNums().get(i));
            for (int j = 0; j < p.getTuples().size(); j++) {
                Tuple tuple = p.getTuples().get(j);
                Object x = tuple.getData().get(col1);
                Object y = tuple.getData().get(col2);
                Object z = tuple.getData().get(col3);
                System.out.println(col1);
                System.out.println(col2);
                System.out.println(col3);
                boolean xNull = x == Values.NULL;
                boolean yNull = y == Values.NULL;
                boolean zNull = z == Values.NULL;
                if (xNull){
                    x = convertMaxToObject(col1 , strTableName);
                }
                if (yNull){
                    y = convertMaxToObject(col2 , strTableName);
                }
                if (zNull){
                    z = convertMaxToObject(col3 , strTableName);
                }
                System.out.println(x);
                System.out.println(y);
                System.out.println(z);
                OctreeReference octreeReference = new OctreeReference(t.getPageNums().get(i) + "," + tuple.getData().get(pk) + "," + xNull + "," + yNull + "," + zNull);
                octree.insert(x, y, z, octreeReference);
            }
        }
        saveOctree(octree, indexName);

        ArrayList<String> metadata = loadMetadata();
        ArrayList<String> newMetadata = new ArrayList<>();
        for (int i = 0; i < metadata.size(); i++) {
            String[] currline = metadata.get(i).split(",");
            String colName = currline[1];
            String lineToWrite = "";
            if (currline[0].equals(strTableName) && arrayContains(strarrColName, colName)) {
                currline[4] = indexName;
                currline[5] = "Octree";
                // create octTree
                lineToWrite = String.join(",", currline);
            } else {
                lineToWrite = metadata.get(i);
            }
            newMetadata.add(lineToWrite);
        }
        writeNewMetadata(newMetadata);


    }

    public void verifyIndex(String[] strarrColName, String strTableName) throws DBAppException {
        if (strarrColName[0].equals(strarrColName[1]) || strarrColName[0].equals(strarrColName[2]) || strarrColName[1].equals(strarrColName[2]))
            throw new DBAppException("CANT HAVE SAME COLUMN TWICE IN INDEX");
        ArrayList<String> indices = DBApp.getIndexNames(strTableName);
        for (int i = 0; i < indices.size(); i++) {
            String[] dets = indices.get(i).split("-");
            if (dets[0].equals(strarrColName[0]) || dets[0].equals(strarrColName[1]) || dets[0].equals(strarrColName[2])) {
                throw new DBAppException("COLUMN " + dets[0] + " ALREADY HAVE INDEX ON IT");
            }
            if (dets[1].equals(strarrColName[0]) || dets[1].equals(strarrColName[1]) || dets[1].equals(strarrColName[2])) {
                throw new DBAppException("COLUMN " + dets[1] + " ALREADY HAVE INDEX ON IT");
            }
            if (dets[2].equals(strarrColName[2]) || dets[0].equals(strarrColName[1]) || dets[2].equals(strarrColName[2])) {
                throw new DBAppException("COLUMN " + dets[2] + " ALREADY HAVE INDEX ON IT");
            }
        }
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        if (!dataOfTable.get(0).keySet().contains(strarrColName[0]))
            throw new DBAppException("COLUMN " + strarrColName[0] + " DOESNT EXIST IN THIS TABLE");
        if (!dataOfTable.get(0).keySet().contains(strarrColName[1]))
            throw new DBAppException("COLUMN " + strarrColName[1] + " DOESNT EXIST IN THIS TABLE");
        if (!dataOfTable.get(0).keySet().contains(strarrColName[2]))
            throw new DBAppException("COLUMN " + strarrColName[2] + " DOESNT EXIST IN THIS TABLE");


    }


    private Object retrieveMinMax(String col, String strTableName, Hashtable<String, String> minOrMax, Hashtable<String, String> type) throws DBAppException {
        if (type.get(col).toLowerCase().equals("java.lang.integer")) {
            int minimum = Integer.parseInt(minOrMax.get(col));
            return minimum;
        }
        if (type.get(col).toLowerCase().equals("java.lang.double")) {
            return Double.parseDouble(minOrMax.get(col));
        }
        if (type.get(col).toLowerCase().equals("java.lang.string")) {
            return minOrMax.get(col);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = dateFormat.parse(minOrMax.get(col));
                return date;
            } catch (Exception e) {
                throw new DBAppException(e);
            }
        }
    }

    private void writeNewMetadata(ArrayList<String> newMetadata) throws DBAppException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        String filename = "metadata.csv";
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write("");
            fileWriter.close();
        } catch (Exception e) {
            throw new DBAppException();
        }
        try {
            fw = new FileWriter(filename, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            for (int i = 0; i < newMetadata.size(); i++) {
                pw.println(newMetadata.get(i));
                pw.flush();
            }


        } catch (Exception e) {
            throw new DBAppException(e);
        } finally {
            try {
                pw.close();
                bw.close();
                fw.close();
            } catch (Exception e) {

                throw new DBAppException();
            }
        }
    }

    public boolean arrayContains(String[] strarrColName, String colName) {
        for (int i = 0; i < strarrColName.length; i++) {
            if (strarrColName[i].equals(colName))
                return true;
        }
        return false;
    }

    public static ArrayList<String> loadMetadata() throws DBAppException {
        ArrayList<String> res = new ArrayList<>();
        BufferedReader br = null;
        String itemsPath = "metadata.csv";
        try {
            br = new BufferedReader(new FileReader(itemsPath));
            String line = br.readLine();
            while (line != null) {
                res.add(line);
                line = br.readLine();
            }
            br.close();
            return res;
        } catch (Exception e) {
            throw new DBAppException(e);
        }
    }

    public static void saveOctree(Octree t, String name) throws DBAppException {
        try {
            // Serialize the object to a file
            FileOutputStream fileOut = new FileOutputStream("Octrees/" + name + ".class");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(t);
            out.close();
            fileOut.close();
            t = null;
            System.gc();
        } catch (IOException e) {
            throw new DBAppException(e);
        }
    }

    public static Octree getOctree(String name) throws DBAppException {
        try {
            Octree t = null;
            File f = new File("Octrees/" + name + ".class");
            if (!f.exists())
                throw new DBAppException("NO SUCH OCTREE");
            FileInputStream fileIn = new FileInputStream("Octrees/" + name + ".class");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            t = (Octree) in.readObject();
            in.close();
            fileIn.close();
            return t;
        } catch (Exception e) {
            throw new DBAppException(e);
        }
    }

    public static ArrayList<String> getIndexNames(String strTableName) throws DBAppException {
        ArrayList<String> res = new ArrayList<>();
        ArrayList<String> data = loadMetadata();
        for (int i = 0; i < data.size(); i++) {
            String[] dataArr = data.get(i).split(",");
            if (dataArr[5].toLowerCase().equals("octree") && dataArr[0].equals(strTableName) && !res.contains(dataArr[4])) {
                res.add(dataArr[4]);
            }
        }

        return res;
    }

    public static ArrayList<ArrayList> checkColsContainIndex(ArrayList<String> cols, String strTableName) throws DBAppException {
        // if there is indices on columns it returns position of first column of each index
        ArrayList<String> indexNames = getIndexNames(strTableName);
        ArrayList<ArrayList<String>> columnIndexNames = new ArrayList<>();
        for (int i = 0; i < indexNames.size(); i++) {
            columnIndexNames.add(new ArrayList<>(Arrays.asList(indexNames.get(i).split("-"))));
        }
        ArrayList<Integer> res = new ArrayList<>();
        ArrayList<String> indices = new ArrayList<>();
        for (int i = 0; i < cols.size() - 2; i++) {
            String colName = cols.get(i);
            for (int j = 0; j < columnIndexNames.size(); j++) {
                if (columnIndexNames.get(j).contains(colName)) {
                    if (columnIndexNames.get(j).contains(cols.get(i + 1)) && columnIndexNames.get(j).contains(cols.get(i + 2))) {
                        res.add(i);
                        indices.add(String.join("-", columnIndexNames.get(j)));
                        i += 2;
                    }
                }
            }

        }
        ArrayList<ArrayList> result = new ArrayList<>();
        result.add(res);
        result.add(indices);
        return result;
    }

    public Iterator selectFromTable(SQLTerm[] arrSQLTerms,
                                    String[] strarrOperators)
            throws DBAppException {
        if (arrSQLTerms.length == 0) {
            throw new DBAppException("YOU MUST ADD arrSQLTERMS to identify table name");
        }
        if (strarrOperators.length != arrSQLTerms.length - 1)
            throw new DBAppException("OPERATORS MUST BE OF LENGTH LESS THAN TERMS BY 1");
        ArrayList<String> colNames = new ArrayList<>();
        for (int i = 0; i < arrSQLTerms.length; i++) {
            colNames.add(arrSQLTerms[i]._strColumnName);
        }
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(arrSQLTerms[0]._strTableName); //type max min pk
        ArrayList<Tuple> result = new ArrayList<>();
        ArrayList<ArrayList> indices = checkColsContainIndex(colNames, arrSQLTerms[0]._strTableName);
        ArrayList<Integer> indicesStart = indices.get(0);
        ArrayList<String> indicesName = indices.get(1);
        boolean onlyAnd = true;
        for (int i = 0; i < strarrOperators.length; i++) {
            if (strarrOperators[i].toLowerCase().equals("or") || strarrOperators[i].toLowerCase().equals("xor")) {
                onlyAnd = false;
                break;
            }
        }
        if (onlyAnd) {
            ArrayList<Integer> done = new ArrayList<>();
            ArrayList<ArrayList<Tuple>> doneUsingIndex = new ArrayList<>();
            ArrayList<OctPoint> res = new ArrayList<>();
            for (int i = 0; i < indicesStart.size(); i++) {
                int startIndex = indicesStart.get(i);
                if (strarrOperators[startIndex].toLowerCase().equals("and") && strarrOperators[startIndex + 1].toLowerCase().equals("and")) {
                    done.add(startIndex);
                    Octree t = getOctree(indicesName.get(i));
                    String[] arr = {arrSQLTerms[startIndex]._strColumnName, arrSQLTerms[startIndex + 1]._strColumnName, arrSQLTerms[startIndex + 2]._strColumnName};
                    int[] indexOrder = getOrderOfIndex(indicesName.get(i), arr);
                    if (arrSQLTerms[startIndex + indexOrder[0]]._objValue == Values.NULL) {
                        arrSQLTerms[startIndex + indexOrder[0]]._objValue = convertMaxToObject(arrSQLTerms[startIndex + indexOrder[0]]._strColumnName , arrSQLTerms[0]._strTableName);
                    }
                    if (arrSQLTerms[startIndex + indexOrder[1]]._objValue == Values.NULL) {
                        arrSQLTerms[startIndex + indexOrder[1]]._objValue = convertMaxToObject(arrSQLTerms[startIndex + indexOrder[1]]._strColumnName , arrSQLTerms[0]._strTableName);
                    }
                    if (arrSQLTerms[startIndex + indexOrder[2]]._objValue == Values.NULL) {
                        arrSQLTerms[startIndex + indexOrder[2]]._objValue = convertMaxToObject(arrSQLTerms[startIndex + indexOrder[2]]._strColumnName , arrSQLTerms[0]._strTableName);
                    }
                    String[] ops = {arrSQLTerms[startIndex + indexOrder[0]]._strOperator, arrSQLTerms[startIndex + indexOrder[1]]._strOperator, arrSQLTerms[startIndex + indexOrder[2]]._strOperator};
                    res.addAll(t.getRange(arrSQLTerms[startIndex + indexOrder[0]]._objValue, arrSQLTerms[startIndex + indexOrder[1]]._objValue, arrSQLTerms[startIndex + indexOrder[2]]._objValue, ops));
                    if (arrSQLTerms[startIndex + indexOrder[1]]._objValue == Values.NULL ||
                            arrSQLTerms[startIndex + indexOrder[0]]._objValue == Values.NULL ||
                            arrSQLTerms[startIndex + indexOrder[2]]._objValue == Values.NULL) {
                        boolean xNull = arrSQLTerms[startIndex + indexOrder[0]]._objValue == Values.NULL;
                        boolean yNull = arrSQLTerms[startIndex + indexOrder[1]]._objValue == Values.NULL;
                        boolean zNull = arrSQLTerms[startIndex + indexOrder[2]]._objValue == Values.NULL;
                        if (xNull && !arrSQLTerms[startIndex + indexOrder[0]]._strOperator.equals("="))
                            throw new DBAppException("CAN ONLY EVALUATE EXPRESSIONS WITH = NULL");
                        if (yNull && !arrSQLTerms[startIndex + indexOrder[1]]._strOperator.equals("="))
                            throw new DBAppException("CAN ONLY EVALUATE EXPRESSIONS WITH = NULL");
                        if (zNull && !arrSQLTerms[startIndex + indexOrder[2]]._strOperator.equals("="))
                            throw new DBAppException("CAN ONLY EVALUATE EXPRESSIONS WITH = NULL");
                        for (int m = 0; m < res.size(); m++) {
                            OctPoint p = res.get(m);
                            String ref = p.getRef().getReference();
                            ArrayList<String> refs = p.getRef().getDuplicates();
                            if (!(ref.split(",")[2].toLowerCase().equals((xNull + "").toLowerCase()) ||
                                    ref.split(",")[3].toLowerCase().equals((yNull + "").toLowerCase()) ||
                                    ref.split(",")[4].toLowerCase().equals((zNull + "").toLowerCase()))) {
                                ref = null;
                                for (int n = 0; n < refs.size(); n++) {
                                    String tmpref = refs.get(n);
                                    if (tmpref.split(",")[2].toLowerCase().equals((xNull + "").toLowerCase()) ||
                                            tmpref.split(",")[3].toLowerCase().equals((yNull + "").toLowerCase()) ||
                                            tmpref.split(",")[4].toLowerCase().equals((zNull + "").toLowerCase())) {
                                        if (ref == null) {
                                            p.getRef().setReference(tmpref);
                                            refs.remove(n);
                                            n--;
                                            break;
                                        } else continue;
                                    } else {
                                        refs.remove(n);
                                        n--;
                                        if (refs.size() == 0 && ref == null) {
                                            res.remove(m);
                                            m--;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                    }
                    doneUsingIndex.add(convertOctPointtoTuple(res, arrSQLTerms[0]._strTableName));
                    break;
                }
            }
            int startingIndex;
            int index = 0;
            if (done.contains(0)) {
                result = doneUsingIndex.get(0);
                index++;
                startingIndex = 3;
            } else if (done.size() != 0) {
                result = doneUsingIndex.get(0);
                startingIndex = 0;
            } else {

                result = getMatchingTuples(arrSQLTerms[0].get_strColumnName(), arrSQLTerms[0]._strOperator, arrSQLTerms[0]._objValue, arrSQLTerms[0]._strTableName);
                startingIndex = 1;
            }
            for (int i = startingIndex; i < arrSQLTerms.length; i++) {
                ArrayList<Tuple> matchingTuples;
                if (done.contains(i)) {
                    i += 2;
                    continue;
                }
                matchingTuples = getMatchingTuples(arrSQLTerms[i].get_strColumnName(), arrSQLTerms[i]._strOperator, arrSQLTerms[i]._objValue, arrSQLTerms[i]._strTableName);
                if (strarrOperators[i - 1].toLowerCase().equals("and")) {
                    if (strarrOperators[i - 1].toLowerCase().equals("and"))
                        result = andLists(result, matchingTuples, arrSQLTerms[i]._strTableName);
                }

            }
            return result.iterator();
        } else {
            result = getMatchingTuples(arrSQLTerms[0].get_strColumnName(), arrSQLTerms[0]._strOperator, arrSQLTerms[0]._objValue, arrSQLTerms[0]._strTableName);
            for (int i = 1; i < arrSQLTerms.length; i++) {
                ArrayList<Tuple> matchingTuples = getMatchingTuples(arrSQLTerms[i].get_strColumnName(), arrSQLTerms[i]._strOperator, arrSQLTerms[i]._objValue, arrSQLTerms[i]._strTableName);
                if (strarrOperators[i - 1].toLowerCase().equals("and")) {
                    result = andLists(result, matchingTuples, arrSQLTerms[i]._strTableName);
                } else if (strarrOperators[i - 1].toLowerCase().equals("or")) {
                    result = OrLists(result, matchingTuples, arrSQLTerms[i]._strTableName);

                } else if (strarrOperators[i - 1].toLowerCase().equals("xor")) {
                    result = XOrLists(result, matchingTuples, arrSQLTerms[i]._strTableName);
                }
            }
            return result.iterator();
        }


    }

    public ArrayList<Tuple> andingLists(ArrayList<Tuple> result, SQLTerm arrSQLTerm, String strTableName) throws DBAppException {
        for (int i = 0; i < result.size(); i++) {
            Tuple t = result.get(i);
            System.out.println(t.getData().get(arrSQLTerm._strColumnName) + "    " + arrSQLTerm._objValue);
            if (compareObjects(t.getData().get(arrSQLTerm._strColumnName), arrSQLTerm._objValue) != 0) {
                result.remove(i);
                i--;
            }
        }
        return result;
    }


    public static ArrayList<Tuple> convertOctPointtoTuple(ArrayList<OctPoint> res, String strTableName) throws DBAppException {
        Table t = getTable(strTableName);
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        String pk = getPK(t.name);
        String pkType = dataOfTable.get(0).get(pk);
        ArrayList<Tuple> result = new ArrayList<>();
        for (int i = 0; i < res.size(); i++) {
            String ref = res.get(i).getRef().getReference();
            Tuple tuple = convertRefToTuple(ref, pkType, t, pk);
            result.add(tuple);
            for (int j = 0; j < res.get(i).getRef().getDuplicates().size(); j++) {
                Tuple tup = convertRefToTuple(res.get(i).getRef().getDuplicates().get(j), pkType, t, pk);
                result.add(tup);
            }
        }
        return result;
    }

    public static Tuple convertRefToTuple(String ref, String pkType, Table t, String pk) throws DBAppException {
        if (pkType.toLowerCase().equals("java.lang.integer")) {
            int pkVal = Integer.parseInt(ref.split(",")[1]);
            Hashtable<String, Object> htbl = new Hashtable<>();
            htbl.put(pk, pkVal);
            Tuple tuple = new Tuple(htbl, t.name);
            Page p = t.getPageByTuple(tuple);
            int index = t.binSearchRecs(p, tuple);
            return p.getTuples().get(index);
        } else if (pkType.toLowerCase().equals("java.lang.double")) {
            double pkVal = Double.parseDouble(ref.split(",")[1]);
            Hashtable<String, Object> htbl = new Hashtable<>();
            htbl.put(pk, pkVal);
            Tuple tuple = new Tuple(htbl, t.name);
            Page p = t.getPageByTuple(tuple);
            int index = t.binSearchRecs(p, tuple);
            return p.getTuples().get(index);
        } else if (pkType.toLowerCase().equals("java.lang.string")) {
            String pkVal = ref.split(",")[1];
            Hashtable<String, Object> htbl = new Hashtable<>();
            htbl.put(pk, pkVal);
            Tuple tuple = new Tuple(htbl, t.name);
            Page p = t.getPageByTuple(tuple);
            int index = t.binSearchRecs(p, tuple);
            return p.getTuples().get(index);
        } else {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date pkVal = dateFormat.parse(ref.split(",")[1]);
                Hashtable<String, Object> htbl = new Hashtable<>();
                htbl.put(pk, pkVal);
                Tuple tuple = new Tuple(htbl, t.name);
                Page p = t.getPageByTuple(tuple);
                int index = t.binSearchRecs(p, tuple);
                return p.getTuples().get(index);
            } catch (Exception e) {
                throw new DBAppException(e);
            }
        }
    }

    public int[] getOrderOfIndex(String indexName, String[] cols) {
        String[] correctOrder = indexName.split("-");
        int[] res = new int[3];
        int index = 0;
        // id name gpa
        // name gpa id  >> 1 0 2
        for (int i = 0; i < cols.length; i++) {
            if (cols[0].equals(correctOrder[i]))
                res[index] = 0;
            else if (cols[1].equals(correctOrder[i]))
                res[index] = 1;
            else if (cols[2].equals(correctOrder[i]))
                res[index] = 2;
            index++;
        }

        return res;
    }

    public ArrayList<Tuple> getMatchingTuples(String colName, String operator, Object value, String strTableName) throws DBAppException {
        ArrayList<Tuple> res = new ArrayList<>();
        Table t = getTable(strTableName);
        String pk = getPK(strTableName);
        if (colName.equals(pk)) {
            if (operator.equals("=")) {
                Hashtable<String, Object> htbl = new Hashtable<>();
                htbl.put(pk, value);
                Tuple tuple = new Tuple(htbl, strTableName);
                Page p = t.getPageByTuple(tuple);
                if (p == null) {
                    return new ArrayList<>();
                }
                int index = t.binSearchRecs(p, tuple);
                if (index == -1) {
                    return new ArrayList<>();
                }
                Tuple tup = p.getTuples().get(index);
                ArrayList<Tuple> result = new ArrayList<>();
                result.add(tup);
                return result;
            } else {
                for (int i = 0; i < t.getPageNums().size(); i++) {
                    Page p = t.getPageByNumber(t.getPageNums().get(i));
                    boolean cont = true;
                    for (int j = 0; j < p.getTuples().size(); j++) {
                        Tuple tuple = p.getTuples().get(j);
                        Object x = tuple.getData().get(colName);
                        if (operator.equals("!=")) {
                            if (compareObjects(x, value) != 0)
                                res.add(tuple);
                        } else if (operator.equals("<")) {
                            if (compareObjects(x, value) < 0)
                                res.add(tuple);
                        } else if (operator.equals("<=")) {
                            if (compareObjects(x, value) <= 0)
                                res.add(tuple);
                        } else if (operator.equals(">")) {
                            if (compareObjects(x, value) > 0)
                                res.add(tuple);
                        } else if (operator.equals(">=")) {
                            if (compareObjects(x, value) >= 0)
                                res.add(tuple);
                        }
                    }
                }
            }
            return res;
        }
        for (int i = 0; i < t.getPageNums().size(); i++) {
//            System.out.println(colName);
            Page p = t.getPageByNumber(t.getPageNums().get(i));
            for (int j = 0; j < p.getTuples().size(); j++) {
                Tuple tuple = p.getTuples().get(j);
                Object x = tuple.getData().get(colName);
                if (x== Values.NULL)
                    continue;
                if (operator.equals("=")) {
                    if (compareObjects(x, value) == 0)
                        res.add(tuple);
                } else if (operator.equals("!=")) {
                    if (x == Values.NULL){
                        res.add(tuple);
                    }
                    else if (compareObjects(x, value) != 0)
                        res.add(tuple);
                } else if (operator.equals("<")) {
                    if (compareObjects(x, value) < 0)
                        res.add(tuple);
                } else if (operator.equals("<=")) {
                    if (compareObjects(x, value) <= 0)
                        res.add(tuple);
                } else if (operator.equals(">")) {
                    if (compareObjects(x, value) > 0)
                        res.add(tuple);
                } else if (operator.equals(">="))
                    if (compareObjects(x, value) >= 0)
                        res.add(tuple);
            }
        }

        return res;
    }

    public static int compareObjects(Object o1, Object o2) throws DBAppException {
        if (!o1.getClass().getSimpleName().equals(o2.getClass().getSimpleName())) {
            System.out.println(o1.getClass().getSimpleName());
            System.out.println(o2.getClass().getSimpleName());
            throw new DBAppException("INCOMPATIBLE DATA TYPES");
        }
        if (o1 instanceof String) {
            return ((String) o1).compareTo((String) o2);
        } else if (o1 instanceof Double) {
            return ((Double) o1).compareTo((Double) o2);
        } else if (o1 instanceof Integer) {
            return ((Integer) o1).compareTo((Integer) o2);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date d1 = dateFormat.parse(o1 + "");
                Date d2 = dateFormat.parse(o2 + "");
                return d1.compareTo(d2);
            } catch (Exception e) {
                throw new DBAppException(e);
            }
        }
    }

    public ArrayList<Tuple> OrLists(ArrayList<Tuple> result, ArrayList<Tuple> matchingTuples, String strTableName) throws DBAppException {
        String pk = getPK(strTableName);
        Hashtable<Object, Tuple> temp = new Hashtable<>();
        ArrayList<Tuple> res = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            temp.put(result.get(i).getData().get(pk), result.get(i));
            res.add(result.get(i));
        }
        for (int i = 0; i < matchingTuples.size(); i++) {
            if (temp.containsKey(matchingTuples.get(i).getData().get(pk)))
                continue;
            res.add(matchingTuples.get(i));
        }
        return res;
    }

    public ArrayList<Tuple> XOrLists(ArrayList<Tuple> result, ArrayList<Tuple> matchingTuples, String strTableName) throws DBAppException {
        String pk = getPK(strTableName);
        ArrayList<Tuple> res = new ArrayList<>();
        Hashtable<Object, Tuple> temp = new Hashtable<>();
        for (int i = 0; i < result.size(); i++) {
            temp.put(result.get(i).getData().get(pk), result.get(i));
//            res.add(result.get(i));
        }
        for (int i = 0; i < matchingTuples.size(); i++) {
            if (temp.containsKey(matchingTuples.get(i).getData().get(pk))) {
//                res.add(temp.get(i));
                temp.remove(matchingTuples.get(i).getData().get(pk));
                continue;
            }
            temp.put(matchingTuples.get(i).getData().get(pk), matchingTuples.get(i));
        }
        for (Object col : temp.keySet())
            res.add(temp.get(col));
        return res;
    }

    private ArrayList<Tuple> andLists(ArrayList<Tuple> result, ArrayList<Tuple> matchingTuples, String strTableName) throws DBAppException {
        String pk = getPK(strTableName);
        Hashtable<Object, Tuple> temp = new Hashtable<>();
        for (int i = 0; i < result.size(); i++) {
            temp.put(result.get(i).getData().get(pk), result.get(i));
        }
        ArrayList<Tuple> res = new ArrayList<>();
        for (int i = 0; i < matchingTuples.size(); i++) {
            if (temp.containsKey(matchingTuples.get(i).getData().get(pk)))
                res.add(temp.get(matchingTuples.get(i).getData().get(pk)));
        }
        return res;
    }

    public Iterator parseSQL(StringBuffer strbufSQL) throws
            DBAppException {
        String sql = strbufSQL.toString();
        if (sql.split(" ")[0].equalsIgnoreCase("insert")){
            processInsert(sql);
            return new ArrayList<>().iterator();
        }
        if (sql.split(" ")[0].equalsIgnoreCase("update")){
            processUpdate(sql);
            return new ArrayList<>().iterator();
        }
        if (sql.split(" ")[0].equalsIgnoreCase("delete")){
            processDelete(sql);
            return new ArrayList<>().iterator();
        }

        CharStream input = CharStreams.fromString(sql);
        gLexer t = new gLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(t);
        gParser parser = new gParser(tokens);
        ParseTree tree = parser.selectStatement();
        ParseTree set = tree.getChild(5);
        ArrayList<String> operators = new ArrayList<>();
        ArrayList<SQLTerm> sqlTerms = new ArrayList<>();
        String name = tree.getChild(3).getChild(0).toString();
        System.out.println(set.getChildCount());
        Hashtable<String, Object> htblColNameVal = new Hashtable<>();
        for (int i = 0; i < set.getChildCount(); i++) {
            if (set.getChild(i).getChild(0).toString().toLowerCase().contains("and")) {
                operators.add("and");
                continue;

            }
            if (set.getChild(i).getChild(0).toString().toLowerCase().contains("or")) {
                operators.add("or");
                continue;
            }
            if (set.getChild(i).getChild(0).toString().toLowerCase().contains("xor")) {
                operators.add("xor");
                continue;
            }
            ParseTree setItem = set.getChild(i).getChild(0);
            String colName = setItem.getChild(0).getChild(0).getChild(0).toString();
            String operator = setItem.getChild(1).getChild(0).toString();
            Object value;
            if (setItem.getChild(2).toString().toLowerCase().trim().equals("null")) {
                System.out.println("HI");
                value = Values.NULL;
            } else {
                String val = setItem.getChild(2).getChild(0).getChild(0).toString().trim();
                value = parseHelper(val, colName, name);
            }

            SQLTerm sqlTerm = new SQLTerm(name.trim(), colName.trim(), operator.trim(), value);
            sqlTerms.add(sqlTerm);
        }

        return this.selectFromTable(sqlTerms.toArray(new SQLTerm[sqlTerms.size()]), operators.toArray(new String[operators.size()]));

    }

    public static Object parseHelper(String val, String colName, String strTableName) throws DBAppException {
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        String type = dataOfTable.get(0).get(colName);
        if (type.toLowerCase().equals("java.lang.string")) {
            return val;
        }
        if (type.toLowerCase().equals("java.lang.integer"))
            return Integer.parseInt(val);
        if (type.toLowerCase().equals("java.lang.double"))
            return Double.parseDouble(val);
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return dateFormat.parse(val);
            } catch (Exception e) {
                throw new DBAppException(e);
            }
        }


    }

    public static String convertDateToString(Date d){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(d);
        return dateString;
    }

    public static Object convertMaxToObject(String colName , String strTableName) throws DBAppException {
        ArrayList<Hashtable<String, String>> dataOfTable = readDataOfTable(strTableName); //type max min pk
        if (dataOfTable.get(0).get(colName).toLowerCase().equals("java.lang.string")){
            return dataOfTable.get(1).get(colName);
        }
        else if (dataOfTable.get(0).get(colName).toLowerCase().equals("java.lang.integer")){
            return Integer.parseInt(dataOfTable.get(1).get(colName));
        }
        else if (dataOfTable.get(0).get(colName).toLowerCase().equals("java.lang.double")){
            return Double.parseDouble(dataOfTable.get(1).get(colName));
        }
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date d = dateFormat.parse(dataOfTable.get(1).get(colName));
                return d;
            } catch (Exception e) {
                throw new DBAppException(e);
            }
        }
    }

}
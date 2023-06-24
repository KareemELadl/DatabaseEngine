import java.util.Hashtable;
import java.util.regex.*;

public class MyParser {
    private String[] inp;
    private String inpStr;

    public MyParser(String input) throws ParserException {
        this.inp = input.split(" ");
        this.inpStr = input;
        String type = this.inp[0].toLowerCase();

        if (!(type.equals("select") || type.equals("insert") || type.equals("update") || type.equals("delete"))){
            throw new ParserException("Unsupported Statement type");
        }
    }
    public String getTableNameInsert(){
        return inp[2];
    }
    public Hashtable<String,String> getInsertKeyValues(){
        Hashtable<String,String> htblcolnamevalue = new Hashtable<>();
        Pattern pattern = Pattern.compile("\\s*insert\\s+into\\s+([a-zA-Z_]+)\\s*\\(((\\s*[a-zA-Z_]+\\s*,?\\s*)+)\\)\\s*values\\s*\\((\\s*('[^']*'|\\d+)\\s*,?\\s*)+\\)\\s*;");
        Matcher matcher = pattern.matcher(inpStr.toLowerCase());

        if (matcher.matches()) {
            String tableName = matcher.group(1);
            String cols = matcher.group(2);
            System.out.println(cols);
            System.out.println(matcher.group(3));
            // do something with columns and tables
        }
        return htblcolnamevalue;
    }

    public static void main(String[] args) throws ParserException {
        MyParser m = new MyParser("INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country) VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21, egypt', 'Stavanger', '4006', 'Norway');");
        System.out.println(m.getTableNameInsert());
        Hashtable<String ,String > htbl = m.getInsertKeyValues();
        for (String s : htbl.keySet())
            System.out.println(s + " " + htbl.get(s));
    }

}

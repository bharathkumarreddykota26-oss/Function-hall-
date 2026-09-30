/*
 * HALLBOOK - Java JDBC backend
 *
 * DATABASE:
 *   Run database.sql in MySQL first.
 *
 * REQUIRED LIBRARY:
 *   MySQL Connector/J
 *
 * IMPORTANT:
 *   Change DB_PASSWORD before running.
 *
 * This learning server exposes:
 *   GET  /api/halls
 *   GET  /api/chairs
 *   GET  /api/decorations
 *   GET  /api/food?type=VEG
 *   GET  /api/bookings
 *   POST /api/bookings
 *
 * It stores booking + booking items + payment record using JDBC.
 */
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class BookingServer {
    static final String DB_URL = "jdbc:mysql://localhost:3306/hallbook?serverTimezone=Asia/Kolkata";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
    static final int PORT = 8080;

    static Connection db() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    static void send(HttpExchange ex, int code, String json) throws IOException {
        byte[] out=json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin","*");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods","GET,POST,OPTIONS");
        ex.sendResponseHeaders(code,out.length);
        try(OutputStream os=ex.getResponseBody()){os.write(out);}
    }

    static String body(HttpExchange ex)throws IOException{
        return new String(ex.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
    }

    // Simple JSON string/number reader for this learning project.
    // For production applications use Jackson/Gson and validation.
    static String value(String json,String key){
        String k="\""+key+"\":";
        int p=json.indexOf(k);
        if(p<0)return "";
        p+=k.length();
        while(p<json.length() && Character.isWhitespace(json.charAt(p)))p++;
        if(p<json.length() && json.charAt(p)=='"'){
            int e=p+1;
            while(e<json.length()){
                if(json.charAt(e)=='"' && json.charAt(e-1)!='\\') return json.substring(p+1,e);
                e++;
            }
            return "";
        }
        int e=p;
        while(e<json.length() && json.charAt(e)!=',' && json.charAt(e)!='}')e++;
        return json.substring(p,e).trim();
    }

    static String escape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }

    static String query(String sql) throws SQLException {
        StringBuilder j=new StringBuilder("[");
        try(Connection c=db(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()){
            ResultSetMetaData m=r.getMetaData();
            int cols=m.getColumnCount();
            boolean first=true;
            while(r.next()){
                if(!first)j.append(",");
                first=false;j.append("{");
                for(int i=1;i<=cols;i++){
                    if(i>1)j.append(",");
                    String name=m.getColumnLabel(i);
                    Object v=r.getObject(i);
                    j.append("\"").append(escape(name)).append("\":");
                    if(v==null)j.append("null");
                    else if(v instanceof Number)j.append(v);
                    else j.append("\"").append(escape(v.toString())).append("\"");
                }
                j.append("}");
            }
        }
        return j.append("]").toString();
    }

    static void getData(HttpExchange ex,String path,String query) throws Exception{
        if(path.equals("/api/halls")){
            send(ex,200,query("SELECT id,name,description,capacity,price FROM function_halls WHERE active=TRUE ORDER BY id")); return;
        }
        if(path.equals("/api/chairs")){
            send(ex,200,query("SELECT id,name,description,quantity,price FROM chair_packages WHERE active=TRUE ORDER BY id")); return;
        }
        if(path.equals("/api/decorations")){
            send(ex,200,query("SELECT id,name,description,price FROM decorations WHERE active=TRUE ORDER BY id")); return;
        }
        if(path.equals("/api/food")){
            String type="VEG";
            if(query!=null && query.contains("type=")) type=query.substring(query.indexOf("type=")+5).toUpperCase();
            if(!type.equals("VEG")&&!type.equals("NON_VEG"))type="VEG";
            try(Connection c=db();PreparedStatement p=c.prepareStatement(
                "SELECT id,name,food_type,description,price FROM food_items WHERE active=TRUE AND food_type=? ORDER BY id")){
                p.setString(1,type);
                try(ResultSet r=p.executeQuery()){
                    StringBuilder j=new StringBuilder("[");boolean first=true;
                    while(r.next()){
                        if(!first)j.append(",");first=false;
                        j.append("{\"id\":").append(r.getInt("id"))
                         .append(",\"name\":\"").append(escape(r.getString("name")))
                         .append("\",\"food_type\":\"").append(r.getString("food_type"))
                         .append("\",\"description\":\"").append(escape(r.getString("description")))
                         .append("\",\"price\":").append(r.getBigDecimal("price")).append("}");
                    }
                    send(ex,200,j.append("]").toString());
                }
            }
            return;
        }
        if(path.equals("/api/bookings")){
            send(ex,200,query("SELECT booking_id,customer_name,phone,event_date,payment_method,total,status,created_at FROM bookings ORDER BY id DESC")); return;
        }
        send(ex,404,"{\"message\":\"API endpoint not found\"}");
    }

    static void createBooking(HttpExchange ex)throws Exception{
        String j=body(ex);
        String name=value(j,"customerName"),phone=value(j,"phone"),date=value(j,"eventDate"),
               payment=value(j,"payment"),totalText=value(j,"total");
        if(name.isBlank()||phone.isBlank()||date.isBlank()||payment.isBlank()){
            send(ex,400,"{\"message\":\"Name, phone, date and payment are required\"}");return;
        }
        if(!payment.equals("UPI")&&!payment.equals("CARD")&&!payment.equals("CASH")){
            send(ex,400,"{\"message\":\"Invalid payment method\"}");return;
        }
        java.math.BigDecimal total;
        try{total=new java.math.BigDecimal(totalText);}catch(Exception e){
            send(ex,400,"{\"message\":\"Invalid total\"}");return;
        }

        String bid="HB-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
        try(Connection c=db()){
            c.setAutoCommit(false);
            try{
                int bookingDbId;
                String insert="INSERT INTO bookings(booking_id,customer_name,phone,event_date,payment_method,total,status) VALUES(?,?,?,?,?,?,?)";
                try(PreparedStatement p=c.prepareStatement(insert,Statement.RETURN_GENERATED_KEYS)){
                    p.setString(1,bid);p.setString(2,name);p.setString(3,phone);p.setDate(4,Date.valueOf(date));
                    p.setString(5,payment);p.setBigDecimal(6,total);p.setString(7,"CONFIRMED");p.executeUpdate();
                    try(ResultSet keys=p.getGeneratedKeys()){keys.next();bookingDbId=keys.getInt(1);}
                }

                // Store item rows. The frontend sends an "items" array.
                // We parse only the fields needed for this educational project.
                String items=extractArray(j,"items");
                for(String obj:splitObjects(items)){
                    String itemName=value(obj,"name"), price=value(obj,"price"), qty=value(obj,"qty");
                    if(itemName.isBlank())continue;
                    try(PreparedStatement p=c.prepareStatement(
                        "INSERT INTO booking_items(booking_id,item_type,item_name,unit_price,quantity) VALUES(?,?,?,?,?)")){
                        p.setInt(1,bookingDbId);
                        p.setString(2,"FOOD"); // Frontend cart is shared; production code should send item type.
                        p.setString(3,itemName);
                        p.setBigDecimal(4,new java.math.BigDecimal(price));
                        p.setInt(5,Integer.parseInt(qty));
                        p.executeUpdate();
                    }
                }

                try(PreparedStatement p=c.prepareStatement(
                    "INSERT INTO payments(booking_id,payment_reference,amount,method,status,paid_at) VALUES(?,?,?,?,?,?)")){
                    p.setInt(1,bookingDbId);
                    p.setString(2,"DEMO-"+bid);
                    p.setBigDecimal(3,total);
                    p.setString(4,payment);
                    p.setString(5,payment.equals("CASH")?"PENDING":"PAID");
                    if(payment.equals("CASH"))p.setNull(6,Types.TIMESTAMP);else p.setTimestamp(6,new Timestamp(System.currentTimeMillis()));
                    p.executeUpdate();
                }
                c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }
        send(ex,200,"{\"success\":true,\"bookingId\":\""+bid+"\",\"message\":\"Booking, items and payment saved with JDBC\"}");
    }

    static String extractArray(String json,String key){
        String k="\""+key+"\":";
        int p=json.indexOf(k);if(p<0)return "";
        p=json.indexOf("[",p);if(p<0)return "";
        int depth=0;
        for(int i=p;i<json.length();i++){
            char ch=json.charAt(i);
            if(ch=='[')depth++;
            else if(ch==']'){depth--;if(depth==0)return json.substring(p+1,i);}
        }
        return "";
    }

    static List<String> splitObjects(String a){
        List<String> out=new ArrayList<>();int depth=0,start=-1;
        for(int i=0;i<a.length();i++){
            char ch=a.charAt(i);
            if(ch=='{'){if(depth==0)start=i;depth++;}
            else if(ch=='}'){depth--;if(depth==0&&start>=0){out.add(a.substring(start,i+1));start=-1;}}
        }
        return out;
    }

    public static void main(String[] args)throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        try(Connection c=db()){System.out.println("Connected to MySQL database: hallbook");}
        HttpServer server=HttpServer.create(new InetSocketAddress(PORT),0);
        server.createContext("/api/halls",ex->route(ex));
        server.createContext("/api/chairs",ex->route(ex));
        server.createContext("/api/decorations",ex->route(ex));
        server.createContext("/api/food",ex->route(ex));
        server.createContext("/api/bookings",ex->route(ex));
        server.createContext("/api/health",ex->send(ex,200,"{\"status\":\"Java JDBC backend is running\"}"));
        server.start();
        System.out.println("Java JDBC API: http://localhost:"+PORT);
    }

    static void route(HttpExchange ex){
        try{
            if(ex.getRequestMethod().equalsIgnoreCase("OPTIONS")){send(ex,204,"");return;}
            if(ex.getRequestMethod().equalsIgnoreCase("GET")){getData(ex,ex.getRequestURI().getPath(),ex.getRequestURI().getQuery());return;}
            if(ex.getRequestMethod().equalsIgnoreCase("POST") && ex.getRequestURI().getPath().equals("/api/bookings")){createBooking(ex);return;}
            send(ex,405,"{\"message\":\"Method not allowed\"}");
        }catch(Exception e){
            try{send(ex,500,"{\"message\":\""+escape(e.getMessage()==null?"Database error":e.getMessage())+"\"}");}catch(Exception ignored){}
        }
    }
}

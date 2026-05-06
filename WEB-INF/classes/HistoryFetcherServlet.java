import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HistoryFetcherServlet extends HttpServlet {

    //Note: 'CurrMirr' must be configured in ODBC Data Source Administrator
    private static final String DB_URL = "jdbc:odbc:CurrMirr";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String sortColumn = request.getParameter("sortBy");
        if (sortColumn == null || sortColumn.isEmpty()) {
            sortColumn = "ID"; 
        }

        if (!sortColumn.matches("ID|Topology|Out_Current|Power|Width_Ratio|Out_Impedance")) {
            sortColumn = "ID";
        }

        out.println("<html><head><style>");
        out.println("</style></head><body>");
        
        out.println("<h2>Design History Vault</h2>");
        out.println("<table>");
        out.println("<tr>");
        out.println("<th><a href='viewHistory?sortBy=ID'>ID</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Topology'>Topology</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Out_Current'>Output Current (uA)</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Power'>Power (uW)</a></th>");
        
        out.println("<th><a href='viewHistory?sortBy=Width_Ratio'>Width Ratio</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Out_Impedance'>Out Impedance</a></th>");
        
        out.println("<th>Actions</th>");
        out.println("</tr>");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            conn = DriverManager.getConnection(DB_URL);
            
            //Querying with the order we want
            String sql = "SELECT ID, Topology, Out_Current, Power, Width_Ratio, Out_Impedance FROM designs ORDER BY " + sortColumn + " ASC";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("ID"); 
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + rs.getString("Topology") + "</td>");
                out.println("<td>" + rs.getDouble("Out_Current") + "</td>");
                out.println("<td>" + rs.getDouble("Power") + "</td>");
                out.println("<td>" + rs.getDouble("Width_Ratio") + "</td>");
                out.println("<td>" + rs.getDouble("Out_Impedance") + "</td>");

                //Delete button in red
                out.println("<td><a href='delete?id=" + id + "' style='color:red;'>Delete Now</a></td>");
                }

        } catch (Exception e) {
            out.println("<p style='color:red;'>Error retrieving data: " + e.getMessage() + "</p>");
        } finally {
            //Close Connection
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }  

        out.println("</table>");
        out.println("<br><a href='index.html'>Back to Designer</a>");
        out.println("</body></html>");
    }
}
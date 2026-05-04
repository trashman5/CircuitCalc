import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/viewHistory")
public class HistoryFetcherServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:odbc:CurrMirr";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // 1. Get the sort parameter from the URL (defaults to ID)
        String sortColumn = request.getParameter("sortBy");
        if (sortColumn == null || sortColumn.isEmpty()) {
            sortColumn = "ID"; 
        }

        // Whitelist validation (Security: Prevents SQL Injection in ORDER BY)
        if (!sortColumn.matches("ID|Topology|Out_Current|Power")) {
            sortColumn = "ID";
        }

        out.println("<html><head><style>");
        out.println("table { width: 100%; border-collapse: collapse; font-family: sans-serif; }");
        out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
        out.println("th { background-color: #0f172a; color: white; }");
        out.println("th a { color: #3b82f6; text-decoration: none; }");
        out.println("tr:nth-child(even) { background-color: #f8fafc; }");
        out.println("</style></head><body>");
        
        out.println("<h2>Design History Vault</h2>");
        out.println("<p>Click headers to sort by that column.</p>");

        out.println("<table>");
        out.println("<tr>");
        out.println("<th><a href='viewHistory?sortBy=ID'>ID</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Topology'>Topology</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Out_Current'>Output Current (uA)</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Power'>Power (uW)</a></th>");
        out.println("</tr>");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            conn = DriverManager.getConnection(DB_URL);
            
            // 2. Build the dynamic SQL query
            String sql = "SELECT ID, Topology, Out_Current, Power FROM designs ORDER BY " + sortColumn + " ASC";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            // 3. Iterate through results and build table rows
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getInt("ID") + "</td>");
                out.println("<td>" + rs.getString("Topology") + "</td>");
                out.println("<td>" + rs.getDouble("Out_Current") + "</td>");
                out.println("<td>" + rs.getDouble("Power") + "</td>");
                out.println("</tr>");
            }

        } catch (Exception e) {
            out.println("<p style='color:red;'>Error retrieving data: " + e.getMessage() + "</p>");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        out.println("</table>");
        out.println("<br><a href='index.html'>Back to Designer</a>");
        out.println("</body></html>");
    }
}
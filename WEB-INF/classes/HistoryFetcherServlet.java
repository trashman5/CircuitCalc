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

    // Ensure this matches your Windows ODBC Data Source Name exactly!
    private static final String DB_URL = "jdbc:odbc:CurrMirr";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String sortColumn = request.getParameter("sortBy");
        if (sortColumn == null || sortColumn.isEmpty()) {
            sortColumn = "ID"; 
        }

        // 1. UPDATED WHITELIST: Added Width_Ratio and Out_Impedance
        if (!sortColumn.matches("ID|Topology|Out_Current|Power|Width_Ratio|Out_Impedance")) {
            sortColumn = "ID";
        }

        out.println("<html><head><style>");
        // (Styles remain the same...)
        out.println("</style></head><body>");
        
        out.println("<h2>Design History Vault</h2>");
        out.println("<table>");
        out.println("<tr>");
        out.println("<th><a href='viewHistory?sortBy=ID'>ID</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Topology'>Topology</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Out_Current'>Output Current (uA)</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Power'>Power (uW)</a></th>");
        
        // 2. FIXED TYPO: Changed Radio to Ratio
        out.println("<th><a href='viewHistory?sortBy=Width_Ratio'>Width Ratio</a></th>");
        out.println("<th><a href='viewHistory?sortBy=Out_Impedance'>Out Impedance</a></th>");
        
        // 3. ADDED ACTIONS HEADER: Keeps columns aligned
        out.println("<th>Actions</th>");
        out.println("</tr>");

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            conn = DriverManager.getConnection(DB_URL);
            
            // Querying exactly what we need
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

                // Delete button now has its own column
                out.println("<td><a href='delete?id=" + id + "' style='color:red;'>Delete Now</a></td>");
                }

        } catch (Exception e) {
            out.println("<p style='color:red;'>Error retrieving data: " + e.getMessage() + "</p>");
        } finally {
            // ... (Close resources)
        }

        out.println("</table>");
        out.println("<br><a href='index.html'>Back to Designer</a>");
        out.println("</body></html>");
    }
}
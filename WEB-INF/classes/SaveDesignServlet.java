import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/saveDesign")
public class SaveDesignServlet extends HttpServlet {

    // Database credentials/URL
    // Note: 'CurrentMirrorDSN' must be configured in Windows ODBC Data Source Administrator
    private static final String DB_URL = "jdbc:odbc:CurrMirr";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // 1. Retrieve parameters from the request
        String topology = request.getParameter("Topology");
        double iRef = Double.parseDouble(request.getParameter("Reference_Current"));
        double vdd = Double.parseDouble(request.getParameter("VDD"));
        double ratio = Double.parseDouble(request.getParameter("Width_Ratio"));
        double iOut = Double.parseDouble(request.getParameter("Output_Current"));
        double rOut = Double.parseDouble(request.getParameter("Output_Impedance"));
        double pCons = Double.parseDouble(request.getParameter("Power_Consumption"));

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 2. Load the ODBC Driver (Standard for Java 7 and older)
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            // 3. Establish Connection
            conn = DriverManager.getConnection(DB_URL);

            // 4. Prepare SQL Query
            String sql = "INSERT INTO designs (Topology, Ref_Current, VDD, Width_Ratio, Out_Current, Out_Impedance, Power) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, topology);
            pstmt.setDouble(2, iRef);
            pstmt.setDouble(3, vdd);
            pstmt.setDouble(4, ratio);
            pstmt.setDouble(5, iOut);
            pstmt.setDouble(6, rOut);
            pstmt.setDouble(7, pCons);

            // 5. Execute Update
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                out.print("<div style='color:green; padding:10px; border:1px solid green;'>");
                out.print("<strong>Success!</strong> Design saved to Access Database.");
                out.print("</div>");
            }

        } catch (ClassNotFoundException e) {
            out.print("<p style='color:red;'>Error: JDBC-ODBC Bridge driver not found. (Check Java version)</p>");
            e.printStackTrace(out);
        } catch (SQLException e) {
            out.print("<p style='color:red;'>Database Error: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        } finally {
            // 6. Clean up resources
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
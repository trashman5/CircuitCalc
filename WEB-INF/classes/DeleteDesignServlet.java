import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/deleteDesign")
public class DeleteDesignServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:odbc:CurrentMirrorDSN";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String id = request.getParameter("id");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection conn = DriverManager.getConnection(DB_URL);
            
            // SQL Delete Query
            String sql = "DELETE FROM designs WHERE ID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, Integer.parseInt(id));
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                out.print("<span style='color:green;'>Entry #" + id + " deleted successfully.</span>");
            }
            
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            out.print("<span style='color:red;'>Error: " + e.getMessage() + "</span>");
        }
    }
}
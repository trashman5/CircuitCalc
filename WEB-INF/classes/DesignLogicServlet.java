import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DesignLogicServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            // GET inputs
            String topology = request.getParameter("Topology");
            if (topology == null) topology = "Simple";
            
            double iRef = Double.parseDouble(request.getParameter("Reference_Current")); 
            double vdd = Double.parseDouble(request.getParameter("VDD"));                
            double widthRatio = Double.parseDouble(request.getParameter("Width_Ratio"));
            
            // Constants & Variables from transistors
            double lambda = 0.1; 
            double iOut = iRef * widthRatio;
            double pConsum = vdd * (iRef + iOut); 
            double rOut = 0;

            // Topology-Specific Logic
            // Wilkinson/Wilson mirrors and Cascode mirrors are designed to increase Rout
            switch (topology.toLowerCase()) {
                case "cascode":
                    // Cascode impedance is boosted by the gain of the cascode transistor
                    double roBaseCascode = 1.0 / (lambda * (iOut * 1e-6));
                    rOut = roBaseCascode * 50; 
                    break;

                case "wilkinson":
                    // Wilson mirrors also provide high output impedance
                    double roBaseWilson = 1.0 / (lambda * (iOut * 1e-6));
                    rOut = roBaseWilson * 25;
                    break;

                case "simple":
                default:
                    // Standard drain resistance: Rout = 1 / (lambda * Iout)
                    rOut = 1.0 / (lambda * (iOut * 1e-6));
                    break;
            }

            //Generate HTML Response
            out.println("<div class='result-card fade-in'>");
            out.println("  <h4>Simulation Results: " + topology + "</h4>");
            out.println("  <p><strong>Output Current:</strong> " + String.format("%.2f", iOut) + " µA</p>");
            out.println("  <p><strong>Impedance:</strong> " + String.format("%.1f", rOut/1000) + " kΩ</p>");
            out.println("  <p style='color: var(--accent);'><strong>Power:</strong> " + String.format("%.2f", pConsum) + " µW</p>");
            
            // Save Form
            out.println("<form action='Save' method='GET' style='margin-top:15px;'>");
            out.println("<input type='hidden' name='Topology' value='" + topology + "'>");
            out.println("<input type='hidden' name='Reference_Current' value='" + iRef + "'>");
            out.println("<input type='hidden' name='VDD' value='" + vdd + "'>");
            out.println("<input type='hidden' name='Width_Ratio' value='" + widthRatio + "'>");
            out.println("<input type='hidden' name='Output_Current' value='" + iOut + "'>");
            out.println("<input type='hidden' name='Output_Impedance' value='" + rOut/1000 + "'>");
            out.println("<input type='hidden' name='Power_Consumption' value='" + pConsum + "'>");
            out.println("<button type='submit' class='btn btn-primary'>Confirm & Save to Database</button>");
            out.println("</form>");
            out.println("</div>");

        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("<p style='color:red;'>Error: Invalid input parameters.</p>");
        }
    }
}
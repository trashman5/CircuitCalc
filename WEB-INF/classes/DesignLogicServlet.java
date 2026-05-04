import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/calculate")
public class DesignLogicServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

        try {
            // 2. Extract parameters from the request
            // These names must match the 'name' attribute in your HTML or the keys in your JS fetch
            String topology = request.getParameter("Topology");
            double iRef = Double.parseDouble(request.getParameter("Reference_Current")); // in uA
            double vdd = Double.parseDouble(request.getParameter("VDD"));                // in V
            double widthRatio = Double.parseDouble(request.getParameter("Width_Ratio"));
            
            // 3. The "Brain" - Analog Calculations
            // Assume lambda = 0.1 for a standard 180nm process
            double lambda = 0.1; 
            
            double iOut = iRef * widthRatio;
            double pConsum = vdd * (iRef + iOut); // in uW
            
            // Convert iOut to Amps for Rout calculation to get Ohms
            double rOut = 1.0 / (lambda * (iOut * 1e-6)); 

            // 4. Wrap results in a simple POJO (Plain Old Java Object)
            CalculationResult result = new CalculationResult(iOut, rOut, pConsum);

            // 5. Send JSON response
            String htmlResponse = String.format(
                "<div class='result-card fade-in'>" +
                "   <h4>Simulation Results</h4>" +
                "   <p><strong>Output Current:</strong> %.2f µA</p>" +
                "   <p><strong>Impedance:</strong> %.1f kΩ</p>" +
                "   <p style='color: var(--accent);'><strong>Power:</strong> %.2f µW</p>" +
                "</div>", 
                iOut, (rOut/1000), pConsum
            );

    out.print(htmlResponse);
    out.println("<form action='Save' method='GET' style='margin-top:15px;'>");
    // Hidden fields to pass data to the next Servlet
    out.println("<input type='hidden' name='Topology' value='" + topology + "'>");
    out.println("<input type='hidden' name='Reference_Current' value='" + iRef + "'>");
    out.println("<input type='hidden' name='VDD' value='" + vdd + "'>");
    out.println("<input type='hidden' name='Width_Ratio' value='" + widthRatio + "'>");
    out.println("<input type='hidden' name='Output_Current' value='" + iOut + "'>");
    out.println("<input type='hidden' name='Output_Impedance' value='" + rOut + "'>");
    out.println("<input type='hidden' name='Power_Consumption' value='" + pConsum + "'>");
    
    out.println("<button type='submit' class='btn-save'>Confirm & Save to Database</button>");
    out.println("</form>");
    out.println("</div>");

        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid input parameters\"}");
        } finally {
            out.flush();
        }
    }

    // Inner class to structure the JSON response
    private static class CalculationResult {
        double output_Current;
        double output_Impedance;
        double power_Consumption;

        CalculationResult(double i, double r, double p) {
            this.output_Current = i;
            this.output_Impedance = r;
            this.power_Consumption = p;
        }
    }
}
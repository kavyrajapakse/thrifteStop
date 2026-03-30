package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "GenerateProductIdServlet", urlPatterns = {"/generate-product-id"})
public class GenerateProductIdServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria criteria = session.createCriteria(Product.class);
            // Fetch all products 
            java.util.List<Product> products = criteria.list();

            int maxNumber = 0;
            for (Product p : products) {
                String pid = p.getProductId();
                if (pid != null && pid.startsWith("ITM-")) {
                    try {
                        int num = Integer.parseInt(pid.substring(4));
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            int nextNumber = maxNumber + 1;
            String newId = String.format("ITM-%03d", nextNumber);

            responseObject.addProperty("status", true);
            responseObject.addProperty("nextId", newId);
            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to generate product ID");
            out.print(gson.toJson(responseObject));
        } finally {
            session.close();
            out.flush();
            out.close();
        }
    }

}

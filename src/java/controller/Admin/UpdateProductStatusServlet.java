package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Status;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "UpdateProductStatusServlet", urlPatterns = {"/update-product-status"})
public class UpdateProductStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = null;
        Transaction tx = null;
        JsonObject responseObject = new JsonObject();
        Gson gson = new Gson();

        try {
            // Parse JSON body
            BufferedReader reader = request.getReader();
            JsonObject reqJson = gson.fromJson(reader, JsonObject.class);

            String productId = reqJson.get("productId").getAsString();
            int newStatusId = reqJson.get("status").getAsInt();

            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Product product = (Product) session.get(Product.class, productId);
            if (product == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Invalid product ID.");
            } else {
                Status newStatus = (Status) session.get(Status.class, newStatusId);
                if (newStatus == null) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Invalid status ID.");
                } else {
                    product.setStatus(newStatus);
                    session.update(product);
                    tx.commit();

                    responseObject.addProperty("status", true);
                }
            }

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error updating product status.");
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

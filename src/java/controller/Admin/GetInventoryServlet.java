package controller.Admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Category;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Status;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetInventoryServlet", urlPatterns = {"/get-inventory"})
public class GetInventoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = null;
        JsonObject responseObject = new JsonObject();

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            String statusParam = request.getParameter("status");
            String categoryParam = request.getParameter("category");

            Criteria criteria = session.createCriteria(Product.class, "p");

            // Filter by status
            if (statusParam != null && !statusParam.isEmpty()) {
                try {
                    int statusId = Integer.parseInt(statusParam);
                    criteria.createAlias("status", "s");
                    criteria.add(Restrictions.eq("s.id", statusId));
                } catch (NumberFormatException ignored) {
                }
            } else {
                criteria.createAlias("status", "s");
                criteria.add(Restrictions.or(
                        Restrictions.eq("s.id", 1),
                        Restrictions.eq("s.id", 2)
                ));
            }

            // Filter by category
            if (categoryParam != null && !categoryParam.isEmpty()) {
                try {
                    int categoryId = Integer.parseInt(categoryParam);
                    criteria.createAlias("category", "c");
                    criteria.add(Restrictions.eq("c.id", categoryId));
                } catch (NumberFormatException ignored) {
                }
            } else {
                criteria.createAlias("category", "c");
            }

            List<Product> productList = criteria.list();

            JsonArray productArray = new JsonArray();
            for (Product p : productList) {
                JsonObject obj = new JsonObject();
                obj.addProperty("sku", p.getProductId());
                obj.addProperty("title", p.getTitle());
                obj.addProperty("category", p.getCategory() != null ? p.getCategory().getName() : "-");
                obj.addProperty("status", p.getStatus() != null ? p.getStatus().getId() : 0);
                obj.addProperty("statusName", p.getStatus() != null ? p.getStatus().getName() : "-");
                obj.addProperty("dateAdded", p.getDatetimeAdded().toString());
                productArray.add(obj);
            }

            responseObject.addProperty("status", true);
            responseObject.add("products", productArray);

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error loading products.");
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

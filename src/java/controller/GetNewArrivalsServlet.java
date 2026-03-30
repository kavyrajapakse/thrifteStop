package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.*;
import hibernate.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GetNewArrivalsServlet", urlPatterns = {"/get-new-arrivals"})
public class GetNewArrivalsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject responseObject = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria productCriteria = session.createCriteria(Product.class);
            productCriteria.add(Restrictions.eq("status.id", 1));
            productCriteria.addOrder(Order.desc("datetimeAdded"));
            productCriteria.setMaxResults(8);

            List<Product> products = productCriteria.list();

            JsonArray jsonArray = new JsonArray();

            for (Product p : products) {
                Criteria imgCriteria = session.createCriteria(ProductImage.class);
                imgCriteria.add(Restrictions.eq("product", p));
                imgCriteria.setMaxResults(1);
                ProductImage img = (ProductImage) imgCriteria.uniqueResult();

                JsonObject productJson = new JsonObject();
                productJson.addProperty("id", p.getProductId());
                productJson.addProperty("name", p.getTitle());
                productJson.addProperty("price", "Rs " + String.format("%.2f", p.getPrice()));
                String imageUrl = img != null
                        ? request.getContextPath() + "/" + img.getImgPath()
                        : request.getContextPath() + "/resources/items/default.jpg";
                productJson.addProperty("imageUrl", imageUrl);

                jsonArray.add(productJson);
            }

            responseObject.add("products", jsonArray);
            responseObject.addProperty("status", true);

            Gson gson = new Gson();
            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();

            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load new arrivals.");
            out.print(responseObject.toString());

        } finally {
            session.close();
            out.close();
        }
    }
}

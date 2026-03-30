package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.*;
import hibernate.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GetRelatedProductsServlet", urlPatterns = {"/get-related-products"})
public class GetRelatedProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        JsonArray relatedArray = new JsonArray();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String productId = request.getParameter("id");

        if (productId == null || productId.trim().isEmpty()) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Product ID is missing.");
            out.print(gson.toJson(responseObject));
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Product currentProduct = (Product) session.get(Product.class, productId);

            if (currentProduct == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Product not found.");
                out.print(gson.toJson(responseObject));
                return;
            }

            Category category = currentProduct.getCategory();

            Criteria productCriteria = session.createCriteria(Product.class);
            productCriteria.add(Restrictions.eq("category", category));
            productCriteria.add(Restrictions.ne("productId", productId));
            productCriteria.add(Restrictions.eq("status.id", 1)); // only active
            productCriteria.setMaxResults(4);

            List<Product> products = productCriteria.list();

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

                relatedArray.add(productJson);
            }

            responseObject.addProperty("status", true);
            responseObject.add("related", relatedArray);
            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error loading related products.");
            out.print(gson.toJson(responseObject));
        } finally {
            session.close();
            out.flush();
            out.close();
        }
    }
}

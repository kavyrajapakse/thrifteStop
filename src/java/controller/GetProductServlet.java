package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductImage;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GetProductServlet", urlPatterns = {"/get-product"})
public class GetProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String pid = request.getParameter("id");

        if (pid == null || pid.trim().isEmpty()) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Product ID is missing.");
            out.print(gson.toJson(responseObject));
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Product product = (Product) session.get(Product.class, pid);

            if (product == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Product not found.");
                out.print(gson.toJson(responseObject));
                return;
            }

            // Load product images
            List<ProductImage> images = session.createCriteria(ProductImage.class)
                    .add(Restrictions.eq("product", product))
                    .list();

            // Build flat JSON structure (for frontend)
            JsonObject productJson = new JsonObject();
            productJson.addProperty("id", product.getProductId());
            productJson.addProperty("title", product.getTitle());
            productJson.addProperty("description", product.getDescription());
            productJson.addProperty("price", "Rs " + String.format("%.2f", product.getPrice()));
            productJson.addProperty("category", product.getCategory().getName());
            productJson.addProperty("condition", product.getCondition().getName());
            productJson.addProperty("size", product.getSize().getName());

            JsonArray imageArray = new JsonArray();
            for (ProductImage img : images) {
                imageArray.add(request.getContextPath() + "/" + img.getImgPath());
            }
            productJson.add("images", imageArray);

            // Add final status
            productJson.addProperty("status", true);
            out.print(gson.toJson(productJson));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error loading product details.");
            out.print(gson.toJson(responseObject));
        } finally {
            session.close();
            out.flush();
            out.close();
        }
    }
}

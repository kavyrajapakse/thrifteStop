package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductImage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetProductDetailsServlet", urlPatterns = {"/get-product-details"})
public class GetProductDetailsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pid = request.getParameter("productId");
        JsonObject responseObject = new JsonObject();
        Session session = null;

        try {
            if (pid == null || pid.trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Product ID is required.");
                response.setContentType("application/json");
                response.getWriter().write(responseObject.toString());
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();
            Product product = (Product) session.get(Product.class, pid);

            if (product == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Product not found.");
            } else {
                responseObject.addProperty("status", true);
                responseObject.addProperty("title", product.getTitle());
                responseObject.addProperty("description", product.getDescription());
                responseObject.addProperty("price", product.getPrice());

                if (product.getCategory() != null) {
                    responseObject.addProperty("categoryId", product.getCategory().getId());
                }

                if (product.getCondition() != null) {
                    responseObject.addProperty("conditionId", product.getCondition().getId());
                }

                if (product.getSize() != null) {
                    responseObject.addProperty("sizeId", product.getSize().getId());
                }

                // Load up to 3 image paths for the product
                Criteria imageCriteria = session.createCriteria(ProductImage.class);
                imageCriteria.add(Restrictions.eq("product", product));
                List<ProductImage> imageList = imageCriteria.list();

                JsonArray imageArray = new JsonArray();
                for (int i = 0; i < imageList.size() && i < 3; i++) {
                    imageArray.add(imageList.get(i).getImgPath());
                }

                responseObject.add("images", imageArray);
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "An error occurred while fetching product details.");
        } finally {
            if (session != null) {
                session.close();
            }
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(responseObject));
        }
    }
}

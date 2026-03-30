package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductImage;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "ProductListServlet", urlPatterns = {"/get-product-list"})
public class ProductListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        int page = 1;
        int limit = 8;

        String category = request.getParameter("category");
        String size = request.getParameter("size");
        String condition = request.getParameter("condition");
        String search = request.getParameter("search");

        try {
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        int offset = (page - 1) * limit;

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Gson gson = new Gson();

        JsonObject responseObject = new JsonObject();

        try {
            Criteria criteria = session.createCriteria(Product.class);
            criteria.add(Restrictions.eq("status.id", 1));

            if (category != null && !category.isEmpty()) {
                int catId = Integer.parseInt(category);
                criteria.add(Restrictions.eq("category.id", catId));
            }
            if (size != null && !size.isEmpty()) {
                int sizeId = Integer.parseInt(size);
                criteria.add(Restrictions.eq("size.id", sizeId));
            }
            if (condition != null && !condition.isEmpty()) {
                int conditionId = Integer.parseInt(condition);
                criteria.add(Restrictions.eq("condition.id", conditionId));
            }

            if (search != null && !search.isEmpty()) {
                criteria.add(Restrictions.ilike("title", "%" + search + "%"));
            }

            criteria.addOrder(Order.desc("datetimeAdded"));
            criteria.setFirstResult(offset);
            criteria.setMaxResults(limit);

            List<Product> products = criteria.list();

            JsonArray jsonArray = new JsonArray();

            for (Product p : products) {
                Criteria imgCriteria = session.createCriteria(ProductImage.class);
                imgCriteria.add(Restrictions.eq("product", p));
                imgCriteria.setMaxResults(1);
                ProductImage image = (ProductImage) imgCriteria.uniqueResult();

                JsonObject obj = new JsonObject();
                obj.addProperty("id", p.getProductId());
                obj.addProperty("size", p.getSize().getName());
                obj.addProperty("title", p.getTitle());
                obj.addProperty("price", "Rs " + String.format("%,.2f", p.getPrice()));
                obj.addProperty("image", image != null ? image.getImgPath() : "resources/items/default.jpg");

                jsonArray.add(obj);
            }

            Criteria countCriteria = session.createCriteria(Product.class);
            countCriteria.add(Restrictions.eq("status.id", 1));

            if (category != null && !category.isEmpty()) {
                int catId = Integer.parseInt(category);
                countCriteria.add(Restrictions.eq("category.id", catId));
            }
            if (size != null && !size.isEmpty()) {
                int sizeId = Integer.parseInt(size);
                countCriteria.add(Restrictions.eq("size.id", sizeId));
            }
            if (condition != null && !condition.isEmpty()) {
                int conditionId = Integer.parseInt(condition);
                countCriteria.add(Restrictions.eq("condition.id", conditionId));
            }

            if (search != null && !search.isEmpty()) {
                countCriteria.add(Restrictions.ilike("title", "%" + search + "%"));
            }

            int totalCount = countCriteria.list().size();

            responseObject.add("products", jsonArray);
            responseObject.addProperty("total", totalCount);
            responseObject.addProperty("status", true);

            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();

            responseObject.add("products", new JsonArray());
            responseObject.addProperty("total", 0);
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load product list.");
            out.print(gson.toJson(responseObject));

        } finally {
            session.close();
            out.close();
        }
    }
}

package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet("/load-order-items")
public class LoadOrderItemsServlet extends HttpServlet {
    private static final int ITEMS_PER_PAGE = 10;
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject responseObject = new JsonObject();
        response.setContentType("application/json");

        int page = 1;
        try {
            String pageParam = request.getParameter("page");
            if (pageParam != null) {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Invalid page number.");
            response.getWriter().write(gson.toJson(responseObject));
            return;
        }

        String categoryParam = request.getParameter("categoryId");
        String dateFromParam = request.getParameter("dateFrom");
        String dateToParam = request.getParameter("dateTo");

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(OrderItem.class, "oi");

            criteria.createAlias("oi.order", "o");
            criteria.createAlias("oi.product", "p");
            criteria.createAlias("p.category", "c");

            // Filters
            if (categoryParam != null && !categoryParam.trim().isEmpty()) {
                try {
                    int catId = Integer.parseInt(categoryParam);
                    criteria.add(Restrictions.eq("c.id", catId));
                } catch (NumberFormatException ignored) {}
            }

            if (dateFromParam != null && !dateFromParam.trim().isEmpty()) {
                Date dateFrom = Date.valueOf(dateFromParam);
                criteria.add(Restrictions.ge("o.orderDate", dateFrom));
            }

            if (dateToParam != null && !dateToParam.trim().isEmpty()) {
                Date dateTo = Date.valueOf(dateToParam);
                criteria.add(Restrictions.le("o.orderDate", dateTo));
            }

            // Sort and pagination
            criteria.addOrder(Order.desc("o.orderDate"));
            criteria.setFirstResult((page - 1) * ITEMS_PER_PAGE);
            criteria.setMaxResults(ITEMS_PER_PAGE);

            @SuppressWarnings("unchecked")
            List<OrderItem> orderItems = criteria.list();

            JsonArray itemArray = new JsonArray();
            double total = 0;

            for (OrderItem item : orderItems) {
                CustomerOrder order = (CustomerOrder) session.get(CustomerOrder.class, item.getOrder().getOrderId());
                Product product = (Product) session.get(Product.class, item.getProduct().getProductId());

                if (order == null || product == null) continue;

                product.setDescription(null); // Null sensitive fields

                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("date", order.getOrderDate().toString());
                itemJson.addProperty("orderId", order.getOrderId());
                itemJson.addProperty("title", product.getTitle());
                itemJson.addProperty("category", product.getCategory().getName());
                itemJson.addProperty("price", product.getPrice());

                itemArray.add(itemJson);
                total += product.getPrice();
            }

            responseObject.add("items", itemArray);
            responseObject.addProperty("total", total);
            responseObject.addProperty("status", true);

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Server error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.getWriter().write(gson.toJson(responseObject));
    }
}

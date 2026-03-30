package controller.Admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetOrderItemsServlet", urlPatterns = {"/get-order-items"})
public class GetOrderItemsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderId = request.getParameter("orderId");
        JsonObject responseObject = new JsonObject();
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Load order
            CustomerOrder order = (CustomerOrder) session.get(CustomerOrder.class, orderId);
            if (order == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Order not found.");
            } else {
                // 🟢 Load order items
                Criteria criteria = session.createCriteria(OrderItem.class)
                        .createAlias("order", "o")
                        .add(Restrictions.eq("o.orderId", orderId));

                List<OrderItem> items = criteria.list();
                JsonArray itemArray = new JsonArray();

                for (OrderItem item : items) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", item.getProduct().getProductId());
                    obj.addProperty("title", item.getProduct().getTitle());
                    obj.addProperty("price", "Rs " + String.format("%.2f", item.getPrice()));
                    itemArray.add(obj);
                }

                // 🟡 Load shipping address
                Address addr = (Address) session.get(Address.class, order.getAddressId());
                String shipping = "Address not found.";
                if (addr != null) {
                    City city = (City) session.get(City.class, addr.getCityId());
                    StringBuilder sb = new StringBuilder();
                    sb.append(addr.getLine1());
                    if (addr.getLine2() != null && !addr.getLine2().isEmpty()) {
                        sb.append(", ").append(addr.getLine2());
                    }
                    sb.append(", ").append(city.getCityName());
                    sb.append(", ").append(addr.getPostalCode());
                    shipping = sb.toString();
                }

                responseObject.addProperty("status", true);
                responseObject.add("items", itemArray);
                responseObject.addProperty("address", shipping);
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load order items or address.");
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

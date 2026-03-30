package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GetCustomerOrdersServlet", urlPatterns = {"/get-customer-orders"})
public class GetCustomerOrdersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        Session session = null;

        try {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null || httpSession.getAttribute("loggedInCustomer") == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "User not logged in");
                out.print(gson.toJson(responseObject));
                return;
            }

            String userEmail = (String) httpSession.getAttribute("loggedInCustomer");

            session = HibernateUtil.getSessionFactory().openSession();

            Criteria orderCriteria = session.createCriteria(CustomerOrder.class);
            orderCriteria.add(Restrictions.eq("usersEmail", userEmail));
            orderCriteria.addOrder(Order.desc("orderDate"));
            List<CustomerOrder> orders = orderCriteria.list();

            JsonArray orderArray = new JsonArray();

            for (CustomerOrder order : orders) {
                JsonObject orderJson = new JsonObject();
                orderJson.addProperty("orderId", order.getOrderId());
                orderJson.addProperty("orderDate", order.getOrderDate().toString());
                orderJson.addProperty("totalAmount", "Rs " + String.format("%,.2f", order.getTotalAmount()));
                orderJson.addProperty("status", order.getStatus().getName());

                JsonArray itemsArray = new JsonArray();

                Criteria itemCriteria = session.createCriteria(OrderItem.class);
                itemCriteria.add(Restrictions.eq("order", order));
                List<OrderItem> items = itemCriteria.list();

                for (OrderItem item : items) {
                    Product p = item.getProduct();

                    Criteria imgCriteria = session.createCriteria(ProductImage.class);
                    imgCriteria.add(Restrictions.eq("product", p));
                    imgCriteria.setMaxResults(1);
                    ProductImage img = (ProductImage) imgCriteria.uniqueResult();

                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("title", p.getTitle());
                    itemJson.addProperty("price", "Rs " + String.format("%,.2f", item.getPrice()));
                    itemJson.addProperty("image", img != null ? img.getImgPath() : "resources/items/default.jpg");

                    itemsArray.add(itemJson);
                }

                orderJson.add("items", itemsArray);
                orderArray.add(orderJson);
            }

            responseObject.addProperty("status", true);
            responseObject.add("orders", orderArray);
            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Something went wrong");
            out.print(gson.toJson(responseObject));
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
            out.flush();
            out.close();
        }
    }
}

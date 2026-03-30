package controller.Admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.CustomerOrder;
import hibernate.HibernateUtil;
import hibernate.OrderStatus;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/get-recent-orders")
public class RecentOrdersServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Session session = null;
        JsonObject responseObject = new JsonObject();
        JsonArray ordersArray = new JsonArray();

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(CustomerOrder.class);
            criteria.addOrder(Order.desc("createdAt"));
            criteria.setMaxResults(5);
            List<CustomerOrder> orderList = criteria.list();

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");

            for (CustomerOrder order : orderList) {
                JsonObject orderJson = new JsonObject();
                orderJson.addProperty("orderId", order.getOrderId());
                orderJson.addProperty("customer", order.getUsersEmail());
                orderJson.addProperty("amount", order.getTotalAmount());
                orderJson.addProperty("status", order.getStatus().getName());
                orderJson.addProperty("date", sdf.format(order.getOrderDate()));

                ordersArray.add(orderJson);
            }

            responseObject.addProperty("status", true);
            responseObject.add("orders", ordersArray);

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to fetch recent orders.");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

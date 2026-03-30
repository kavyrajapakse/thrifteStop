package controller.Admin;

import com.google.gson.JsonObject;
import com.google.gson.Gson;
import hibernate.CustomerOrder;
import hibernate.HibernateUtil;
import hibernate.OrderStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "UpdateOrderStatusServlet", urlPatterns = {"/update-order-status"})
public class UpdateOrderStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject responseObject = new JsonObject();
        Gson gson = new Gson();

        String orderId = request.getParameter("orderId");
        String newStatus = request.getParameter("status");

        Session session = null;
        Transaction tx = null;

        try {
            if (orderId == null || newStatus == null || orderId.trim().isEmpty() || newStatus.trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Missing orderId or status");
                respond(response, responseObject);
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Fetch the order using session.get
            CustomerOrder order = (CustomerOrder) session.get(CustomerOrder.class, orderId);
            if (order == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Order not found");
                respond(response, responseObject);
                return;
            }

            // Fetch the new status using Criteria and Restrictions
            Criteria statusCriteria = session.createCriteria(OrderStatus.class);
            statusCriteria.add(Restrictions.eq("name", capitalizeFirst(newStatus)));
            OrderStatus status = (OrderStatus) statusCriteria.uniqueResult();

            if (status == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Invalid status value");
                respond(response, responseObject);
                return;
            }

            // Update and save
            order.setStatus(status);
            session.update(order);
            tx.commit();

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Order status updated successfully");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Server error occurred");
        } finally {
            if (session != null) session.close();
        }

        respond(response, responseObject);
    }

    private void respond(HttpServletResponse response, JsonObject json) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}

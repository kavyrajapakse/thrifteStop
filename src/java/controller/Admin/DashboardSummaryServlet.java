package controller.Admin;

import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;
import hibernate.HibernateUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/get-dashboard-summary")
public class DashboardSummaryServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Total Sales (sum of total_amount from customer_orders)
            Criteria salesCriteria = session.createCriteria(CustomerOrder.class);
            salesCriteria.setProjection(Projections.sum("totalAmount"));
            Double totalSales = (Double) salesCriteria.uniqueResult();
            if (totalSales == null) totalSales = 0.0;

            // Total Orders (count of customer_orders)
            Criteria orderCriteria = session.createCriteria(CustomerOrder.class);
            orderCriteria.setProjection(Projections.rowCount());
            Long totalOrders = (Long) orderCriteria.uniqueResult();

            // Current Inventory (count of products where status.id = 1)
            Criteria inventoryCriteria = session.createCriteria(Product.class, "p");
            inventoryCriteria.createAlias("status", "s");
            inventoryCriteria.add(Restrictions.eq("s.id", 1));
            inventoryCriteria.setProjection(Projections.rowCount());
            Long currentInventory = (Long) inventoryCriteria.uniqueResult();

            // Active Customers (count of users with status = 1)
            Criteria customerCriteria = session.createCriteria(User.class);
            customerCriteria.add(Restrictions.eq("status", 1));
            customerCriteria.setProjection(Projections.rowCount());
            Long activeCustomers = (Long) customerCriteria.uniqueResult();

            // Build JSON
            responseObject.addProperty("status", true);
            responseObject.addProperty("totalSales", totalSales);
            responseObject.addProperty("totalOrders", totalOrders);
            responseObject.addProperty("currentInventory", currentInventory);
            responseObject.addProperty("activeCustomers", activeCustomers);

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load dashboard stats.");
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

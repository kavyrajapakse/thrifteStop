package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "GetOrdersServlet", urlPatterns = {"/get-orders"})
public class GetOrdersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = null;
        JsonObject responseObject = new JsonObject();
        Gson gson = new Gson();

        String statusParam = request.getParameter("status");
        String searchParam = request.getParameter("search");
        String dateFrom = request.getParameter("dateFrom");
        String dateTo = request.getParameter("dateTo");

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(CustomerOrder.class);

            // 🔎 JOIN to Status
            if (statusParam != null && !statusParam.trim().isEmpty()) {
                criteria.createAlias("status", "s");
                criteria.add(Restrictions.ilike("s.name", statusParam.trim(), MatchMode.ANYWHERE));
            }

            // 🔎 Filter by customer name or order ID (requires post-filtering)
            List<CustomerOrder> orders = criteria.list();
            JsonArray result = new JsonArray();

            for (CustomerOrder co : orders) {
                boolean match = true;

                // Search filter (customer name or order ID)
                if (searchParam != null && !searchParam.trim().isEmpty()) {
                    User user = (User) session.get(User.class, co.getUsersEmail());
                    String fullName = (user != null) ? (user.getFname()+ " " + user.getLname()) : "";
                    String oid = co.getOrderId().toLowerCase();
                    String q = searchParam.toLowerCase();

                    if (!fullName.toLowerCase().contains(q) && !oid.contains(q)) {
                        match = false;
                    }
                }

                // Date range filters
                if (match && dateFrom != null && !dateFrom.isEmpty()) {
                    if (co.getOrderDate().before(Date.valueOf(dateFrom))) {
                        match = false;
                    }
                }
                if (match && dateTo != null && !dateTo.isEmpty()) {
                    if (co.getOrderDate().after(Date.valueOf(dateTo))) {
                        match = false;
                    }
                }

                if (match) {
                    JsonObject o = new JsonObject();
                    o.addProperty("orderId", co.getOrderId());
                    o.addProperty("orderDate", co.getOrderDate().toString());
                    o.addProperty("total", co.getTotalAmount());

                    User u = (User) session.get(User.class, co.getUsersEmail());
                    o.addProperty("customerName", (u != null) ? (u.getFname()+ " " + u.getLname()) : "Unknown");

                    o.addProperty("status", (co.getStatus() != null) ? co.getStatus().getName().toLowerCase() : "pending");

                    result.add(o);
                }
            }

            responseObject.addProperty("status", true);
            responseObject.add("orders", result);

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load orders.");
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

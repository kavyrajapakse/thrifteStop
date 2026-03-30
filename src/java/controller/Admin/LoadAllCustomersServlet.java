package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/load-customers")
public class LoadAllCustomersServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Session session = null;
        JsonObject responseObject = new JsonObject();
        Gson gson = new Gson();

        try {
            int page = 1;
            if (request.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(request.getParameter("page"));
                } catch (NumberFormatException e) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Invalid page number.");
                    response.setContentType("application/json");
                    response.getWriter().write(responseObject.toString());
                    return;
                }
            }

            // Read filter params
            String statusParam = request.getParameter("status");  // expected "active", "inactive", or null/empty
            String dateFromStr = request.getParameter("dateFrom"); // yyyy-MM-dd
            String dateToStr = request.getParameter("dateTo");     // yyyy-MM-dd
            String search = request.getParameter("search");        // search term for name/email

            session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(User.class);

            // Filtering by status (assuming 1=active, 0=inactive)
            if (statusParam != null && !statusParam.isEmpty()) {
                if ("active".equalsIgnoreCase(statusParam)) {
                    criteria.add(org.hibernate.criterion.Restrictions.eq("status", 1));
                } else if ("inactive".equalsIgnoreCase(statusParam)) {
                    criteria.add(org.hibernate.criterion.Restrictions.eq("status", 2));
                }
            }

            // Filtering by date range (joinedDate between dateFrom and dateTo)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (dateFromStr != null && !dateFromStr.isEmpty()) {
                try {
                    java.util.Date dateFrom = sdf.parse(dateFromStr);
                    criteria.add(org.hibernate.criterion.Restrictions.ge("joinedDate", dateFrom));
                } catch (Exception ignored) {
                }
            }
            if (dateToStr != null && !dateToStr.isEmpty()) {
                try {
                    java.util.Date dateTo = sdf.parse(dateToStr);
                    criteria.add(org.hibernate.criterion.Restrictions.le("joinedDate", dateTo));
                } catch (Exception ignored) {
                }
            }

            // Filtering by search (name or email)
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.trim().toLowerCase() + "%";
                criteria.add(org.hibernate.criterion.Restrictions.or(
                        org.hibernate.criterion.Restrictions.ilike("fname", searchLike),
                        org.hibernate.criterion.Restrictions.ilike("lname", searchLike),
                        org.hibernate.criterion.Restrictions.ilike("email", searchLike)
                ));
            }

            // Pagination and order
            criteria.setFirstResult((page - 1) * PAGE_SIZE);
            criteria.setMaxResults(PAGE_SIZE);
            criteria.addOrder(Order.desc("joinedDate"));

            List<User> customers = criteria.list();

            JsonArray customerArray = new JsonArray();
            SimpleDateFormat outFormat = new SimpleDateFormat("MMMM dd, yyyy");

            for (User customer : customers) {
                JsonObject customerJson = new JsonObject();
                customerJson.addProperty("id", customer.getEmail());
                customerJson.addProperty("name", customer.getFname() + " " + customer.getLname());
                customerJson.addProperty("email", ""); // Null email field for privacy
                customerJson.addProperty("joined", outFormat.format(customer.getJoinedDate()));
                customerJson.addProperty("mobile", customer.getMobile());
                customerJson.addProperty("status", customer.getStatus());
                customerArray.add(customerJson);
            }

            tx.commit();

            responseObject.addProperty("status", true);
            responseObject.add("customers", customerArray);

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }

}

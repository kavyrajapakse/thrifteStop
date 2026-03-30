package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/update-customer-status")
public class UpdateCustomerStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        Gson gson = new Gson();
        Session session = null;

        try {
            String email = request.getParameter("email");
            String statusStr = request.getParameter("status");

            if (email == null || statusStr == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Missing parameters.");
                response.setContentType("application/json");
                response.getWriter().write(responseObject.toString());
                return;
            }

            int newStatus = Integer.parseInt(statusStr);

            session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            User user = (User) session.get(User.class, email);
            if (user == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "User not found.");
            } else {
                user.setStatus(newStatus);
                session.update(user);
                tx.commit();
                responseObject.addProperty("status", true);
            }

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ThriftAdmin;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import hibernate.HibernateUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        JsonObject responseObject = new JsonObject();
        Session session = null;

        try {
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Username and Password are required.");
                out.print(gson.toJson(responseObject));
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(ThriftAdmin.class);
            criteria.add(Restrictions.eq("username", username));
            criteria.add(Restrictions.eq("password", password)); // Consider hashing in production

            ThriftAdmin admin = (ThriftAdmin) criteria.uniqueResult();

            if (admin == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Invalid username or password.");
            } else {
                // Remove sensitive info
                admin.setPassword(null);

                // Store admin in session
                HttpSession httpSession = request.getSession();
                httpSession.setAttribute("admin", admin);
                httpSession.setMaxInactiveInterval(60 * 60); // 1 hour

                responseObject.addProperty("status", true);
            }

            out.print(gson.toJson(responseObject));
        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Server error occurred.");
            out.print(gson.toJson(responseObject));
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}

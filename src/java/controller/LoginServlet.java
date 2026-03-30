package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        Session session = null;

        try {
            JsonObject req = gson.fromJson(request.getReader(), JsonObject.class);
            String email = req.get("email").getAsString();
            String password = req.get("password").getAsString();

            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Email and password required");
            } else {
                session = HibernateUtil.getSessionFactory().openSession();
                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));
                criteria.add(Restrictions.eq("password", password));
                criteria.add(Restrictions.eq("status", 1));

                User user = (User) criteria.uniqueResult();

                if (user != null) {
                    HttpSession httpSession = request.getSession(true);
                    httpSession.setAttribute("loggedInCustomer", user.getEmail());
                    httpSession.setMaxInactiveInterval(60 * 60); // 1 hour
                    httpSession.removeAttribute("sessionCart");

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Login successful");
                    responseObject.addProperty("fname", user.getFname());
                    responseObject.addProperty("email", user.getEmail());
                } else {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Invalid email or password");
                }
            }

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
            out.print(responseObject);
            out.flush();
            out.close();
        }
    }
}

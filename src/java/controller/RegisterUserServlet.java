package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import org.hibernate.Session;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.regex.Pattern;

@WebServlet(name = "RegisterUserServlet", urlPatterns = {"/register"})
public class RegisterUserServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10}$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        PrintWriter out = response.getWriter();
        Session session = null;

        try {
            User user = gson.fromJson(request.getReader(), User.class);

            
            if (user.getFname() == null || user.getFname().trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "First name is required");
                out.print(responseObject);
                return;
            }

            
            if (user.getLname() == null || user.getLname().trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Last name is required");
                out.print(responseObject);
                return;
            }

            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Email is required");
                out.print(responseObject);
                return;
            }

            if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Invalid email format");
                out.print(responseObject);
                return;
            }

            
            if (user.getMobile() == null || user.getMobile().trim().isEmpty()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Mobile number is required");
                out.print(responseObject);
                return;
            }

            if (!MOBILE_PATTERN.matcher(user.getMobile()).matches()) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Mobile number must be exactly 10 digits");
                out.print(responseObject);
                return;
            }

            
            if (user.getPassword() == null || user.getPassword().length() < 6) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Password must be at least 6 characters");
                out.print(responseObject);
                return;
            }

            session = HibernateUtil.getSessionFactory().openSession();

            
            User existingUser = (User) session.get(User.class, user.getEmail());
            session.clear();
            if (existingUser != null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "Email already registered");
                out.print(responseObject);
                return;
            }

            
            session.save(user);
            session.beginTransaction().commit();

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Registration successful");
            out.print(responseObject);

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error: " + e.getMessage());
            out.print(responseObject);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
            out.flush();
            out.close();
        }
    }
}

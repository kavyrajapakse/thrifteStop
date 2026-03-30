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

@WebServlet(name = "UpdatePasswordServlet", urlPatterns = {"/update-password"})
public class UpdatePasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        JsonObject input = gson.fromJson(request.getReader(), JsonObject.class);
        String email = input.get("email").getAsString();
        String newPassword = input.get("password").getAsString();

        if (newPassword == null || newPassword.length() < 6) {
            out.print(error("Password must be at least 6 characters."));
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("email", email));
        User user = (User) criteria.uniqueResult();

        if (user != null) {
            user.setPassword(newPassword);
            user.setVerificationCode(null); // clear code

//            session.beginTransaction();
            session.update(user);
            session.beginTransaction().commit();
//            session.close();

            JsonObject success = new JsonObject();
            success.addProperty("status", "success");
            success.addProperty("message", "Password updated successfully!");
            out.print(success);
        } else {
            
            out.print(error("User not found."));
        }
        session.close();
    }

    private String error(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", msg);
        return json.toString();
    }
}


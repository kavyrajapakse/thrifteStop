package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import hibernate.EmailUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SendCodeServlet", urlPatterns = {"/send-code"})
public class SendCodeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            String email = json.get("email").getAsString();

            Session hSession = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = hSession.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));
            User user = (User) criteria.uniqueResult();

            if (user == null) {
                hSession.close();
                out.print(error("No user found with that email"));
                return;
            }

            // Generate and save verification code
            String code = String.valueOf(100000 + new Random().nextInt(900000));
            user.setVerificationCode(code);

//            hSession.beginTransaction();
            hSession.update(user);
            hSession.beginTransaction().commit();
            

            // Send code via email using utility class
            if (EmailUtil.sendVerificationCode(email, code)) {
                JsonObject success = new JsonObject();
                success.addProperty("status", "success");
                success.addProperty("message", "Verification code sent to " + email);
                out.print(success);
            } else {
                out.print(error("Failed to send verification email"));
            }
            
            hSession.close();

        } catch (Exception e) {
            e.printStackTrace();
            out.print(error("Unexpected server error"));
        } finally {
            out.flush();
            out.close();
        }
    }

    private String error(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", msg);
        return json.toString();
    }
}

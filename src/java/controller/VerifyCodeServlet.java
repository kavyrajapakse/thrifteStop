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

@WebServlet(name = "VerifyCodeServlet", urlPatterns = {"/verify-code"})
public class VerifyCodeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
        String email = json.get("email").getAsString();
        String code = json.get("code").getAsString();

        Session session = HibernateUtil.getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("email", email));
        criteria.add(Restrictions.eq("verificationCode", code));
        User user = (User) criteria.uniqueResult();
        session.close();

        if (user != null) {
            JsonObject success = new JsonObject();
            success.addProperty("status", "success");
            success.addProperty("message", "Verification successful");
            out.print(success);
        } else {
            out.print(error("Invalid code or email"));
        }
        
        
    }

    private String error(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("status", "error");
        json.addProperty("message", msg);
        return json.toString();
    }
}

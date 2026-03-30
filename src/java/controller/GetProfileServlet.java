package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.HibernateUtil;
import hibernate.User;
import org.hibernate.Session;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

@WebServlet(name = "GetProfileServlet", urlPatterns = {"/get-profile"})
public class GetProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("loggedInCustomer") == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "No user session found");
            out.print(gson.toJson(responseObject));
            return;
        }

        String email = (String) httpSession.getAttribute("loggedInCustomer");

        Session dbSession = null;
        try {
            dbSession = HibernateUtil.getSessionFactory().openSession();

            // Use session.get() for primary key fetch (assuming email is PK for User)
            User user = (User) dbSession.get(User.class, email);
            if (user == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "User not found");
                out.print(gson.toJson(responseObject));
                return;
            }

            // Fetch Address by email using Criteria (since Address PK is not email)
            Address address = (Address) dbSession.createCriteria(Address.class)
                    .add(org.hibernate.criterion.Restrictions.eq("usersEmail", email))
                    .uniqueResult();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            responseObject.addProperty("fname", user.getFname());
            responseObject.addProperty("lname", user.getLname());
            responseObject.addProperty("email", user.getEmail());
            responseObject.addProperty("mobile", user.getMobile());
            // Do NOT expose password:
             responseObject.addProperty("password", user.getPassword());
            responseObject.addProperty("joinedDate", user.getJoinedDate() != null ? sdf.format(user.getJoinedDate()) : "");

            if (address != null) {
                responseObject.addProperty("line1", address.getLine1());
                responseObject.addProperty("line2", address.getLine2());
                responseObject.addProperty("postalCode", address.getPostalCode());
                responseObject.addProperty("cityId", address.getCityId());
            }

            responseObject.addProperty("status", true);
            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Something went wrong");
            out.print(gson.toJson(responseObject));
        } finally {
            if (dbSession != null && dbSession.isOpen()) {
                dbSession.close();
            }
            out.flush();
            out.close();
        }
    }
}

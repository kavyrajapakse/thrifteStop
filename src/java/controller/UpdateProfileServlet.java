package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.HibernateUtil;
import hibernate.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "UpdateProfileServlet", urlPatterns = {"/update-profile"})
public class UpdateProfileServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        HttpSession httpSession = request.getSession(false);

        if (httpSession == null || httpSession.getAttribute("loggedInCustomer") == null) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Session expired. Please log in again.");
            response.getWriter().write(gson.toJson(responseObject));
            return;
        }

        String loggedInEmail = (String) httpSession.getAttribute("loggedInCustomer");
        JsonObject input = gson.fromJson(request.getReader(), JsonObject.class);

        String newFname = input.has("fname") ? input.get("fname").getAsString() : "";
        String newLname = input.has("lname") ? input.get("lname").getAsString() : "";
        String newEmail = input.has("email") ? input.get("email").getAsString() : "";
        String newMobile = input.has("mobile") ? input.get("mobile").getAsString() : "";
        String newPassword = input.has("password") ? input.get("password").getAsString() : "";
        String newAddress1 = input.has("address1") ? input.get("address1").getAsString() : "";
        String newAddress2 = input.has("address2") ? input.get("address2").getAsString() : "";
        int newCityId = input.has("cityId") ? input.get("cityId").getAsInt() : 0;
        String newPostalCode = input.has("postalCode") ? input.get("postalCode").getAsString() : "";

        // Basic email format validation
        if (newEmail == null || !newEmail.matches("^\\S+@\\S+\\.\\S+$")) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Invalid email format.");
            response.getWriter().write(gson.toJson(responseObject));
            return;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Check if email changed and if new email is already used
            if (!newEmail.equalsIgnoreCase(loggedInEmail)) {
                Criteria emailCheck = session.createCriteria(User.class);
                emailCheck.add(Restrictions.eq("email", newEmail));
                if (emailCheck.uniqueResult() != null) {
                    responseObject.addProperty("status", false);
                    responseObject.addProperty("message", "Email already in use.");
                    response.getWriter().write(gson.toJson(responseObject));
                    return;
                }
            }

            // Fetch user by PK email
            User user = (User) session.get(User.class, loggedInEmail);
            if (user == null) {
                responseObject.addProperty("status", false);
                responseObject.addProperty("message", "User not found.");
                response.getWriter().write(gson.toJson(responseObject));
                return;
            }

            Criteria addressCriteria = session.createCriteria(Address.class);
            addressCriteria.add(Restrictions.eq("usersEmail", user.getEmail()));
            Address address = (Address) addressCriteria.uniqueResult();

            if (address == null) {
                address = new Address();
                address.setUsersEmail(user.getEmail());
            }

            // Update user fields
            user.setFname(newFname);
            user.setLname(newLname);
            user.setMobile(newMobile);
            user.setPassword(newPassword);

            boolean emailChanged = !newEmail.equalsIgnoreCase(user.getEmail());
            user.setEmail(newEmail);

            // Update address fields
            address.setLine1(newAddress1);
            address.setLine2(newAddress2);
            address.setPostalCode(newPostalCode);
            address.setCityId(newCityId);

            if (emailChanged) {
                address.setUsersEmail(newEmail);
            }

            tx = session.beginTransaction();
            session.update(user);
            session.saveOrUpdate(address);
            tx.commit();

            if (emailChanged) {
                httpSession.setAttribute("loggedInCustomer", newEmail);
            }

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Profile and address updated successfully!");
            response.getWriter().write(gson.toJson(responseObject));

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Update failed: " + e.getMessage());
            response.getWriter().write(gson.toJson(responseObject));
        } finally {
            if (session != null && session.isOpen()) session.close();
            response.getWriter().flush();
            response.getWriter().close();
        }
    }
}

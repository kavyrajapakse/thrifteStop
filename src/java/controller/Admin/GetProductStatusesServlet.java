package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Status;
import org.hibernate.Criteria;
import org.hibernate.Session;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetProductStatusesServlet", urlPatterns = {"/get-product-statuses"})
public class GetProductStatusesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = null;
        JsonObject responseObject = new JsonObject();
        Gson gson = new Gson();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Status.class);
            List<Status> statusList = criteria.list();

            JsonArray statusArray = new JsonArray();
            for (Status status : statusList) {
                JsonObject s = new JsonObject();
                s.addProperty("id", status.getId());       // Make sure your entity has getId()
                s.addProperty("name", status.getName());   // And getName()
                statusArray.add(s);
            }

            responseObject.addProperty("status", true);
            responseObject.add("statuses", statusArray);
        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Error loading statuses");
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

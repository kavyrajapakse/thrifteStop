package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Size;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;

@WebServlet(name = "GetSizesServlet", urlPatterns = {"/get-sizes"})
public class GetSizesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        try {
            Criteria c = session.createCriteria(Size.class);
            List<Size> list = c.list();

            JsonArray array = new JsonArray();
            for (Size size : list) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", size.getId());
                obj.addProperty("name", size.getName());
                array.add(obj);
            }

            responseObject.add("categories", array);
            responseObject.addProperty("status", true);

            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load sizes.");
            out.print(gson.toJson(responseObject));
        } finally {
            session.close();
            out.close();
        }
    }
}

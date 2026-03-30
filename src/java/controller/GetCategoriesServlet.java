package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Category;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;

@WebServlet("/get-categories")
public class GetCategoriesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();

        try {
            Criteria c = session.createCriteria(Category.class);
            List<Category> list = c.list();

            JsonArray array = new JsonArray();
            for (Category cat : list) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", cat.getId());
                obj.addProperty("name", cat.getName());
                array.add(obj);
            }

            responseObject.add("categories", array);
            responseObject.addProperty("status", true);

            out.print(gson.toJson(responseObject));

        } catch (Exception e) {
            e.printStackTrace();

            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load categories.");
            out.print(gson.toJson(responseObject));
        } finally {
            session.close();
            out.close();
        }
    }
}

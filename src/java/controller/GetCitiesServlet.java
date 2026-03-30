package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.City;
import hibernate.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "GetCitiesServlet", urlPatterns = {"/get-cities"})
public class GetCitiesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Session session = null;
        PrintWriter out = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(City.class);
            List<City> cities = criteria.list();

            JsonObject responseObject = new JsonObject();
            responseObject.addProperty("status", true);
            responseObject.add("cities", new Gson().toJsonTree(cities));

            response.setContentType("application/json");
            out = response.getWriter();
            out.print(responseObject.toString());

        } catch (Exception e) {
            e.printStackTrace();

            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", false);
            errorResponse.addProperty("message", "Something went wrong while loading cities.");

            response.setContentType("application/json");
            try {
                if (out == null) out = response.getWriter();
                out.print(errorResponse.toString());
            } catch (Exception ignored) {}
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
}

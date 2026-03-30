/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.Admin;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.OrderStatus;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetOrderStatusesServlet", urlPatterns = {"/get-order-statuses"})
public class GetOrderStatusesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = null;
        JsonObject responseObject = new JsonObject();

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(OrderStatus.class);
            criteria.addOrder(Order.asc("id")); // Optional: order by ID or name
            List<OrderStatus> statuses = criteria.list();

            JsonArray statusArray = new JsonArray();
            Gson gson = new Gson();

            for (OrderStatus status : statuses) {
                JsonObject statusJson = new JsonObject();
                statusJson.addProperty("id", status.getId());
                statusJson.addProperty("name", status.getName());
                statusArray.add(statusJson);
            }

            responseObject.addProperty("status", true);
            responseObject.add("statuses", statusArray);

        } catch (Exception e) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Failed to load statuses");
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
    }
}

package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "SendInvoiceServlet", urlPatterns = {"/send-invoice"})
public class SendInvoiceServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        HttpSession httpSession = request.getSession();
        String email = (String) httpSession.getAttribute("loggedInCustomer");

        if (email == null) {
            out.print(error("User not logged in."));
            return;
        }

        Session s = HibernateUtil.getSessionFactory().openSession();

        try {
            // Load latest customer order
            Criteria orderCriteria = s.createCriteria(CustomerOrder.class);
            orderCriteria.add(Restrictions.eq("usersEmail", email));
            orderCriteria.addOrder(Order.desc("createdAt"));
            orderCriteria.setMaxResults(1);
            CustomerOrder order = (CustomerOrder) orderCriteria.uniqueResult();

            if (order == null) {
                out.print(error("No order found for this user."));
                return;
            }

            // Load order items
            Criteria itemCriteria = s.createCriteria(OrderItem.class);
            itemCriteria.add(Restrictions.eq("order", order));
            List<OrderItem> items = itemCriteria.list();

            if (items.isEmpty()) {
                out.print(error("No items found in this order."));
                return;
            }

            // Build HTML invoice
            StringBuilder html = new StringBuilder();
            html.append("<h2>🧾 Thrift eStop - Invoice</h2>");
            html.append("<p><strong>Order ID:</strong> ").append(order.getOrderId()).append("</p>");
            html.append("<p><strong>Order Date:</strong> ").append(order.getOrderDate()).append("</p>");
            html.append("<table border='1' cellpadding='8' cellspacing='0' style='border-collapse: collapse;'>");
            html.append("<thead><tr><th>Product</th><th>Price</th></tr></thead><tbody>");

            double total = 0;
            for (OrderItem item : items) {
                Product p = item.getProduct();
                double price = item.getPrice();
                total += price;

                html.append("<tr>")
                    .append("<td>").append(p.getTitle()).append("</td>")
                    .append("<td>Rs.").append(String.format("%.2f", price)).append("</td>")
                    .append("</tr>");
            }

            html.append("</tbody></table>");
            html.append("<h3>Total Paid: Rs.").append(String.format("%.2f", total)).append("</h3>");
            html.append("<p>Thank you for shopping with Thrift eStop! 🎉</p>");

            // Send email
            boolean sent = EmailUtil.sendInvoice(email, html.toString());

            JsonObject res = new JsonObject();
            if (sent) {
                res.addProperty("status", "success");
                res.addProperty("message", "Invoice sent to " + email);
            } else {
                res.addProperty("status", "error");
                res.addProperty("message", "Failed to send invoice email");
            }
            out.print(res);

        } catch (Exception e) {
            e.printStackTrace();
            out.print(error("Unexpected error occurred."));
        } finally {
            s.close();
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
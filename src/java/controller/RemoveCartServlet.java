package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@WebServlet(name = "RemoveCartServlet", urlPatterns = {"/RemoveCartServlet"})
public class RemoveCartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        String productId = request.getParameter("productId");

        if (productId == null || productId.isEmpty()) {
            responseObject.addProperty("message", "Invalid product ID");
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseObject));
            return;
        }

        HttpSession httpSession = request.getSession();
        String email = (String) httpSession.getAttribute("loggedInCustomer");

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            Product product = (Product) s.get(Product.class, productId);
            if (product == null) {
                responseObject.addProperty("message", "Product not found");
            } else if (email != null) {
                // Remove from DB Cart
                Transaction tx = s.beginTransaction();
                Criteria c = s.createCriteria(Cart.class);
                User user = (User) s.get(User.class, email);
                c.add(Restrictions.eq("customer", user));
                c.add(Restrictions.eq("product.productId", productId));
                Cart cart = (Cart) c.uniqueResult();

                if (cart != null) {
                    s.delete(cart);
                    tx.commit();
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Item removed from cart");
                } else {
                    responseObject.addProperty("message", "Item not found in your cart");
                }
            } 
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}

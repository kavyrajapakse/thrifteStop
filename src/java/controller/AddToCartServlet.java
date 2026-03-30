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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "AddToCartServlet", urlPatterns = {"/AddToCartServlet"})
public class AddToCartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tx = s.beginTransaction();

        HttpSession httpSession = request.getSession();
        String email = (String) httpSession.getAttribute("loggedInCustomer");
        httpSession.removeAttribute("sessionCart");

        User user = null;
        if (email != null) {
            Criteria uc = s.createCriteria(User.class);
            uc.add(Restrictions.eq("email", email));
            user = (User) uc.uniqueResult();
        }

        try {
            Product product = (Product) s.get(Product.class, productId);
            if (product == null) {
                responseObject.addProperty("message", "Product not found");
            } else {
                if (user != null) {
                    Criteria c = s.createCriteria(Cart.class);
                    c.add(Restrictions.eq("customer", user));
                    c.add(Restrictions.eq("product", product));
                    List<Cart> existing = c.list();

                    if (existing.isEmpty()) {
                        Cart cart = new Cart();
                        cart.setCustomer(user);
                        cart.setProduct(product);
                        cart.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                        s.save(cart);
                        tx.commit(); // only commit if save happened
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "Product added to cart");
                    } else {
                        tx.rollback(); 
                        responseObject.addProperty("message", "Product is already in your cart");
                    }
                }
            }
        } catch (Exception e) {
            tx.rollback();
            responseObject.addProperty("message", "Something went wrong");
            e.printStackTrace(); // 🔍 log for debugging
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}

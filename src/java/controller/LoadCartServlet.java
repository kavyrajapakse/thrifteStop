package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductImage;
import hibernate.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "LoadCartServlet", urlPatterns = {"/LoadCartServlet"})
public class LoadCartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession();
        String email = (String) httpSession.getAttribute("loggedInCustomer");
         httpSession.removeAttribute("sessionCart");

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            List<Cart> cartList;

            if (email == null) {
                responseObject.addProperty("message", "User not logged in.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseObject));
                return;
            }

            User user = (User) s.get(User.class, email);
            Criteria c = s.createCriteria(Cart.class);
            c.add(Restrictions.eq("customer", user));
            cartList = c.list();

            if (cartList == null || cartList.isEmpty()) {
                responseObject.addProperty("message", "Your Cart is Empty...");
            } else {
                JsonObject[] enrichedCartItems = new JsonObject[cartList.size()];
                for (int i = 0; i < cartList.size(); i++) {
                    Cart cart = cartList.get(i);

                    // Remove sensitive fields
                    if (cart.getCustomer() != null) {
                        cart.setCustomer(null);
                    }
                    Product product = cart.getProduct();
                    product.setCondition(null);
                    product.setCategory(null);
                    product.setStatus(null);
                    product.setSize(null);

                    // Fetch main image (first one)
                    Criteria imgCriteria = s.createCriteria(ProductImage.class);
                    imgCriteria.add(Restrictions.eq("product", product));
                    imgCriteria.setMaxResults(1);
                    ProductImage image = (ProductImage) imgCriteria.uniqueResult();

                    JsonObject itemJson = (JsonObject) gson.toJsonTree(cart);
                    itemJson.addProperty("imagePath", image != null ? image.getImgPath() : "");

                    enrichedCartItems[i] = itemJson;
                }

                responseObject.addProperty("status", true);
                responseObject.add("cartItems", gson.toJsonTree(enrichedCartItems));
            }
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}

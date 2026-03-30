package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LoadCartSummaryServlet", urlPatterns = {"/LoadCartSummaryServlet"})
public class LoadCartSummaryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession();
        String email = (String) httpSession.getAttribute("loggedInCustomer");

        if (email == null) {
            responseObject.addProperty("message", "User not logged in.");
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseObject));
            return;
        }

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            User user = (User) s.get(User.class, email);
            if (user == null) {
                responseObject.addProperty("message", "Invalid user.");
                return;
            }

            // Get user address
            Criteria addressCriteria = s.createCriteria(Address.class);
            addressCriteria.add(Restrictions.eq("usersEmail", email));
            addressCriteria.setMaxResults(1);
            Address address = (Address) addressCriteria.uniqueResult();

            double shippingCost = 0.0;
            if (address != null) {
                City city = (City) s.get(City.class, address.getCityId());
                if (city != null && city.getShippingCost() != null) {
                    shippingCost = city.getShippingCost();
                    responseObject.addProperty("city", city.getCityName());
                }
            }

            // Load cart items
            Criteria cartCriteria = s.createCriteria(Cart.class);
            cartCriteria.add(Restrictions.eq("customer", user));
            List<Cart> cartList = cartCriteria.list();

            if (cartList == null || cartList.isEmpty()) {
                responseObject.addProperty("message", "Your cart is empty.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseObject));
                return;
            }

            JsonObject[] enrichedCartItems = new JsonObject[cartList.size()];
            double subtotal = 0.0;
            int itemCount = 0;

            for (int i = 0; i < cartList.size(); i++) {
                Cart cart = cartList.get(i);
                Product product = cart.getProduct();
                subtotal += product.getPrice();
                itemCount++;

                // Avoid exposing unnecessary fields
                product.setCategory(null);
                product.setCondition(null);
                product.setSize(null);
                product.setStatus(null);

                Criteria imgCriteria = s.createCriteria(ProductImage.class);
                imgCriteria.add(Restrictions.eq("product", product));
                imgCriteria.setMaxResults(1);
                ProductImage image = (ProductImage) imgCriteria.uniqueResult();

                JsonObject cartJson = (JsonObject) gson.toJsonTree(cart);
                cartJson.addProperty("imagePath", image != null ? image.getImgPath() : "");
                enrichedCartItems[i] = cartJson;
            }

            double grandTotal = subtotal + shippingCost;

            responseObject.addProperty("status", true);
            responseObject.add("cartItems", gson.toJsonTree(enrichedCartItems));
            responseObject.addProperty("subtotal", subtotal);
            responseObject.addProperty("shippingCost", shippingCost);
            responseObject.addProperty("grandTotal", grandTotal);
            responseObject.addProperty("itemCount", itemCount);

        } catch (Exception e) {
            responseObject.addProperty("message", "An error occurred: " + e.getMessage());
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}

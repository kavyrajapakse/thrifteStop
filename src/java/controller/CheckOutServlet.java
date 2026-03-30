package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.*;
import model.PayHere;
import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.List;

@WebServlet(name = "CheckOutServlet", urlPatterns = {"/CheckOutServlet"})
public class CheckOutServlet extends HttpServlet {

    private static final int PENDING_STATUS_ID = 1;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession httpSession = request.getSession();
        String email = (String) httpSession.getAttribute("loggedInCustomer");

        if (email == null) {
            responseObject.addProperty("message", "Session expired. Please login again.");
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseObject));
            return;
        }

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        Transaction tx = s.beginTransaction();

        try {
            User user = (User) s.get(User.class, email);

            Criteria addressCriteria = s.createCriteria(Address.class);
            addressCriteria.add(Restrictions.eq("usersEmail", email));
            addressCriteria.setMaxResults(1);
            Address address = (Address) addressCriteria.uniqueResult();

            if (address == null) {
                responseObject.addProperty("message", "Please add your shipping address in your profile first.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseObject));
                return;
            }

            City city = (City) s.get(City.class, address.getCityId());
            double shippingCost = city != null ? city.getShippingCost() : 0.0;

            Criteria cartCriteria = s.createCriteria(Cart.class);
            cartCriteria.add(Restrictions.eq("customer", user));
            List<Cart> cartList = cartCriteria.list();

            if (cartList == null || cartList.isEmpty()) {
                responseObject.addProperty("message", "Your cart is empty.");
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(responseObject));
                return;
            }

            // Get last order id
            Criteria lastOrderCriteria = s.createCriteria(CustomerOrder.class);
            lastOrderCriteria.addOrder(Order.desc("orderId"));
            lastOrderCriteria.setMaxResults(1);
            List<CustomerOrder> orders = lastOrderCriteria.list();

            int nextId = 1;
            if (!orders.isEmpty()) {
                String lastId = orders.get(0).getOrderId(); // ORD-005
                String[] parts = lastId.split("-");
                nextId = Integer.parseInt(parts[1]) + 1;
            }
            String newOrderId = String.format("ORD-%03d", nextId);

            // Create order
            CustomerOrder order = new CustomerOrder();
            order.setOrderId(newOrderId);
            order.setUsersEmail(email);
            order.setAddressId(address.getAddressId());
            order.setOrderDate(new Date(System.currentTimeMillis()));
            order.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            order.setStatus((OrderStatus) s.get(OrderStatus.class, PENDING_STATUS_ID));

            double total = 0;
            int itemCount = 0;

            s.save(order);

            System.out.println(cartList.size());
            for (Cart cart : cartList) {
                OrderItem item = new OrderItem();
                Product product = cart.getProduct();

                item.setOrder(order);
                item.setProduct(product);
                item.setPrice(product.getPrice());

                // Mark the product as sold out (status_id = 2)
                Status soldOutStatus = (Status) s.get(Status.class, 2);
                product.setStatus(soldOutStatus);
                s.update(product);

                total += product.getPrice();
                itemCount++;

                s.save(item);
                s.delete(cart);
            }

            total += shippingCost;

            order.setTotalAmount(total);
            s.update(order);

            tx.commit();

            // --- PayHere setup
            String merchantId = "1222311";
            String merchantSecret = "ODE5MDQ1MTQ1MTQ1ODQzMDc5NDMxNjM2NDI5ODYzMzE4MzQ3NTIy";
            String currency = "LKR";
            String formattedAmount = new DecimalFormat("0.00").format(total);
            String orderRef = newOrderId;

            String hash = PayHere.generateMD5(merchantId + orderRef + formattedAmount + currency + PayHere.generateMD5(merchantSecret));

            JsonObject payHereJson = new JsonObject();
            payHereJson.addProperty("sandbox", true);
            payHereJson.addProperty("merchant_id", merchantId);
            payHereJson.addProperty("return_url", "http://localhost:8080/ThrifteStop/invoice.html");
            payHereJson.addProperty("cancel_url", "http://localhost:8080/ThrifteStop/cart.html");
            payHereJson.addProperty("notify_url", "http://localhost:8080/ThrifteStop/VerifyPayments");

            payHereJson.addProperty("order_id", orderRef);
            payHereJson.addProperty("items", itemCount + " item(s)");
            payHereJson.addProperty("amount", formattedAmount);
            payHereJson.addProperty("currency", currency);
            payHereJson.addProperty("hash", hash);

            payHereJson.addProperty("first_name", user.getFname());
            payHereJson.addProperty("last_name", user.getLname());
            payHereJson.addProperty("email", user.getEmail());
            payHereJson.addProperty("phone", user.getMobile());
            payHereJson.addProperty("address", address.getLine1() + ", " + address.getLine2());
            payHereJson.addProperty("city", city.getCityName());
            payHereJson.addProperty("country", "Sri Lanka");

            responseObject.addProperty("status", true);
            responseObject.add("payhereJson", gson.toJsonTree(payHereJson));
            httpSession.removeAttribute("sessionCart");

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            responseObject.addProperty("message", "Checkout failed. Please try again.");
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}
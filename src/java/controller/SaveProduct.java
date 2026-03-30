package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Category;
import hibernate.Condition;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductImage;
import hibernate.Size;
import hibernate.Status;
import model.Util;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import org.hibernate.Session;

@WebServlet(name = "SaveProduct", urlPatterns = {"/SaveProduct"})
@MultipartConfig
public class SaveProduct extends HttpServlet {

    private static final int PENDING_STATUS_ID = 1;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject resJson = new JsonObject();
        resJson.addProperty("status", false);

        // Get form data
        String productId = request.getParameter("productId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String categoryId = request.getParameter("categoryId");
        String conditionId = request.getParameter("conditionId");
        String sizeId = request.getParameter("sizeId");
        String stockStatus = request.getParameter("stockStatus");
        String price = request.getParameter("price");

        Part image1 = request.getPart("image1");
        Part image2 = request.getPart("image2");
        Part image3 = request.getPart("image3");

        // Validation
        if (title == null || title.trim().isEmpty()) {
            resJson.addProperty("message", "Product title is required.");
        } else if (description == null || description.trim().isEmpty()) {
            resJson.addProperty("message", "Product description is required.");
        } else if (categoryId == null || categoryId.trim().isEmpty()) {
            resJson.addProperty("message", "Please select a category.");
        } else if (conditionId == null || conditionId.trim().isEmpty()) {
            resJson.addProperty("message", "Please select a condition.");
        } else if (sizeId == null || sizeId.trim().isEmpty()) {
            resJson.addProperty("message", "Please select a size.");
        } else if (!Util.isDouble(price)) {
            resJson.addProperty("message", "Invalid price format.");
        } else if (Double.parseDouble(price) <= 0) {
            resJson.addProperty("message", "Price must be greater than 0.");
        } else if (stockStatus == null || (!stockStatus.equals("in_stock") && !stockStatus.equals("out_of_stock"))) {
            resJson.addProperty("message", "Please select stock status.");
        } else if (image1 == null || image2 == null || image3 == null
                || image1.getSize() == 0 || image2.getSize() == 0 || image3.getSize() == 0) {
            resJson.addProperty("message", "All 3 product images are required.");
        } else {
            Session session = null;
            try {
                session = HibernateUtil.getSessionFactory().openSession();

                Category category = (Category) session.get(Category.class, Integer.parseInt(categoryId));
                Condition condition = (Condition) session.get(Condition.class, Integer.parseInt(conditionId));
                Size size = (Size) session.get(Size.class, Integer.parseInt(sizeId));
                Status status = (Status) session.get(Status.class, PENDING_STATUS_ID);

                if (category == null || condition == null || size == null || status == null) {
                    resJson.addProperty("message", "Invalid reference data (category, condition, size, or status).");
                } else {
                    Product product = new Product();
                    product.setProductId(productId);
                    product.setTitle(title);
                    product.setDescription(description);
                    product.setPrice(Double.parseDouble(price));
                    product.setDatetimeAdded(new Date());
                    product.setCategory(category);
                    product.setCondition(condition);
                    product.setSize(size);
                    product.setStatus(status);

                    session.beginTransaction();
                    session.save(product);
                    session.getTransaction().commit();

                    // Save product images
                    String appPath = getServletContext().getRealPath("/");
                    String imageFolderPath = appPath.replace("build\\web", "web\\resources\\items");

                    File folder = new File(imageFolderPath);
                    folder.mkdirs(); // Ensure folder exists

                    Files.copy(image1.getInputStream(), new File(folder, productId + "_1.jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(image2.getInputStream(), new File(folder, productId + "_2.jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(image3.getInputStream(), new File(folder, productId + "_3.jpg").toPath(), StandardCopyOption.REPLACE_EXISTING);

                    session.beginTransaction();
                    ProductImage img1 = new ProductImage();
                    img1.setImgPath("resources/items/" + productId + "_1.jpg");
                    img1.setProduct(product);
                    session.save(img1);

                    ProductImage img2 = new ProductImage();
                    img2.setImgPath("resources/items/" + productId + "_2.jpg");
                    img2.setProduct(product);
                    session.save(img2);

                    ProductImage img3 = new ProductImage();
                    img3.setImgPath("resources/items/" + productId + "_3.jpg");
                    img3.setProduct(product);
                    session.save(img3);
                    session.getTransaction().commit();

                    resJson.addProperty("status", true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                resJson.addProperty("message", "Warning: " + e.getMessage());
            }
//            } finally {
//                if (session != null && session.isOpen()) {
//                    session.close();
//                }
//            }
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(resJson));
    }
}

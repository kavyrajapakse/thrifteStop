package controller.Admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.*;
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
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "UpdateProductServlet", urlPatterns = {"/UpdateProduct"})
@MultipartConfig
public class UpdateProductServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject resJson = new JsonObject();
        resJson.addProperty("status", false);

        String productId = request.getParameter("productId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String categoryId = request.getParameter("categoryId");
        String conditionId = request.getParameter("conditionId");
        String sizeId = request.getParameter("sizeId");
        String price = request.getParameter("price");

        Part image1 = request.getPart("image1");
        Part image2 = request.getPart("image2");
        Part image3 = request.getPart("image3");

        // Validate
        if (productId == null || productId.trim().isEmpty()) {
            resJson.addProperty("message", "Product ID is required.");
        } else if (title == null || title.trim().isEmpty()) {
            resJson.addProperty("message", "Title is required.");
        } else if (description == null || description.trim().isEmpty()) {
            resJson.addProperty("message", "Description is required.");
        } else if (categoryId == null || categoryId.trim().isEmpty()) {
            resJson.addProperty("message", "Please select a category.");
        } else if (conditionId == null || conditionId.trim().isEmpty()) {
            resJson.addProperty("message", "Please select a condition.");
        } else if (sizeId == null || sizeId.trim().isEmpty()) {
            resJson.addProperty("message", "Please select a size.");
        } else if (!Util.isDouble(price)) {
            resJson.addProperty("message", "Invalid price.");
        } else {
            Session session = null;
            try {
                session = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) session.get(Product.class, productId);
                if (product == null) {
                    resJson.addProperty("message", "Product not found.");
                } else {
                    Category category = (Category) session.get(Category.class, Integer.parseInt(categoryId));
                    Condition condition = (Condition) session.get(Condition.class, Integer.parseInt(conditionId));
                    Size size = (Size) session.get(Size.class, Integer.parseInt(sizeId));

                    if (category == null || condition == null || size == null) {
                        resJson.addProperty("message", "Invalid category, condition, or size.");
                    } else {
                        session.beginTransaction();

                        product.setTitle(title);
                        product.setDescription(description);
                        product.setPrice(Double.parseDouble(price));
                        product.setCategory(category);
                        product.setCondition(condition);
                        product.setSize(size);
                        session.update(product);
                        session.getTransaction().commit();

                        // Handle images only if new ones are uploaded
                        String appPath = getServletContext().getRealPath("/");
                        String imageFolderPath = appPath.replace("build\\web", "web\\resources\\items");
                        File folder = new File(imageFolderPath);
                        folder.mkdirs();

                        // Delete existing images
                        session.beginTransaction();
                        Criteria criteria = session.createCriteria(ProductImage.class);
                        criteria.add(Restrictions.eq("product", product));
                        List<ProductImage> existingImages = criteria.list();
                        for (ProductImage img : existingImages) {
                            session.delete(img);
                        }
                        session.getTransaction().commit();

                        // Save only the ones uploaded
                        session.beginTransaction();
                        if (image1 != null && image1.getSize() > 0) {
                            String imgName = productId + "_1.jpg";
                            Files.copy(image1.getInputStream(), new File(folder, imgName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            ProductImage pi1 = new ProductImage();
                            pi1.setImgPath("resources/items/" + imgName);
                            pi1.setProduct(product);
                            session.save(pi1);
                        }
                        if (image2 != null && image2.getSize() > 0) {
                            String imgName = productId + "_2.jpg";
                            Files.copy(image2.getInputStream(), new File(folder, imgName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            ProductImage pi2 = new ProductImage();
                            pi2.setImgPath("resources/items/" + imgName);
                            pi2.setProduct(product);
                            session.save(pi2);
                        }
                        if (image3 != null && image3.getSize() > 0) {
                            String imgName = productId + "_3.jpg";
                            Files.copy(image3.getInputStream(), new File(folder, imgName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            ProductImage pi3 = new ProductImage();
                            pi3.setImgPath("resources/items/" + imgName);
                            pi3.setProduct(product);
                            session.save(pi3);
                        }
                        session.getTransaction().commit();

                        resJson.addProperty("status", true);
                        resJson.addProperty("message", "Product updated successfully.");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                resJson.addProperty("message", "Server error: " + e.getMessage());
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

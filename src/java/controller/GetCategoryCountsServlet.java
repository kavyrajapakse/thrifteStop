package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Category;
import hibernate.HibernateUtil;
import hibernate.Product;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "GetCategoryCountsServlet", urlPatterns = {"/get-category-counts"})
public class GetCategoryCountsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        try {
            List<String> excludeList = Arrays.asList("Bags", "Shoes");
            JsonArray categoryArray = new JsonArray();

            // Bags
            Category bagsCategory = (Category) session.createCriteria(Category.class)
                    .add(Restrictions.eq("name", "Bags"))
                    .uniqueResult();

            long bagsCount = 0;
            if (bagsCategory != null) {
                Criteria bagCriteria = session.createCriteria(Product.class);
                bagCriteria.add(Restrictions.eq("category.id", bagsCategory.getId()));
                bagCriteria.createAlias("status", "statusAlias");
                bagCriteria.add(Restrictions.eq("statusAlias.id", 1));
                bagCriteria.setProjection(Projections.rowCount());
                bagsCount = (Long) bagCriteria.uniqueResult();

                JsonObject bagsJson = new JsonObject();
                bagsJson.addProperty("name", "Bags");
                bagsJson.addProperty("count", bagsCount);
                categoryArray.add(bagsJson);
            }

            // Shoes
            Category shoesCategory = (Category) session.createCriteria(Category.class)
                    .add(Restrictions.eq("name", "Shoes"))
                    .uniqueResult();

            long shoesCount = 0;
            if (shoesCategory != null) {
                Criteria shoesCriteria = session.createCriteria(Product.class);
                shoesCriteria.add(Restrictions.eq("category.id", shoesCategory.getId()));
                shoesCriteria.createAlias("status", "statusAlias");
                shoesCriteria.add(Restrictions.eq("statusAlias.id", 1));
                shoesCriteria.setProjection(Projections.rowCount());
                shoesCount = (Long) shoesCriteria.uniqueResult();

                JsonObject shoesJson = new JsonObject();
                shoesJson.addProperty("name", "Shoes");
                shoesJson.addProperty("count", shoesCount);
                categoryArray.add(shoesJson);
            }

            // Clothes (others)
            Criteria otherCategoryCriteria = session.createCriteria(Category.class);
            otherCategoryCriteria.add(Restrictions.not(Restrictions.in("name", excludeList)));
            List<Category> clothesCategories = otherCategoryCriteria.list();

            long clothesCount = 0;
            for (Category category : clothesCategories) {
                Criteria productCountCriteria = session.createCriteria(Product.class);
                productCountCriteria.add(Restrictions.eq("category.id", category.getId()));
                productCountCriteria.createAlias("status", "statusAlias");
                productCountCriteria.add(Restrictions.eq("statusAlias.id", 1));
                productCountCriteria.setProjection(Projections.rowCount());

                Long count = (Long) productCountCriteria.uniqueResult();
                clothesCount += count != null ? count : 0;
            }

            JsonObject clothesJson = new JsonObject();
            clothesJson.addProperty("name", "Clothes");
            clothesJson.addProperty("count", clothesCount);
            categoryArray.add(clothesJson);

            // Success response
            responseJson.addProperty("status", true);
            responseJson.add("categories", categoryArray);
            out.print(gson.toJson(responseJson));

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Failed to get category counts.");
            out.print(gson.toJson(responseJson));
        } finally {
            session.close();
            out.close();
        }
    }
}

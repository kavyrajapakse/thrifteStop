
package model;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/addNewProduct.html", "/adminCustomer.html","/adminInventory.html","/adminOrder.html","/adminSale.html","/updateProduct.html"})
public class AdminSignInCheckFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession ses = request.getSession(false);
        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "8");
        
        if (ses != null && ses.getAttribute("admin") != null) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect("adminSignIn.html");
        }
        
        
    }

    @Override
    public void destroy() {
        
    }
    
    
    
}

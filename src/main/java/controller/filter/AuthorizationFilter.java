package controller.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(urlPatterns = "/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Lấy đường dẫn yêu cầu
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        // Lấy session(nếu có)
        HttpSession session = httpRequest.getSession();
        // Kiểm tra vai trò người dùng
        String role = null;
        if (session != null && session.getAttribute("role") != null) {
            role = (String) session.getAttribute("role");
        }




        if (uri.startsWith(contextPath + "/uploads/") || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg") || uri.endsWith(".gif") || uri.endsWith(".woff") || uri.endsWith(".ttf")
                || uri.endsWith(".eot") || uri.endsWith(".svg")) {
            // Tài nguyên tĩnh, bỏ qua filter
            chain.doFilter(request, response);
            return;
        }
        // Phân quyền dựa trên vai trò và URL
        if (uri.startsWith(contextPath + "/public/") ) {
            chain.doFilter(request, response);
            return;
        } else if (uri.startsWith(contextPath + "/admin/")) {
            if ("ADMIN".equals(role)) {
                chain.doFilter(request, response);
                return;
            } else {
                httpResponse.sendRedirect(contextPath + "/access-denied");
                return;
            }
        } else if (uri.startsWith(contextPath + "/customer/")) {
            if ("ADMIN".equals(role) || "CUSTOMER".equals(role)) {
                chain.doFilter(request, response);
                return;
            } else {
                httpResponse.sendRedirect(contextPath + "/public/login");
                return;
            }
        }
        chain.doFilter(request, response);

    }

}

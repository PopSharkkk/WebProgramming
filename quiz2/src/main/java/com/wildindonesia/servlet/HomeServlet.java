package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());

        // Forward static resources (assets/, wild-hero.html, favicon.ico) to Tomcat's default static servlet
        if (!"/".equals(path) && !"".equals(path)) {
            RequestDispatcher rd = getServletContext().getNamedDispatcher("default");
            if (rd != null) {
                rd.forward(req, resp);
                return;
            }
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            req.setAttribute("counts", DataStore.getPlatformCounts());
            req.setAttribute("featuredDestinations", DataStore.featuredDestinations(6));
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Failed loading homepage guide data", e);
        }
    }
}

package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.db.DatabaseManager;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class UtilityServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        try {
            if ("/database-diagrams".equals(servletPath)) {
                req.setAttribute("pageTitle", "CDM & PDM Database Architecture — Wild Indonesia");
                req.setAttribute("dbDialect", DatabaseManager.isMySql() ? "MySQL JDBC" : "SQLite JDBC");
                req.setAttribute("counts", DataStore.getPlatformCounts());
                WebUtil.render(req, resp, "utility/diagrams.jsp");
                return;
            }

            String role = WebUtil.param(req, "role", "");
            String search = WebUtil.param(req, "search", "");
            req.setAttribute("pageTitle", "QA Registered Accounts Inspector — Wild Indonesia");
            req.setAttribute("users", DataStore.listUsersWithStats(role, search));
            req.setAttribute("counts", DataStore.getPlatformCounts());
            req.setAttribute("roleFilter", role);
            req.setAttribute("search", search);
            req.setAttribute("dbEngine", DatabaseManager.isMySql() ? "MySQL" : "SQLite (" + DatabaseManager.getDbUrl() + ")");
            WebUtil.render(req, resp, "utility/debug_accounts.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading utility view", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if ("/accounts/switch".equals(pathInfo)) {
                long userId = WebUtil.parseLong(req.getParameter("user_id"), 0);
                Map<String, Object> target = DataStore.findUserById(userId);
                if (target == null) {
                    WebUtil.redirectWithError(req, resp, "/debug/accounts", "User not found.");
                    return;
                }
                HttpSession s = req.getSession(true);
                s.setAttribute("currentUser", target);
                WebUtil.redirectWithSuccess(req, resp, "/destinations",
                        "QA Session Switched: Now signed in as " + target.get("full_name") + " (" + target.get("role") + ").");
            } else if ("/accounts/delete".equals(pathInfo)) {
                long userId = WebUtil.parseLong(req.getParameter("user_id"), 0);
                Long cur = WebUtil.currentUserId(req);
                if (cur != null && cur == userId) {
                    req.getSession().removeAttribute("currentUser");
                }
                DataStore.deleteUser(userId);
                WebUtil.redirectWithSuccess(req, resp, "/debug/accounts", "Test user #" + userId + " deleted.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error executing QA account action", e);
        }
    }
}

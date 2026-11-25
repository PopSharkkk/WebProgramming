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

public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/login".equals(path)) {
            req.setAttribute("pageTitle", "Sign In — Wild Indonesia");
            WebUtil.render(req, resp, "auth/login.jsp");
        } else if ("/register".equals(path)) {
            req.setAttribute("pageTitle", "Join Wild Indonesia");
            WebUtil.render(req, resp, "auth/register.jsp");
        } else if ("/profile".equals(path)) {
            if (!WebUtil.requireAuth(req, resp)) return;
            req.setAttribute("pageTitle", "Traveler Profile — Wild Indonesia");
            WebUtil.render(req, resp, "auth/profile.jsp");
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            if ("/login".equals(path)) {
                handleLogin(req, resp);
            } else if ("/register".equals(path)) {
                handleRegister(req, resp);
            } else if ("/logout".equals(path)) {
                HttpSession s = req.getSession(false);
                if (s != null) s.invalidate();
                WebUtil.redirectWithSuccess(req, resp, "/", "You have signed out cleanly.");
            } else if ("/profile".equals(path)) {
                handleProfileUpdate(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException("Authentication error", e);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        String email = WebUtil.param(req, "email", "").toLowerCase();
        String password = WebUtil.param(req, "password", "");

        Map<String, Object> user = DataStore.findUserByEmail(email);
        if (user == null || !DatabaseManager.verifyPassword(password, String.valueOf(user.get("password_hash")))) {
            WebUtil.redirectWithError(req, resp, "/login", "Invalid email or password. Try curator@wildindonesia.id / password.");
            return;
        }

        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession fresh = req.getSession(true);
        fresh.setAttribute("currentUser", user);
        WebUtil.redirectWithSuccess(req, resp, "/destinations", "Welcome back, " + user.get("full_name") + ".");
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        String fullName = WebUtil.param(req, "full_name", "");
        String email = WebUtil.param(req, "email", "").toLowerCase();
        String password = WebUtil.param(req, "password", "");
        String role = "curator".equals(WebUtil.param(req, "role", "traveler")) ? "curator" : "traveler";
        String nationality = WebUtil.param(req, "nationality", "Indonesia");
        String phone = WebUtil.param(req, "phone", "");
        String bio = WebUtil.param(req, "bio", "Exploring the Indonesian archipelago.");

        if (fullName.isBlank() || email.isBlank() || password.length() < 4) {
            WebUtil.redirectWithError(req, resp, "/register", "Please provide a valid name, email, and password (at least 4 characters).");
            return;
        }
        if (DataStore.findUserByEmail(email) != null) {
            WebUtil.redirectWithError(req, resp, "/register", "That email is already registered.");
            return;
        }

        long id;
        try {
            id = DataStore.createUser(fullName, email, DatabaseManager.hashPassword(password), role, nationality, phone, bio);
        } catch (SQLException e) {
            if (WebUtil.isConstraintViolation(e)) {
                WebUtil.redirectWithError(req, resp, "/register", "That email is already registered.");
                return;
            }
            throw e;
        }
        Map<String, Object> user = DataStore.findUserById(id);
        req.getSession(true).setAttribute("currentUser", user);
        WebUtil.redirectWithSuccess(req, resp, "/destinations", "Account created for " + fullName + ". Start planning your Indonesia trip!");
    }

    private void handleProfileUpdate(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        Long userId = WebUtil.currentUserId(req);
        String fullName = WebUtil.param(req, "full_name", "");
        String nationality = WebUtil.param(req, "nationality", "Indonesia");
        String phone = WebUtil.param(req, "phone", "");
        String bio = WebUtil.param(req, "bio", "");

        if (fullName.isBlank()) {
            WebUtil.redirectWithError(req, resp, "/profile", "Full name cannot be empty.");
            return;
        }
        DataStore.updateUserProfile(userId, fullName, nationality, phone, bio);
        req.getSession(true).setAttribute("currentUser", DataStore.findUserById(userId));
        WebUtil.redirectWithSuccess(req, resp, "/profile", "Traveler profile updated.");
    }
}

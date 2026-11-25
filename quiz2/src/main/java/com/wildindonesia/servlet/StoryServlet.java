package com.wildindonesia.servlet;

import com.wildindonesia.dao.DataStore;
import com.wildindonesia.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class StoryServlet extends HttpServlet {
    private static final String DEFAULT_IMG = "https://images.unsplash.com/photo-1576363399501-0dd08fc750e1?auto=format&fit=crop&w=1200&q=80";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long destId = WebUtil.parseLong(req.getParameter("destination_id"), 0);
            req.setAttribute("activeNav", "stories");
            req.setAttribute("pageTitle", "Traveler Field Stories & Tips — Wild Indonesia");
            req.setAttribute("stories", DataStore.listStories(destId > 0 ? destId : null));
            req.setAttribute("destinations", DataStore.listDestinations(null, null, null, null, "name"));
            req.setAttribute("selectedDestId", destId);

            long editId = WebUtil.parseLong(req.getParameter("edit_id"), 0);
            if (editId > 0) {
                req.setAttribute("editing", DataStore.findStory(editId));
            }
            WebUtil.render(req, resp, "stories/index.jsp");
        } catch (SQLException e) {
            throw new ServletException("Error loading travel stories", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!WebUtil.requireAuth(req, resp)) return;
        String pathInfo = req.getPathInfo();
        try {
            if ("/save".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                long userId = WebUtil.currentUserId(req);
                long destId = WebUtil.parseLong(req.getParameter("destination_id"), 1);
                String title = WebUtil.param(req, "title", "");
                int rating = Math.min(5, Math.max(1, WebUtil.parseInt(req.getParameter("rating"), 5)));
                String visitedMonth = WebUtil.param(req, "visited_month", "November 2026");
                String body = WebUtil.param(req, "story_body", "");
                String photo = WebUtil.safePhotoOrFallback(req.getParameter("photo_url"), DEFAULT_IMG);
                String tip = WebUtil.param(req, "travel_tip", "");

                if (title.isBlank() || body.isBlank()) {
                    WebUtil.redirectWithError(req, resp, "/stories", "Story title and body are required.");
                    return;
                }
                if (id > 0) {
                    var existing = DataStore.findStory(id);
                    if (existing == null || (!WebUtil.isCurator(req) && ((Number) existing.get("user_id")).longValue() != userId)) {
                        WebUtil.redirectWithError(req, resp, "/stories", "You do not have permission to edit this story.");
                        return;
                    }
                    DataStore.updateStory(id, destId, title, rating, visitedMonth, body, photo, tip);
                    WebUtil.redirectWithSuccess(req, resp, "/stories", "Updated field story: " + title);
                } else {
                    DataStore.createStory(userId, destId, title, rating, visitedMonth, body, photo, tip);
                    WebUtil.redirectWithSuccess(req, resp, "/stories", "Published new field story: " + title);
                }
            } else if ("/delete".equals(pathInfo)) {
                long id = WebUtil.parseLong(req.getParameter("id"), 0);
                var existing = DataStore.findStory(id);
                if (existing == null || (!WebUtil.isCurator(req) && ((Number) existing.get("user_id")).longValue() != WebUtil.currentUserId(req))) {
                    WebUtil.redirectWithError(req, resp, "/stories", "You do not have permission to delete this story.");
                    return;
                }
                DataStore.deleteStory(id);
                WebUtil.redirectWithSuccess(req, resp, "/stories", "Field story removed.");
            }
        } catch (SQLException e) {
            throw new ServletException("Error saving travel story", e);
        }
    }
}

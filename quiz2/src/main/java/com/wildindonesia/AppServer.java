package com.wildindonesia;

import com.wildindonesia.db.DatabaseManager;
import com.wildindonesia.servlet.AuthServlet;
import com.wildindonesia.servlet.BookingServlet;
import com.wildindonesia.servlet.DestinationServlet;
import com.wildindonesia.servlet.GuideServlet;
import com.wildindonesia.servlet.HomeServlet;
import com.wildindonesia.servlet.ItineraryServlet;
import com.wildindonesia.servlet.RegionServlet;
import com.wildindonesia.servlet.RentalServlet;
import com.wildindonesia.servlet.SavedPlaceServlet;
import com.wildindonesia.servlet.StoryServlet;
import com.wildindonesia.servlet.UtilityServlet;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Embedded Apache Tomcat 10.1 launcher for Wild Indonesia (Jakarta EE 10 / JSP 3.1).
 * Supports dev classpath, packaged JAR/dependency runs, and Docker deployment.
 */
public class AppServer {
    private Tomcat tomcat;
    private int boundPort;

    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        AppServer server = new AppServer();
        server.start(port);
        System.out.println("Wild Indonesia JSP Server listening on http://localhost:" + server.getBoundPort());
        server.await();
    }

    public void start(int port) throws Exception {
        DatabaseManager.initializeAndSeed();

        File webappDir = locateWebappDir();
        File workDir = Files.createTempDirectory("wild-indonesia-tomcat").toFile();
        workDir.deleteOnExit();

        tomcat = new Tomcat();
        tomcat.setBaseDir(workDir.getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector().setURIEncoding("UTF-8");

        Context ctx = tomcat.addWebapp("", webappDir.getAbsolutePath());
        ctx.setReloadable(false);

        File classesDir = new File("target/classes");
        if (classesDir.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        // Enforce UTF-8 on every request & response
        FilterDef utf8Filter = new FilterDef();
        utf8Filter.setFilterName("utf8Filter");
        utf8Filter.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
                req.setCharacterEncoding("UTF-8");
                res.setCharacterEncoding("UTF-8");
                if (req instanceof HttpServletRequest r && res instanceof HttpServletResponse hres) {
                    hres.setHeader("X-Content-Type-Options", "nosniff");
                    String uri = r.getRequestURI();
                    if (uri != null && uri.endsWith(".woff2")) {
                        hres.setHeader("Cache-Control", "public, max-age=604800");
                    }
                }
                chain.doFilter(req, res);
            }
        });
        ctx.addFilterDef(utf8Filter);
        FilterMap utf8Map = new FilterMap();
        utf8Map.setFilterName("utf8Filter");
        utf8Map.addURLPattern("/*");
        ctx.addFilterMap(utf8Map);

        // Explicit Servlet Registrations (guarantee deterministic routing inside embedded Tomcat)
        Tomcat.addServlet(ctx, "HomeServlet", new HomeServlet());
        ctx.addServletMappingDecoded("", "HomeServlet");
        ctx.addServletMappingDecoded("/", "HomeServlet");

        Tomcat.addServlet(ctx, "AuthServlet", new AuthServlet());
        ctx.addServletMappingDecoded("/login", "AuthServlet");
        ctx.addServletMappingDecoded("/register", "AuthServlet");
        ctx.addServletMappingDecoded("/logout", "AuthServlet");
        ctx.addServletMappingDecoded("/profile", "AuthServlet");

        Tomcat.addServlet(ctx, "DestinationServlet", new DestinationServlet());
        ctx.addServletMappingDecoded("/destinations", "DestinationServlet");
        ctx.addServletMappingDecoded("/destinations/*", "DestinationServlet");

        Tomcat.addServlet(ctx, "RegionServlet", new RegionServlet());
        ctx.addServletMappingDecoded("/regions", "RegionServlet");
        ctx.addServletMappingDecoded("/regions/*", "RegionServlet");

        Tomcat.addServlet(ctx, "RentalServlet", new RentalServlet());
        ctx.addServletMappingDecoded("/rentals", "RentalServlet");
        ctx.addServletMappingDecoded("/rentals/*", "RentalServlet");

        Tomcat.addServlet(ctx, "GuideServlet", new GuideServlet());
        ctx.addServletMappingDecoded("/guides", "GuideServlet");
        ctx.addServletMappingDecoded("/guides/*", "GuideServlet");

        Tomcat.addServlet(ctx, "ItineraryServlet", new ItineraryServlet());
        ctx.addServletMappingDecoded("/itineraries", "ItineraryServlet");
        ctx.addServletMappingDecoded("/itineraries/*", "ItineraryServlet");

        Tomcat.addServlet(ctx, "BookingServlet", new BookingServlet());
        ctx.addServletMappingDecoded("/bookings", "BookingServlet");
        ctx.addServletMappingDecoded("/bookings/*", "BookingServlet");

        Tomcat.addServlet(ctx, "StoryServlet", new StoryServlet());
        ctx.addServletMappingDecoded("/stories", "StoryServlet");
        ctx.addServletMappingDecoded("/stories/*", "StoryServlet");

        Tomcat.addServlet(ctx, "SavedPlaceServlet", new SavedPlaceServlet());
        ctx.addServletMappingDecoded("/saved", "SavedPlaceServlet");
        ctx.addServletMappingDecoded("/saved/*", "SavedPlaceServlet");

        Tomcat.addServlet(ctx, "UtilityServlet", new UtilityServlet());
        ctx.addServletMappingDecoded("/debug/*", "UtilityServlet");
        ctx.addServletMappingDecoded("/database-diagrams", "UtilityServlet");

        tomcat.start();
        boundPort = tomcat.getConnector().getLocalPort();
    }

    public int getBoundPort() {
        return boundPort;
    }

    public void await() {
        tomcat.getServer().await();
    }

    public void stop() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    private static int resolvePort() {
        String env = System.getenv("PORT");
        if (env != null && !env.isBlank()) {
            try { return Integer.parseInt(env.trim()); } catch (NumberFormatException ignored) {}
        }
        return 8080;
    }

    private static File locateWebappDir() {
        File dir = new File("src/main/webapp");
        if (dir.exists()) return dir;
        File parent = new File("quiz2/src/main/webapp");
        if (parent.exists()) return parent;
        throw new IllegalStateException("Cannot locate src/main/webapp directory from " + new File(".").getAbsolutePath());
    }
}

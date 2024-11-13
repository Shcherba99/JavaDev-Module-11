package com.goit.pshcherba;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * A servlet that handles requests to display the current time in a specified timezone.
 * The timezone can be provided via a request parameter or retrieved from cookies.
 *
 * @see HttpServlet
 */
@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private TemplateEngine templateEngine;


    /**
     * Initializes the servlet and sets up the Thymeleaf template engine.
     * It configures the template resolver to read HTML templates from the /WEB-INF/templates/ directory.
     */
    @Override
    public void init() {
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(getServletContext().getRealPath("/WEB-INF/templates/"));
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }


    /**
     * Handles the HTTP GET request.
     * It processes the timezone parameter from the request or retrieves it from cookies.
     * Then, it renders the current time in the specified timezone using Thymeleaf templates.
     *
     * @param req the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws IOException if an I/O error occurs during request processing
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        Context context = new Context();
        String timezone = getTimezone(req);

        if (timezone != null && !timezone.isEmpty()) {
            Cookie lastTimezone = new Cookie("timezone", timezone);
            lastTimezone.setMaxAge(60 * 60 * 24);
            resp.addCookie(lastTimezone);
        } else {
            timezone = getCookie(req);
        }

        context.setVariable("currentTime", getCurrentTimeInTimezone(timezone));
        String result = templateEngine.process("time", context);
        resp.getWriter().write(result);
    }


    /**
     * Returns the current date and time in the specified timezone as a formatted string.
     * The timezone should be a valid identifier according to ZoneId.SHORT_IDS.
     *
     * @param timezone the timezone identifier
     * @return a formatted string representing the current date and time in the specified timezone
     */
    private String getCurrentTimeInTimezone(String timezone) {
        ZoneId zoneId = ZoneId.of(timezone, ZoneId.SHORT_IDS);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
        return zonedDateTime.format(formatter);
    }


    /**
     * Retrieves the timezone value from the request attribute.
     *
     * @param req the HttpServletRequest object
     * @return the timezone value from the request attribute, or null if not found
     */
    private String getTimezone(HttpServletRequest req) {
        return (String) req.getAttribute("timezone");
    }


    /**
     * Retrieves the timezone value from the cookies sent with the request.
     * If no timezone cookie is found, it returns the default value "UTC".
     *
     * @param req the HttpServletRequest object
     * @return the timezone value from the cookies, or "UTC" if not found
     */
    private String getCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            return cookies[0].getValue();
        }

        return "UTC";
    }
}

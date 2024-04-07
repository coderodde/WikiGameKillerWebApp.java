//package com.github.coderodde.wikipedia.game.killer;
//
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class CorsFilter implements Filter {
//
//    private static final Logger LOGGER =
//            Logger.getLogger(CorsFilter.class.getName());
//    
//    @Override
//    public void init(final FilterConfig filterConfig) throws ServletException {
//        LOGGER.log(Level.INFO, "Initializing {1}", filterConfig);
//    }
//
//    @Override
//    public void doFilter(final ServletRequest servletRequest, 
//                         final ServletResponse servletResponse, 
//                         final FilterChain filterChain)
//            throws IOException, ServletException {
//        LOGGER.log(Level.INFO, "Filtering...");
//        
//        HttpServletResponse httpServletResponse = 
//                (HttpServletResponse) servletResponse;
//        
//        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
//        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
//        httpServletResponse.setHeader(
//                "Access-Control-Allow-Headers",
//                "Origin, Accept, X-Requested-With, Content-Type, " + 
//                "Access-Control-Request-Method, " + 
//                "Access-Control-Request-Headers");
//        
//        if ("OPTIONS"
//                .equalsIgnoreCase(
//                        ((HttpServletRequest) servletRequest).getMethod())) {
//            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
//        } else {
//            filterChain.doFilter(servletRequest, servletResponse);
//        }
//    }
//
//    @Override
//    public void destroy() {
//        LOGGER.log(Level.INFO, "Destructing CorsFilter.");
//    }
//}

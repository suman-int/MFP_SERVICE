package com.mnao.mfp.user.interceptors;

import com.mnao.mfp.common.util.AppConstants;
import com.mnao.mfp.common.util.Utils;
import com.mnao.mfp.config.CrConfig;
import com.mnao.mfp.list.emp.AllEmployeesCache;
import com.mnao.mfp.user.dao.MFPUser;
import com.mnao.mfp.user.service.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

@Configuration
@Component
public class MFPRequestInterceptor implements HandlerInterceptor {

    @Autowired
    private CrConfig crConfig;

    private static final Logger log = LoggerFactory.getLogger(MFPRequestInterceptor.class);
    private static final String USERID_REQUEST_HEADER = "IV-USER";
    private final boolean useDBDomain;

    public MFPRequestInterceptor() {
        String useDB = Utils.getAppProperty(AppConstants.EMP_USE_DB_RGN_ZONE_DSTR, "false");
        useDBDomain = useDB.equalsIgnoreCase("true");
        log.debug("Interceptor Created. Use DB for USer Domain: {}", useDBDomain);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
        boolean rv = true;
        String userID = request.getHeader(USERID_REQUEST_HEADER);
        Boolean isDev = "dev".equalsIgnoreCase(crConfig.getProfile()) || "default".equalsIgnoreCase(crConfig.getProfile());
        if (isDev || validateToken(request, response)) {

            log.debug("UserID= {}", userID);
            if (userID == null || userID.trim().length() == 0) {
                response.sendError(401, "UNAUTHORISED");
                log.error("Unauthorised Access");
                rv = false;
            } else {
                UserDetailsService ud = new UserDetailsService();
                MFPUser u = ud.getMFPUser(userID);
                if (u == null) {
                    response.sendError(401, "UNAUTHORISED");
                    log.error("Unauthorised Access");
                    rv = false;
                } else {
                    if (useDBDomain) {
                        u.setUseDBDomain(true);
                        try {
                            ServletContext servletContext = request.getServletContext();
                            WebApplicationContext wac = WebApplicationContextUtils
                                    .getRequiredWebApplicationContext(servletContext);
                            AllEmployeesCache allEmployeesCache = wac.getBean(AllEmployeesCache.class);
                            if (allEmployeesCache != null) {
                                allEmployeesCache.updateDomain(u);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    HttpSession session = request.getSession();
                    session.setAttribute("mfpUser", u);
                }
            }

        } else {
            response.sendError(401, "UNAUTHORISED");
            log.error("Unauthorised Access");
            rv = false;
        }

        return rv;
    }

    public boolean validateToken(HttpServletRequest request, HttpServletResponse response) {

        boolean validateFlag = false;
        HttpURLConnection con = null;

        try (InputStream ist = Utils.class.getResourceAsStream("/wslusersvc-test.properties")) {
            Properties wslProperties = new Properties();
            wslProperties.load(ist);

            URL url = new URL(wslProperties.getProperty("AUTH_SVC_URL"));
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty(wslProperties.getProperty("AUTH_RS_SEC_HDR_TOKEN_NAME"),
                    request.getHeader(AppConstants.RS_SEC_HDR_TOKEN_NAME));
            con.setRequestProperty(wslProperties.getProperty("AUTH_RS_SEC_HDR_IV_NAME"),
                    request.getHeader(AppConstants.RS_SEC_HDR_IV_NAME));
            con.setRequestProperty(wslProperties.getProperty("AUTH_RS_SEC_HDR_VENDOR_ID"),
                    request.getHeader(AppConstants.RS_SEC_HDR_VENDOR_ID));
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setDoOutput(true);

            int statusCode = con.getResponseCode();
            if (statusCode == 200) {
                validateFlag = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return validateFlag;
    }

}
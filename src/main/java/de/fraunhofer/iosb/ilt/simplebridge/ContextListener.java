/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.simplebridge;

import de.fraunhofer.iosb.ilt.configurable.ConfigurationException;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jab, scf
 */
@WebListener
public class ContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (sce != null && sce.getServletContext() != null) {
            LOGGER.info("Context initialised, loading settings.");
            ServletContext context = sce.getServletContext();

            String serverConfigString = context.getInitParameter(ServerConfig.TAG_SERVER_CONFIG);
            ServerConfig serverConfig;
            try {
                serverConfig = ServerConfig.fromString(serverConfigString);
                initLinks(serverConfig);
            } catch (ConfigurationException ex) {
                LOGGER.error("Failed to read server config!", ex);
                serverConfig = new ServerConfig();
            }

            context.setAttribute(ServerConfig.TAG_SERVER_CONFIG, serverConfig);
        }
    }

    private void initLinks(ServerConfig serverConfig) {
        LOGGER.info("Initialising links...");
        CloseableHttpClient client = HttpClients.createSystem();
        for (String linkUrl : serverConfig.getLinkServers()) {
            LOGGER.info("Link: {}", linkUrl);
            HttpGet httpGet = new HttpGet(linkUrl + "/link");
            httpGet.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
            try (CloseableHttpResponse response = client.execute(httpGet)) {
                String content = throwIfNotOk(httpGet, response);
                ServerConfig otherConfig = ServerConfig.fromString(content);
                for (EndPoint endPoint : otherConfig.getEndpoints()) {
                    endPoint.setLinkUrl(linkUrl);
                    serverConfig.getEndpoints().add(endPoint);
                }
            } catch (IOException ex) {
                LOGGER.error("Failed to connect to {}", linkUrl, ex);
            } catch (ConfigurationException ex) {
                LOGGER.error("Failed to parse server config from {}", linkUrl, ex);
            }
        }

    }

    public static String throwIfNotOk(HttpRequestBase request, CloseableHttpResponse response) {
        final int statusCode = response.getStatusLine().getStatusCode();
        String returnContent = "";
        if (statusCode < 200 || statusCode >= 300) {
            try {
                returnContent = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
                return returnContent;
            } catch (IOException exc) {
                LOGGER.warn("Failed to get content from error response.", exc);
            }
            if (statusCode == 401) {
                request.getURI();
                throw new RuntimeException(returnContent);
            }
            if (statusCode == 404) {
                throw new RuntimeException(returnContent);
            }
            throw new RuntimeException(returnContent);
        }
        try {
            returnContent = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch (IOException exc) {
            LOGGER.warn("Failed to get content from error response.", exc);
        }
        return returnContent;
    }

}

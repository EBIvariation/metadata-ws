/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.metadata.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
    public class JdbcConnection {

    @Value("${" + DbParametersName.DB_URL + ":#{null}}")
    private String url;

    @Value("${" + DbParametersName.DB_DRIVER + ":#{null}}")
    private String driver;

    @Value("${" + DbParametersName.DB_HOST + ":#{null}}")
    private String host;

    @Value("${" + DbParametersName.DB_SERVICENAME + ":#{null}}")
    private String serviceName;

    @Value("${" + DbParametersName.DB_PORT + ":#{null}}")
    private String port;

    @Value("${" + DbParametersName.DB_PROTOCOL + ":#{null}}")
    private String protocol;

    @Value("${" + DbParametersName.DB_USERNAME + ":#{null}}")
    private String userName;

    @Value("${" + DbParametersName.DB_PASSWORD + ":#{null}}")
    private String password;

    private String getUrl() {
        return url;
    }

    public String getDriver() {
        return driver;
    }

    private String getHost() {
        return host;
    }

    private String getServiceName() {
        return serviceName;
    }

    private String getPort() {
        return port;
    }

    private String getProtocol() {
        return protocol;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getCompleteUrl() {
        String url;
        String host;
        String protocol;
        String serviceName;
        String port;
        if ((this.getUrl() == null || this.getUrl().trim().isEmpty()) ||
                (this.getHost() == null || this.getHost().trim().isEmpty()) ||
                (this.getPort() == null || this.getPort().trim().isEmpty()) ||
                (this.getProtocol() == null || this.getProtocol().trim().isEmpty()) ||
                (this.getServiceName() == null || this.getServiceName().trim().isEmpty())) {
            throw new IllegalArgumentException("Some fields are not present in properties file.");
        } else {
            url = this.getUrl();
            host = this.getHost();
            protocol = this.getProtocol();
            serviceName = this.getServiceName();
            port = this.getPort();
        }

        url = url + ":@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = " + protocol + ")(HOST = " + host + ")(PORT = " +
                port + ")))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = " + serviceName + ")))";

        return url;
    }

}

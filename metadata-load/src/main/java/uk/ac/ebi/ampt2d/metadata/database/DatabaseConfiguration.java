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

import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class DatabaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Autowired
    JdbcConnection jdbcConnection;

    public DatabaseConfiguration(JdbcConnection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }

    @Bean
    public DataSource getdataSource() throws SQLException {
        OracleDataSource oracleDataSource;
        try {
            oracleDataSource = new OracleDataSource();
            if ((jdbcConnection.getUserName() == null || jdbcConnection.getUserName().trim().isEmpty()) ||
                    (jdbcConnection.getPassword() == null || jdbcConnection.getPassword().trim().isEmpty())) {
                throw new IllegalArgumentException("Some fields are not present in properties file.");
            } else {
                oracleDataSource.setUser(jdbcConnection.getUserName());
                oracleDataSource.setPassword(jdbcConnection.getPassword());
                oracleDataSource.setURL(jdbcConnection.getCompleteUrl());
            }
        } catch (SQLException ex) {
            logger.error("Could not create OracleDataSource ", ex);
            throw ex;
        }
        return oracleDataSource;
    }

}

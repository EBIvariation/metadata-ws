/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.metadata.importer.database;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "import.source", havingValue = "DB")
public class SraDatabaseConfiguration {

    @Bean("enaDatasourceProperties")
    @ConfigurationProperties("ena.datasource")
    @Primary
    public DataSourceProperties dbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("enaDatasource")
    @ConfigurationProperties("ena.datasource.tomcat")
    public DataSource dbDataSource() {
        return dbDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("enaDatasourceTransactionManager")
    public DataSourceTransactionManager dbTransactionManager() {
        return new DataSourceTransactionManager(dbDataSource());
    }

    @Bean("enaJdbcTemplate")
    public NamedParameterJdbcTemplate enaJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dbDataSource());
    }

    @Bean
    public SraXmlRetrieverThroughDatabase sraXmlRetrieverThroughDatabase() {
        return new SraXmlRetrieverThroughDatabase(enaJdbcTemplate(), EnaObjectQuery.STUDY_QUERY);
    }
}
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

package uk.ac.ebi.ampt2d.metadata.importer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "uk.ac.ebi.ampt2d.metadata.persistence.entities")
@EnableJpaRepositories(entityManagerFactoryRef = "metadataEntityManagerFactory",
        basePackages = "uk.ac.ebi.ampt2d.metadata.persistence.repositories",
        transactionManagerRef = "metadataTransactionManager")
@EnableJpaAuditing
public class MetadataDatabaseConfiguration {

    @Autowired
    private JpaProperties jpaProperties;

    @Bean(name = "metadataDatasource")
    @ConfigurationProperties(prefix = "metadata.datasource")
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "metadataEntityManagerFactory")
    @ConfigurationProperties(prefix = "metadata")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                            @Qualifier("metadataDatasource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean =
                builder.dataSource(dataSource)
                        .packages("uk.ac.ebi.ampt2d.metadata.persistence.entities")
                        .persistenceUnit("metadata")
                        .build();
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(jpaProperties.getHibernateProperties(dataSource));
        return localContainerEntityManagerFactoryBean;
    }

    @Bean(name = "metadataTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("metadataEntityManagerFactory")
                                                                 EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

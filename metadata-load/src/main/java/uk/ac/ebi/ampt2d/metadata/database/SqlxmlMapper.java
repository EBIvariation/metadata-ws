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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;

public class SqlxmlMapper implements RowMapper<SQLXML> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlxmlMapper.class);

    private String column;

    public SqlxmlMapper(String column) {
        this.column = column;
    }

    @Override
    public SQLXML mapRow(ResultSet rs, int rowNum) throws SQLException {
        SQLXML sqlxml;
        try {
            sqlxml = rs.getSQLXML(column);
        } catch (SQLException ex) {
            LOGGER.error("Column {} at row number {} could not be fetched", column, rowNum, ex);
            throw ex;
        }
        return sqlxml;
    }

}

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
package uk.ac.ebi.ampt2d.metadata.persistence.idconverter;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.DbAccessionVersionId;

public class DbIdVersionIdConverter implements Converter<String, DbAccessionVersionId> {

    @Override
    public DbAccessionVersionId convert(String id) {
        DbAccessionVersionId dbAccessionVersionId;
        try {
            String[] DbIdVersion = id.split("\\.");
            String[] DbId = DbIdVersion[0].split("\\_");
            dbAccessionVersionId = new DbAccessionVersionId(DbId[0], DbId[1], DbIdVersion[1]);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Please provide an ID in the form db_id.version");
        }
        return dbAccessionVersionId;
    }
}

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

public interface EnaObjectQuery {

    String STUDY_QUERY = "SELECT STUDY_XML FROM ERA.STUDY WHERE STUDY_ID = :accession";

    String ANALYSIS_QUERY = "SELECT ANALYSIS_XML FROM ERA.ANALYSIS WHERE ANALYSIS_ID = :accession";

    String SAMPLE_QUERY = "SELECT SAMPLE_XML FROM ERA.SAMPLE WHERE SAMPLE_ID = :accession";
}

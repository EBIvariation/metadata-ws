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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.importer.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.converter.StudyConverter;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.PublicationOrWebResourceExtractorFromStudy;
import uk.ac.ebi.ampt2d.metadata.importer.extractor.TaxonomyExtractor;
import uk.ac.ebi.ampt2d.metadata.importer.objectImporters.ObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.objectImporters.SraObjectsImporter;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraStudyXmlParser;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PublicationRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;
import uk.ac.ebi.ena.sra.xml.StudyType;

@Configuration
public class MetadataImporterMainApplicationConfiguration {

    @Bean
    public ObjectsImporter objectImporter(SraRetrieverByAccession sraRetrieverByAccession,
                                          Converter<StudyType, Study> studyConverter,
                                          PublicationRepository publicationRepository,
                                          WebResourceRepository webResourceRepository) {
        return new SraObjectsImporter(sraRetrieverByAccession, sraStudyXmlParser(), studyConverter,
                new PublicationOrWebResourceExtractorFromStudy(publicationRepository, webResourceRepository));
    }

    @Bean
    public Converter<StudyType, Study> getStudyConverter(TaxonomyRepository taxonomyRepository) {
        return new StudyConverter(getTaxonomyExtractor(taxonomyRepository));
    }

    private SraXmlParser<StudyType> sraStudyXmlParser() {
        return new SraStudyXmlParser();
    }

    private TaxonomyExtractor getTaxonomyExtractor(TaxonomyRepository taxonomyRepository) {
        return new TaxonomyExtractor(taxonomyRepository);
    }
}

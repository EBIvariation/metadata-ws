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

package uk.ac.ebi.ampt2d.metadata.importer.extractor;

import uk.ac.ebi.ampt2d.metadata.persistence.entities.Publication;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PublicationRepository;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.ProjectType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PublicationExtractor {

    private static final String PUBMED = "pubmed";

    private PublicationRepository publicationRepository;

    public PublicationExtractor(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    public List<Publication> getPublicationsFromProject(ProjectType.PROJECTLINKS projectLinks) {
        if (projectLinks == null) {
            return new ArrayList<>();
        } else {
            return getPublicationsFromLinks(
                    Stream.of(projectLinks.getPROJECTLINKArray())
                          .map(link -> link.getXREFLINK()));
        }
    }

    public List<Publication> getPublicationsFromStudy(StudyType.STUDYLINKS studylinks) {
        if (studylinks == null) {
            return new ArrayList<>();
        } else {
            return getPublicationsFromLinks(
                    Stream.of(studylinks.getSTUDYLINKArray())
                          .map(link -> link.getXREFLINK()));
        }
    }

    public List<Publication> getPublicationsFromLinks(Stream<XRefType> xrefs) {
        return xrefs.filter(Objects::nonNull)
                    .filter(xref -> xref.getDB().equalsIgnoreCase(PUBMED))
                    .map(xref -> findOrCreatePublication(xref.getID()))
                    .collect(Collectors.toList());
    }

    private Publication findOrCreatePublication(String publicationId) {
        Publication publication = publicationRepository.findByPublicationId(publicationId);
        if (publication == null) {
            publication = publicationRepository.save(new Publication(publicationId));
        }
        return publication;
    }
}

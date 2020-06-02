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

public class PublicationExtractor {

    private static final String PUBMED = "pubmed";

    private PublicationRepository publicationRepository;

    public PublicationExtractor(PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    public List<Publication> getPublicationsFromProject(ProjectType.PROJECTLINKS projectlinks) {
        List<Publication> publications = new ArrayList<>();
        if (projectlinks == null) {
            return publications;
        }
        ProjectType.PROJECTLINKS.PROJECTLINK[] projectLinksArray = projectlinks.getPROJECTLINKArray();
        for (int i = 0; i < projectLinksArray.length; i++) {
            XRefType xRefType = projectLinksArray[i].getXREFLINK();
            if (xRefType != null && xRefType.getDB().equalsIgnoreCase(PUBMED)) {
                publications.add(findOrCreatePublication(xRefType.getID()));
            }
        }
        return publications;
    }

    public List<Publication> getPublicationsFromStudy(StudyType.STUDYLINKS studylinks) {
        List<Publication> publications = new ArrayList<>();
        if (studylinks == null) {
            return publications;
        }
        LinkType[] studyLinksArray = studylinks.getSTUDYLINKArray();
        for (int i = 0; i < studyLinksArray.length; i++) {
            XRefType xRefType = studyLinksArray[i].getXREFLINK();
            addPublication(publications, xRefType);
        }
        return publications;
    }

    private void addPublication(List<Publication> publications, XRefType xRefType) {
        if (xRefType != null && xRefType.getDB().equalsIgnoreCase(PUBMED)) {
            publications.add(findOrCreatePublication(xRefType.getID()));
        }
    }

    private Publication findOrCreatePublication(String publicationId) {
        Publication publication = publicationRepository.findByPublicationId(publicationId);
        if (publication == null) {
            publication = publicationRepository.save(new Publication(publicationId));
        }
        return publication;
    }
}

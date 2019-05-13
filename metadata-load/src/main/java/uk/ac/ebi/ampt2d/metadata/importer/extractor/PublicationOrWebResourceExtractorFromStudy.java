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

import uk.ac.ebi.ampt2d.metadata.persistence.entities.Auditable;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Publication;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.PublicationRepository;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.StudyType;
import uk.ac.ebi.ena.sra.xml.XRefType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicationOrWebResourceExtractorFromStudy {

    public static final String PUBLICATIONS = "publications";

    public static final String WEB_RESOURCES = "webResources";

    private static final String PUB_MED = "pubmed";

    private PublicationRepository publicationRepository;

    private WebResourceRepository webResourceRepository;

    public PublicationOrWebResourceExtractorFromStudy(PublicationRepository publicationRepository, WebResourceRepository webResourceRepository) {
        this.publicationRepository = publicationRepository;
        this.webResourceRepository = webResourceRepository;
    }

    public Map<String, List<? extends Auditable>> getPublicationsAndWebResources(StudyType.STUDYLINKS studylinks) {
        Map<String, List<? extends Auditable>> mapOfPublicationAndWebResources = new HashMap<>();
        List<Publication> publications = new ArrayList<>();
        List<WebResource> webResources = new ArrayList<>();
        if (studylinks == null) {
            return mapOfPublicationAndWebResources;
        }
        LinkType[] studyLinksArray = studylinks.getSTUDYLINKArray();

        for (int i = 0; i < studyLinksArray.length; i++) {
            XRefType xRefType = studyLinksArray[i].getXREFLINK();
            if (xRefType != null && xRefType.getDB().equals(PUB_MED)) {
                publications.add(findOrCreatePublication(xRefType.getID()));
            } else {
                LinkType.URLLINK urlLink = studyLinksArray[i].getURLLINK();
                if (urlLink != null) {
                    webResources.add(findOrCreateWebResource(urlLink.getURL()));
                }
            }

        }
        mapOfPublicationAndWebResources.put(PUBLICATIONS, publications);
        mapOfPublicationAndWebResources.put(WEB_RESOURCES, webResources);
        return mapOfPublicationAndWebResources;
    }

    private WebResource findOrCreateWebResource(String url) {
        WebResource webResource = webResourceRepository.findByResourceUrl(url);
        if (webResource == null) {
            webResource = webResourceRepository.save(new WebResource(WebResource.Type.STUDY_WEB, url));
        }
        return webResource;
    }

    private Publication findOrCreatePublication(String id) {
        Publication publication = publicationRepository.findOne(id);
        if (publication == null) {
            publication = publicationRepository.save(new Publication(id));
        }
        return publication;
    }
}

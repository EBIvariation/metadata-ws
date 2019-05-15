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

import uk.ac.ebi.ampt2d.metadata.persistence.entities.WebResource;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.WebResourceRepository;
import uk.ac.ebi.ena.sra.xml.LinkType;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.util.ArrayList;
import java.util.List;

public class WebResourceExtractorFromStudy {

    private WebResourceRepository webResourceRepository;

    public WebResourceExtractorFromStudy(WebResourceRepository webResourceRepository) {
        this.webResourceRepository = webResourceRepository;
    }

    public List<WebResource> getWebResources(StudyType.STUDYLINKS studylinks) {
        List<WebResource> webResources = new ArrayList<>();
        if (studylinks == null) {
            return webResources;
        }
        LinkType[] studyLinksArray = studylinks.getSTUDYLINKArray();
        for (int i = 0; i < studyLinksArray.length; i++) {
            LinkType.URLLINK urlLink = studyLinksArray[i].getURLLINK();
            if (urlLink != null) {
                webResources.add(findOrCreateWebResource(urlLink.getURL()));
            }
        }
        return webResources;
    }

    private WebResource findOrCreateWebResource(String url) {
        WebResource webResource = webResourceRepository.findByResourceUrl(url);
        if (webResource == null) {
            webResource = webResourceRepository.save(new WebResource(WebResource.Type.STUDY_WEB, url));
        }
        return webResource;
    }

}

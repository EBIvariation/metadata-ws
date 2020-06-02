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

package uk.ac.ebi.ampt2d.metadata.importer.converter;

import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Project;
import uk.ac.ebi.ena.sra.xml.AttributeType;
import uk.ac.ebi.ena.sra.xml.ProjectType;

import java.time.LocalDate;

public class ProjectConverter implements Converter<ProjectType, Project> {

    @Override
    public Project convert(ProjectType projectType) {
        String projectAccession = projectType.getAccession();
        String projectName = projectType.getTITLE().trim();
        String projectDescription = projectType.getDESCRIPTION();
        ProjectType.PROJECTATTRIBUTES projectattributes = projectType.getPROJECTATTRIBUTES();
        LocalDate projectReleaseDate = getReleaseDate(projectattributes);
        return new Project(new AccessionVersionId(projectAccession, 1), projectName,
                projectDescription, projectType.getCenterName(), projectReleaseDate);
    }

    private LocalDate getReleaseDate(ProjectType.PROJECTATTRIBUTES projectattributes) {
        LocalDate projectReleaseDate = LocalDate.of(9999, 12, 31);
        if (projectattributes == null) {
            return projectReleaseDate;
        }
        AttributeType[] projectattributesArray = projectattributes.getPROJECTATTRIBUTEArray();
        for (int i = 0; i < projectattributesArray.length; i++) {
            String attributeTag = projectattributesArray[i].getTAG();
            if (attributeTag.equals("ENA-FIRST-PUBLIC")) {
                projectReleaseDate = LocalDate.parse(projectattributesArray[i].getVALUE());
                break;
            }
        }
        return projectReleaseDate;
    }
}

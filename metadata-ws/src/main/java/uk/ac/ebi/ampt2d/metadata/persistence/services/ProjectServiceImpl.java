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
package uk.ac.ebi.ampt2d.metadata.persistence.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.QProject;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Project;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.ProjectRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaxonomyService taxonomyService;

    @Override
    public Project findOneProjectByPredicate(Predicate predicate) {
        return projectRepository.findOne(predicate);
    }

    @Override
    public Project findOneProjectById(long id) {
        QProject project = QProject.project;
        Predicate predicate = project.id.eq(id);
        return findOneProjectByPredicate(predicate);
    }

    @Override
    public List<Project> findProjectsByPredicate(Predicate predicate) {
        return (List<Project>) projectRepository.findAll(predicate);
    }

    @Override
    public Project findProjectByAccession(String accession) {
        QProject project = QProject.project;
        Predicate predicate = project.accessionVersionId.accession.equalsIgnoreCase(accession);

        List<Project> projects = findProjectsByPredicate(predicate);

        projects.sort((new Comparator<Project>() {
            @Override
            public int compare(Project o1, Project o2) {
                return o1.getAccessionVersionId().getVersion() - o2.getAccessionVersionId().getVersion();
            }
        }).reversed());

        return projects.isEmpty() ? null : projects.get(0);
    }

    @Override
    public List<Project> findProjectsByReleaseDate(LocalDate from, LocalDate to) {
        QProject project = QProject.project;
        Predicate predicate = project.releaseDate.between(from, to);

        return findProjectsByPredicate(predicate);
    }

    @Override
    public List<Project> findProjectsByTextSearch(String searchTerm) {
        QProject project = QProject.project;
        Predicate predicate = project.name.containsIgnoreCase(searchTerm).
                or(project.description.containsIgnoreCase(searchTerm));

        return findProjectsByPredicate(predicate);
    }

    @Override
    public List<Project> findProjectsByTaxonomyId(long id) {
        List<Long> taxonomyIds = taxonomyService.findAllTaxonomiesInATreeByTaxonomyIds(id);
        return getProjectsByTaxonomyIds(taxonomyIds);
    }

    @Override
    public List<Project> findProjectsByTaxonomyName(String name) {
        List<Long> taxonomyIds = taxonomyService.findAllTaxonomiesInATreeByTaxonomyName(name);
        return getProjectsByTaxonomyIds(taxonomyIds);
    }

    public List<Project> getProjectsByTaxonomyIds(List<Long> taxonomyIds) {
        QProject project = QProject.project;
        Predicate predicate = project.taxonomy.taxonomyId.in(taxonomyIds);
        return (List<Project>) projectRepository.findAll(predicate);
    }

    @Override
    public List<Project> findLinkedProjects(long id) {
        Project project = projectRepository.findOne(id);
        if (project == null) {
            return Arrays.asList();
        }

        Set<Project> projects = new HashSet<>();

        List<Project> parents = findParentProject(project);
        parents.forEach(parent -> {
            projects.add(parent);
            projects.addAll(parent.getChildProjects());
        });

        projects.addAll(project.getChildProjects());

        return projects.stream()
                .filter(project1 -> !project1.getId().equals(project.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Project patch(Project project, String patch) throws Exception {
        Project project1 = objectMapper.readerForUpdating(project).readValue(patch);

        return projectRepository.save(project1);
    }

    private List<Project> findParentProject(Project child) {
        QProject project = QProject.project;
        Predicate predicate = project.childProjects.any().eq(child);

        return findProjectsByPredicate(predicate);
    }

}
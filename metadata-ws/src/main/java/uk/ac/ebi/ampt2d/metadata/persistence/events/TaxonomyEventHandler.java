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

package uk.ac.ebi.ampt2d.metadata.persistence.events;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.util.DomQueryUsingXPath;

@RepositoryEventHandler(Taxonomy.class)
public class TaxonomyEventHandler {

    private static final String ENA_TAXON_URL = "https://www.ebi.ac.uk/ena/data/view/Taxon:{taxonomyId}&display=xml";

    private static RestTemplate restTemplate = new RestTemplate();

    private TaxonomyRepository taxonomyRepository;

    public TaxonomyEventHandler(TaxonomyRepository taxonomyRepository) {
        this.taxonomyRepository = taxonomyRepository;
    }

    private static String getXml(Long taxonomyId) {
        return restTemplate.getForEntity(ENA_TAXON_URL, String.class, taxonomyId).getBody();
    }

    private static String findTaxId(DomQueryUsingXPath domQueryUsingXPath, String rank) throws Exception {
        String expression = "//lineage/taxon[@rank='" + rank + "']/@taxId";
        return domQueryUsingXPath.findInDom(expression);
    }

    @HandleBeforeCreate
    public Taxonomy importTaxonomyTree(Taxonomy taxonomy) throws Exception {
        long taxonomyId = taxonomy.getTaxonomyId();
        Taxonomy existingTaxonomy = taxonomyRepository.findByTaxonomyId(taxonomyId);

        if (existingTaxonomy != null) {
            return existingTaxonomy;
        }

        String taxonXml = getXml(taxonomyId);
        if (taxonXml.contains("type is either not supported or entry is not found")) {
            throw new IllegalArgumentException("taxonomyId provided is invalid");
        }

        DomQueryUsingXPath domQueryUsingXPath = new DomQueryUsingXPath(taxonXml);
        String taxonomyName = domQueryUsingXPath.findInDom("/ROOT/taxon/@scientificName");
        String rank = domQueryUsingXPath.findInDom("/ROOT/taxon/@rank");
        if (rank.isEmpty()) {
            rank = "no rank";
        }
        taxonomy.setName(taxonomyName);
        taxonomy.setRank(rank);

        for (RANK rankEnum : RANK.values()) {
            String taxId = findTaxId(domQueryUsingXPath, rankEnum.toString());
            if (taxId != null && !taxId.isEmpty()) {
                long taxIdLong = Long.parseLong(taxId);
                Taxonomy taxonomyParent = importTaxonomyTree(new Taxonomy(taxIdLong));
                taxonomy.setTaxonomyForRank(taxonomyParent, rankEnum.toString());
            } else if (taxonomy.getRank().equals(RANK.SPECIES.name) && taxonomy.getTaxonomySpecies() == null) {
                // self-reference at species level
                taxonomy.setTaxonomySpecies(taxonomy);
            }
        }

        return taxonomyRepository.save(taxonomy);
    }

    public enum RANK {
        SPECIES("species"),
        GENUS("genus"),
        ORDER("order"),
        CLASS("class");

        private String name;

        RANK(String rank) {
            name = rank;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}

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
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Taxonomy;
import uk.ac.ebi.ampt2d.metadata.persistence.repositories.TaxonomyRepository;
import uk.ac.ebi.ampt2d.metadata.util.DomQueryUsingXPath;

@RepositoryEventHandler(Taxonomy.class)
public class TaxonomyEventHandler {

    public static final String SPECIES = "species";

    public static final String GENUS = "genus";

    public static final String ORDER = "order";

    public static final String CLASS = "class";

    private static final String ENA_TAXON_URL = "https://www.ebi.ac.uk/ena/data/view/Taxon:{taxonomyId}&display=xml";

    private static RestTemplate restTemplate = new RestTemplate();

    private TaxonomyRepository taxonomyRepository;

    public TaxonomyEventHandler(TaxonomyRepository taxonomyRepository) {
        this.taxonomyRepository = taxonomyRepository;
    }

    private static String getXml(Long taxonomyId) {
        return restTemplate.exchange(ENA_TAXON_URL, HttpMethod.GET, null, String.class, taxonomyId).getBody();
    }

    private static String findTaxId(DomQueryUsingXPath domQueryUsingXPath, String rank) throws Exception {
        String expression = "//lineage/taxon[@rank='" + rank + "']/@taxId";
        return domQueryUsingXPath.findInDom(expression);
    }

    private static String findTaxName(DomQueryUsingXPath domQueryUsingXPath, String rank) throws Exception {
        String expression = "//lineage/taxon[@rank='" + rank + "']/@scientificName";
        return domQueryUsingXPath.findInDom(expression);
    }

    @HandleBeforeCreate
    @HandleBeforeSave
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
        if (rank.equals("")) {
            rank = "no rank";
        }
        taxonomy.setName(taxonomyName);
        taxonomy.setRank(rank);

        String classTaxId = findTaxId(domQueryUsingXPath, CLASS);
        String classTaxName = findTaxName(domQueryUsingXPath, CLASS);
        Taxonomy classTaxonomy = null;
        if (classTaxId != null && !classTaxId.equals("")) {
            long classTaxIdLong = Long.parseLong(classTaxId);
            classTaxonomy = taxonomyRepository.findByTaxonomyId(classTaxIdLong);
            if (classTaxonomy == null) {
                classTaxonomy = taxonomyRepository.save(new Taxonomy(classTaxIdLong, classTaxName, CLASS));
            }
            taxonomy.setTaxonomyClass(classTaxonomy);
        }

        String orderTaxId = findTaxId(domQueryUsingXPath, ORDER);
        String orderTaxName = findTaxName(domQueryUsingXPath, ORDER);
        Taxonomy orderTaxonomy = null;
        if (orderTaxId != null && !orderTaxId.equals("")) {
            long orderTaxIdLong = Long.parseLong(orderTaxId);
            orderTaxonomy = taxonomyRepository.findByTaxonomyId(orderTaxIdLong);
            if (orderTaxonomy == null) {
                orderTaxonomy = new Taxonomy(orderTaxIdLong, orderTaxName, ORDER);
                orderTaxonomy.setTaxonomyClass(classTaxonomy);
                orderTaxonomy = taxonomyRepository.save(orderTaxonomy);
            }
            taxonomy.setTaxonomyOrder(orderTaxonomy);
        }

        String genusTaxId = findTaxId(domQueryUsingXPath, GENUS);
        String genusTaxName = findTaxName(domQueryUsingXPath, GENUS);
        Taxonomy genusTaxonomy = null;
        if (genusTaxId != null && !genusTaxId.equals("")) {
            long genusTaxIdLong = Long.parseLong(genusTaxId);
            genusTaxonomy = taxonomyRepository.findByTaxonomyId(genusTaxIdLong);
            if (genusTaxonomy == null) {
                genusTaxonomy = new Taxonomy(genusTaxIdLong, genusTaxName, GENUS);
                genusTaxonomy.setTaxonomyClass(classTaxonomy);
                genusTaxonomy.setTaxonomyOrder(orderTaxonomy);
                genusTaxonomy = taxonomyRepository.save(genusTaxonomy);
            }
            taxonomy.setTaxonomyGenus(genusTaxonomy);
        }

        String speciesTaxId = findTaxId(domQueryUsingXPath, SPECIES);
        String speciesTaxName = findTaxName(domQueryUsingXPath, SPECIES);
        Taxonomy speciesTaxonomy = null;
        if (speciesTaxId != null && !speciesTaxId.equals("")) {
            long speciesTaxIdLong = Long.parseLong(speciesTaxId);
            speciesTaxonomy = taxonomyRepository.findByTaxonomyId(speciesTaxIdLong);
            if (speciesTaxonomy == null) {
                speciesTaxonomy = new Taxonomy(speciesTaxIdLong, speciesTaxName, SPECIES);
                speciesTaxonomy.setTaxonomyClass(classTaxonomy);
                speciesTaxonomy.setTaxonomyOrder(orderTaxonomy);
                speciesTaxonomy.setTaxonomyGenus(genusTaxonomy);
                speciesTaxonomy = taxonomyRepository.save(speciesTaxonomy);
            }
            taxonomy.setTaxonomySpecies(speciesTaxonomy);
        }

        return taxonomyRepository.save(taxonomy);
    }

}

Feature: taxonomy object

  Scenario: register a taxonomy successfully
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    And the response code should be 201
    And the Location header should be present with value of TAXONOMY
    When I request GET with value of TAXONOMY
    Then the response code should be 200
    And the response JSON should be:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    When I request GET /taxonomies
    Then the response should contain one taxonomy


  Scenario: register a taxonomy with taxonomyId less than 1 should fail
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 0,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then the response code should be 4xx
    When I request GET /taxonomies
    Then the response should contain no taxonomy


  Scenario: register a taxonomy and taxonomy tree successfully
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 40674 for ID, Mammalia for name and CLASS for rank
    Then set the URL to TAXONOMY_CLASS_MAMMALIA
    When I request POST taxonomy with 9443 for ID, Primates for name and ORDER for rank
    Then set the URL to TAXONOMY_ORDER_PRIMATES
    When I request POST taxonomy with 9605 for ID, Homo for name and GENUS for rank
    Then set the URL to TAXONOMY_GENUS_HOMO
    When I request POST taxonomy with 9606 for ID, Homo sapiens for name and SPECIES for rank
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS
    When I request POST taxonomyTree with TAXONOMY_SPECIES_HOMO_SAPIENS for species , TAXONOMY_GENUS_HOMO for GENUS , TAXONOMY_ORDER_PRIMATES for ORDER and TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_TREE_HOMO_SAPIENS
    When I request elaborate search for the taxonomyTrees base findByTaxonomySpecies and with the parameters: speciesId=9606
    Then the response code should be 200
    And the href of the class taxonomyTree should be TAXONOMY_TREE_HOMO_SAPIENS

  Scenario Outline: search taxonomy tree by id
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 40674 for ID, Mammalia for name and CLASS for rank
    Then set the URL to TAXONOMY_CLASS_MAMMALIA
    When I request POST taxonomy with 9443 for ID, Primates for name and ORDER for rank
    Then set the URL to TAXONOMY_ORDER_PRIMATES
    When I request POST taxonomy with 9605 for ID, Homo for name and GENUS for rank
    Then set the URL to TAXONOMY_GENUS_HOMO
    When I request POST taxonomy with 9596 for ID, Pan for name and GENUS for rank
    Then set the URL to TAXONOMY_GENUS_PAN
    When I request POST taxonomy with 9606 for ID, Homo sapiens for name and SPECIES for rank
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS
    When I request POST taxonomy with 9598 for ID, Pan troglodytes for name and SPECIES for rank
    Then set the URL to TAXONOMY_SPECIES_PAN_TROGLODYTES
    When I request POST taxonomy with 9597 for ID, Pan paniscus for name and SPECIES for rank
    Then set the URL to TAXONOMY_SPECIES_PAN_PANISCUS
    When I request POST taxonomyTree with TAXONOMY_SPECIES_HOMO_SAPIENS for species , TAXONOMY_GENUS_HOMO for GENUS , TAXONOMY_ORDER_PRIMATES for ORDER and TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_TREE_HOMO_SAPIENS
    When I request POST taxonomyTree with TAXONOMY_SPECIES_PAN_TROGLODYTES for species , TAXONOMY_GENUS_PAN for GENUS , TAXONOMY_ORDER_PRIMATES for ORDER and TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_TREE_PAN_TROGLODYTES
    When I request POST taxonomyTree with TAXONOMY_SPECIES_PAN_PANISCUS for species , TAXONOMY_GENUS_PAN for GENUS , TAXONOMY_ORDER_PRIMATES for ORDER and TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_TREE_PAN_PANISCUS
    When I request elaborate search for the taxonomyTrees base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain <N> taxonomyTrees
    And the href of the taxonomyTree of taxonomyTrees has items <url>
    Examples:
      | base                | query         | N | url                                                                                 |
      | findByTaxonomyGenus | genusId=9596  | 2 | TAXONOMY_TREE_PAN_TROGLODYTES,TAXONOMY_TREE_PAN_PANISCUS                            |
      | findByTaxonomyOrder | orderId=9443  | 3 | TAXONOMY_TREE_HOMO_SAPIENS,TAXONOMY_TREE_PAN_TROGLODYTES,TAXONOMY_TREE_PAN_PANISCUS |
      | findByTaxonomyClass | classId=40674 | 3 | TAXONOMY_TREE_HOMO_SAPIENS,TAXONOMY_TREE_PAN_TROGLODYTES,TAXONOMY_TREE_PAN_PANISCUS |

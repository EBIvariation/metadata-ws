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
    Then set the URL to TAXONOMY_1
    When I request POST taxonomy with 9443 for ID, Primates for name and ORDER for rank
    Then set the URL to TAXONOMY_2
    When I request POST taxonomy with 9605 for ID, Homo for name and GENUS for rank
    Then set the URL to TAXONOMY_3
    When I request POST taxonomy with 9606 for ID, Homo sapiens for name and SPECIES for rank
    Then set the URL to TAXONOMY_5
    When I request POST taxonomyTree with TAXONOMY_5 for species , TAXONOMY_3 for GENUS , TAXONOMY_2 for ORDER and TAXONOMY_1 for CLASS
    Then set the URL to TAXONOMY_TREE_1
    When I request elaborate search for the taxonomyTrees base findByTaxonomySpecies and with the parameters: speciesId=9606
    Then the response code should be 200
    And the href of the class taxonomyTree should be TAXONOMY_TREE_1

  Scenario Outline: search taxonomy tree by id
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 40674 for ID, Mammalia for name and CLASS for rank
    Then set the URL to TAXONOMY_1
    When I request POST taxonomy with 9443 for ID, Primates for name and ORDER for rank
    Then set the URL to TAXONOMY_2
    When I request POST taxonomy with 9605 for ID, Homo for name and GENUS for rank
    Then set the URL to TAXONOMY_3
    When I request POST taxonomy with 9596 for ID, Pan for name and GENUS for rank
    Then set the URL to TAXONOMY_4
    When I request POST taxonomy with 9606 for ID, Homo sapiens for name and SPECIES for rank
    Then set the URL to TAXONOMY_5
    When I request POST taxonomy with 9598 for ID, Pan troglodytes for name and SPECIES for rank
    Then set the URL to TAXONOMY_6
    When I request POST taxonomy with 9597 for ID, Pan paniscus for name and SPECIES for rank
    Then set the URL to TAXONOMY_7
    When I request POST taxonomyTree with TAXONOMY_5 for species , TAXONOMY_3 for GENUS , TAXONOMY_2 for ORDER and TAXONOMY_1 for CLASS
    Then set the URL to TAXONOMY_TREE_1
    When I request POST taxonomyTree with TAXONOMY_6 for species , TAXONOMY_4 for GENUS , TAXONOMY_2 for ORDER and TAXONOMY_1 for CLASS
    Then set the URL to TAXONOMY_TREE_2
    When I request POST taxonomyTree with TAXONOMY_7 for species , TAXONOMY_4 for GENUS , TAXONOMY_2 for ORDER and TAXONOMY_1 for CLASS
    Then set the URL to TAXONOMY_TREE_3
    When I request elaborate search for the taxonomyTrees base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain <N> taxonomyTrees
    And the href of the taxonomyTree of taxonomyTrees has items <url>
    Examples:
      | base                | query         | N | url                                             |
      | findByTaxonomyGenus | genusId=9596  | 2 | TAXONOMY_TREE_2,TAXONOMY_TREE_3                 |
      | findByTaxonomyOrder | orderId=9443  | 3 | TAXONOMY_TREE_1,TAXONOMY_TREE_2,TAXONOMY_TREE_3 |
      | findByTaxonomyClass | classId=40674 | 3 | TAXONOMY_TREE_1,TAXONOMY_TREE_2,TAXONOMY_TREE_3 |

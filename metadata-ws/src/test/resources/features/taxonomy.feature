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

  Scenario Outline: search taxonomy tree by id
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 40674 for ID, Mammalia for name and class for rank NONE for SPECIES NONE for GENUS NONE for ORDER NONE for CLASS
    Then set the URL to TAXONOMY_CLASS_MAMMALIA
    When I request POST taxonomy with 9443 for ID, Primates for name and order for rank NONE for SPECIES NONE for GENUS NONE for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_ORDER_PRIMATES
    When I request POST taxonomy with 9605 for ID, Homo for name and genus for rank NONE for SPECIES NONE for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_GENUS_HOMO
    When I request elaborate search for the taxonomies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the href of the class taxonomy should be <url>
    Examples:
      | base             | query            | url                     |
      | findByTaxonomyId | taxonomyId=40674 | TAXONOMY_CLASS_MAMMALIA |
      | findByName       | name=Primates    | TAXONOMY_ORDER_PRIMATES |

  Scenario Outline: search taxonomy tree
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 40674 for ID, Mammalia for name and class for rank NONE for SPECIES NONE for GENUS NONE for ORDER NONE for CLASS
    Then set the URL to TAXONOMY_CLASS_MAMMALIA
    When I request POST taxonomy with 9443 for ID, Primates for name and order for rank NONE for SPECIES NONE for GENUS NONE for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_ORDER_PRIMATES
    When I request POST taxonomy with 9605 for ID, Homo for name and genus for rank NONE for SPECIES NONE for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_GENUS_HOMO
    When I request POST taxonomy with 9596 for ID, Pan for name and genus for rank NONE for SPECIES NONE for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_GENUS_PAN
    When I request POST taxonomy with 9606 for ID, Homo sapiens for name and species for rank NONE for SPECIES TAXONOMY_GENUS_HOMO for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS
    When I request POST taxonomy with 9598 for ID, Pan troglodytes for name and species for rank NONE for SPECIES TAXONOMY_GENUS_PAN for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_SPECIES_PAN_TROGLODYTES
    When I request POST taxonomy with 9597 for ID, Pan paniscus for name and species for rank NONE for SPECIES TAXONOMY_GENUS_PAN for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_SPECIES_PAN_PANISCUS
    When I request POST taxonomy with 37010 for ID, Pan troglodytes schweinfurthii for name and subspecies for rank TAXONOMY_SPECIES_PAN_TROGLODYTES for SPECIES TAXONOMY_GENUS_PAN for GENUS TAXONOMY_ORDER_PRIMATES for ORDER TAXONOMY_CLASS_MAMMALIA for CLASS
    Then set the URL to TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII
    When I request elaborate search for the taxonomies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain <N> taxonomies
    And the href of the taxonomy of taxonomies has items <url>
    Examples:
      | base                | query            | N | url                                                                                                                                                                                                           |
      | findByTaxonomyClass | taxonomyId=40674 | 7 | TAXONOMY_ORDER_PRIMATES,TAXONOMY_GENUS_HOMO,TAXONOMY_GENUS_PAN,TAXONOMY_SPECIES_HOMO_SAPIENS,TAXONOMY_SPECIES_PAN_TROGLODYTES,TAXONOMY_SPECIES_PAN_PANISCUS,TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII |
      | findByTaxonomyOrder | taxonomyId=9443  | 6 | TAXONOMY_GENUS_HOMO,TAXONOMY_GENUS_PAN,TAXONOMY_SPECIES_HOMO_SAPIENS,TAXONOMY_SPECIES_PAN_TROGLODYTES,TAXONOMY_SPECIES_PAN_PANISCUS,TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII                         |
      | findByTaxonomyGenus | taxonomyId=9605  | 1 | TAXONOMY_SPECIES_HOMO_SAPIENS                                                                                                                                                                                 |
      | findByTaxonomyGenus | taxonomyId=9596  | 3 | TAXONOMY_SPECIES_PAN_TROGLODYTES,TAXONOMY_SPECIES_PAN_PANISCUS,TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII                                                                                              |

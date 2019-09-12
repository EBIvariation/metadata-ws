Feature: taxonomy object
  Scenario: register a taxonomy successfully
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606
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
      "taxonomyId" : 9606,
      "name" : "Homo sapiens",
      "rank" : "species"
    }
    """
    When I request GET /taxonomies
    Then the response should contain 4 taxonomies

  Scenario: register a taxonomy with taxonomyId less than 1 should fail
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 0
    }
    """
    Then the response code should be 4xx
    When I request GET /taxonomies
    Then the response should contain no taxonomy

  Scenario Outline: search taxonomy tree
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomy with 9606 for ID
    Then set the URL to TAXONOMY_SPECIES_HOMO_SAPIENS
    When I request POST taxonomy with 9598 for ID
    Then set the URL to TAXONOMY_SPECIES_PAN_TROGLODYTES
    When I request POST taxonomy with 9597 for ID
    Then set the URL to TAXONOMY_SPECIES_PAN_PANISCUS
    When I request POST taxonomy with 37010 for ID
    Then set the URL to TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII
    When I request elaborate search for the taxonomies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain only <N> taxonomies excluding class , order and genus
    And the taxonomies contains items <url>
    Examples:
      | base                | query            | N | url                                                                                                                                            |
      | findByTaxonomyClass | taxonomyId=40674 | 4 | TAXONOMY_SPECIES_HOMO_SAPIENS,TAXONOMY_SPECIES_PAN_TROGLODYTES,TAXONOMY_SPECIES_PAN_PANISCUS,TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII |
      | findByTaxonomyOrder | taxonomyId=9443  | 4 | TAXONOMY_SPECIES_HOMO_SAPIENS,TAXONOMY_SPECIES_PAN_TROGLODYTES,TAXONOMY_SPECIES_PAN_PANISCUS,TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII |
      | findByTaxonomyGenus | taxonomyId=9605  | 1 | TAXONOMY_SPECIES_HOMO_SAPIENS                                                                                                                  |
      | findByTaxonomyGenus | taxonomyId=9596  | 3 | TAXONOMY_SPECIES_PAN_TROGLODYTES,TAXONOMY_SPECIES_PAN_PANISCUS,TAXONOMY_SUBSPECIES_PAN_TROGLODYTES_SCWEINFURTHII                               |

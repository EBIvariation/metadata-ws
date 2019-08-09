Feature: taxonomy object

  Scenario: register a taxonomy successfully
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
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
      "name": "Homo Sapiens"
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
      "name": "Homo Sapiens"
    }
    """
    Then the response code should be 4xx
    When I request GET /taxonomies
    Then the response should contain no taxonomy

  Scenario Outline: search taxonomy tree by name or id
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST taxonomies with 207598 for ID, Homininae for name and NONE for parent
    Then set the URL to TAXONOMY_1
    When I request POST taxonomies with 9606 for ID, Homo Sapiens for name and TAXONOMY_1 for parent
    Then set the URL to TAXONOMY_2
    When I request POST taxonomies with 9596 for ID, Pan for name and TAXONOMY_1 for parent
    Then set the URL to TAXONOMY_3
    When I request POST taxonomies with 9597 for ID, Pan paniscus for name and TAXONOMY_3 for parent
    Then set the URL to TAXONOMY_4
    When I request POST taxonomies with 9598 for ID, Pan troglodytes for name and TAXONOMY_3 for parent
    Then set the URL to TAXONOMY_5
    When I request elaborate search for the taxonomies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the response should contain <N> taxonomies
    And the href of the taxonomy of taxonomies has items <url>
    Examples:
      | base                                    | query                     | N | url                                                    |
      | findAllTaxonomyTreeByParentTaxonomyId   | taxonomyId=9606           | 1 | TAXONOMY_2                                             |
      | findAllTaxonomyTreeByParentTaxonomyId   | taxonomyId=9596           | 3 | TAXONOMY_3,TAXONOMY_4,TAXONOMY_5                       |
      | findAllTaxonomyTreeByParentTaxonomyId   | taxonomyId=207598         | 5 | TAXONOMY_1,TAXONOMY_2,TAXONOMY_3,TAXONOMY_4,TAXONOMY_5 |
      | findAllTaxonomyTreeByParentTaxonomyId   | taxonomyId=0              | 0 | NONE                                                   |
      | findAllTaxonomyTreeByParentTaxonomyName | taxonomyName=Homo sapiens | 1 | TAXONOMY_2                                             |
      | findAllTaxonomyTreeByParentTaxonomyName | taxonomyName=Pan          | 3 | TAXONOMY_3,TAXONOMY_4,TAXONOMY_5                       |
      | findAllTaxonomyTreeByParentTaxonomyName | taxonomyName=Homininae    | 5 | TAXONOMY_1,TAXONOMY_2,TAXONOMY_3,TAXONOMY_4,TAXONOMY_5 |
      | findAllTaxonomyTreeByParentTaxonomyName | taxonomyName=None         | 0 | NONE                                                   |
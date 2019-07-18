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
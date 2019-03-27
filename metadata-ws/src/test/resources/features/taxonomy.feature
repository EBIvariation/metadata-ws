Feature: taxonomy object

  Scenario: register a taxonomy successfully
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    And the response code should be 201
    And the Location header should be present with value of TEST_TAXONOMY
    When I request GET with value of TEST_TAXONOMY
    Then the response code should be 200
    And the result json should be:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    When I request GET /taxonomies
    Then the result should contain 1 taxonomies

  Scenario: register a taxonomy with taxonomyId less than 1 should fail
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 0,
      "name": "Homo Sapiens"
    }
    """
    Then the response code should be 4xx
    When I request GET /taxonomies
    Then the result should contain 0 taxonomies
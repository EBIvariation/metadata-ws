Feature: taxonomy object

  Scenario: register a taxonomy successfully
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    Then the response code should be 201
    Then the Location header should be present with value of TEST_TAXONOMY
    When user request GET with value of TEST_TAXONOMY
    Then the response code should be 200
    Then the result json should be:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    When user request GET /taxonomies
    Then the result should contain 1 taxonomies

  Scenario: register a taxonomy with taxonomyId less than 1 should fail
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 0,
      "name": "Homo Sapiens"
    }
    """
    Then the response code should be 4xx
    When user request GET /taxonomies
    Then the result should contain 0 taxonomies
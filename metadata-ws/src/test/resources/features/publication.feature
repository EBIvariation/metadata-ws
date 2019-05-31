Feature: publication object

  Scenario: register a publication successfully
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication1"
    }
    """
    And set the URL to PUBLICATION
    Then the response code should be 201
    And the Location header should be present with value of PUBLICATION
    When I request GET with value of PUBLICATION
    Then the response code should be 200
    And the response should contain field publicationId with value Publication1
    And the response should contain field id with a numeric value
    And the response should contain field lastModifiedDate with a non-empty value


  Scenario: retrieve publication with accession id should fail
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication2"
    }
    """
    And set the URL to PUBLICATION2
    Then the response code should be 201
    When I request GET /publications/Publication2
    Then the response code should be 4xx

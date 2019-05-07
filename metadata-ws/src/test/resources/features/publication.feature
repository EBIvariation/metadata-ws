Feature: publication object

  Scenario: register a publication successfully
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication1"
    }
    """
    Then set the URL to PUBLICATION
    And the response code should be 201
    And the Location header should be present with value of PUBLICATION
    When I request GET with value of PUBLICATION
    Then the response code should be 200
    And the response JSON should be:
    """
    {
      "publicationId": "Publication1"
    }
    """

    Scenario: retrieve publication with accession id should fail
      When I request POST /publications with JSON payload:
      """
      {
        "publicationId": "Publication1"
      }
      """
      Then set the URL to PUBLICATION
      And the response code should be 201
      When I request GET /publications/Publication1
      Then the response code should be 4xx

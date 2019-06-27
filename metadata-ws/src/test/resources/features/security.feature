Feature: Security related tests

  Scenario: access security disabled links
    When I request ananymous GET /
    Then the response code should be 200
    When I request ananymous GET /swagger-ui.html
    Then the response code should be 200


  Scenario: update security enabled links
    Given I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication5"
    }
    """
    Then set the URL to PUBLICATION5
    And the response code should be 201

    When I request PUT PUBLICATION5 with JSON payload:
    """
    {
      "publicationId": "Publication7"
    }
    """
    Then the response code should be 204
    When I request GET with value of PUBLICATION5
    Then the response code should be 200
    And the response should contain field publicationId with value Publication7

    When I request DELETE with value of PUBLICATION5
    Then the response code should be 204


  Scenario: access security enabled links and fail
    When I request unauthorized POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication4"
    }
    """
    Then the response code should be 401

    When I request authorized POST /publications having lesser privileges and with JSON payload:
    """
    {
      "publicationId": "Publication6"
    }
    """
    Then the response code should be 403

    Given I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication5"
    }
    """
    And set the URL to PUBLICATION5
    Then the response code should be 201

    When I request unauthorized PUT PUBLICATION5 with JSON payload:
    """
    {
      "publicationId": "Publication7"
    }
    """
    Then the response code should be 401

    When I request authorized PUT PUBLICATION5 having lesser privileges and with JSON payload:
    """
    {
      "publicationId": "Publication7"
    }
    """
    Then the response code should be 403

    When I request unauthorized DELETE with value of PUBLICATION5
    Then the response code should be 401

    When I request authorized DELETE having lesser privileges and with value of PUBLICATION5
    Then the response code should be 403

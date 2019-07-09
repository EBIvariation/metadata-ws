Feature: Security related tests

  Scenario: access security disabled links
    When I request anonymous GET /
    Then the response code should be 200
    When I request anonymous GET /swagger-ui.html
    Then the response code should be 200


  Scenario: update security enabled links
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication5"
    }
    """
    And set the URL to PUBLICATION5
    Then the response code should be 201

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
    Given I set authorization with testuser having default role
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication4"
    }
    """
    Then the response code should be 403

    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "Publication5"
    }
    """
    And set the URL to PUBLICATION5
    Then the response code should be 201

    Given I set authorization with testuser having default role
    When I request PUT PUBLICATION5 with JSON payload:
    """
    {
      "publicationId": "Publication7"
    }
    """
    Then the response code should be 403

    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request PUT PUBLICATION5 with JSON payload:
    """
    {
      "publicationId": "Publication7"
    }
    """
    Then the response code should be 204

    When I request no authority DELETE with value of PUBLICATION5
    Then the response code should be 401

    Given I set authorization with testuser having default role
    When I request DELETE with value of PUBLICATION5
    Then the response code should be 403

  Scenario: verify only authorized read should succeed
    When I request anonymous GET /publications
    Then the response code should be 401

    When I set authorization with testuser having default role
    And I request GET /publications
    Then the response code should be 200
Feature: study object

  Scenario: register a study successfully
    When user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test study with TEST_TAXONOMY for taxonomy
    And set the URL to TEST_STUDY
    When user request GET with value of TEST_STUDY
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAS0001


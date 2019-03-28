Feature: accession object

  Scenario Outline: find different objects by accession and version
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with EGAS0001 for accession, 1 for version, test_study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    And I request POST /files with JSON payload:
    """
    {
      "accessionVersionId": {
        "accession": "EGAF0001",
        "version": 1
        },
      "hash": "asd123",
      "name": "file1",
      "size": 100,
      "type": "TSV"
    }
    """
    Then set the URL to TEST_FILE
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 1,
      "name": "Species1"
    }
    """
    Then set the URL to TEST_TAXONOMY1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 2,
      "name": "Species2"
    }
    """
    Then set the URL to TEST_TAXONOMY2
    When I create a test parameterized sample with EGAN0001 for accession, 1 for version, Sample1 for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    Then the response code should be 201
    And set the URL to TEST_SAMPLE

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.2
    Then the response code should be 200
    And the result should contain 0 studies

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001
    Then the response code should be 4xx
    And the result should have message with value Please provide an ID in the form accession.version

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.S1
    Then the response code should be 4xx
    And the result should have message with value Please provide an ID in the form accession.version

    When I request elaborate find for the <object> with the parameters: <param>
    Then the response code should be 200
    And the result should contain 1 <object>
    And the accessionVersionId.accession field of <object> 0 should be <value>

    Examples:
      | object  | param                         | value    |
      | studies | accessionVersionId=EGAS0001.1 | EGAS0001 |
      | files   | accessionVersionId=EGAF0001.1 | EGAF0001 |
      | samples | accessionVersionId=EGAN0001.1 | EGAN0001 |

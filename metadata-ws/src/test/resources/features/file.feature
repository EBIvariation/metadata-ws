Feature: file object

  Scenario: register a file successfully
    When I request POST /files with JSON payload:
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
    Then set the URL to FILE
    And the response code should be 201
    And the Location header should be present with value of FILE
    When I request GET with value of FILE
    Then the response code should be 200
    And the response JSON should be:
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
    When I request GET /files
    Then the response should contain one file

  Scenario: attempt to get a file of an analysis of an unreleased study must fail

    Given I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then the response code should be 201
    And set the URL to TAXONOMY

    Given I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY",
      "taxonomy": "TAXONOMY"
    """
    Then the response code should be 201
    And set the URL to REFERENCE_SEQUENCE

    Given I create a study with TAXONOMY for taxonomy and ST0001 for accession
    Then the response code should be 201
    And set the URL to STUDY1

    Given I create a study with TAXONOMY for taxonomy and ST0002 for accession
    Then the response code should be 201
    And set the URL to STUDY2

    Given I create an analysis with AN0001 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY1 for study
    Then the response code should be 201
    And set the URL to ANALYSIS1
    Given I create an analysis with AN0002 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY2 for study
    Then the response code should be 201
    And set the URL to ANALYSIS2

    Given I request POST /files with JSON payload:
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
    Then the response code should be 201
    And set the URL to FILE

    When I request PATCH ANALYSIS1 with list FILE of files
    Then the response code should be 2xx
    When I request PATCH ANALYSIS2 with list FILE of files
    Then the response code should be 2xx

    Given I request PATCH STUDY1 with patch and day tomorrow
    Then the response code should be 200
    # Two studies link to file, one released and another unreleased. File should be available.
    When I request GET with value of FILE
    Then the response code should be 2xx

    Given I request PATCH STUDY2 with patch and day tomorrow
    Then the response code should be 200
    # Now both studies which link to the file are not released, file should become unavailable
    When I request GET with value of FILE
    Then the response code should be 4xx

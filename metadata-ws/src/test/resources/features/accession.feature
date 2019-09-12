Feature: accession object

  Scenario: validation of accession and version
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    And I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test_study",
    "deprecated": false,
    "releaseDate": today,
    """

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001
    Then the response code should be 4xx
    And the response should contain field message with value Please provide an ID in the form accession.version

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.S1
    Then the response code should be 4xx
    And the response should contain field message with value Please provide an ID in the form accession.version

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.1
    Then the response code should be 200
    And the response should contain one study
    And the accessionVersionId.accession field of studies 0 should be EGAS0001

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.2
    Then the response code should be 200
    And the response should contain no study


  Scenario: find study by accession and version
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test_study",
    "deprecated": false,
    "releaseDate": today
    """

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.1
    Then the response code should be 200
    And the response should contain one study
    And the accessionVersionId.accession field of studies 0 should be EGAS0001


  Scenario: find file by accession and version
    Given I set authorization with testoperator having SERVICE_OPERATOR role
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

    When I request elaborate find for the files with the parameters: accessionVersionId=EGAF0001.1
    Then the response code should be 200
    And the response should contain one file
    And the accessionVersionId.accession field of files 0 should be EGAF0001

  Scenario: find sample by accession and version
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 1,
      "name": "Species1",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY1
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 2,
      "name": "Species2",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY2
    When I create a parameterized sample with EGAN0001 for accession, 1 for version, Sample1 for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then the response code should be 201
    And set the URL to SAMPLE

    When I request elaborate find for the samples with the parameters: accessionVersionId=EGAN0001.1
    Then the response code should be 200
    And the response should contain one sample
    And the accessionVersionId.accession field of samples 0 should be EGAN0001


  Scenario: find analysis by accession and version
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank": "SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    And the response code should be 201
    When I request POST /reference-sequences with JSON-like payload:
    """
      "name": "GRCh37",
      "patch": "p2",
      "accession": "GCA_000001407.3",
      "type": "GENOME_ASSEMBLY",
      "taxonomy": "TAXONOMY"
    """
    Then set the URL to REFERENCE_SEQUENCE
    When I create a study
    Then set the URL to STUDY
    When I create an analysis with STUDY for study and REFERENCE_SEQUENCE for reference sequence
    Then the response code should be 201
    And set the URL to ANALYSIS

    When I request elaborate find for the analyses with the parameters: accessionVersionId=EGAA0001.1
    Then the response code should be 200
    And the response should contain 1 analyses
    And the accessionVersionId.accession field of analyses 0 should be EGAA0001


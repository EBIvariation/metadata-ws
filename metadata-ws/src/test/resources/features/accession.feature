Feature: accession object

  Scenario: validation of accession and version
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    And I request POST /studies with JSONLIKE payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test_study",
    "deprecated": false,
    "releaseDate": today,
    "taxonomy": "TEST_TAXONOMY"
    """

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001
    Then the response code should be 4xx
    And the result should have message with value Please provide an ID in the form accession.version

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.S1
    Then the response code should be 4xx
    And the result should have message with value Please provide an ID in the form accession.version

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.1
    Then the response code should be 200
    And the result should contain one study
    And the accessionVersionId.accession field of studies 0 should be EGAS0001

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.2
    Then the response code should be 200
    And the result should contain 0 studies


  Scenario: find study by accession and version
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I request POST /studies with JSONLIKE payload:
    """
    "accessionVersionId": {
      "accession": "EGAS0001",
      "version": 1
    },
    "name": "test_study",
    "deprecated": false,
    "releaseDate": today,
    "taxonomy": "TEST_TAXONOMY"
    """

    When I request elaborate find for the studies with the parameters: accessionVersionId=EGAS0001.1
    Then the response code should be 200
    And the result should contain one study
    And the accessionVersionId.accession field of studies 0 should be EGAS0001


  Scenario: find file by accession and version
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

    When I request elaborate find for the files with the parameters: accessionVersionId=EGAF0001.1
    Then the response code should be 200
    And the result should contain 1 files
    And the accessionVersionId.accession field of files 0 should be EGAF0001


  Scenario: find sample by accession and version
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

    When I request elaborate find for the samples with the parameters: accessionVersionId=EGAN0001.1
    Then the response code should be 200
    And the result should contain 1 samples
    And the accessionVersionId.accession field of samples 0 should be EGAN0001


  Scenario: find analysis by accession and version
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test study with TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY
    When I create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE for reference sequence
    Then the response code should be 201
    And set the URL to TEST_ANALYSIS

    When I request elaborate find for the analyses with the parameters: accessionVersionId=EGAA0001.1
    Then the response code should be 200
    And the result should contain 1 analyses
    And the accessionVersionId.accession field of analyses 0 should be EGAA0001


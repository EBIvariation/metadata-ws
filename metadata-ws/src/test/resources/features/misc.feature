Feature: Miscellaneous functions

  Scenario: verify cross origin resource sharing
    When I request OPTIONS / with GET for Access-Control-Request-Method header and http://www.evil-url.com for Origin header
    Then the response code should be 200
    Given there is an URL http://www.evil-url.com with key EVIL_URL
    And the Access-Control-Allow-Origin header should be present with value of EVIL_URL
    And the Allow header should contain GET

  Scenario Outline: verify metadata objects are auditable
    Given current time as START_TIME1
    When I request POST /reference-sequences with JSON payload:
    """
      {
        "name": "GRCh37",
        "patch": "p2",
        "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
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
    When I create a test parameterized study with testhuman for accession, 1 for version, test human stud for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY
    When I create a test analysis with testhuman for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then set the URL to TEST_ANALYSIS
    When I request POST /files with JSON payload:
    """
      {
        "accessionVersionId": {
          "accession": "testhuman",
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
    When I create a test parameterized sample with testhuman for accession, 1 for version, test human sample for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    Then set the URL to TEST_SAMPLE
    When I request POST /webResources with JSON payload:
    """
      {
        "type": "CENTER_WEB",
        "resourceUrl": "http://www.ebi.ac.uk"
      }
      """
    Then set the URL to TEST_WEB_RESOURCE

    Given current time as END_TIME1
    When I request GET with value of <url>
    Then the response code should be 200
    And the href of the class <class> should be <url>
    And the result should have lastModifiedDate non empty
    And the lastModifiedDate should be after START_TIME1 and before END_TIME1

    Given current time as START_TIME2
    When I request PATCH <url> with content <content> and patch false
    Then the response code should be 2xx

    Given current time as END_TIME2
    When I request GET with value of <url>
    Then the response code should be 200
    And the href of the class <class> should be <url>
    And the result should have lastModifiedDate non empty
    And the lastModifiedDate should be after START_TIME2 and before END_TIME2

    Examples:
      | url | class | content |
      | TEST_REFERENCE_SEQUENCE | referenceSequence | {"name": "nothing important"} |
      | TEST_TAXONOMY | taxonomy | {"name": "nothing important"} |
      | TEST_STUDY | study | {"name": "nothing important"} |
      | TEST_ANALYSIS | analysis | {"name": "nothing important"} |
      | TEST_FILE | file | {"name": "nothing important"} |
      | TEST_SAMPLE | sample | {"name": "nothing important"} |
      | TEST_WEB_RESOURCE | webResource | {"resourceUrl": "http://nothing.important.com"} |

Feature: Common object

  Scenario: perform options
    When user request OPTIONS with header Access-Control-Request-Method,Origin value GET,http://www.evil-url.com
    Then the response code should be 200
    And the Access-Control-Allow-Origin header should be present with actual value http://www.evil-url.com
    And the Allow header should contain GET

  Scenario Outline: verify metadata objects are auditable
    Given current time as START_TIME1
    When user request POST /reference-sequences with json data:
    """
      {
        "name": "GRCh37",
        "patch": "p2",
        "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
        "type": "ASSEMBLY"
      }
      """
    Then set the URL to TEST_REFERENCE_SEQUENCE
    When user request POST /taxonomies with json data:
    """
      {
        "taxonomyId": 9606,
        "name": "Homo Sapiens"
      }
      """
    Then set the URL to TEST_TAXONOMY
    When user create a test parameterized study with testhuman for accession, 1 for version, test human stud for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY
    When user create a test analysis with testhuman for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then set the URL to TEST_ANALYSIS
    When user request POST /files with json data:
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
    When user request POST /taxonomies with json data:
    """
      {
        "taxonomyId": 1,
        "name": "Species1"
      }
      """
    Then set the URL to TEST_TAXONOMY1
    When user request POST /taxonomies with json data:
    """
      {
        "taxonomyId": 2,
        "name": "Species2"
      }
      """
    Then set the URL to TEST_TAXONOMY2
    When user create a test parameterized sample with testhuman for accession, 1 for version, test human sample for name and TEST_TAXONOMY1,TEST_TAXONOMY2 for taxonomy
    Then set the URL to TEST_SAMPLE
    When user request POST /webResources with json data:
    """
      {
        "type": "CENTER_WEB",
        "resourceUrl": "http://www.ebi.ac.uk"
      }
      """
    Then set the URL to TEST_WEB_RESOURCE

    Given current time as END_TIME1
    When user request GET with value of <url>
    Then the response code should be 200
    And the href of the class <class> should be <url>
    And the result should have lastModifiedDate non empty
    And the lastModifiedDate should be after START_TIME1 and before END_TIME1

    Given current time as START_TIME2
    When user request PATCH <url> with content <content> and patch false
    Then the response code should be 2xx

    Given current time as END_TIME2
    When user request GET with value of <url>
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

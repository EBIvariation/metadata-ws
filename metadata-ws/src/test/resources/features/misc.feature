Feature: Miscellaneous functions

  Scenario: verify cross origin resource sharing
    When I request OPTIONS / with GET for Access-Control-Request-Method header and http://www.evil-url.com for Origin header
    Then the response code should be 200

    Given there is an URL http://www.evil-url.com with key EVIL_URL
    And the Access-Control-Allow-Origin header should be present with value of EVIL_URL
    And the Allow header should contain GET


  Scenario Outline: verify metadata objects are auditable
    Given I set authorization with testoperator having SERVICE_OPERATOR role
    And current date as TODAY
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens",
      "rank":"SPECIES"
    }
    """
    Then set the URL to TAXONOMY
    And the response code should be 201
    When I request POST /reference-sequences with JSON-like payload:
    """
        "name": "GRCh37",
        "patch": "p2",
        "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
        "type": "GENOME_ASSEMBLY",
        "taxonomy": "TAXONOMY"
      """
    Then set the URL to REFERENCE_SEQUENCE

    When I request POST /studies with JSON-like payload:
    """
    "accessionVersionId": {
      "accession": "testhuman",
      "version": 1
    },
    "name": "test human study",
    "deprecated": false,
    "releaseDate": today
    """
    Then set the URL to STUDY
    When I create an analysis with testhuman for accession, REFERENCE_SEQUENCE for reference sequence, STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then set the URL to ANALYSIS
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
    Then set the URL to FILE
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
    When I create a parameterized sample with testhuman for accession, 1 for version, test human sample for name and TAXONOMY1,TAXONOMY2 for taxonomy
    Then set the URL to SAMPLE
    When I request POST /webResources with JSON payload:
    """
      {
        "type": "CENTER_WEB",
        "resourceUrl": "http://www.ebi.ac.uk"
      }
      """
    Then set the URL to WEB_RESOURCE

    When I request GET with value of <url>
    Then the response code should be 200

    And the href of the class <class> should be <url>
    And the response should contain field lastModifiedDate with a non-empty value
    And the lastModifiedDate should equal TODAY

    When I request PATCH <url> with content <content> and patch false
    Then the response code should be 2xx

    When I request GET with value of <url>
    Then the response code should be 200

    And the href of the class <class> should be <url>
    And the response should contain field lastModifiedDate with a non-empty value
    And the lastModifiedDate should equal TODAY

    Examples:
      | url                | class             | content                                         |
      | REFERENCE_SEQUENCE | referenceSequence | {"name": "nothing important"}                   |
      | TAXONOMY           | taxonomy          | {"name": "nothing important"}                   |
      | STUDY              | study             | {"name": "nothing important"}                   |
      | ANALYSIS           | analysis          | {"name": "nothing important"}                   |
      | FILE               | file              | {"name": "nothing important"}                   |
      | SAMPLE             | sample            | {"name": "nothing important"}                   |
      | WEB_RESOURCE       | webResource       | {"resourceUrl": "http://nothing.important.com"} |

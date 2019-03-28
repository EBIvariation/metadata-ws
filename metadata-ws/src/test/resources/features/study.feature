Feature: study object

  Scenario: register a study successfully
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
    When I request GET with value of TEST_STUDY
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAS0001


  Scenario Outline: search various study by taxonomy name and id
    When I request POST taxonomies 207598 for id, Homininae for name and NONE for ancestors
    Then set the URL to TEST_TAXONOMY_1
    When I request POST taxonomies 9606 for id, Homo Sapiens for name and TEST_TAXONOMY_1 for ancestors
    Then set the URL to TEST_TAXONOMY_2
    When I request POST taxonomies 9596 for id, Pan for name and TEST_TAXONOMY_1 for ancestors
    Then set the URL to TEST_TAXONOMY_3
    When I request POST taxonomies 9597 for id, Pan paniscus for name and TEST_TAXONOMY_1,TEST_TAXONOMY_3 for ancestors
    Then set the URL to TEST_TAXONOMY_4
    When I request POST taxonomies 9598 for id, Pan troglodytes for name and TEST_TAXONOMY_1,TEST_TAXONOMY_3 for ancestors
    Then set the URL to TEST_TAXONOMY_5

    When I create a test parameterized study with testhuman for accession, 1 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY_2 for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with testbonobo for accession, 1 for version, test bonobo study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY_4 for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with testchimpanzee for accession, 1 for version, test chimpanzee study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY_5 for taxonomy
    Then set the URL to TEST_STUDY3

    When I request elaborate search for the studies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | base | query | N | url |
      | taxonomy-id | id=9606 | 1 | TEST_STUDY1 |
      | taxonomy-id | id=9596 | 2 | TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-id | id=207598 | 3 | TEST_STUDY1,TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-id | id=0 | 0 | NONE |
      | taxonomy-name | name=Homo sapiens | 1 | TEST_STUDY1 |
      | taxonomy-name | name=Pan | 2 | TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-name | name=Homininae | 3 | TEST_STUDY1,TEST_STUDY2,TEST_STUDY3 |
      | taxonomy-name | name=None | 0 | NONE |


  Scenario Outline: search various studies by release date
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with releasedYesterday for accession, 1 for version, nothing important for name, false for deprecated, -1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with releasedToday for accession, 1 for version, nothing important for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with releasedTomorrow for accession, 1 for version, nothing important for name, false for deprecated, 1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY3

    When I request elaborate search with day for the studies base <base> and with the parameters: <query> and <day>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | base | day | query | N | url |
      | release-date | 0 | to= | 2 | TEST_STUDY1,TEST_STUDY2 |
      | release-date | 0 | from= | 1 | TEST_STUDY2 |


  Scenario Outline: find various studies by release date range
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with releasedYesterday for accession, 1 for version, nothing important for name, false for deprecated, -1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with releasedToday for accession, 1 for version, nothing important for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with releasedTomorrow for accession, 1 for version, nothing important for name, false for deprecated, 1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY3

    When I request elaborate search with date range for the studies base <base> and with the parameters: <day>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | base | day | query | N | url |
      | release-date | 0 | from= | 1 | TEST_STUDY2 |


  Scenario Outline: find various studies by analysis
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh38",
      "patch": "p2",
      "accessions": ["GCA_000001405.17", "GCF_000001405.28"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_2
    When I create a test parameterized study with EGAS0001 for accession, 1 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with EGAS0001 for accession, 2 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE_1 for reference sequence, TEST_STUDY1 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then the response code should be 201
    When I create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE_2 for reference sequence, TEST_STUDY2 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then the response code should be 201

    When I request elaborate find for the studies with the parameters: <param>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | param | N | url |
      | analyses.referenceSequences.name=GRCh37 | 1 | TEST_STUDY1 |
      | analyses.referenceSequences.name=GRCh38 | 1 | TEST_STUDY2 |
      | analyses.referenceSequences.name=NCBI36 | 0 | NONE |
      | analyses.referenceSequences.name=GRCh37&analyses.referenceSequences.patch=p2 | 1 | TEST_STUDY1 |
      | analyses.referenceSequences.name=GRCh38&analyses.referenceSequences.patch=p2 | 1 | TEST_STUDY2 |
      | analyses.referenceSequences.name=GRCh37&analyses.referenceSequences.patch=p3 | 0 | NONE |
      | analyses.referenceSequences.name=NCBI36&analyses.referenceSequences.patch=p2 | 0 | NONE |
      | analyses.type=CASE_CONTROL | 2 | TEST_STUDY1,TEST_STUDY2 |
      | analyses.type=TUMOR | 0 | NONE |
      | analyses.type=COLLECTION | 0 | NONE |
      | analyses.referenceSequences.name=GRCh38&analyses.type=CASE_CONTROL | 1 | TEST_STUDY2 |
      | analyses.referenceSequences.name=GRCh38&analyses.type=TUMOR | 0 | NONE |
      | analyses.referenceSequences.name=GRCh38&analyses.type=COLLECTION | 0 | NONE |
      | analyses.referenceSequences.name=NCBI36&analyses.type=CASE_CONTROL | 0 | NONE |


  Scenario: search various studies by name value pair for accession
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with EGAS0001 for accession, 1 for version, test human study based on GRCh37 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with EGAS0001 for accession, 2 for version, test human study based on GRCh37 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with EGAS0002 for accession, 3 for version, test human study based on GRCh38 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY3

    When I request search for the studies with base accession and name accession value EGAS0001
    Then the response code should be 200
    And the href of the class study should be TEST_STUDY2
    And the result should have accessionVersionId.accession with value EGAS0001
    And the result should have accessionVersionId.version with value 2

    When I request search for the studies with base accession and name accession value EGAS0002
    Then the response code should be 200
    And the href of the class study should be TEST_STUDY3
    And the result should have accessionVersionId.accession with value EGAS0002
    And the result should have accessionVersionId.version with value 3

    When I request search for the studies with base accession and name accession value EGAS0003
    Then the response code should be 404


  Scenario Outline: search various studies by name value pair
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with EGAS0001 for accession, 1 for version, test human study based on GRCh37 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with EGAS0001 for accession, 2 for version, test human study based on GRCh37 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with EGAS0002 for accession, 1 for version, test human study based on GRCh38 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY3

    When I request search for the studies with base accession and name accession value EGAS0001
    Then the response code should be 200
    And the href of the class study should be TEST_STUDY2
    And the result should have accessionVersionId.accession with value EGAS0001
    And the result should have accessionVersionId.version with value 2

    When I request search for the studies with base text and name searchTerm value grCh37
    Then the response code should be 200
    And the result should contain 2 studies
    And the accessionVersionId.accession field of studies 0 should be EGAS0001

    When I request search for the <class> with base <base> and name <name> value <value>
    Then the response code should be 200
    And the result should contain <items> <class>

    Examples:
      | class | base  | name | value | items |
      | studies | text | searchTerm | human | 3 |
      | studies | text | searchTerm | important | 3 |
      | studies | text | searchTerm | grCh39 | 0 |


  Scenario Outline: search various studies by paging and sorting
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with EGAS0001 for accession, 1 for version, test human B for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with EGAS0002 for accession, 1 for version, test human A for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1

    When I request GET /studies
    Then the response code should be 200
    And the result should contain 2 studies
    And the result should have page.size with value 20
    And the result should have page.totalElements with value 2
    And the result should have page.totalPages with value 1

    When I request GET for the studies with query parameter <param>
    Then the response code should be 200
    And the result should contain 1 studies
    And the href of the study of studies has items <url>

    When I request GET for the studies with query parameter page=1
    Then the response code should be 200
    And the result should contain 0 studies

    Examples:
    | param | url |
    | size=1 | TEST_STUDY2 |
    | size=1&sort=name | TEST_STUDY1 |


  Scenario Outline: search various public studies
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001405.3", "GCF_000001405.14"],
      "type": "ASSEMBLY"
    }
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When I create a test parameterized study with 1kg for accession, 1 for version, 1kg pilot for name, false for deprecated, -1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with 1kg for accession, 2 for version, 1kg phase 1 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with 1kg for accession, 3 for version, 1kg phase 3 for name, false for deprecated, 1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY3
    When I create a test analysis with analysisReleasedYesterday for accession, TEST_REFERENCE_SEQUENCE_1 for reference sequence, TEST_STUDY1 for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
    Then set the URL to TEST_ANALYSIS

    When I request GET /studies
    Then the result should contain 2 studies
    And the href of the study of studies has items TEST_STUDY1,TEST_STUDY2

    When I request GET with value of TEST_STUDY1
    Then the response code should be 200

    When I request GET for analyses of TEST_STUDY1
    Then the result should contain 1 analyses
    And the href of the analysis of analyses has items TEST_ANALYSIS

    When I request GET for analyses of TEST_STUDY2
    Then the response code should be 200
    And the result should contain 0 analyses

    When I request search for the studies with the parameters: taxonomy.taxonomyId=9606
    Then the response code should be 200
    And the result should contain 2 studies
    And the href of the study of studies has items TEST_STUDY1,TEST_STUDY2

    When I request elaborate search for the studies base accession and with the parameters: accession=1kg
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value 1kg
    And the result should have accessionVersionId.version with value 2
    And the href of the class study should be TEST_STUDY2

    When I request elaborate search with date range for the studies base release-date and with the parameters: 0
    Then the response code should be 200
    And the result should contain 1 studies
    And the href of the study of studies has items TEST_STUDY2

    When I request elaborate search for the studies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <url>

  Examples:
    | base | query | N | url |
    | taxonomy-id | id=9606 | 2 | TEST_STUDY1,TEST_STUDY2 |
    | taxonomy-name | name=Homo sapiens | 2 | TEST_STUDY1,TEST_STUDY2 |
    | text | searchTerm=1kg | 2 | TEST_STUDY1,TEST_STUDY2 |


  Scenario Outline: search various undeprecated studies
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 1 for version, 1kg pilot for name, true for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with 1kg for accession, 2 for version, 1kg phase 1 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2

    When I request GET /studies
    Then the result should contain 1 studies
    And the href of the study of studies has items TEST_STUDY2

    When I request GET with value of TEST_STUDY2
    Then the response code should be 200
    And the href of the class study should be TEST_STUDY2

    When I request GET for analyses of TEST_STUDY2
    Then the result should contain 0 analyses

    When I request search for the studies with the parameters: taxonomy.taxonomyId=9606
    Then the response code should be 200
    And the result should contain 1 studies
    And the href of the study of studies has items TEST_STUDY2

    When I request elaborate search for the studies base accession and with the parameters: accession=1kg
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value 1kg
    And the result should have accessionVersionId.version with value 2
    And the href of the class study should be TEST_STUDY2

    When I request elaborate search for the studies base <base> and with the parameters: <query>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <url>

    Examples:
      | base | query | N | url |
      | taxonomy-id | id=9606 | 1 | TEST_STUDY2 |
      | taxonomy-name | name=Homo sapiens | 1 | TEST_STUDY2 |
      | text | searchTerm=1kg | 1 | TEST_STUDY2 |


  Scenario: search various yet to publish studies
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 3 for version, 1kg phase 3 for name, false for deprecated, 1 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1

    When I request GET with value of TEST_STUDY1
    Then the response code should be 404

    When I request GET for analyses of TEST_STUDY1
    Then the response code should be 404


  Scenario: search studies invalid dates
    When I request search for studies with release-date
    Then the response code should be 4xx
    And the result should have exception with value java.lang.IllegalArgumentException
    And the result should have message with value Either from or to needs to be non-null

    When I request elaborate search for the studies base release-date and with the parameters: from=wrong-format-date
    Then the result should have exception with value java.lang.IllegalArgumentException
    And the result should have message with value Please provide a date in the form yyyy-mm-dd


  Scenario: search various deprecated studies
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 1 for version, 1kg pilot for name, true for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1

    When I request GET with value of TEST_STUDY1
    Then the response code should be 404

    When I request GET for analyses of TEST_STUDY1
    Then the response code should be 404


  Scenario: search studies on various deprecated fields
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 1 for version, 1kg pilot for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1

    When I request GET with value of TEST_STUDY1
    Then the response code should be 200
    And the href of the class study should be TEST_STUDY1
    And the result should have description with value Nothing important
    And the result should not contain deprecated

    When I request PATCH TEST_STUDY1 with content {"deprecated": "true"}
    Then the response code should be 2xx

    When I request GET with value of TEST_STUDY1
    Then the response code should be 4xx


  Scenario Outline: find linked studies
    Given there is an URL https://nohost//studies/999 with key STUDY_NON_EXISTING
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with testhuman for accession, 1 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY1
    When I create a test parameterized study with testhuman for accession, 2 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY2
    When I create a test parameterized study with testhuman for accession, 3 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY3
    When I create a test parameterized study with testhuman for accession, 4 for version, test human study for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY4

    When I request PATCH TEST_STUDY1 with list TEST_STUDY2,TEST_STUDY3 for childStudies
    Then the response code should be 2xx

    When I request GET for linkedStudies of <url>
    Then the response code should be 200
    And the result should contain <N> studies
    And the href of the study of studies has items <linkedStudies>

    When I request GET for linkedStudies of TEST_STUDY4
    Then the response code should be 200
    And the result should contain 0 studies

    When I request PATCH TEST_STUDY1 with list STUDY_NON_EXISTING for childStudies
    Then the response code should be 2xx

    When I request GET for linkedStudies of TEST_STUDY1
    Then the response code should be 200
    And the result should contain 0 studies

    Examples:
  | url | N | linkedStudies |
  | TEST_STUDY1 | 2 | TEST_STUDY2,TEST_STUDY3 |
  | TEST_STUDY2 | 2 | TEST_STUDY1,TEST_STUDY3 |
  | TEST_STUDY3 | 2 | TEST_STUDY1,TEST_STUDY2 |


  Scenario: deprecate to undeprecate studies
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 1 for version, 1kg pilot for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY

    When I request GET with value of TEST_STUDY
    Then the response code should be 200
    And the href of the class study should be TEST_STUDY

    When I request PATCH TEST_STUDY with patch and content {"deprecated": "true"}
    Then the response code should be 2xx
    And the href of the class study should be TEST_STUDY

    When I request GET with value of TEST_STUDY
    Then the response code should be 4xx

    When I request PATCH TEST_STUDY with patch and content {"deprecated": "false"}
    Then the response code should be 2xx
    And the href of the class study should be TEST_STUDY

    When I request GET with value of TEST_STUDY
    Then the response code should be 200


  Scenario: verify browsable is a property of study
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 1 for version, 1kg pilot for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY

    When I request GET with value of TEST_STUDY
    Then the response code should be 200
    And the result should have browsable as false

    When I request search for the studies with the parameters: browsable=true
    Then the response code should be 200
    And the result should contain 0 studies

    When I request PATCH TEST_STUDY with content {"browsable": "true"}
    Then the response code should be 2xx

    When I request search for the studies with the parameters: browsable=true
    Then the response code should be 200
    And the result should contain 1 studies
    And the href of the study of studies has items TEST_STUDY


  Scenario: verify study release date could be changed
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 3 for version, 1kg phase 3 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY

    When I request GET with value of TEST_STUDY
    Then the response code should be 200
    And the result should have releaseDate existing

    When I request PATCH TEST_STUDY with patch and day 1
    Then the response code should be 200
    And the result should have releaseDate existing
    And the difference between releaseDate and today should be 1 day

    When I request GET with value of TEST_STUDY
    Then the response code should be 404

    When I request PATCH TEST_STUDY with patch and day 0
    Then the response code should be 200
    And the result should have releaseDate existing
    And the difference between releaseDate and today should be 0 day

    When I request GET with value of TEST_STUDY
    Then the response code should be 200
    And the result should have releaseDate existing
    And the difference between releaseDate and today should be 0 day


  Scenario: verify non-existing study with patch
    Given there is an URL https://nohost//studies/999 with key STUDY_NON_EXISTING
    When I request PATCH STUDY_NON_EXISTING with patch and day 0
    Then the response code should be 4xx

  Scenario: patch study with invalid request
    When I request POST /taxonomies with JSON payload:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    Then set the URL to TEST_TAXONOMY
    When I create a test parameterized study with 1kg for accession, 3 for version, 1kg phase 3 for name, false for deprecated, 0 for releaseDay and TEST_TAXONOMY for taxonomy
    Then set the URL to TEST_STUDY

    When I request PATCH TEST_STUDY with content {"releaseDate": 2001}
    Then the response code should be 400

    When I request PATCH TEST_STUDY with content {""}
    Then the response code should be 400

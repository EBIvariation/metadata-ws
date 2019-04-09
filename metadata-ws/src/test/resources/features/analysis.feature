Feature: analysis object

  Scenario: register an analysis successfully
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
    When I request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAA0001

  Scenario: update an analysis successfully
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_1
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

    When I create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1 for reference sequence
    Then the response code should be 201
    And set the URL to TEST_ANALYSIS
    When I request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAA0001
    When I request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    And the result should contain one reference-sequence
    And the href of the referenceSequence of reference-sequences has items TEST_REFERENCE_SEQUENCE_1

    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p3",
      "accessions": ["GCA_000001407.4", "GCF_000001407.15"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_2
    When I request PATCH TEST_ANALYSIS with list TEST_REFERENCE_SEQUENCE_2 for referenceSequences
    Then the response code should be 2xx
    When I request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAA0001
    When I request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    And the result should contain one reference-sequence
    And the href of the referenceSequence of reference-sequences has items TEST_REFERENCE_SEQUENCE_2

  Scenario Outline: update an analysis with invalid reference sequences list should fail
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_1
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

    When I create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1 for reference sequence
    Then the response code should be 201
    And set the URL to TEST_ANALYSIS
    When I request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAA0001
    When I request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    And the result should contain one reference-sequence
    And the href of the referenceSequence of reference-sequences has items TEST_REFERENCE_SEQUENCE_1

    When I request PATCH TEST_ANALYSIS with list <list> for referenceSequences
    Then the response code should be 4xx
    And the result should have exception with value <exception>

    Examples:
     | list | exception |
     | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException |
     | EMPTY | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException   |
     | TEST_REFERENCE_SEQUENCE_1,EMPTY | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException |

  Scenario Outline: register an analysis with invalid reference sequence should fail
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
    When I create a test analysis with TEST_STUDY for study and <list> for reference sequence
    Then the response code should be 4xx
    And the result should have exception with value <exception>

    Examples:
      | list | exception |
      | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException |
      | EMPTY | uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException |

  Scenario: delete all of an analysis's reference sequences should fail
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "BRCA1",
      "patch": "3",
      "accessions": ["NM_007294.3"],
      "type": "GENE"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "BRCA2",
      "patch": "3",
      "accessions": ["NM_000059.3"],
      "type": "GENE"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_2
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
    When I create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2 for reference sequence
    Then the response code should be 201
    And set the URL to TEST_ANALYSIS
    When I request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAA0001
    When I request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    And the result should contain 2 reference-sequences
    And the href of the referenceSequence of reference-sequences has items TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2

    When I request DELETE for the referenceSequences of TEST_REFERENCE_SEQUENCE_1 of the TEST_ANALYSIS
    Then the response code should be 2xx

    When I request DELETE for the referenceSequences of TEST_REFERENCE_SEQUENCE_2 of the TEST_ANALYSIS
    Then the response code should be 4xx
    And the result should have exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException


  Scenario: register an analysis with multiple gene reference sequences successfully
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "BRCA1",
      "patch": "3",
      "accessions": ["NM_007294.3"],
      "type": "GENE"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    {
      "name": "BRCA2",
      "patch": "3",
      "accessions": ["NM_000059.3"],
      "type": "GENE"
    }
    """
    Then set the URL to TEST_REFERENCE_SEQUENCE_2
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
    When I create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2 for reference sequence
    Then the response code should be 201
    And set the URL to TEST_ANALYSIS
    When I request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    And the result should have accessionVersionId.accession with value EGAA0001
    When I request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    And the result should contain 2 reference-sequences
    And the href of the referenceSequence of reference-sequences has items TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2


  Scenario Outline: register an analysis with multiple non-gene reference sequences should fail
    When I request POST /reference-sequences with JSON payload:
    """
    <test_reference_sequence_1_json>
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When I request POST /reference-sequences with JSON payload:
    """
    <test_reference_sequence_2_json>
    """
    Then the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_2
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
    When I create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2 for reference sequence
    Then the response code should be 4xx
    And the result should have exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException
    And the result should have message with value Invalid type of reference sequences. When multiple reference sequence URLs are provided, all of them should point to gene sequences

    Examples:
      | test_reference_sequence_1_json | test_reference_sequence_2_json |
      | {"name": "GRCh37","patch": "p2","accessions": ["GCA_000001407.3", "GCF_000001407.14"],"type": "ASSEMBLY"} | {"name": "GRCh37","patch": "p3","accessions": ["GCA_000001407.4", "GCF_000001407.15"],"type": "ASSEMBLY"} |
      | {"name": "FOXP2","patch": "nothing important","accessions": ["NM_014491.3"],"type": "TRANSCRIPTOME"} | {"name": "BRCA2","patch": "nothing important","accessions": ["NM_000059.3"],"type": "TRANSCRIPTOME"} |
      | {"name": "BRCA1","patch": "3","accessions": ["NM_007294.3"],"type": "GENE"} | {"name": "BRCA2","patch": "nothing important","accessions": ["NM_000059.3"],"type": "TRANSCRIPTOME"} |


   Scenario Outline: find one analysis by type, technology or platform
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
     When I create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
     Then the response code should be 201
     And set the URL to TEST_ANALYSIS_1
     When I create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, ARRAY for technology, TUMOR for type and PacBio for platform
     Then the response code should be 201
     And set the URL to TEST_ANALYSIS_2

     When I request search for the analyses with the parameters: <query>
     Then the response code should be 200
     And the result should contain 1 analyses
     And the href of the analysis of analyses has items <analysis_url>

     Examples:
      | query | analysis_url |
      | type=CASE_CONTROL | TEST_ANALYSIS_1 |
      | type=TUMOR | TEST_ANALYSIS_2 |
      | platform=Illumina | TEST_ANALYSIS_1 |
      | platform=PacBio | TEST_ANALYSIS_2 |
      | platform=pacbio&type=TUMOR | TEST_ANALYSIS_2 |
      | technology=GWAS | TEST_ANALYSIS_1 |
      | technology=ARRAY&type=TUMOR | TEST_ANALYSIS_2 |


   Scenario Outline: find zero analysis by type, technology or platform
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
     When I create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
     Then the response code should be 201
     And set the URL to TEST_ANALYSIS_1
     When I create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, ARRAY for technology, TUMOR for type and PacBio for platform
     Then the response code should be 201
     And set the URL to TEST_ANALYSIS_2

     When I request search for the analyses with the parameters: <query>
     Then the response code should be 200
     And the result should contain 0 analyses

     Examples:
       | query |
       | type=COLLECTION |
       | platform=nextSeq |
       | technology=CURATION |


   Scenario Outline: find analysis by invalid type or technology should fail
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
     When I create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
     Then the response code should be 201
     And set the URL to TEST_ANALYSIS_1
     When I create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, ARRAY for technology, TUMOR for type and PacBio for platform
     Then the response code should be 201
     And set the URL to TEST_ANALYSIS_2

     When I request search for the analyses with the parameters: <query>
     Then the response code should be 4xx

     Examples:
       | query |
       | type=UNKNOWN |
       | technology=UNKNOWN |



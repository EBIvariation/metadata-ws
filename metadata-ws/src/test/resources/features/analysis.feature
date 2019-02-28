Feature: analysis object

  Scenario: register an analysis successfully
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE
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
    When user create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE for reference sequence
    And the response code should be 201
    And set the URL to TEST_ANALYSIS
    When user request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAA0001

  Scenario: update an analysis successfully
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_1
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

    When user create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1 for reference sequence
    And the response code should be 201
    And set the URL to TEST_ANALYSIS
    When user request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAA0001
    When user request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain 1 reference-sequences
    And the href of the referenceSequence of reference-sequences 0 should be TEST_REFERENCE_SEQUENCE_1

    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh37",
      "patch": "p3",
      "accessions": ["GCA_000001407.4", "GCF_000001407.15"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_2
    When user request PATCH TEST_ANALYSIS with list TEST_REFERENCE_SEQUENCE_2 for referenceSequences
    And the response code should be 2xx
    When user request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAA0001
    When user request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain 1 reference-sequences
    And the href of the referenceSequence of reference-sequences 0 should be TEST_REFERENCE_SEQUENCE_2

  Scenario Outline: update an analysis with invalid reference sequences list should fail
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "GRCh37",
      "patch": "p2",
      "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
      "type": "ASSEMBLY"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_1
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

    When user create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1 for reference sequence
    And the response code should be 201
    And set the URL to TEST_ANALYSIS
    When user request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAA0001
    When user request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain 1 reference-sequences
    And the href of the referenceSequence of reference-sequences 0 should be TEST_REFERENCE_SEQUENCE_1

    When user request PATCH TEST_ANALYSIS with list <list> for referenceSequences
    And the response code should be 4xx
    And the result should contain exception with value <exception>

    Examples:
     | list | exception |
     | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException |
     | EMPTY | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException   |
     | TEST_REFERENCE_SEQUENCE_1,EMPTY | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException |

  Scenario Outline: register an analysis with invalid reference sequence should fail
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
    When user create a test analysis with TEST_STUDY for study and <list> for reference sequence
    And the response code should be 4xx
    And the result should contain exception with value <exception>

    Examples:
      | list | exception |
      | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException |
      | EMPTY | uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException |

  Scenario: delete all of an analysis's reference sequences should fail
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "BRCA1",
      "patch": "3",
      "accessions": ["NM_007294.3"],
      "type": "GENE"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "BRCA2",
      "patch": "3",
      "accessions": ["NM_000059.3"],
      "type": "GENE"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_2
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
    When user create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2 for reference sequence
    And the response code should be 201
    And set the URL to TEST_ANALYSIS
    When user request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAA0001
    When user request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain 2 reference-sequences
    And the href of the referenceSequence of reference-sequences 0 should be TEST_REFERENCE_SEQUENCE_1
    And the href of the referenceSequence of reference-sequences 1 should be TEST_REFERENCE_SEQUENCE_2

    When user request DELETE for the referenceSequences of TEST_REFERENCE_SEQUENCE_1 of the TEST_ANALYSIS
    And the response code should be 2xx

    When user request DELETE for the referenceSequences of TEST_REFERENCE_SEQUENCE_2 of the TEST_ANALYSIS
    And the response code should be 4xx
    And the result should contain exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.AnalysisWithoutReferenceSequenceException


  Scenario: register an analysis with multiple gene reference sequences successfully
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "BRCA1",
      "patch": "3",
      "accessions": ["NM_007294.3"],
      "type": "GENE"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When user request POST /reference-sequences with json data:
    """
    {
      "name": "BRCA2",
      "patch": "3",
      "accessions": ["NM_000059.3"],
      "type": "GENE"
    }
    """
    And set the URL to TEST_REFERENCE_SEQUENCE_2
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
    When user create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2 for reference sequence
    And the response code should be 201
    And set the URL to TEST_ANALYSIS
    When user request GET with value of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAA0001
    When user request GET for referenceSequences of TEST_ANALYSIS
    Then the response code should be 200
    Then the result should contain 2 reference-sequences
    And the href of the referenceSequence of reference-sequences 0 should be TEST_REFERENCE_SEQUENCE_1
    And the href of the referenceSequence of reference-sequences 1 should be TEST_REFERENCE_SEQUENCE_2


  Scenario Outline: register an analysis with multiple non-gene reference sequences should fail
    When user request POST /reference-sequences with json data:
    """
    <test_reference_sequence_1_json>
    """
    And the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_1
    When user request POST /reference-sequences with json data:
    """
    <test_reference_sequence_2_json>
    """
    And the response code should be 201
    And set the URL to TEST_REFERENCE_SEQUENCE_2
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
    When user create a test analysis with TEST_STUDY for study and TEST_REFERENCE_SEQUENCE_1,TEST_REFERENCE_SEQUENCE_2 for reference sequence
    And the response code should be 4xx
    And the result should contain exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidReferenceSequenceException
    And the result should contain message with value Invalid type of reference sequences. When multiple reference sequence URLs are provided, all of them should point to gene sequences

    Examples:
      | test_reference_sequence_1_json | test_reference_sequence_2_json |
      | {"name": "GRCh37","patch": "p2","accessions": ["GCA_000001407.3", "GCF_000001407.14"],"type": "ASSEMBLY"} | {"name": "GRCh37","patch": "p3","accessions": ["GCA_000001407.4", "GCF_000001407.15"],"type": "ASSEMBLY"} |
      | {"name": "FOXP2","patch": "nothing important","accessions": ["NM_014491.3"],"type": "TRANSCRIPTOME"} | {"name": "BRCA2","patch": "nothing important","accessions": ["NM_000059.3"],"type": "TRANSCRIPTOME"} |
      | {"name": "BRCA1","patch": "3","accessions": ["NM_007294.3"],"type": "GENE"} | {"name": "BRCA2","patch": "nothing important","accessions": ["NM_000059.3"],"type": "TRANSCRIPTOME"} |


   Scenario Outline: find one analysis by type, technology or platform
     When user request POST /reference-sequences with json data:
     """
     {
       "name": "GRCh37",
       "patch": "p2",
       "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
       "type": "ASSEMBLY"
     }
     """
     And set the URL to TEST_REFERENCE_SEQUENCE
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
     When user create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
     And the response code should be 201
     And set the URL to TEST_ANALYSIS_1
     When user create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, ARRAY for technology, TUMOR for type and PacBio for platform
     And the response code should be 201
     And set the URL to TEST_ANALYSIS_2

     When user request search for the analyses with the parameters: <query>
     And the response code should be 200
     And the result should contain 1 analyses
     And the href of the analysis of analyses 0 should be <analysis_url>

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
     When user request POST /reference-sequences with json data:
     """
     {
       "name": "GRCh37",
       "patch": "p2",
       "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
       "type": "ASSEMBLY"
     }
     """
     And set the URL to TEST_REFERENCE_SEQUENCE
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
     When user create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
     And the response code should be 201
     And set the URL to TEST_ANALYSIS_1
     When user create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, ARRAY for technology, TUMOR for type and PacBio for platform
     And the response code should be 201
     And set the URL to TEST_ANALYSIS_2

     When user request search for the analyses with the parameters: <query>
     And the response code should be 200
     And the result should contain 0 analyses

     Examples:
       | query |
       | type=COLLECTION |
       | platform=nextSeq |
       | technology=CURATION |


   Scenario Outline: find analysis by invalid type or technology should fail
     When user request POST /reference-sequences with json data:
     """
     {
       "name": "GRCh37",
       "patch": "p2",
       "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
       "type": "ASSEMBLY"
     }
     """
     And set the URL to TEST_REFERENCE_SEQUENCE
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
     When user create a test analysis with EGAA0001 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, GWAS for technology, CASE_CONTROL for type and Illumina for platform
     And the response code should be 201
     And set the URL to TEST_ANALYSIS_1
     When user create a test analysis with EGAA0002 for accession, TEST_REFERENCE_SEQUENCE for reference sequence, TEST_STUDY for study, ARRAY for technology, TUMOR for type and PacBio for platform
     And the response code should be 201
     And set the URL to TEST_ANALYSIS_2

     When user request search for the analyses with the parameters: <query>
     And the response code should be 4xx

     Examples:
       | query |
       | type=UNKNOWN |
       | technology=UNKNOWN |



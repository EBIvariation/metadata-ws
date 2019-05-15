Feature: Transitive release date control for child objects of a Study

  Scenario Outline: Objects to which at least released study links should be accessible, and vice versa

    # Create the common taxonomy
    When I request POST taxonomies with 9606 for ID, Homo Sapiens for name and NONE for ancestors
    Then set the URL to TAXONOMY
    And the response code should be 201

    # Create the common reference sequences
    Given I request POST /reference-sequences with JSON-like payload:
    """
          "name": "GRCh37",
          "patch": "p2",
          "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
          "type": "ASSEMBLY",
          "taxonomy": "TAXONOMY"
    """
    Then set the URL to REFERENCE_SEQUENCE
    And the response code should be 201

    # Create two studies with the release date of "yesterday"
    Given I create a study with TAXONOMY for taxonomy and Study1 for accession
    Then set the URL to STUDY1
    And the response code should be 201
    Given I create a study with TAXONOMY for taxonomy and Study2 for accession
    Then set the URL to STUDY2
    And the response code should be 201

    # Create two analyses: Study1 → Analysis1 and Study2 → Analysis2
    Given I create an analysis with Analysis1 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY1 for study
    Then set the URL to ANALYSIS1
    And the response code should be 201
    Given I create an analysis with Analysis2 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY2 for study
    Then set the URL to ANALYSIS2
    And the response code should be 201

    # Create three files
    Given I request POST /files with JSON payload:
    """
    {
      "accessionVersionId": {
        "accession": "File1",
        "version": 1
        },
      "hash": "Hash1",
      "name": "File1",
      "size": 100,
      "type": "TSV"
    }
    """
    Then the response code should be 201
    And set the URL to FILE1

    Given I request POST /files with JSON payload:
    """
    {
      "accessionVersionId": {
        "accession": "File2",
        "version": 1
        },
      "hash": "Hash2",
      "name": "File2",
      "size": 100,
      "type": "TSV"
    }
    """
    Then the response code should be 201
    And set the URL to FILE2

    Given I request POST /files with JSON payload:
    """
    {
      "accessionVersionId": {
        "accession": "File3",
        "version": 1
        },
      "hash": "Hash3",
      "name": "File3",
      "size": 100,
      "type": "TSV"
    }
    """
    Then the response code should be 201
    And set the URL to FILE3

    # Assign files to analyses: Analysis1 → File1, Analysis2 → File2, Analysis1,2 → File3
    When I request PATCH ANALYSIS1 with list FILE1,FILE3 of files
    Then the response code should be 2xx
    When I request PATCH ANALYSIS2 with list FILE2,FILE3 of files
    Then the response code should be 2xx

    # Set release dates for studies
    Given I request PATCH STUDY1 with patch and day <S1_RELEASE>
    Then the response code should be 200
    Given I request PATCH STUDY2 with patch and day <S2_RELEASE>
    Then the response code should be 200

    # Check availability of analyses
    When I request GET with value of ANALYSIS1
    Then the response code should be <A1>
    When I request GET with value of ANALYSIS2
    Then the response code should be <A2>

    # Check availability of files
    When I request GET with value of FILE1
    Then the response code should be <F1>
    When I request GET with value of FILE2
    Then the response code should be <F2>
    When I request GET with value of FILE3
    Then the response code should be <F3>

    Examples:
      # Analysis is available WHEN AND ONLY WHEN its parent study is available
      # File is available WHEN AND ONLY WHEN at least one study which links to it is available
      # Release dates of "null" or "yesterday" should result in a RELEASED state;
      # "tomorrow" in an UNRELEASED state.
      | S1_RELEASE | S2_RELEASE | A1  | A2  | F1  | F2  | F3  |
      | null       | null       | 2xx | 2xx | 2xx | 2xx | 2xx |
      | null       | tomorrow   | 2xx | 4xx | 2xx | 4xx | 2xx |
      | null       | yesterday  | 2xx | 2xx | 2xx | 2xx | 2xx |
      | tomorrow   | tomorrow   | 4xx | 4xx | 4xx | 4xx | 4xx |
      | tomorrow   | yesterday  | 4xx | 2xx | 4xx | 2xx | 2xx |
      | yesterday  | yesterday  | 2xx | 2xx | 2xx | 2xx | 2xx |

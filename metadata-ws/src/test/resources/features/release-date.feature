Feature: Transitive release date control for child objects of a Study

  Scenario Outline: Objects to which at least released study links should be accessible, and vice versa

    # Create the common taxonomy
    When I request POST taxonomies with 9606 for ID, Homo Sapiens for name and NONE for ancestors
    And set the URL to TAXONOMY
    Then the response code should be 201

    # Create the common reference sequences
    When I request POST /reference-sequences with JSON-like payload:
    """
          "name": "GRCh37",
          "patch": "p2",
          "accessions": ["GCA_000001407.3", "GCF_000001407.14"],
          "type": "ASSEMBLY",
          "taxonomy": "TAXONOMY"
    """
    And set the URL to REFERENCE_SEQUENCE
    Then the response code should be 201

    # Create two studies with the release date of "yesterday"
    When I create a study with TAXONOMY for taxonomy and Study1 for accession
    And set the URL to STUDY1
    Then the response code should be 201
    When I create a study with TAXONOMY for taxonomy and Study2 for accession
    And set the URL to STUDY2
    Then the response code should be 201

    # Create two analyses: Study1 → Analysis1 and Study2 → Analysis2
    When I create an analysis with Analysis1 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY1 for study
    And set the URL to ANALYSIS1
    Then the response code should be 201
    When I create an analysis with Analysis2 for accession, REFERENCE_SEQUENCE for reference sequence and STUDY2 for study
    And set the URL to ANALYSIS2
    Then the response code should be 201

    # Create three files
    When I request POST /files with JSON payload:
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
    And set the URL to FILE1
    Then the response code should be 201

    When I request POST /files with JSON payload:
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
    And set the URL to FILE2
    Then the response code should be 201

    When I request POST /files with JSON payload:
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

    # Create a publication for Study1
    # Name has to be modified each time to prevent violating UNIQUE constraint on publicationId
    When I request POST /publications with JSON payload:
    """
    {
      "publicationId": "publication-<S1_RELEASE>-<S2_RELEASE>"
    }
    """
    Then set the URL to PUBLICATION1
    And the response code should be 201
    When I request PATCH STUDY1 with list PUBLICATION1 of publications
    Then the response code should be 2xx

    # Create a web resource for Study2
    When I request POST /webResources with JSON payload:
    """
    {
      "type": "CENTER_WEB",
      "resourceUrl": "http://test-release-date.example.com"
    }
    """
    Then set the URL to WEBRESOURCE2
    And the response code should be 201
    When I request PATCH STUDY2 with list WEBRESOURCE2 of resources
    Then the response code should be 2xx

    # Set both studies to released initially
    When I request PATCH STUDY1 with patch and day null
    Then the response code should be 200
    When I request PATCH STUDY1 with patch and day today
    Then the response code should be 200

    # Make sure that all elements are available initially
    When I request GET with value of ANALYSIS1
    Then the response code should be 2xx
    When I request GET with value of ANALYSIS2
    Then the response code should be 2xx
    When I request GET with value of FILE1
    Then the response code should be 2xx
    When I request GET with value of FILE2
    Then the response code should be 2xx
    When I request GET with value of FILE3
    Then the response code should be 2xx
    When I request GET with value of PUBLICATION1
    Then the response code should be 2xx
    When I request GET with value of WEBRESOURCE2
    Then the response code should be 2xx

    # Set release dates for studies according to the scenario
    When I request PATCH STUDY1 with patch and day <S1_RELEASE>
    Then the response code should be 200
    When I request PATCH STUDY2 with patch and day <S2_RELEASE>
    Then the response code should be 200

    # Check availability of entities according to the scenario
    When I request GET with value of ANALYSIS1
    Then the response code should be <A1>
    When I request GET with value of ANALYSIS2
    Then the response code should be <A2>
    # Check availability of files according to the scenario
    When I request GET with value of FILE1
    Then the response code should be <F1>
    When I request GET with value of FILE2
    Then the response code should be <F2>
    When I request GET with value of FILE3
    Then the response code should be <F3>
    When I request GET with value of PUBLICATION1
    Then the response code should be <P1>
    When I request GET with value of WEBRESOURCE2
    Then the response code should be <W2>

    Examples:
      # Analysis, Publication, and WebResource are available WHEN AND ONLY WHEN their parent study is available
      # File is available WHEN AND ONLY WHEN at least one study which links to it is available
      # Release dates of "null", "yesterday", or "today" should result in a RELEASED state;
      # "tomorrow" in an UNRELEASED state.
      | S1_RELEASE | S2_RELEASE | A1  | A2  | F1  | F2  | F3  | P1  | W2  |
      | null       | null       | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx |
      | null       | yesterday  | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx |
      | null       | today      | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx |
      | null       | tomorrow   | 2xx | 4xx | 2xx | 4xx | 2xx | 2xx | 4xx |
      | yesterday  | yesterday  | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx |
      | yesterday  | today      | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx |
      | yesterday  | tomorrow   | 2xx | 4xx | 2xx | 4xx | 2xx | 2xx | 4xx |
      | today      | today      | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx | 2xx |
      | today      | tomorrow   | 2xx | 4xx | 2xx | 4xx | 2xx | 2xx | 4xx |
      | tomorrow   | tomorrow   | 4xx | 4xx | 4xx | 4xx | 4xx | 4xx | 4xx |

Feature: sample object

  Scenario: register a sample successfully
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test sample with TEST_TAXONOMY for taxonomy
    And the response code should be 201
    And set the URL to TEST_SAMPLE
    When user request GET with value of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAS0001
    Then the result should contain name with value test_human_sample
    When user request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain 1 taxonomies
    Then the href of the taxonomy of taxonomies 0 should be TEST_TAXONOMY


  Scenario: update a sample successfully
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    And set the URL to TEST_TAXONOMY_1
    When user create a test sample with TEST_TAXONOMY_1 for taxonomy
    And the response code should be 201
    And set the URL to TEST_SAMPLE
    When user request GET with value of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAS0001
    Then the result should contain name with value test_human_sample
    When user request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain 1 taxonomies
    Then the href of the taxonomy of taxonomies 0 should be TEST_TAXONOMY_1

    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY_2
    When user request PATCH TEST_SAMPLE with list TEST_TAXONOMY_2 for taxonomies
    And the response code should be 2xx
    When user request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain 1 taxonomies
    Then the href of the taxonomy of taxonomies 0 should be TEST_TAXONOMY_2


  Scenario Outline: post a test sample with invalid taxonomy list should fail
    When user create a test sample with <list> for taxonomy
    And the response code should be 4xx
    And the result should contain exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException

    Examples:
      | list |
      | NONE |
      | BLANK |

  Scenario: delete all of a sample's taxonomies should fail
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    And set the URL to TEST_TAXONOMY_1
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 9606,
      "name": "Homo Sapiens"
    }
    """
    And set the URL to TEST_TAXONOMY_2
    When user create a test sample with TEST_TAXONOMY_1,TEST_TAXONOMY_2 for taxonomy
    And the response code should be 201
    And set the URL to TEST_SAMPLE
    When user request GET with value of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAS0001
    Then the result should contain name with value test_human_sample
    When user request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain 2 taxonomies
    Then the href of the taxonomy of taxonomies 0 should be TEST_TAXONOMY_1
    Then the href of the taxonomy of taxonomies 1 should be TEST_TAXONOMY_2

    When user request DELETE for the taxonomies of TEST_TAXONOMY_1 of the TEST_SAMPLE
    And the response code should be 2xx

    When user request DELETE for the taxonomies of TEST_TAXONOMY_2 of the TEST_SAMPLE
    And the response code should be 4xx
    And the result should contain exception with value uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException


  Scenario Outline: update a sample with invalid taxonomies should fail
    Given user request POST /taxonomies with json data:
    """
    {
      "taxonomyId": 10090,
      "name": "Mus Musculus"
    }
    """
    And set the URL to TEST_TAXONOMY
    When user create a test sample with TEST_TAXONOMY for taxonomy
    And the response code should be 201
    And set the URL to TEST_SAMPLE
    When user request GET with value of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain accessionVersionId.accession with value EGAS0001
    Then the result should contain name with value test_human_sample
    When user request GET for taxonomies of TEST_SAMPLE
    Then the response code should be 200
    Then the result should contain 1 taxonomies
    Then the href of the taxonomy of taxonomies 0 should be TEST_TAXONOMY

    When user request PATCH TEST_SAMPLE with list <list> for taxonomies
    And the response code should be 4xx
    And the result should contain exception with value <exception>

    Examples:
      | list | exception |
      | NONE | uk.ac.ebi.ampt2d.metadata.exceptionhandling.SampleWithoutTaxonomyException |
      | BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException |
      | TEST_TAXONOMY,BLANK | uk.ac.ebi.ampt2d.metadata.exceptionhandling.InvalidTaxonomyException |

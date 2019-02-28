Feature: file object

  Scenario: register a file successfully
    When user request POST /files with json data:
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
    And set the URL to TEST_FILE
    Then the response code should be 201
    Then the Location header should be present with value of TEST_FILE
    When user request GET with value of TEST_FILE
    Then the response code should be 200
    Then the result json should be:
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
    When user request GET /files
    Then the result should contain 1 files

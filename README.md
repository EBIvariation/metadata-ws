# Metadata API

Metadata API for projects that follow the SRA model, such as those submitted to the EGA, EVA and AMP T2D.

Available documentation:
* [Setting up local development environment](docs/setting-up-environment.md)
* Running the import pipeline
  + [Running the import](docs/import/running-the-import.md)
  + [Reference sequence import](docs/import/reference-sequence-import.md)
  + [Technology type import](docs/import/technology-type-import.md)
  + [Handling missing and suppressed entities during import](docs/import/missing-and-suppressed-entities.md)
* Using the metadata service
  + [Using the service](docs/metadata-service/using-the-service.md)
  + [Release date control](docs/metadata-service/release-date.md)
  + [Security](docs/metadata-service/security.md)



## Tests

The profile for testing in a non-Travis environment requires an ENA database connection to be configured.

Unfortunately the corresponding properties can't be populated using a Maven profile because they are defined inside the src/test/resources folder, so they must be explicitly set before running said tests.
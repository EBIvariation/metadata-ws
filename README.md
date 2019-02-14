# Metadata API

Metadata API for projects that follow the SRA model, such as those submitted to the EGA, EVA and AMP T2D.


## Tests

The profile for testing in a non-Travis environment requires an ENA database connection to be configured.

Unfortunately the corresponding properties can't be populated using a Maven profile because they are defined inside
the src/test/resources folder, so they must be explicitly set before running said tests.
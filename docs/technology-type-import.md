# Notes on technology type import

One of the attributes of an analysis is a “Technology type”. SRA supports 8 types of technology types as of 1.5.58 schema version. The details are available here: https://github.com/enasequence/schema/blob/1.5.58/src/main/resources/uk/ac/ebi/ena/sra/schema/SRA.analysis.xsd

During the process of import, the pipeline will convert SRA technology types to an internal representation, which mostly corresponds to the original list.

If an importable document has a "technology type" value which is not supported in the SRA schema (as of version 1.5.58), an assertion error will be thrown. If an importable document *does not specify* any technology type, it will be stored as `UNSPECIFIED`.  
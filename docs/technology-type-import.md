# Notes on technology type import

One of the attributes of an analysis is a “Technology type”. SRA supports 8 types of technology types as of 1.5.58 schema version. The details are available here: https://github.com/enasequence/schema/blob/1.5.58/src/main/resources/uk/ac/ebi/ena/sra/schema/SRA.analysis.xsd

During the process of import, the pipeline will convert SRA technology types to an internal representation, which mostly corresponds to the original list. However, note that “Genotyping by array” and “Genotyping by sequencing” will both get converted to just `GENOTYPING`.
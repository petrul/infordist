package ncd.cli


import inform.dist.FileNCDCalculator
import inform.dist.ncd.compressor.Bzip2Compressor
import inform.dist.ncd.compressor.Compressor

/**
 * this computes the NCD using bzip2 between two files: it receives the file names in
 * command line
 * -- then computes the NCD.
 */


def main1(String[] args) {

    if (args.length < 2)
        throw new IllegalArgumentException("must provide two files over which to compute ncd")

    def filename1 = args[0]
    def filename2 = args[1]

    Compressor bzip2 = new Bzip2Compressor()
    FileNCDCalculator ncdCalculator = new FileNCDCalculator(new File(filename1), new File(filename2), bzip2)

    ncdCalculator.compute()

    println "C($filename1) = ${ncdCalculator.c1}"
    println "C($filename2) = ${ncdCalculator.c2}"
    println "C($filename1, $filename2) = ${ncdCalculator.cc}"

    println "=" * 80
    double ncd = ncdCalculator.getNormalizedDistance()
    double uncd = ncdCalculator.getUnnormalizedDistance()

    println "NCD($filename1, $filename2) = $ncd"
    println "UNCD($filename1, $filename2) = $uncd"

}

this.main1(args)

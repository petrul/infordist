package ncd.cli

import inform.dist.DistanceCalculator
import matrix.store.TermMatrixReadOnly

if (args.length < 1)
    throw new IllegalArgumentException("usage ${this.class} gist_dir [outfile.csv]")

final dir = args[0]

Writer wr
if (args.length > 1)
    wr = new BufferedWriter(new FileWriter(args[1]))
else
    wr = new BufferedWriter(new OutputStreamWriter(System.out))

TermMatrixReadOnly mat = new TermMatrixReadOnly(new File(dir), 1)
String[] terms = mat.getTerms()

wr.write("*,")
terms.each { wr.write("$it, ")}


for (t1 in terms) {

    wr.write("\n")
    wr.write("$t1,")

    System.err.println(t1) // log

    final int c1 = mat.getComplexity(t1)

    for (t2 in terms) {
        final int c2 = mat.getComplexity(t2)
        final int cc = mat.getCombinedComplexity(t1, t2)

        final double ncd = DistanceCalculator.getNormalizedDistance(c1, c2, cc)
        wr.printf("%.12f,", ncd)
    }
}


wr.flush()
wr.close()

System.err.println("done")
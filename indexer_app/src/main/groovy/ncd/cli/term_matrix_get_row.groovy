package ncd.cli

import matrix.store.TermMatrixReadOnly

dir = args[0]
int row_idx = Integer.parseInt(args[1])

TermMatrixReadOnly mat = new TermMatrixReadOnly(new File(dir), 1)
term = mat.getTerm(row_idx)
row = mat.getCombinedComplexityRow(term)

nr_intialized = row.findAll { it != -1 }.size() ;
println "$nr_intialized  initialized values"
println "row $row_idx ($term) : $row"

#! /usr/bin/env groovy

/**
 * see how many lines are non-zeroes in the cooccurrences matrix
 */

import inform.dist.util.*
import inform.dist.serialization.*
import inform.dist.ngd.*
import org.springframework.jdbc.datasource.*
import org.springframework.jdbc.object.*


def main(String[] args) {
	matfile = new MatBinaryReader(new File("cooccurrences.mat.bin"));
	total_docs = matfile.getLongScalar("total_docs")
	terms = matfile.getStringArray('terms')
	println "got ${terms.length} terms, inserting them into db ..."

	int[] absfreq = matfile.getNumberStream('absfreq').nextIntRow()
	
	stream = matfile.getNumberStream("cooccurrences")
	for (i in 0..<stream.rows) {
		int[] row = stream.nextIntRow();
		non_zeros = 0
		row.each {
			if (it != 0) non_zeros ++;
		}
		println "$i : $non_zeros"
	}
}

main(args)


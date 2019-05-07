#! /usr/bin/env groovy

import inform.dist.util.*
import inform.dist.serialization.*
import inform.dist.ngd.*
import org.springframework.jdbc.datasource.*
import org.springframework.jdbc.object.*


def main(String[] args) {
	matfile = new MatBinaryReader(new File("ngd.mat.bin"));
	//total_docs = matfile.getLongScalar("total_docs")

	terms = matfile.getStringArray('terms')
	println "got ${terms.length} terms..."

	//matfile.getNumberStream('absfreq').nextIntRow()
	
	stream = matfile.getNumberStream("ngd")

	//maxrow = stream.rows
	maxrow = 15000
	double[][] ngdmatrix = new double[maxrow][];

	for (i in 0..<maxrow) {
		if (i % 1000 == 0) println "read $i lines..."
		row = stream.nextDoubleRow();
		ngdmatrix[i] = row
	}

	// now test
	rnd = new Random()
	nr_essays = maxrow

        nr_problems = 0 // triangly inequality unobserved
        nr_ok = 0 // triangle inequality observed
        nr_infinity = 0 // if we have infinity cannot calculate

	for (i in 0..< nr_essays) {
                int idx1, idx2, idx3;

                boolean found_different = false
                while (!found_different) {
  		  idx1 = rnd.nextInt(maxrow)
		  idx2 = rnd.nextInt(maxrow)
		  idx3 = rnd.nextInt(maxrow)
                  found_different = ((idx1 != idx2) && (idx2 != idx3))
                }


		[
			[idx1, idx2, idx3],
			[idx2, idx1, idx3],
			[idx3, idx1, idx2]
		].each {
			(a, b, c) = it
                        (term_a, term_b, term_c) = [terms[a], terms[b], terms[c]]
			// assert d(a,b) + d(a,c) >= d(b,c)
			d_ab = ngdmatrix[a][b]
                        d_bc = ngdmatrix[b][c]
                        d_ac = ngdmatrix[a][c]
                        if (Double.isInfinite(d_ab) || Double.isInfinite(d_ac) || Double.isInfinite(d_bc)) {
                           nr_infinity++;
                        } else {
			  if (d_ab + d_ac < d_bc) {
			     //println "problem with $a $b $c $term_a $term_b $term_c ($d_ab, $d_bc, $d_ac)"
			     nr_problems++
			  } else {
			    nr_ok++
			    //println "OK: $a $b $c $term_a $term_b $term_c"
			  }
			}
		}
	}
	println "tried a total of 3 * $nr_essays , got $nr_ok OK, $nr_problems NOTOK, and $nr_infinity had infinity"
}

main(args)

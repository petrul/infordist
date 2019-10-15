#! /usr/bin/env groovy

/*
 * call this in a directory with ngd matrices generated in order to upload them into db
 */

import inform.dist.util.*
import inform.dist.serialization.*
import inform.dist.ngd.*
import org.springframework.jdbc.datasource.*
import org.springframework.jdbc.object.*
import org.springframework.jdbc.core.*
import java.sql.*

/* number of closest neighbours to store for each term */
NR_NEIGHBOURS = 2000;

sql_conf = [
	"home" : 
		[
		 	"driver" 	: "com.mysql.jdbc.Driver",
		 	"url" 		: "jdbc:mysql://localhost:3306/dev",
		 	"username" 	: "dadi",
		 	"password" 	: "dadi"
	    ],

	"aristote" : 
		[
		 	"driver" 	: "com.mysql.jdbc.Driver",
		 	"url" 		: "jdbc:mysql://aristote:3306/infordist",
		 	"username" 	: "dadi",
		 	"password" 	: "dadi"
	    ],

	"tmp" : 
		[
		 	"driver" 	: "com.mysql.jdbc.Driver",
		 	"url" 		: "jdbc:mysql://aristote:3306/infordist_10k",
		 	"username" 	: "dadi",
		 	"password" 	: "dadi"
	    ]
];

//my_conf = sql_conf["dev_home"]
datasource = null;


def do_main(args) {
	if (args.length < 1)
		throw new IllegalArgumentException("you need to provide the sql configuration name");

	sql_conf_name = args[0]
	my_conf = sql_conf[sql_conf_name]
	if (my_conf == null)
		throw new IllegalArgumentException("no sql configuration for [$sql_conf_name]");
	datasource = new DriverManagerDataSource (
			my_conf["driver"],
			my_conf["url"],
			my_conf["username"], 
			my_conf["password"] 
	)
	
	println "connecting to ${my_conf['url']} ..."

	matfile = new MatBinaryReader(new File("cooccurrences.mat.bin"))
	total_docs = matfile.getLongScalar("total_docs")
	println "total docs: $total_docs , will insert $NR_NEIGHBOURS neighbours for each term"
	update_total_docs(total_docs)

	terms = matfile.getStringArray('terms')
	println "got ${terms.length} terms, inserting them into db ..."

	int[] absfreq = matfile.getNumberStream('absfreq').nextIntRow()
	
	ensure_tables_empty()
	int [] params = [Types.INTEGER, Types.VARCHAR, Types.INTEGER]
	sqlupdate = new BatchSqlUpdate(datasource, "insert into `terms` values (?, ?, ?)", params)
	for (i in 0..< terms.length) {
		//println "inserting ${[i, terms[i], absfreq[i]]}"
		Object[] sql_args = [i, terms[i], absfreq[i]]
		sqlupdate.update(sql_args)
	}
	sqlupdate.flush()
	
	upload_data_to_db("ngd", "ngd", "ngd_matrix", terms)
	upload_data_to_db("ungd", "ungd", "ungd_matrix", terms)
	upload_data_to_db("ncc-from-here", "ncc-from-here", "ncc_from_here_matrix", terms)
	upload_data_to_db("ncc-to-here", "ncc-to-here", "ncc_to_here_matrix", terms)
	
}


def upload_data_to_db(filename, matrixname, tablename, terms) {
	drop_create_table(tablename)
	println "inserting $matrixname values in db table..."
	f = new File("${filename}.mat.bin")
	if (! f.exists()) 
		throw new IllegalStateException("expected file " + f.absolutePath + " to exist.");
	matfile = new MatBinaryReader(f)
	matfile.getStringArray('terms') // ignore this
	stream = matfile.getNumberStream(matrixname)
	println "matrix $matrixname : ${stream.rows} x ${stream.columns}"
	int[] params = [Types.INTEGER, Types.INTEGER, Types.DOUBLE]
	sqlupdate = new BatchSqlUpdate(datasource, "insert into `$tablename` values (?, ?, ?)", params)
	sqlupdate.setBatchSize(10000)
	
	for (i in 0 ..< stream.rows) {
		if (i % 100 == 0) {
			print "$i ... "
		}
		double[] row = stream.nextDoubleRow();
		
		Set row_of_weightedterms = new TreeSet()
		row.eachWithIndex { val, idx ->
			if (Double.isNaN(val) || Double.isInfinite(val)) {
				//System.err.println("Nan found for ${terms[i]} - ${terms[idx]}")
				val = 1000.0 * 1000.0 * 1000.0;
			}
			row_of_weightedterms.add(new WeightedTerm(terms[idx], val, idx));
		}
		
		int limitCounter = 0
		for (wt in row_of_weightedterms) {
			if ((++limitCounter) > NR_NEIGHBOURS) break;
			
			Object[] _args = [i, wt.index, wt.weight]
			sqlupdate.update(_args)
		}
	}
	sqlupdate.flush()
}

def ensure_tables_empty() {
	jdbc = new JdbcTemplate(datasource)
	jdbc.update("DROP TABLE IF EXISTS `terms`")
	jdbc.update("""
		CREATE TABLE IF NOT EXISTS `terms` (
		  `id` INT  NOT NULL,
		  `term` VARCHAR(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
		  `absfreq` INT,
		  PRIMARY KEY (`id`),
		  INDEX `idx_term`(`term`)
		)
		ENGINE = MyISAM
		CHARACTER SET utf8 COLLATE utf8_general_ci
	""");
	//jdbc.update("delete from terms")
	//jdbc.update(get_drop_create_query("ngd_matrix"))
	//jdbc.update(get_drop_create_query("ungd_matrix"))
	//jdbc.update(get_drop_create_query("cc_from_here_matrix"))
	//jdbc.update(get_drop_create_query("cc_to_here_matrix"))
/*	jdbc.update("delete from `ungd_matrix`")
	jdbc.update("delete from `cc_from_here_matrix`")
	jdbc.update("delete from `cc_to_here_matrix`") */
}

def drop_create_table(tablename) {
	jdbc = new JdbcTemplate(datasource)
	ddls = get_drop_create_query(tablename)
	ddls.each { jdbc.update(it) }
}

def get_drop_create_query(tablename) {
	return ["DROP TABLE IF EXISTS `$tablename`",
    """
		CREATE TABLE IF NOT EXISTS `$tablename` (
		  `term1` int(11) NOT NULL,
		  `term2` int(11) NOT NULL,
		  `value` double default NULL,
		  PRIMARY KEY  (`term1`,`term2`)
		) ENGINE=MyISAM DEFAULT CHARSET=utf8
	"""]
}

def update_total_docs(total_docs) {
	jdbc = new JdbcTemplate(datasource)
	jdbc.update("DROP TABLE IF EXISTS `vars`");
	jdbc.update("""
	CREATE TABLE `vars` (
	  `name` VARCHAR(200)  NOT NULL,
	  `value` double,
	  PRIMARY KEY (`name`)
	)
	ENGINE = MyISAM
	CHARACTER SET utf8 COLLATE utf8_bin
	""");
	//jdbc.update("delete from vars where name = 'total_docs'");
	jdbc.update("insert into vars(name,value) values ('total_docs', ?)", new Double(total_docs));
}
//println this.args
this.do_main(this.args)

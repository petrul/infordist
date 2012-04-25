package infordist.gg;

import inform.dist.Constants;
import inform.dist.nld.GistComplexity;
import inform.dist.nld.compressor.Bzip2Compressor;
import inform.lucene.IndexUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import matrix.store.TermMatrixRW;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.gridgain.grid.Grid;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridFactory;

import wiki.indexer.TermAndFreq;

public class CombinedComplexityJobManager {

	public static void main(String[] args) {
		if (args.length < 4)
			throw new IllegalArgumentException("must provide arguments : mt|grid index_dir gists_dir matrix_dir" );

		String howToRun = args[0];
		if (howToRun.equalsIgnoreCase("mt")) doItLocally(args);
		else
			if (howToRun.equalsIgnoreCase("grid")) doItOnGrigain(args);
			else
				throw new IllegalArgumentException("first argument must be mt or grid (multithreaded local execution or gridgain distributed execution");
		
	}

	static void doItLocally(String[] args) {

		try {
			ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(6);
			doitOnExecutor(args, newFixedThreadPool);
			newFixedThreadPool.shutdown();
			newFixedThreadPool.awaitTermination(10, TimeUnit.DAYS);
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	static void doItOnGrigain(String[] args) {
		try {
			Grid grid = GridFactory.start();
			ExecutorService executor = grid.newGridExecutorService();

			doitOnExecutor(args, executor);

			executor.shutdown();
			executor.awaitTermination(10, TimeUnit.DAYS);
			System.out.println("after shutdown");

		} catch (GridException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}  catch (ExecutionException e) {
			throw new RuntimeException(e);
		}finally {
			GridFactory.stop(true);
		}
	}

	static void doitOnExecutor(String[] args, ExecutorService executor) throws CorruptIndexException, IOException, InterruptedException, ExecutionException {
		int cli_idx = 1;
		String indexLocation = args[cli_idx++];
		String gistsLocation = args[cli_idx++];
		String ngcdMatrixLocation = args[cli_idx++];

		IndexReader index = IndexReader.open(indexLocation);
		List<TermAndFreq> termsList = new IndexUtil(index).getTermsOrderedByFreqDesc(20);
		termsList = termsList.subList(0, 60001);
		String[] terms = new String[termsList.size()];
		{
			int i = 0;
			for (TermAndFreq tf : termsList) {
				terms[i++] = tf.getTerm();
			}
		}
		index.close(); index = null;
		termsList = null;
		LOG.info("got " + terms.length + " terms");

		File cacheDir = new File(gistsLocation).getAbsoluteFile();
		if (!cacheDir.exists()) throw new IllegalArgumentException("directory [" + gistsLocation + "] does not exist");

		LOG.info("creating or opening matrix [" + ngcdMatrixLocation + "]... ");
		File ngcdMatrixFile = new File(ngcdMatrixLocation);
		TermMatrixRW matrix = new TermMatrixRW(terms, ngcdMatrixFile);
		LOG.info("done.");

		{
			// initialize simple complexities
			LOG.info("checking term complexities...");
			GistComplexity bgcomplx = new GistComplexity(cacheDir, new Bzip2Compressor());
			for (String term : terms) {
				int c = matrix.getComplexity(term);
				if (c == Constants.MATRIX_CELL_UNINITIALIZED) {
					c = (int) bgcomplx.getComplexity(term);
					matrix.setComplexity(term, c);
				}
			}
			LOG.info("done.");
		}

		
		List<Future<CombinedComplexityJobResult>> futureResults = new ArrayList<Future<CombinedComplexityJobResult>>();
		
		for (int i = 200; i < terms.length; i++) {
			String mainTerm = terms[i];
			int mainTermComplexity = matrix.getComplexity(mainTerm);
			int[] row = matrix.getCombinedComplexityRow(mainTerm);
			List<String> notDone = new ArrayList<String>();
			
			for (int j = i + 1; j < row.length; j++) {
				if (row[j] == Constants.MATRIX_CELL_UNINITIALIZED) {
					
					String term = matrix.getTerm(j);
					int term_c = matrix.getComplexity(term);
					if (term_c > mainTermComplexity / 10) // do not compare mainTerm with extremeley small words 
						notDone.add(term);

				}
			}
			if (notDone.size() == 0) {
				LOG.info("nothing to do for term " + mainTerm);
				continue;
			}

			List<List<String>> subArrays = splitArrayInChunks(notDone, 50);
			notDone = null;
			
			for (List<String> subarr : subArrays) { 
				CombinedComplexityJob job = new CombinedComplexityJob(
						cacheDir, mainTerm, 
						(String[]) subarr.toArray(new String[subarr.size()]));
				Future<CombinedComplexityJobResult> futureResult = executor.submit(job);
				futureResults.add(futureResult);
				
				listenForResultsIfEnoughJobsSent(futureResults, matrix);
				
				LOG.info("sent job with " + subarr.size() + " terms for main term " + mainTerm);
			}
			

		}
	}

	
	static void listenForResultsIfEnoughJobsSent(List<Future<CombinedComplexityJobResult>> futureResults, TermMatrixRW matrix) throws InterruptedException, ExecutionException {
		int maxJobs = 40;
		
		// when the job list is full, start listening for responses
		if (futureResults.size() >= maxJobs) {
			LOG.info("sent " + futureResults.size() + " jobs, listening for results...");
			
			for (int j = 0; j < futureResults.size(); j++) {
			//for (Future<CombinedComplexityJobResult> res : futureResults) {
				Future<CombinedComplexityJobResult> res = futureResults.get(j);
				CombinedComplexityJobResult jobResult = res.get();
				String t1 = jobResult.getMainTerm();
				String[] termsToCompare = jobResult.getTermsToCompare();
				for (int t_idx = 0; t_idx < jobResult.termsToCompare.length; t_idx ++) {
					String t2 = termsToCompare[t_idx];
					int cc = jobResult.getResults()[t_idx];
					matrix.setCombinedComplexity(t1, t2, cc);
				}
				LOG.info("wrote a chunk of results for " + t1 + "(" + termsToCompare[0] + " -> " + termsToCompare[termsToCompare.length - 1] + ").");
				futureResults.set(j, null); // free memory
			}
			futureResults.clear();
		}
	}
	
	
	static List<List<String>> splitArrayInChunks(List<String> list, int chunkSize) {
		List<List<String>> result = new ArrayList<List<String>>();
		int i = 0;
		while (i < list.size()) {
			int new_i = i + chunkSize;
			if (new_i > list.size()) 
				new_i = list.size();
			List<String> subList = list.subList(i, new_i);
			result.add(subList);
			i = new_i;
		}
		return result;
	}

	static Logger LOG = Logger.getLogger(CombinedComplexityJobManager.class);
}

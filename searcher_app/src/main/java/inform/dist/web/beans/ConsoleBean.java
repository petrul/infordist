package inform.dist.web.beans;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import inform.dist.ngd.DistanceCalculatorFromFreqMatrix;
import matrix.store.TermMatrixReadOnly;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ConsoleBean {
    TermMatrixReadOnly matrix;
    String script = "[\n" +
            "calc.getConditionalComplexity('bird', 'fli'),\n" +
            "calc.getConditionalComplexity('plane', 'fli'),\n" +
            "calc.getConditionalComplexity('man', 'fli'),\n" +
            "calc.getConditionalComplexity('bird', 'walk'),\n" +
            "calc.getConditionalComplexity('plane', 'walk'),\n" +
            "calc.getConditionalComplexity('man', 'walk'),\n" +
            "]";
    String results = "";

    String resultsBackgroundColor = "#fff";

    GroovyShell shell;


    public String getResultsBackgroundColor() {
        return resultsBackgroundColor;
    }

    public void setResultsBackgroundColor(String resultsBackgroundColor) {
        this.resultsBackgroundColor = resultsBackgroundColor;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setMatrix(TermMatrixReadOnly matrix) {
        this.matrix = matrix;

        Binding binding = new Binding();
        binding.setVariable("mtx", matrix);
        binding.setVariable("calc", new DistanceCalculatorFromFreqMatrix(matrix));
        this.shell = new GroovyShell(binding);
    }

    public ConsoleBean() {
    }

    public void run(javax.faces.event.ActionEvent event) {
        this.setResultsBackgroundColor("#fff");
        try {
            Object res = this.shell.evaluate(this.script);
            if (res != null)
                this.setResults(res.toString());
            else
                this.setResults("<null>");
        } catch (Exception e) {
            this.setResultsBackgroundColor("#CC6666");
            StringWriter s = new StringWriter();
            PrintWriter wr = new PrintWriter(s);
            e.printStackTrace(wr);
            this.setResults(s.toString());
        }
    }
}

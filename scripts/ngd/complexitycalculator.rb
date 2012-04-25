class ComplexityCalculator


    def initialize(searchengine, debug=false)
        @cache = {}
        @searchengine = searchengine
        @all_docs = @searchengine.get_size()
        @log_alldocs = log_base(2, @all_docs)
        @debug_mode = debug
    end

    
    def get_complexity(term)
        hits = self.get_or_retrieve_hits("\"#{term}\"")
        freq_term = hits.to_f / @all_docs
        - log_base(2, freq_term)
    end

    
    def get_ngd(term1, term2)
        hits1 = self.get_or_retrieve_hits("\"#{term1}\"")
        log_hits1 = log_base(2, hits1)
        hits2 = self.get_or_retrieve_hits("\"#{term2}\"")
        log_hits2 = log_base(2, hits2)

        the_two_args = "\"#{term1}\" \"#{term2}\""
        hits_both = self.get_or_retrieve_hits(the_two_args)
        log_hits_both = log_base(2, hits_both)

        max_hits = max(log_hits1, log_hits2)
        min_hits = min(log_hits1, log_hits2)

        ngd = (max_hits - log_hits_both) / (@log_alldocs - min_hits)

        ngd
    end


    #
    # G(y|x) = G(x,y) - G(x) = log hits(x) - log hits(xy)
    #
    def get_conditional_complexity(y, x)
        term1 = x
        term2 = y
        
        the_two_args = "\"#{term1}\" \"#{term2}\""
        hits_both = self.get_or_retrieve_hits(the_two_args)
        log_hits_both = log_base(2, hits_both)

        hits1 = self.get_or_retrieve_hits("\"#{term1}\"")
        log_hits1 = log_base(2, hits1)
        
        return log_hits1 - log_hits_both
    end


    ###########################################################

    def get_or_retrieve_hits(term)
        hits1 = -1;
        if @cache.has_key?(term) 
            hits1 = @cache[term]
        else
            hits1 = @searchengine.get_hits(term)
            @cache[term] = hits1
        end
        hits1
    end

    def log_base(n, x)
      return (Math.log(x) / Math.log(n))
    end 

    def max(x, y) 
      if x > y then x else y end 
    end

    def min(x, y) 
      if x < y then x else y end 
    end

    def format_decimal(st)
      st.to_s.gsub(/(\d)(?=(\d\d\d)+(?!\d))/, "\\1,")
    end

end
